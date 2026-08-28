package com.codetrio.overdrive.data.artist

import android.content.Context
import android.util.Log
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.codetrio.overdrive.OverDriveApplication
import com.codetrio.overdrive.data.innertube.OnlineSong
import com.codetrio.overdrive.data.innertube.SearchFilter
import com.codetrio.overdrive.data.innertube.SearchItem
import com.codetrio.overdrive.data.innertube.YouTubeMusic
import com.codetrio.overdrive.ui.player.ArtistData
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

/**
 * Cached artist metadata entity containing timestamp for TTL validation.
 */
data class CachedArtistEntry(
    val data: ArtistData,
    val cachedAtMs: Long = System.currentTimeMillis()
)

/**
 * High-performance 2-Tier Cache (L1 Memory + L2 Disk) and Smart Auto-Update
 * (Stale-While-Revalidate) manager for Artist avatars, subscriber counts, and discography.
 */
object ArtistCacheManager {
    private const val TAG = "ArtistCacheManager"
    private const val CACHE_DIR_NAME = "artist_cache"
    
    // 3 Days TTL for fresh cache; stale data is returned immediately while refreshing in background
    const val CACHE_TTL_MS = 3 * 24 * 60 * 60 * 1000L
    // Maximum disk cache files before LRU cleanup
    private const val MAX_DISK_FILES = 250
    // Max age for garbage collection (14 days)
    private const val MAX_FILE_AGE_MS = 14 * 24 * 60 * 60 * 1000L

