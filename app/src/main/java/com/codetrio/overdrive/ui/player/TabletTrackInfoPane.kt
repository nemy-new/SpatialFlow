package com.codetrio.overdrive.ui.player

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.codetrio.overdrive.data.diagnostics.PlaybackDiagnosticsLogger
import com.codetrio.overdrive.data.innertube.OnlineSong
import com.codetrio.overdrive.data.innertube.SearchFilter
import com.codetrio.overdrive.data.innertube.SearchItem
import com.codetrio.overdrive.data.innertube.YouTubeMusic
import com.codetrio.overdrive.model.SongItem
import com.codetrio.overdrive.player.queue.YouTubeQueue
import com.codetrio.overdrive.viewmodel.PlayerSharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import java.util.Locale

/**
 * Detailed artist information including resolved browse ID, avatar URL, subscriber count, and top songs.
 */
data class ArtistData(
    val resolvedArtistId: String?,
    val thumbnailUrl: String?,
    val subscriberCount: String?,
    val topSongs: List<OnlineSong> = emptyList()
)

/**
 * Parses collaborative / multi-artist strings into individual artist names.
 * Supports Japanese punctuation ("、"), commas (","), slashes ("/"), ampersands ("&"),
 * and collaboration tokens ("feat.", "ft.", "with", "vs.", "x").
 */
fun parseArtistNames(rawArtist: String?): List<String> {
    if (rawArtist.isNullOrBlank() || rawArtist == "<unknown>" || rawArtist == "Unknown Artist") {
        return emptyList()
    }
    val regex = Regex("""(?:\s*(?:、|,|/|&|\bfeat\.?|\bft\.?|\bfeaturing\b|\bwith\b|\bvs\.?|\bx\b)\s*)+""", RegexOption.IGNORE_CASE)
    val parts = rawArtist.split(regex)
        .map { it.trim().trim('(', ')', '[', ']', '\"') }
        .filter { it.isNotBlank() && !it.equals("feat", ignoreCase = true) && !it.equals("ft", ignoreCase = true) }
        .distinct()
    return if (parts.isEmpty()) listOf(rawArtist.trim()) else parts
}

/**
 * High-speed artist metadata and avatar provider backed by 2-tier caching (Memory + Disk)
 * and smart Stale-While-Revalidate auto-updating.
 */
object ArtistAvatarHelper {
    suspend fun getArtistInfo(
        artistId: String?,
        artistName: String?,
        forceRefresh: Boolean = false
    ): ArtistData {
        return com.codetrio.overdrive.data.artist.ArtistCacheManager.getArtistInfo(
            artistId = artistId,
            artistName = artistName,
            forceRefresh = forceRefresh
        )
    }
}

/**
 * Modern, music-centric Track Information Hub designed for tablet landscape.
 * Rebuilt from scratch to deliver an Apple Music / Spotify level aesthetic.
 */
