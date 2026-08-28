package com.codetrio.overdrive.player.queue

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.codetrio.overdrive.R
import com.codetrio.overdrive.data.innertube.InnerTubeClient
import com.codetrio.overdrive.data.innertube.InnerTubeParser
import com.codetrio.overdrive.data.innertube.path
import com.codetrio.overdrive.model.SongItem
import com.codetrio.overdrive.ui.player.deriveArtworkSurfaceColor
import com.codetrio.overdrive.viewmodel.PlayerSharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.math.abs

// --- 1. Strategies Section ---

interface Queue {
    suspend fun getInitialStatus(): Status
    fun hasNextPage(): Boolean
    suspend fun nextPage(): List<SongItem>
    
    val currentTitle: String?
    val preloadItem: SongItem?

    fun shouldExpandToFullQueueWhenAutoLoadMoreDisabled(): Boolean = false

    data class Status(
        val title: String?,
        val items: List<SongItem>,
        val mediaItemIndex: Int = 0,
        val position: Long = 0L
    )
}

class StaticQueue(
    override val currentTitle: String? = null,
    private val initialItems: List<SongItem>,
    override val preloadItem: SongItem? = null,
    private val startIndex: Int = 0,
    private val position: Long = 0L
) : Queue {

    override suspend fun getInitialStatus(): Queue.Status {
        return Queue.Status(
            title = currentTitle,
            items = initialItems,
            mediaItemIndex = startIndex,
            position = position
        )
    }

    override fun hasNextPage(): Boolean = false

    override suspend fun nextPage(): List<SongItem> = emptyList()
}

