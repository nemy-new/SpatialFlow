package com.codetrio.overdrive.viewmodel.lyrics

import android.content.Context
import android.util.Log
import com.codetrio.overdrive.data.lyrics.LrcParser
import com.codetrio.overdrive.data.lyrics.LyricLine
import com.codetrio.overdrive.data.lyrics.LyricsNormalizer
import com.codetrio.overdrive.data.lyrics.LyricsRepository
import com.codetrio.overdrive.data.lyrics.LyricsResult
import com.codetrio.overdrive.data.lyrics.LyricsState
import com.codetrio.overdrive.model.SongItem
import com.codetrio.overdrive.data.lyrics.LyricsTranslator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.preference.PreferenceManager
import java.util.Locale

/**
 * Handles lyrics state, fetch, and retry flows for player view models.
 * Kotlin + StateFlow implementation for modern reactive architecture.
 */
class PlayerLyricsStateController(private val logTag: String) {
    private var activeLyricsTrackKey: String? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _syncedLyrics = MutableStateFlow<List<LyricLine>?>(null)
    val syncedLyrics: StateFlow<List<LyricLine>?> = _syncedLyrics.asStateFlow()

    private val _plainLyrics = MutableStateFlow<String?>(null)
    val plainLyrics: StateFlow<String?> = _plainLyrics.asStateFlow()

    private val _translatedPlainLyrics = MutableStateFlow<String?>(null)
    val translatedPlainLyrics: StateFlow<String?> = _translatedPlainLyrics.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()

    private val _isLyricsModeEnabled = MutableStateFlow(false)
    val isLyricsModeEnabled: StateFlow<Boolean> = _isLyricsModeEnabled.asStateFlow()