@Composable
fun TabletTrackInfoPane(
    song: SongItem?,
    uiState: PlayerUiState,
    viewModel: PlayerSharedViewModel,
    dynamicAccentColor: Color,
    contentColor: Color,
    contentSecondary: Color,
    isStatsEnabled: Boolean = false,
    onArtistClick: (artistId: String?, artistName: String) -> Unit,
    onAlbumClick: (albumId: String?, albumName: String) -> Unit,
    onSaveClick: () -> Unit = {},
    onOpenStats: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val cardBgColor = contentColor.copy(alpha = 0.055f)

    // State for artist info (parsed multi-artist list)
    val parsedArtistNames = remember(song?.artist) {
        parseArtistNames(song?.artist)
    }
    var multiArtistData by remember(parsedArtistNames, song?.artistId) {
        mutableStateOf<Map<String, ArtistData>>(emptyMap())
    }
    var isArtistLoading by remember(parsedArtistNames) { mutableStateOf(false) }
    var showArtistSelectionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(parsedArtistNames, song?.artistId) {
        if (parsedArtistNames.isNotEmpty()) {
            isArtistLoading = true
            withContext(Dispatchers.IO) {
                val map = mutableMapOf<String, ArtistData>()
                parsedArtistNames.forEachIndexed { index, name ->
                    val idToUse = if (index == 0 && parsedArtistNames.size == 1) song?.artistId else null
                    val data = ArtistAvatarHelper.getArtistInfo(idToUse, name)
                    map[name] = data
                }
                withContext(Dispatchers.Main) {
                    multiArtistData = map
                    isArtistLoading = false
                }
            }
        } else {
            multiArtistData = emptyMap()
            isArtistLoading = false
        }
    }

    // State for related / recommended songs
    var sameArtistSongs by remember(song?.videoId, song?.artistId, song?.artist) { mutableStateOf<List<OnlineSong>>(emptyList()) }
    var recommendedSongs by remember(song?.videoId) { mutableStateOf<List<OnlineSong>>(emptyList()) }
    var isLoadingRelated by remember(song?.videoId) { mutableStateOf(false) }

    LaunchedEffect(song?.videoId, song?.artistId, song?.artist, multiArtistData) {
        val vId = song?.videoId
        if (!vId.isNullOrBlank()) {
            isLoadingRelated = true
            withContext(Dispatchers.IO) {
                // 1. Same Artist Songs (from primary artist's top tracks)
                val primaryArtistData = multiArtistData[parsedArtistNames.firstOrNull()]
                val artistSongs = (primaryArtistData?.topSongs ?: emptyList())
                    .filter { it.videoId != vId }
                    .take(6)
                sameArtistSongs = artistSongs

                // 2. Radio / Similar discoveries
                val result = YouTubeMusic.startRadio(videoId = vId)
                result.onSuccess { list ->
                    val existingIds = artistSongs.map { it.videoId }.toSet() + vId
                    recommendedSongs = list.filter { it.videoId !in existingIds }
                }
                isLoadingRelated = false
            }
        } else {
            sameArtistSongs = emptyList()
            recommendedSongs = emptyList()
            isLoadingRelated = false
        }
    }

    // State for tech specs disclosure (Stats)
    var isStatsExpanded by remember { mutableStateOf(false) }
    val session by PlaybackDiagnosticsLogger.currentSessionState.collectAsStateWithLifecycle()

    // Artist navigation handler (Direct if single, Selection dialog if multiple)
    val artistName = song?.artist?.takeIf { it.isNotBlank() && it != "<unknown>" } ?: "Unknown Artist"
    val handleArtistNavigation = {
        if (parsedArtistNames.size > 1) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            showArtistSelectionDialog = true
        } else {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            val primaryName = parsedArtistNames.firstOrNull() ?: artistName
            val targetArtistId = multiArtistData[primaryName]?.resolvedArtistId ?: song?.artistId
            onArtistClick(targetArtistId, primaryName)
        }
    }

    // ==========================================
    // MULTI-ARTIST SELECTION DIALOG
    // ==========================================
    if (showArtistSelectionDialog && parsedArtistNames.size > 1) {
        AlertDialog(
            onDismissRequest = { showArtistSelectionDialog = false },
            title = {
                Text(
                    text = "アーティストを選択",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 19.sp),
                    color = contentColor
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "表示するアーティストを選択してください",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentSecondary
                    )
                    parsedArtistNames.forEach { singleArtistName ->
                        val data = multiArtistData[singleArtistName]
                        val singleArtistId = data?.resolvedArtistId
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showArtistSelectionDialog = false
                                    onArtistClick(singleArtistId, singleArtistName)
                                },
                            shape = RoundedCornerShape(16.dp),
                            color = contentColor.copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(dynamicAccentColor.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!data?.thumbnailUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = data.thumbnailUrl,
                                            contentDescription = singleArtistName,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Rounded.Person,
                                            contentDescription = null,
                                            tint = dynamicAccentColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = singleArtistName,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = contentColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = data?.subscriberCount?.takeIf { it.isNotBlank() } ?: "アーティストページを開く",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = contentSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = dynamicAccentColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showArtistSelectionDialog = false }) {
                    Text("閉じる", color = contentSecondary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ==========================================
        // 1. HERO ARTIST CARD (With Multi-Artist Support)
        // ==========================================
        val isMultiArtist = parsedArtistNames.size > 1

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .clickable {
                    handleArtistNavigation()
                },
            shape = RoundedCornerShape(22.dp),
            color = cardBgColor
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Artist Avatar(s) - Single or Overlapping Multi-Avatar
                if (isMultiArtist) {
                    val displayCount = parsedArtistNames.size.coerceAtMost(3)
                    val avatarSize = 46.dp
                    val overlapOffset = 22.dp
                    val totalWidth = avatarSize + overlapOffset * (displayCount - 1)

                    Box(
                        modifier = Modifier
                            .size(width = totalWidth, height = 54.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        parsedArtistNames.take(3).forEachIndexed { idx, name ->
                            val aData = multiArtistData[name]
                            Box(
                                modifier = Modifier
                                    .offset(x = overlapOffset * idx)
                                    .size(avatarSize)
                                    .clip(CircleShape)
                                    .background(dynamicAccentColor.copy(alpha = 0.20f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!aData?.thumbnailUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = aData.thumbnailUrl,
                                        contentDescription = name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = null,
                                        tint = dynamicAccentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    val singleData = multiArtistData[parsedArtistNames.firstOrNull() ?: artistName]
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(dynamicAccentColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!singleData?.thumbnailUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = singleData.thumbnailUrl,
                                contentDescription = artistName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = dynamicAccentColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                // Artist Details
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = artistName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val subText = if (isMultiArtist) {
                        "${parsedArtistNames.size}組のアーティスト（タップして選択）"
                    } else {
                        val singleData = multiArtistData[parsedArtistNames.firstOrNull() ?: artistName]
                        singleData?.subscriberCount?.takeIf { it.isNotBlank() } ?: "アーティストの作品・ディスコグラフィを見る"
                    }

                    Text(
                        text = subText,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Action Arrow Pill
                Surface(
                    shape = CircleShape,
                    color = dynamicAccentColor.copy(alpha = 0.14f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                            contentDescription = "アーティストページを開く",
                            tint = dynamicAccentColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        // ==========================================
        // 2. QUICK ACTION ROW
        // ==========================================
        var isStartingRadio by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Radio Button (Plays Next seamlessly without interrupting current song)
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(enabled = !isStartingRadio && song != null) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        val current = song ?: return@clickable
                        isStartingRadio = true
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                val vId = current.videoId
                                val radioTracks = if (!vId.isNullOrBlank()) {
                                    YouTubeMusic.startRadio(videoId = vId).getOrNull() ?: emptyList()
                                } else emptyList()

                                val radioSongItems = radioTracks
                                    .filter { it.videoId != current.videoId }
                                    .map { onlineSong ->
                                        SongItem(
                                            id = onlineSong.videoId.hashCode().toLong(),
                                            rawTitle = onlineSong.title,
                                            rawArtist = onlineSong.artist,
                                            albumId = 0,
                                            path = null,
                                            duration = onlineSong.durationMs / 1000,
                                            dateAdded = System.currentTimeMillis()
                                        ).apply {
                                            videoId = onlineSong.videoId
                                            thumbnailUrl = onlineSong.thumbnailUrl
                                            artistId = onlineSong.artistId
                                        }
                                    }

                                withContext(Dispatchers.Main) {
                                    if (radioSongItems.isNotEmpty()) {
                                        viewModel.addSongsToQueueNext(radioSongItems)
                                        com.codetrio.overdrive.ui.SnackbarController.showMessage("📻 ${radioSongItems.size}曲のラジオを次の再生キューに割り込み追加しました")
                                    } else {
                                        com.codetrio.overdrive.ui.SnackbarController.showMessage("ラジオキューの取得に失敗しました")
                                    }
                                    isStartingRadio = false
                                }
                            } catch (_: Exception) {
                                withContext(Dispatchers.Main) {
                                    isStartingRadio = false
                                    com.codetrio.overdrive.ui.SnackbarController.showMessage("ラジオの開始中にエラーが発生しました")
                                }
                            }
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                color = cardBgColor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Radio,
                        contentDescription = null,
                        tint = dynamicAccentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isStartingRadio) "キュー追加中..." else "ラジオを開始",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = contentColor
                    )
                }
            }

            // Add to Playlist Button
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSaveClick()
                    },
                shape = RoundedCornerShape(16.dp),
                color = cardBgColor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.PlaylistAdd,
                        contentDescription = null,
                        tint = dynamicAccentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "プレイリスト追加",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = contentColor
                    )
                }
            }

            // Copy Track Info Button
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        val text = "${song?.title ?: "Unknown"} - $artistName"
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Track Info", text)
                        clipboard.setPrimaryClip(clip)
                        com.codetrio.overdrive.ui.SnackbarController.showMessage("曲情報をコピーしました")
                    },
                shape = RoundedCornerShape(16.dp),
                color = cardBgColor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = null,
                        tint = dynamicAccentColor,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "曲情報をコピー",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = contentColor
                    )
                }
            }
        }

        // ==========================================
        // 3. SAME ARTIST'S POPULAR SONGS (Artist Centric)
        // ==========================================
        if (sameArtistSongs.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = cardBgColor
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${artistName} の人気曲",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = contentColor
                        )

                        Text(
                            text = "${sameArtistSongs.size}曲",
                            style = MaterialTheme.typography.labelSmall,
                            color = contentSecondary
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        sameArtistSongs.forEach { relSong ->
                            RecommendationTrackRow(
                                song = relSong,
                                dynamicAccentColor = dynamicAccentColor,
                                contentColor = contentColor,
                                contentSecondary = contentSecondary,
                                onPlay = {
                                    val songItem = SongItem(
                                        id = relSong.videoId.hashCode().toLong(),
                                        rawTitle = relSong.title,
                                        rawArtist = relSong.artist,
                                        albumId = 0,
                                        path = null,
                                        duration = relSong.durationMs / 1000,
                                        dateAdded = System.currentTimeMillis()
                                    ).apply {
                                        videoId = relSong.videoId
                                        thumbnailUrl = relSong.thumbnailUrl
                                        artistId = relSong.artistId
                                    }
                                    viewModel.playSong(songItem)
                                },
                                onAddToQueue = {
                                    val songItem = SongItem(
                                        id = relSong.videoId.hashCode().toLong(),
                                        rawTitle = relSong.title,
                                        rawArtist = relSong.artist,
                                        albumId = 0,
                                        path = null,
                                        duration = relSong.durationMs / 1000,
                                        dateAdded = System.currentTimeMillis()
                                    ).apply {
                                        videoId = relSong.videoId
                                        thumbnailUrl = relSong.thumbnailUrl
                                        artistId = relSong.artistId
                                    }
                                    viewModel.addToQueue(songItem)
                                    com.codetrio.overdrive.ui.SnackbarController.showMessage("キューに追加しました: ${relSong.title}")
                                }
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 4. RELATED / DISCOVERY RECOMMENDATIONS
        // ==========================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = cardBgColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "関連する曲・おすすめ",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = contentColor
                    )

                    if (recommendedSongs.isNotEmpty()) {
                        Text(
                            text = "${recommendedSongs.size}曲",
                            style = MaterialTheme.typography.labelSmall,
                            color = contentSecondary
                        )
                    }
                }

                if (isLoadingRelated && recommendedSongs.isEmpty() && sameArtistSongs.isEmpty()) {
                    // Skeleton Shimmer Loading State
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(3) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(contentColor.copy(alpha = 0.04f))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(contentColor.copy(alpha = 0.08f))
                                )
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.65f)
                                            .height(12.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(contentColor.copy(alpha = 0.08f))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.4f)
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(contentColor.copy(alpha = 0.05f))
                                    )
                                }
                            }
                        }
                    }
                } else if (recommendedSongs.isEmpty() && sameArtistSongs.isEmpty()) {
                    Text(
                        text = "おすすめの楽曲が見つかりませんでした",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentSecondary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    // List of Recommendations
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        recommendedSongs.take(8).forEach { relSong ->
                            RecommendationTrackRow(
                                song = relSong,
                                dynamicAccentColor = dynamicAccentColor,
                                contentColor = contentColor,
                                contentSecondary = contentSecondary,
                                onPlay = {
                                    val songItem = SongItem(
                                        id = relSong.videoId.hashCode().toLong(),
                                        rawTitle = relSong.title,
                                        rawArtist = relSong.artist,
                                        albumId = 0,
                                        path = null,
                                        duration = relSong.durationMs / 1000,
                                        dateAdded = System.currentTimeMillis()
                                    ).apply {
                                        videoId = relSong.videoId
                                        thumbnailUrl = relSong.thumbnailUrl
                                        artistId = relSong.artistId
                                    }
                                    viewModel.playSong(songItem)
                                },
                                onAddToQueue = {
                                    val songItem = SongItem(
                                        id = relSong.videoId.hashCode().toLong(),
                                        rawTitle = relSong.title,
                                        rawArtist = relSong.artist,
                                        albumId = 0,
                                        path = null,
                                        duration = relSong.durationMs / 1000,
                                        dateAdded = System.currentTimeMillis()
                                    ).apply {
                                        videoId = relSong.videoId
                                        thumbnailUrl = relSong.thumbnailUrl
                                        artistId = relSong.artistId
                                    }
                                    viewModel.addToQueue(songItem)
                                    com.codetrio.overdrive.ui.SnackbarController.showMessage("キューに追加しました: ${relSong.title}")
                                }
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 5. TRACK & RELEASE METADATA CARD
        // ==========================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = cardBgColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = dynamicAccentColor,
                        modifier = Modifier.size(19.dp)
                    )
                    Text(
                        text = "楽曲・作品情報",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = contentColor
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CleanMetadataRow(label = "タイトル", value = song?.title ?: "不明なタイトル", contentColor = contentColor, contentSecondary = contentSecondary)
                    CleanMetadataRow(
                        label = "アーティスト",
                        value = artistName,
                        contentColor = contentColor,
                        contentSecondary = contentSecondary,
                        isClickable = true,
                        onClick = { handleArtistNavigation() }
                    )
                    if (song?.duration != null && song.duration > 0) {
                        val minutes = song.duration / 60
                        val seconds = song.duration % 60
                        CleanMetadataRow(
                            label = "再生時間",
                            value = String.format(Locale.getDefault(), "%d:%02d", minutes, seconds),
                            contentColor = contentColor,
                            contentSecondary = contentSecondary
                        )
                    }
                    CleanMetadataRow(
                        label = "オーディオ品質",
                        value = if (song?.path?.startsWith("http") == true || song?.videoId != null) "高音質ストリーミング (Adaptive)" else "ローカル音源 (Lossless/High)",
                        contentColor = contentColor,
                        contentSecondary = contentSecondary
                    )
                }
            }
        }

        // ==========================================
        // 6. DETAILED TECHNICAL SPECS (Stats Enabled Only)
        // ==========================================
        if (isStatsEnabled) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow)),
                shape = RoundedCornerShape(20.dp),
                color = cardBgColor
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Expandable Accordion Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                isStatsExpanded = !isStatsExpanded
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Speed,
                                contentDescription = null,
                                tint = dynamicAccentColor,
                                modifier = Modifier.size(19.dp)
                            )
                            Text(
                                text = "詳細な技術仕様 (Stats)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = contentColor
                            )
                        }

                        Icon(
                            imageVector = if (isStatsExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = if (isStatsExpanded) "閉じる" else "展開",
                            tint = contentSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Expanded Technical Diagnostic Content
                    AnimatedVisibility(
                        visible = isStatsExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val activeSession = session
                            val format = activeSession?.codec ?: activeSession?.mimeType
                            val rawBitrate = (activeSession?.bitrate ?: 0)
                            val bitrateKbps = when {
                                rawBitrate > 1000 -> rawBitrate / 1000
                                rawBitrate > 0 -> rawBitrate
                                format?.contains("opus", ignoreCase = true) == true -> 160
                                else -> 256
                            }
                            val sampleRate = (activeSession?.sampleRate ?: 0).takeIf { it > 0 } ?: 44100
                            val channelCount = (activeSession?.channelCount ?: 0).takeIf { it > 0 } ?: 2

                            CleanMetadataRow(label = "コーデック / 形式", value = format ?: "AAC (Advanced Audio Coding)", contentColor = contentColor, contentSecondary = contentSecondary)
                            CleanMetadataRow(label = "ビットレート", value = "$bitrateKbps kbps", contentColor = contentColor, contentSecondary = contentSecondary)
                            CleanMetadataRow(label = "サンプリング周波数", value = String.format(Locale.getDefault(), "%.1f kHz", sampleRate / 1000f), contentColor = contentColor, contentSecondary = contentSecondary)
                            CleanMetadataRow(label = "チャンネル構成", value = if (channelCount >= 6) "5.1ch サラウンド" else "ステレオ (2ch)", contentColor = contentColor, contentSecondary = contentSecondary)
                            CleanMetadataRow(label = "Loudness Normalization", value = "有効 (-14 LUFS ターゲット)", contentColor = contentColor, contentSecondary = contentSecondary)

                            Spacer(modifier = Modifier.height(4.dp))

                            // Open Full Stats Dialog Button
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onOpenStats()
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = dynamicAccentColor.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Equalizer,
                                        contentDescription = null,
                                        tint = dynamicAccentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "詳細な統計情報ダイアログを開く",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = dynamicAccentColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun RecommendationTrackRow(
    song: OnlineSong,
    dynamicAccentColor: Color,
    contentColor: Color,
    contentSecondary: Color,
    onPlay: () -> Unit,
    onAddToQueue: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onPlay()
            }
            .padding(vertical = 6.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Thumbnail Cover
        AsyncImage(
            model = song.thumbnailUrl,
            contentDescription = song.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(contentColor.copy(alpha = 0.08f))
        )

        // Titles
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${song.artist}${song.duration?.let { " • $it" } ?: ""}",
                style = MaterialTheme.typography.bodySmall,
                color = contentSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Quick Add to Queue Icon Button
        IconButton(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onAddToQueue()
            },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "キューに追加",
                tint = dynamicAccentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun CleanMetadataRow(
    label: String,
    value: String,
    contentColor: Color,
    contentSecondary: Color,
    isClickable: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isClickable) Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onClick)
                else Modifier
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = contentSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isClickable) FontWeight.Bold else FontWeight.Medium
            ),
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