class YouTubeQueue(
    private val videoId: String? = null,
    private val playlistId: String? = null,
    private val initialParams: String? = null,
    private var continuationToken: String? = null,
    override val preloadItem: SongItem? = null,
    private val followAutomixPreview: Boolean = true,
    private val startIndex: Int = 0,
    private val position: Long = 0L
) : Queue {

    override var currentTitle: String? = null
        private set

    companion object {
        fun radio(song: SongItem, params: String? = null) = YouTubeQueue(
            videoId = song.videoId,
            playlistId = song.videoId?.let { "RDAMVM$it" },
            initialParams = params,
            preloadItem = song,
            followAutomixPreview = true
        )
    }

    override suspend fun getInitialStatus(): Queue.Status {
        val status = fetchNext(isInitial = true)
        val matchedIndex = if (preloadItem != null) {
            status.items.indexOfFirst { it.videoId == preloadItem.videoId }
        } else {
            startIndex
        }
        val finalIndex = if (matchedIndex >= 0) matchedIndex else 0
        return Queue.Status(
            title = status.title,
            items = status.items,
            mediaItemIndex = finalIndex,
            position = position
        )
    }

    override fun hasNextPage(): Boolean {
        return continuationToken != null
    }

    override suspend fun nextPage(): List<SongItem> {
        if (continuationToken == null) return emptyList()
        return fetchNext(isInitial = false).items
    }

    private suspend fun fetchNext(isInitial: Boolean): Queue.Status = withContext(Dispatchers.IO) {
        val response = if (isInitial) {
            InnerTubeClient.next(videoId = videoId, playlistId = playlistId, params = initialParams)
        } else {
            InnerTubeClient.next(videoId = null, continuation = continuationToken)
        }

        val songs = mutableListOf<SongItem>()
        
        val playlistPanel = if (isInitial) {
            findJsonObjectRecursively(response, "playlistPanelRenderer")
        } else {
            findJsonObjectRecursively(response, "playlistPanelRenderer") ?: 
            response.path("continuationContents.playlistPanelContinuation")?.asJsonObject
        }

        if (isInitial) {
            currentTitle = playlistPanel?.path("title")?.asString
        }

        val playlistContents = playlistPanel?.path("contents")?.asJsonArray

        playlistContents?.forEach { content ->
            val renderer = content.asJsonObject.path("playlistPanelVideoRenderer")?.asJsonObject ?: return@forEach
            val id = renderer.path("videoId")?.asString ?: return@forEach
            val titleText = renderer.path("title.runs.0.text")?.asString ?: return@forEach
            val shortByline = renderer.path("shortBylineText.runs")?.asJsonArray
                ?.joinToString("") { it.asJsonObject.path("text")?.asString ?: "" } ?: ""
            val lengthText = renderer.path("lengthText.runs.0.text")?.asString
            val thumb = InnerTubeParser.getHighResThumbnailUrl(
                renderer.path("thumbnail.thumbnails")
                    ?.asJsonArray?.lastOrNull()?.asJsonObject?.path("url")?.asString
            )

            songs.add(SongItem.createOnlineSong(
                videoId = id,
                title = titleText,
                artist = shortByline.trim(),
                streamUrl = "",
                durationMs = parseDuration(lengthText),
                thumbnailUrl = thumb
            ))
        }

        continuationToken = playlistPanel?.path("continuations.0.nextRadioContinuationData.continuation")?.asString
            ?: playlistPanel?.path("continuations.0.nextContinuationData.continuation")?.asString

        val automixRenderer = findJsonObjectRecursively(response, "automixPreviewVideoRenderer")
        if (followAutomixPreview && automixRenderer != null) {
            val watchPlaylistEndpoint = findJsonObjectRecursively(automixRenderer, "watchPlaylistEndpoint")
            val autoPlaylistId = watchPlaylistEndpoint?.path("playlistId")?.asString
            val autoParams = watchPlaylistEndpoint?.path("params")?.asString
            if (!autoPlaylistId.isNullOrEmpty()) {
                android.util.Log.d("YouTubeQueue", "Found automixPreviewVideoRenderer! Recursively resolving automix playlistId=$autoPlaylistId, params=$autoParams")
                try {
                    val autoResponse = InnerTubeClient.next(videoId = null, playlistId = autoPlaylistId, params = autoParams)
                    val autoPlaylistPanel = findJsonObjectRecursively(autoResponse, "playlistPanelRenderer")
                    val autoPlaylistContents = autoPlaylistPanel?.path("contents")?.asJsonArray
                    
                    autoPlaylistContents?.forEach { content ->
                        val renderer = content.asJsonObject.path("playlistPanelVideoRenderer")?.asJsonObject ?: return@forEach
                        val id = renderer.path("videoId")?.asString ?: return@forEach
                        val titleText = renderer.path("title.runs.0.text")?.asString ?: return@forEach
                        val shortByline = renderer.path("shortBylineText.runs")?.asJsonArray
                            ?.joinToString("") { it.asJsonObject.path("text")?.asString ?: "" } ?: ""
                        val lengthText = renderer.path("lengthText.runs.0.text")?.asString
                        val thumb = InnerTubeParser.getHighResThumbnailUrl(
                            renderer.path("thumbnail.thumbnails")
                                ?.asJsonArray?.lastOrNull()?.asJsonObject?.path("url")?.asString
                        )

                        if (songs.none { it.videoId == id }) {
                            songs.add(SongItem.createOnlineSong(
                                videoId = id,
                                title = titleText,
                                artist = shortByline.trim(),
                                streamUrl = "",
                                durationMs = parseDuration(lengthText),
                                thumbnailUrl = thumb
                            ))
                        }
                    }
                    
                    val autoContinuation = autoPlaylistPanel?.path("continuations.0.nextRadioContinuationData.continuation")?.asString
                        ?: autoPlaylistPanel?.path("continuations.0.nextContinuationData.continuation")?.asString
                    if (autoContinuation != null) {
                        continuationToken = autoContinuation
                    }
                } catch (e: Exception) {
                    android.util.Log.e("YouTubeQueue", "Failed recursively resolving automix playlist", e)
                }
            }
        }

        Queue.Status(title = currentTitle, items = songs)
    }

    private fun findJsonObjectRecursively(element: com.google.gson.JsonElement?, targetKey: String): com.google.gson.JsonObject? {
        if (element == null || !element.isJsonObject) return null
        val obj = element.asJsonObject
        if (obj.has(targetKey) && obj.get(targetKey).isJsonObject) {
            return obj.getAsJsonObject(targetKey)
        }
        for (entry in obj.entrySet()) {
            val child = entry.value
            if (child.isJsonObject) {
                val found = findJsonObjectRecursively(child, targetKey)
                if (found != null) return found
            } else if (child.isJsonArray) {
                for (item in child.asJsonArray) {
                    val found = findJsonObjectRecursively(item, targetKey)
                    if (found != null) return found
                }
            }
        }
        return null
    }

    private fun parseDuration(duration: String?): Long {
        if (duration.isNullOrEmpty()) return 0L
        val parts = duration.split(":")
        var millis = 0L
        try {
            when (parts.size) {
                3 -> {
                    millis += parts[0].toLong() * 3600000L
                    millis += parts[1].toLong() * 60000L
                    millis += parts[2].toLong() * 1000L
                }
                2 -> {
                    millis += parts[0].toLong() * 60000L
                    millis += parts[1].toLong() * 1000L
                }
                1 -> {
                    millis += parts[0].toLong() * 1000L
                }
            }
        } catch (e: Exception) {
            // Ignore format errors
        }
        return millis
    }
}