    private val _lyricsState = MutableStateFlow(LyricsState.IDLE)
    val lyricsState: StateFlow<LyricsState> = _lyricsState.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)

    private val _providerResults = MutableStateFlow<Map<String, LyricsResult>>(emptyMap())
    val providerResults: StateFlow<Map<String, LyricsResult>> = _providerResults.asStateFlow()

    private val _selectedProvider = MutableStateFlow<String?>(null)
    val selectedProvider: StateFlow<String?> = _selectedProvider.asStateFlow()

    val shouldShowLyrics: Boolean get() = _isLyricsModeEnabled.value

    private fun getTrackKey(song: SongItem?): String? {
        if (song == null) return null
        return song.videoId ?: song.path ?: song.id.toString()
    }

    fun setLyricsModeEnabled(enabled: Boolean, context: Context? = null, song: SongItem? = null) {
        _isLyricsModeEnabled.value = enabled
        if (enabled && context != null && song != null) {
            if (_syncedLyrics.value.isNullOrEmpty() && _plainLyrics.value.isNullOrBlank() && _lyricsState.value != LyricsState.FETCHING) {
                fetchLyrics(context, song)
            }
        }
    }

    fun clearForSongChange(song: SongItem? = null) {
        activeLyricsTrackKey = getTrackKey(song)
        _syncedLyrics.value = null
        _plainLyrics.value = null
        _translatedPlainLyrics.value = null
        _error.value = null
        _isLoading.value = false
        _lyricsState.value = LyricsState.IDLE
        _statusMessage.value = null
        _providerResults.value = emptyMap()
        _selectedProvider.value = null
    }

    fun fetchLyrics(context: Context, song: SongItem?) {
        if (song == null) return

        val repository = LyricsRepository.getInstance(context)
        val trackKey = getTrackKey(song)
        activeLyricsTrackKey = trackKey
        repository.cancelCurrent()

        // Reset state
        _lyricsState.value = LyricsState.IDLE
        _syncedLyrics.value = null
        _plainLyrics.value = null
        _translatedPlainLyrics.value = null
        _error.value = null
        _providerResults.value = emptyMap()
        _selectedProvider.value = null

        setLyricsState(LyricsState.FETCHING)
        _statusMessage.value = "Searching for lyrics..."

        repository.fetchLyrics(
            song.title,
            song.artist,
            null,
            song.duration,
            song.path,
            createCallback(
                context = context,
                requestTrackKey = trackKey,
                keepExistingLyricsOnNotFound = true,
                instrumentalMessage = "Instrumental track - no vocals",
                logUpgrades = true
            ),
            song.videoId
        )
    }

    fun retryLyrics(context: Context, song: SongItem?) {
        if (song == null) return

        val trackKey = getTrackKey(song)
        activeLyricsTrackKey = trackKey

        // Reset state
        _syncedLyrics.value = null
        _plainLyrics.value = null
        _error.value = null
        _lyricsState.value = LyricsState.IDLE
        _providerResults.value = emptyMap()
        _selectedProvider.value = null

        setLyricsState(LyricsState.FETCHING)
        _statusMessage.value = "Retrying all sources..."

        LyricsRepository.getInstance(context).retryLyrics(
            song.title,
            song.artist,
            null,
            song.duration,
            song.path,
            createCallback(
                context = context,
                requestTrackKey = trackKey,
                keepExistingLyricsOnNotFound = false,
                instrumentalMessage = "Instrumental track",
                logUpgrades = false
            ),
            song.videoId
        )
    }

    fun selectProvider(providerName: String, context: Context? = null, song: SongItem? = null) {
        val result = _providerResults.value[providerName]
        if (result != null && result.hasLyrics()) {
            _selectedProvider.value = providerName
            if (context != null) {
                applyLyricsResult(result, context)
            } else {
                applyLyricsResult(result, null)
            }
            _lyricsState.value = LyricsState.SUCCESS
            _error.value = null
        } else if (context != null && song != null) {
            retryLyrics(context, song)
        }
    }

    fun determineBestResult(results: Map<String, LyricsResult>): LyricsResult? {
        val candidates = results.values

        // Ensure isWordByWord flag is accurately populated from content heuristics
        candidates.forEach { candidate ->
            if (!candidate.isWordByWord && !candidate.syncedLyrics.isNullOrEmpty() &&
                candidate.syncedLyrics!!.contains(Regex("""<\d+:\d{2}[.:]\d+>"""))
            ) {
                candidate.isWordByWord = true
            }
        }

        // First priority: Karaoke (word-by-word)
        val karaokeCandidates = candidates.filter { it.isWordByWord && it.hasLyrics() }
        if (karaokeCandidates.isNotEmpty()) {
            return karaokeCandidates.maxWithOrNull(
                compareBy<LyricsResult> { it.providerName?.startsWith("BetterLyrics") == true }
                    .thenBy { it.providerName == "SyncLRC" }
                    .thenBy { it.confidence }
            )
        }

        // Second priority: Synced
        val syncedCandidates = candidates.filter { it.isSynced && it.hasLyrics() }
        if (syncedCandidates.isNotEmpty()) {
            return syncedCandidates.maxByOrNull { it.confidence }
        }

        // Third priority: Plain lyrics
        val plainCandidates = candidates.filter { !it.isSynced && !it.isWordByWord && !it.isInstrumental && it.hasLyrics() }
        if (plainCandidates.isNotEmpty()) {
            return plainCandidates.maxByOrNull { it.confidence }
        }

        // Fourth priority: Instrumental
        return candidates.firstOrNull { it.isInstrumental }
    }

    private fun createCallback(
        context: Context,
        requestTrackKey: String?,
        keepExistingLyricsOnNotFound: Boolean,
        instrumentalMessage: String,
        logUpgrades: Boolean
    ): LyricsRepository.ExtendedLyricsCallback {
        return object : LyricsRepository.ExtendedLyricsCallback {
            override fun onLyricsFound(result: LyricsResult) {
                if (!isActiveLyricsRequest(requestTrackKey)) return

                if (_selectedProvider.value == null) {
                    applyLyricsResult(result, context)
                    _lyricsState.value = LyricsState.SUCCESS
                    _isLoading.value = false
                    _error.value = null
                    _statusMessage.value = "Lyrics from ${result.providerName}"
                }
            }

            override fun onLyricsUpgraded(betterResult: LyricsResult) {
                if (!isActiveLyricsRequest(requestTrackKey)) return

                if (_selectedProvider.value == null) {
                    applyLyricsResult(betterResult, context)
                    _lyricsState.value = LyricsState.SUCCESS
                    
                    if (logUpgrades) {
                        _statusMessage.value = "Upgraded lyrics from ${betterResult.providerName}"
                        Log.d(logTag, "Lyrics upgraded from ${betterResult.providerName}")
                    }
                }
            }

            override fun onLyricsNotFound(reason: String) {
                if (!isActiveLyricsRequest(requestTrackKey)) return

                if (keepExistingLyricsOnNotFound && hasLyricsData()) return

                if (_syncedLyrics.value.isNullOrEmpty() && _plainLyrics.value.isNullOrEmpty()) {
                    _lyricsState.value = LyricsState.FAILED
                    _isLoading.value = false
                    _error.value = Exception(reason)
                    _statusMessage.value = null
                }
            }

            override fun onInstrumental() {
                if (!isActiveLyricsRequest(requestTrackKey)) return

                if (_syncedLyrics.value.isNullOrEmpty() && _plainLyrics.value.isNullOrEmpty()) {
                    _lyricsState.value = LyricsState.FAILED
                    _isLoading.value = false
                    _error.value = Exception(instrumentalMessage)
                    _statusMessage.value = null
                }
            }

            override fun onSearchStatus(message: String) {
                if (!isActiveLyricsRequest(requestTrackKey)) return

                _statusMessage.value = message
            }

            override fun onProviderResult(providerName: String, result: LyricsResult?) {
                if (!isActiveLyricsRequest(requestTrackKey)) return

                val currentMap = _providerResults.value.toMutableMap()
                if (result != null) {
                    currentMap[providerName] = result
                } else {
                    currentMap[providerName] = LyricsResult(providerName = providerName, confidence = -1f)
                }
                _providerResults.value = currentMap

                if (_selectedProvider.value == null) {
                    val best = determineBestResult(currentMap)
                    if (best != null && best.hasLyrics()) {
                        applyLyricsResult(best, context)
                        _lyricsState.value = LyricsState.SUCCESS
                        _error.value = null
                    } else if (best != null && best.isInstrumental) {
                        _syncedLyrics.value = null
                        _plainLyrics.value = null
                        _lyricsState.value = LyricsState.FAILED
                        _error.value = Exception(instrumentalMessage)
                    }
                }
            }
        }
    }

    private fun isActiveLyricsRequest(requestTrackKey: String?): Boolean {
        return activeLyricsTrackKey != null && activeLyricsTrackKey == requestTrackKey
    }

    private fun applyLyricsResult(result: LyricsResult, context: Context?) {
        val rawSynced = result.syncedLyrics
        val lines = if (!rawSynced.isNullOrBlank()) LrcParser.parse(rawSynced) else emptyList()

        if (lines.isNotEmpty()) {
            _syncedLyrics.value = lines
            _plainLyrics.value = null
            _translatedPlainLyrics.value = null

            if (context != null) {
                triggerTranslationIfEnabled(lines, context)
            }
            return
        }

        // Fallback: If synced lyrics are not available or not timed, extract clean plain lyrics
        val plainText = when {
            !result.plainLyrics.isNullOrBlank() -> result.plainLyrics
            !rawSynced.isNullOrBlank() -> LyricsNormalizer.extractCleanPlainText(rawSynced)
            else -> null
        }

        if (!plainText.isNullOrBlank()) {
            _syncedLyrics.value = null
            _plainLyrics.value = plainText
            _translatedPlainLyrics.value = null

            if (context != null) {
                triggerPlainTranslationIfEnabled(plainText, context)
            }
        } else {
            _syncedLyrics.value = null
            _plainLyrics.value = null
            _translatedPlainLyrics.value = null
        }
    }
    
    fun toggleTranslation(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val current = prefs.getBoolean("enable_lyrics_translation", false)
        val newValue = !current
        prefs.edit().putBoolean("enable_lyrics_translation", newValue).apply()
        
        val lines = _syncedLyrics.value
        val plain = _plainLyrics.value
        if (newValue) {
            if (!lines.isNullOrEmpty()) {
                triggerTranslationIfEnabled(lines, context)
            } else if (!plain.isNullOrBlank()) {
                triggerPlainTranslationIfEnabled(plain, context)
            }
        } else {
            if (!lines.isNullOrEmpty()) {
                _syncedLyrics.value = lines.map { it.copy(translatedContent = null) }
            }
            _translatedPlainLyrics.value = null
        }
    }

    private fun triggerTranslationIfEnabled(lines: List<LyricLine>, context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val enableTranslation = prefs.getBoolean("enable_lyrics_translation", false)
        
        if (enableTranslation && lines.isNotEmpty()) {
            val targetLang = prefs.getString("lyrics_translation_language", Locale.getDefault().language) ?: Locale.getDefault().language
            val engine = prefs.getString("lyrics_translation_engine", "mlkit") ?: "mlkit"
            
            scope.launch(Dispatchers.IO) {
                val translatedLines = LyricsTranslator.translateLyrics(lines, targetLang, engine) { progress ->
                    _statusMessage.value = progress
                }
                
                // Update state instantly on Main thread
                if (translatedLines.any { it.translatedContent != null }) {
                    withContext(Dispatchers.Main) {
                        _syncedLyrics.value = translatedLines
                    }
                }
            }
        }
    }

    private fun triggerPlainTranslationIfEnabled(plainText: String, context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val enableTranslation = prefs.getBoolean("enable_lyrics_translation", false)
        
        if (enableTranslation && plainText.isNotBlank()) {
            val targetLang = prefs.getString("lyrics_translation_language", Locale.getDefault().language) ?: Locale.getDefault().language
            
            scope.launch(Dispatchers.IO) {
                val translated = LyricsTranslator.translatePlainText(plainText, targetLang) { progress ->
                    _statusMessage.value = progress
                }
                
                if (translated.isNotBlank() && translated != plainText) {
                    withContext(Dispatchers.Main) {
                        _translatedPlainLyrics.value = translated
                    }
                }
            }
        }
    }

    private fun hasLyricsData(): Boolean {
        val synced = _syncedLyrics.value
        val plain = _plainLyrics.value
        return !synced.isNullOrEmpty() || !plain.isNullOrEmpty()
    }

    private fun setLyricsState(newState: LyricsState) {
        val current = _lyricsState.value
        if (!current.canTransitionTo(newState)) {
            Log.w(logTag, "Invalid lyrics state transition: $current -> $newState. Blocked.")
            return
        }
        _lyricsState.value = newState
        _isLoading.value = newState.isLoading
    }
}