    private val gson = Gson()
    private val memoryCache = ConcurrentHashMap<String, CachedArtistEntry>()
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true
        managerScope.launch {
            try {
                val cacheDir = getCacheDir(context)
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs()
                }
                cleanupOldFiles(cacheDir)
            } catch (e: Exception) {
                Log.w(TAG, "Init cleanup error: ${e.message}")
            }
        }
    }

    /**
     * Retrieves artist metadata.
     * 1. If Memory Cache hits and is fresh -> returns immediately.
     * 2. If Disk Cache hits -> loads to Memory, returns immediately.
     *    If stale (> 3 days), triggers background refresh asynchronously.
     * 3. If Cache misses or forceRefresh is true -> fetches from YouTubeMusic API, caches to Disk & Memory, and returns.
     */
    suspend fun getArtistInfo(
        artistId: String?,
        artistName: String?,
        forceRefresh: Boolean = false,
        context: Context? = null
    ): ArtistData {
        val rawKey = artistId?.takeIf { it.isNotBlank() } ?: artistName?.takeIf { it.isNotBlank() } ?: return ArtistData(null, null, null)
        val key = rawKey.trim()

        val appContext = context ?: try { OverDriveApplication.instance } catch (_: Exception) { null }

        if (!forceRefresh) {
            // 1. Check L1 Memory Cache
            val memEntry = memoryCache[key]
            if (memEntry != null) {
                val age = System.currentTimeMillis() - memEntry.cachedAtMs
                if (age < CACHE_TTL_MS) {
                    return memEntry.data
                }
                // Memory is stale -> return immediately and trigger background refresh
                triggerBackgroundRefresh(artistId, artistName, key, appContext)
                return memEntry.data
            }

            // 2. Check L2 Disk Cache
            if (appContext != null) {
                val diskEntry = readFromDisk(appContext, key)
                if (diskEntry != null) {
                    memoryCache[key] = diskEntry
                    val age = System.currentTimeMillis() - diskEntry.cachedAtMs
                    if (age < CACHE_TTL_MS) {
                        return diskEntry.data
                    }
                    // Disk is stale -> return immediately and trigger background refresh
                    triggerBackgroundRefresh(artistId, artistName, key, appContext)
                    return diskEntry.data
                }
            }
        }

        // 3. Network Fetch
        return withContext(Dispatchers.IO) {
            fetchFromNetworkAndCache(artistId, artistName, key, appContext)
        }
    }

    @Volatile
    private var currentPrefetchJob: kotlinx.coroutines.Job? = null

    /**
     * Non-blocking background prefetch method for artist metadata and avatar icon.
     * Called immediately when a song starts playing.
     * Guaranteed to have ZERO impact on audio playback latency or main thread.
     */
    fun prefetchArtistForSong(song: com.codetrio.overdrive.model.SongItem?, context: Context? = null) {
        if (song == null) return
        val artistId = song.artistId?.takeIf { it.isNotBlank() }
        val artistName = song.artist.takeIf { it.isNotBlank() && it != "Unknown Artist" && it != "Unknown" && it != "<unknown>" }
        if (artistId == null && artistName == null) return

        val rawKey = artistId ?: artistName ?: return
        val key = rawKey.trim()

        val appContext = context ?: try { OverDriveApplication.instance } catch (_: Exception) { null }

        // Check if fresh in memory - if so, thumbnail is already warm
        val memEntry = memoryCache[key]
        if (memEntry != null) {
            val age = System.currentTimeMillis() - memEntry.cachedAtMs
            if (age < CACHE_TTL_MS) {
                // Already fresh in memory! Ensure coil disk cache has the image
                memEntry.data.thumbnailUrl?.let { url ->
                    appContext?.let { prefetchImage(it, url) }
                }
                return
            }
        }

        // Cancel previous prefetch job if any to avoid competing network requests during rapid skipping
        currentPrefetchJob?.cancel()
        currentPrefetchJob = managerScope.launch(Dispatchers.IO) {
            try {
                // Check L2 Disk Cache first before making any network call
                if (appContext != null) {
                    val diskEntry = readFromDisk(appContext, key)
                    if (diskEntry != null) {
                        memoryCache[key] = diskEntry
                        val age = System.currentTimeMillis() - diskEntry.cachedAtMs
                        if (age < CACHE_TTL_MS) {
                            diskEntry.data.thumbnailUrl?.let { url ->
                                prefetchImage(appContext, url)
                            }
                            return@launch
                        }
                    }
                }

                // If cache miss or stale, fetch from network in background
                fetchFromNetworkAndCache(artistId, artistName, key, appContext)
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Cancelled due to song change - perfectly fine
            } catch (e: Exception) {
                Log.d(TAG, "Prefetch artist failed for '$key': ${e.message}")
            }
        }
    }

    private fun triggerBackgroundRefresh(artistId: String?, artistName: String?, key: String, context: Context?) {
        managerScope.launch {
            try {
                Log.d(TAG, "🔄 Stale-While-Revalidate: Refreshing artist metadata in background for '$key'")
                fetchFromNetworkAndCache(artistId, artistName, key, context)
            } catch (e: Exception) {
                Log.d(TAG, "Background refresh skipped: ${e.message}")
            }
        }
    }

    private suspend fun fetchFromNetworkAndCache(
        artistId: String?,
        artistName: String?,
        key: String,
        context: Context?
    ): ArtistData {
        try {
            if (!artistId.isNullOrBlank()) {
                val res = YouTubeMusic.artist(artistId).getOrNull()
                if (res != null) {
                    val thumb = res.artist.thumbnailUrl
                    val subs = res.artist.subscriberCount
                    val songs = res.sections.flatMap { section ->
                        section.items.filterIsInstance<SearchItem.Song>().map { it.song }
                    }.distinctBy { it.videoId }

                    val data = ArtistData(
                        resolvedArtistId = res.artist.browseId,
                        thumbnailUrl = thumb,
                        subscriberCount = subs,
                        topSongs = songs
                    )
                    saveEntry(key, data, context)
                    return data
                }
            }

            if (!artistName.isNullOrBlank() && artistName != "Unknown Artist" && artistName != "Unknown" && artistName != "<unknown>") {
                val searchRes = YouTubeMusic.search(artistName, SearchFilter.ARTISTS).getOrNull()
                val firstArtist = searchRes?.items?.filterIsInstance<SearchItem.Artist>()?.firstOrNull()?.artist
                    ?: searchRes?.items?.filterIsInstance<SearchItem.TopResult>()?.firstOrNull()?.artist

                if (firstArtist != null) {
                    val browseId = firstArtist.browseId
                    var songs: List<OnlineSong> = emptyList()
                    if (!browseId.isNullOrBlank()) {
                        val artistRes = YouTubeMusic.artist(browseId).getOrNull()
                        if (artistRes != null) {
                            songs = artistRes.sections.flatMap { section ->
                                section.items.filterIsInstance<SearchItem.Song>().map { it.song }
                            }.distinctBy { it.videoId }
                        }
                    }
                    if (songs.isEmpty()) {
                        val songSearchRes = YouTubeMusic.search(artistName, SearchFilter.SONGS).getOrNull()
                        if (songSearchRes != null) {
                            songs = songSearchRes.items.filterIsInstance<SearchItem.Song>().map { it.song }
                        }
                    }

                    val data = ArtistData(
                        resolvedArtistId = browseId,
                        thumbnailUrl = firstArtist.thumbnailUrl,
                        subscriberCount = firstArtist.subscriberCount,
                        topSongs = songs
                    )
                    saveEntry(key, data, context)
                    return data
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "fetchFromNetwork failed for '$key': ${e.message}")
        }
        return ArtistData(null, null, null)
    }

    private fun saveEntry(key: String, data: ArtistData, context: Context?) {
        val entry = CachedArtistEntry(data = data, cachedAtMs = System.currentTimeMillis())
        memoryCache[key] = entry

        val appContext = context ?: try { OverDriveApplication.instance } catch (_: Exception) { null }
        if (appContext != null) {
            // Write to Disk Cache
            writeToDisk(appContext, key, entry)

            // Prefetch image to Coil disk cache for instant offline rendering
            val thumbUrl = data.thumbnailUrl
            if (!thumbUrl.isNullOrBlank()) {
                prefetchImage(appContext, thumbUrl)
            }
        }
    }

    private fun prefetchImage(context: Context, url: String) {
        try {
            val request = ImageRequest.Builder(context)
                .data(url)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .build()
            context.imageLoader.enqueue(request)
        } catch (e: Exception) {
            Log.d(TAG, "Prefetch image failed: ${e.message}")
        }
    }

    private fun getCacheDir(context: Context): File {
        return File(context.cacheDir, CACHE_DIR_NAME)
    }

    private fun getCacheFile(context: Context, key: String): File {
        val hashedKey = hashKey(key)
        return File(getCacheDir(context), "$hashedKey.json")
    }

    private fun hashKey(key: String): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val bytes = md.digest(key.toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            key.hashCode().toString()
        }
    }

    private fun readFromDisk(context: Context, key: String): CachedArtistEntry? {
        return try {
            val file = getCacheFile(context, key)
            if (file.exists() && file.canRead()) {
                val json = file.readText(Charsets.UTF_8)
                gson.fromJson(json, CachedArtistEntry::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error reading disk cache for '$key': ${e.message}")
            null
        }
    }

    private fun writeToDisk(context: Context, key: String, entry: CachedArtistEntry) {
        managerScope.launch {
            try {
                val dir = getCacheDir(context)
                if (!dir.exists()) dir.mkdirs()
                val file = getCacheFile(context, key)
                val json = gson.toJson(entry)
                file.writeText(json, Charsets.UTF_8)
            } catch (e: Exception) {
                Log.w(TAG, "Error writing disk cache for '$key': ${e.message}")
            }
        }
    }

    /**
     * Cleans up expired files or enforces MAX_DISK_FILES limit.
     */
    private fun cleanupOldFiles(dir: File) {
        try {
            val files = dir.listFiles { f -> f.isFile && f.name.endsWith(".json") } ?: return
            val now = System.currentTimeMillis()

            // 1. Delete files older than MAX_FILE_AGE_MS
            val survivingFiles = mutableListOf<File>()
            for (file in files) {
                val lastMod = file.lastModified()
                if (now - lastMod > MAX_FILE_AGE_MS) {
                    file.delete()
                } else {
                    survivingFiles.add(file)
                }
            }

            // 2. If count exceeds MAX_DISK_FILES, delete oldest
            if (survivingFiles.size > MAX_DISK_FILES) {
                survivingFiles.sortBy { it.lastModified() }
                val toDeleteCount = survivingFiles.size - MAX_DISK_FILES
                for (i in 0 until toDeleteCount) {
                    survivingFiles[i].delete()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Cleanup failed: ${e.message}")
        }
    }

    /**
     * Clears all artist memory and disk caches.
     */
    fun clearAll(context: Context? = null) {
        memoryCache.clear()
        val appContext = context ?: try { OverDriveApplication.instance } catch (_: Exception) { null }
        if (appContext != null) {
            managerScope.launch {
                try {
                    val dir = getCacheDir(appContext)
                    dir.deleteRecursively()
                } catch (e: Exception) {
                    Log.w(TAG, "Clear all failed: ${e.message}")
                }
            }
        }
    }
}