// --- 2. Reordering Core & UI Presentation Section ---

fun formatTrackDuration(durationMs: Long): String {
    if (durationMs <= 0L) return ""
    val totalSeconds = if (durationMs > 36000000L) durationMs / 1000L else if (durationMs > 1000L) durationMs / 1000L else durationMs
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes >= 60) {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        String.format("%d:%02d:%02d", hours, remainingMinutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}

/**
 * YouTube Music styled Queue List Item.
 * Features rounded album art, animated playing equalizer, clear typography with duration, and drag handle.
 */
@Composable
fun QueueTrackListItem(
    song: SongItem,
    isPlaying: Boolean,
    isDark: Boolean,
    contentColor: Color,
    contentSecondary: Color,
    dynamicAccentColor: Color,
    onClick: () -> Unit,
    onRemoveClick: (() -> Unit)?,
    dragHandle: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val albumArtModel = remember(song.id) { song.getAlbumArtUri() ?: R.drawable.default_album_art }
    val itemBgColor = if (isPlaying) {
        dynamicAccentColor.copy(alpha = 0.12f)
    } else {
        Color.Transparent
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = itemBgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album Art Thumbnail (YT Music 48dp style)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = albumArtModel,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // If currently playing, render animated equalizer bars over dark overlay
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.52f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val transition = rememberInfiniteTransition(label = "equalizer")
                        val bar1Scale by transition.animateFloat(
                            initialValue = 0.25f,
                            targetValue = 0.85f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 480, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "bar1"
                        )
                        val bar2Scale by transition.animateFloat(
                            initialValue = 0.35f,
                            targetValue = 0.98f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 380, easing = androidx.compose.animation.core.LinearOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "bar2"
                        )
                        val bar3Scale by transition.animateFloat(
                            initialValue = 0.15f,
                            targetValue = 0.75f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 520, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "bar3"
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier
                                .height(16.dp)
                                .align(Alignment.Center)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .fillMaxHeight(bar1Scale)
                                    .background(dynamicAccentColor, RoundedCornerShape(1.5.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .fillMaxHeight(bar2Scale)
                                    .background(dynamicAccentColor, RoundedCornerShape(1.5.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .fillMaxHeight(bar3Scale)
                                    .background(dynamicAccentColor, RoundedCornerShape(1.5.dp))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Metadata column: Title & (Artist • Duration)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = song.title,
                    fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = if (isPlaying) dynamicAccentColor else contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val durationFormatted = formatTrackDuration(song.duration)
                val subtitleText = if (durationFormatted.isNotEmpty()) {
                    "${song.artist} • $durationFormatted"
                } else {
                    song.artist
                }
                Text(
                    text = subtitleText,
                    fontSize = 13.sp,
                    color = contentSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Action: Remove & Drag Handle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (onRemoveClick != null) {
                    IconButton(
                        onClick = onRemoveClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = "Remove from Queue",
                            tint = contentSecondary.copy(alpha = 0.45f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                dragHandle()
            }
        }
    }
}
