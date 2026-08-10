@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package com.codetrio.overdrive.ui.player

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import coil.size.Precision
import com.codetrio.overdrive.model.SongItem
import com.codetrio.overdrive.ui.player.canvas.CanvasArtwork
import com.codetrio.overdrive.ui.player.canvas.CanvasArtworkPlaybackCache
import com.codetrio.overdrive.ui.player.canvas.CanvasArtworkPlayer
import com.codetrio.overdrive.ui.player.canvas.resolveCanvasArtworkForPlayback
import com.codetrio.overdrive.viewmodel.PlayerSharedViewModel
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.AspectRatioFrameLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import androidx.activity.compose.BackHandler

@Composable
fun ArtworkPager(
    viewModel: PlayerSharedViewModel,
    currentSong: SongItem,
    songList: List<SongItem>,
    currentSongIndex: Int,
    context: Context,
    modifier: Modifier = Modifier,
    userScrollEnabled: Boolean = true,
    allowCanvas: Boolean = true
) {
    val isMvFullscreen by viewModel.isMvFullscreen.collectAsStateWithLifecycle()
    val isInPipMode by viewModel.isInPipMode.collectAsStateWithLifecycle()
    val isTrueFullscreen = isMvFullscreen || isInPipMode
    
    BackHandler(enabled = isMvFullscreen) {
        viewModel.setMvFullscreen(false)
    }
    val pagerState = rememberPagerState(
        initialPage = currentSongIndex.coerceAtLeast(0)
    ) {
        songList.size.coerceAtLeast(1)
    }

    // Sync Pager Page with VM when active song changes externally
    LaunchedEffect(currentSongIndex) {
        if (currentSongIndex >= 0 && currentSongIndex < pagerState.pageCount && pagerState.currentPage != currentSongIndex) {
            pagerState.animateScrollToPage(currentSongIndex)
        }
    }

    // Sync VM when swiped in Pager
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress && currentSongIndex >= 0 && pagerState.currentPage != currentSongIndex) {
            viewModel.playSongAtIndex(pagerState.currentPage)
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = userScrollEnabled,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val song = if (page == currentSongIndex) currentSong else (songList.getOrNull(page) ?: currentSong)
            val isCurrentPage = page == currentSongIndex
            
            // Phase 1: Local State Isolation
            val activeCanvasArtwork by viewModel.canvasArtwork.collectAsState()
            val currentSongArtwork by viewModel.currentSongArtwork.collectAsState()

            val rawUri = song.getAlbumArtUri()
            val videoId = song.videoId
            
            // Determine artwork source with fallback chain
            val artworkData = remember(song.id, isCurrentPage, currentSongArtwork) {
                if (isCurrentPage && currentSongArtwork != null) {
                    currentSongArtwork // Use pre-extracted bytes for active song
                } else if (rawUri != null && rawUri.toString().isNotEmpty()) {
                    SongItem.enhanceThumbnailUrl(rawUri.toString()).toUri()
                } else if (!videoId.isNullOrEmpty()) {
                    "https://img.youtube.com/vi/$videoId/hqdefault.jpg".toUri()
                } else {
                    null
                }
            }

            var isError by remember(artworkData) { mutableStateOf(false) }

            // Canvas artwork state — resolved asynchronously per song
            val prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
            val showAnimatedArt = prefs.getBoolean("show_animated_art", true) && allowCanvas

            var canvasArtworkState by remember(song.id) { mutableStateOf<CanvasArtwork?>(null) }

            // Attempt to resolve canvas artwork
            LaunchedEffect(song.id, isCurrentPage, showAnimatedArt, activeCanvasArtwork) {
                if (!showAnimatedArt) {
                    canvasArtworkState = null
                    return@LaunchedEffect
                }

                // If this is the active page, prioritize the VM's resolved artwork
                if (isCurrentPage && activeCanvasArtwork != null) {
                    canvasArtworkState = activeCanvasArtwork
                    return@LaunchedEffect
                }

                // Fast path: check if the song already has an animatedThumbnailUrl from InnerTube
                val ytAnimatedUrl = song.animatedThumbnailUrl
                if (!ytAnimatedUrl.isNullOrEmpty()) {
                    canvasArtworkState = CanvasArtwork(
                        animated = ytAnimatedUrl,
                        videoUrl = ytAnimatedUrl,
                    )
                    return@LaunchedEffect
                }

                // Slow path: resolve from network / cache
                val mediaId = song.id.toString()
                val songTitle = song.title ?: return@LaunchedEffect
                val artistName = song.artist ?: return@LaunchedEffect

                val resolved = withContext(Dispatchers.IO) {
                    resolveCanvasArtworkForPlayback(
                        mediaId = mediaId,
                        songTitleRaw = songTitle,
                        artistNameRaw = artistName,
                        albumTitleRaw = null,
                        requireVertical = true,
                        allowNetwork = isCurrentPage,
                    )
                }
                canvasArtworkState = resolved
            }

            val canvasPrimaryUrl = canvasArtworkState?.preferredVerticalAnimationUrl ?: canvasArtworkState?.preferredAnimationUrl
            val canvasFallbackUrl = canvasArtworkState?.videoUrlVertical ?: canvasArtworkState?.videoUrl

            val isMvMode by viewModel.isMvMode.collectAsState()
            val musicVideoUrl by viewModel.musicVideoUrl.collectAsState()
            val currentPosition by viewModel.currentPosition.collectAsState()
            val isPlaying by viewModel.isPlaying.collectAsState()
            val mvSeekRequest by viewModel.mvSeekRequest.collectAsState()

            val showStaticArt = !(isCurrentPage && isMvMode)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isCurrentPage) {
                        if (!isCurrentPage) return@pointerInput
                        detectTapGestures(
                            onTap = {
                                val prefs = context.getSharedPreferences("AppSettings", android.content.Context.MODE_PRIVATE)
                                if (prefs.getBoolean("debug_toasts_enabled", false)) {
                                    val s = songList.getOrNull(currentSongIndex)
                                    android.widget.Toast.makeText(context, "Song ID: ${s?.id}\nVideo ID: ${s?.videoId}", android.widget.Toast.LENGTH_LONG).show()
                                }
                            },
                            onDoubleTap = { offset ->
                                val width = size.width
                                if (offset.x < width * 0.33f) {
                                    val currentMs = currentPosition
                                    viewModel.seekTo((currentMs - 10000).coerceAtLeast(0))
                                } else if (offset.x > width * 0.66f) {
                                    val currentMs = currentPosition
                                    val duration = viewModel.duration.value
                                    val targetMs = if (duration > 0) (currentMs + 10000).coerceAtMost(duration) else (currentMs + 10000)
                                    viewModel.seekTo(targetMs)
                                } else {
                                    viewModel.toggleFavorite()
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // 1. Static album art as base layer (hidden during MV playback)
                if (showStaticArt && artworkData != null && !isError) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(artworkData)
                            .crossfade(100)
                            .build(),
                        contentDescription = null,
                        onState = { state ->
                            if (state is AsyncImagePainter.State.Error) isError = true
                        },
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (showStaticArt && (artworkData == null || isError)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                } else if (!showStaticArt) {
                    // Clean black background behind Music Video so no album art bleeds through
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    )
                }

                // 2. Canvas motion artwork overlay (crossfades in when ready)
                AnimatedVisibility(
                    visible = showAnimatedArt && isCurrentPage && !isMvMode &&
                        (!canvasPrimaryUrl.isNullOrBlank() || !canvasFallbackUrl.isNullOrBlank()),
                    enter = fadeIn(tween(1500)),
                    exit = fadeOut(tween(800)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    CanvasArtworkPlayer(
                        primaryUrl = canvasPrimaryUrl,
                        fallbackUrl = canvasFallbackUrl,
                        isPlaying = isCurrentPage && !isMvMode && isPlaying,
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // 3. Music Video Playback overlay
                AnimatedVisibility(
                    visible = isCurrentPage && isMvMode && !musicVideoUrl.isNullOrBlank(),
                    enter = fadeIn(tween(500)),
                    exit = fadeOut(tween(500)),
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (isTrueFullscreen) Modifier else Modifier.padding(horizontal = 14.dp))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        com.codetrio.overdrive.ui.player.canvas.MusicVideoPlayer(
                            videoUrl = musicVideoUrl,
                            isPlaying = isCurrentPage && isMvMode && isPlaying,
                            currentPositionMs = currentPosition.toLong(),
                            onPositionUpdate = { },
                            onDurationUpdate = { durMs ->
                                if (isMvMode && durMs > 0) {
                                    viewModel.setDuration(durMs.toInt())
                                }
                            },
                            seekRequest = mvSeekRequest,
                            onSeekRequestConsumed = {
                                viewModel.clearMvSeekRequest()
                            },
                            onPlaybackCompleted = {
                                if (isMvMode) {
                                    viewModel.playNextSong()
                                }
                            },
                            resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT,
                            modifier = Modifier
                                .fillMaxSize()
                                .then(if (isTrueFullscreen) Modifier else Modifier.clip(RoundedCornerShape(12.dp)))
                        )
                        
                        if (!isInPipMode) {
                            androidx.compose.material3.IconButton(
                                onClick = { viewModel.setMvFullscreen(!isMvFullscreen) },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = if (isMvFullscreen) androidx.compose.material.icons.Icons.Default.FullscreenExit else androidx.compose.material.icons.Icons.Default.Fullscreen,
                                    contentDescription = if (isMvFullscreen) "Exit Fullscreen" else "Enter Fullscreen",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
