package com.codetrio.overdrive.ui.player

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.codetrio.overdrive.R
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
    allowCanvas: Boolean = true,
    showTonearm: Boolean = true
) {
    val isMvFullscreen by viewModel.isMvFullscreen.collectAsStateWithLifecycle()
    val isInPipMode by viewModel.isInPipMode.collectAsStateWithLifecycle()
    val isTrueFullscreen = isMvFullscreen || isInPipMode
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    
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
            val artworkData: Any? = remember(song.id, isCurrentPage, currentSongArtwork, rawUri) {
                if (isCurrentPage && currentSongArtwork != null) {
                    currentSongArtwork // Use pre-extracted bytes for active song
                } else if (song.id == -9999L) {
                    R.drawable.artwork_autumn_wind // Direct resource ID for 100% reliable zero-delay onboarding preview
                } else if (rawUri != null && rawUri.toString().isNotEmpty()) {
                    val uriStr = rawUri.toString()
                    if (uriStr.startsWith("android.resource://")) {
                        rawUri
                    } else {
                        SongItem.enhanceThumbnailUrl(uriStr).toUri()
                    }
                } else if (!videoId.isNullOrEmpty()) {
                    "https://img.youtube.com/vi/$videoId/hqdefault.jpg".toUri()
                } else {
                    null
                }
            }

            var isError by remember(artworkData) { mutableStateOf(false) }

            val duration by viewModel.duration.collectAsState()
            val prefs = remember { context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE) }
            var playerTheme by remember { mutableStateOf(prefs.getString("player_theme", "fluid") ?: "fluid") }
            var showAnimatedArtPref by remember { mutableStateOf(prefs.getBoolean("show_animated_art", true)) }

            androidx.compose.runtime.DisposableEffect(prefs) {
                val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
                    if (key == "player_theme") {
                        playerTheme = sp.getString(key, "fluid") ?: "fluid"
                    } else if (key == "show_animated_art") {
                        showAnimatedArtPref = sp.getBoolean(key, true)
                    }
                }
                prefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
            }

            val showAnimatedArt = showAnimatedArtPref && allowCanvas
            val isVinyl = playerTheme == "vinyl"

            var canvasArtworkState by remember(song.id) { mutableStateOf<CanvasArtwork?>(null) }

            // Attempt to resolve canvas artwork
            LaunchedEffect(song.id, isCurrentPage, showAnimatedArt, activeCanvasArtwork) {
                if (!showAnimatedArt || isVinyl) {
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
                val songTitle = song.title
                val artistName = song.artist

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
            val hasMusicVideo by viewModel.hasMusicVideo.collectAsState()
            val musicVideoUrl by viewModel.musicVideoUrl.collectAsState()
            val currentPosition by viewModel.currentPosition.collectAsState()
            val isPlaying by viewModel.isPlaying.collectAsState()
            val mvSeekRequest by viewModel.mvSeekRequest.collectAsState()

            val isEffectiveMvMode = isMvMode && hasMusicVideo && !musicVideoUrl.isNullOrBlank()
            val showStaticArt = !(isCurrentPage && isEffectiveMvMode)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (userScrollEnabled) {
                            Modifier.pointerInput(isCurrentPage, isVinyl) {
                                if (!isCurrentPage || isVinyl) return@pointerInput
                                detectTapGestures(
                                    onTap = {
                                        val sp = context.getSharedPreferences("AppSettings", android.content.Context.MODE_PRIVATE)
                                        if (sp.getBoolean("debug_toasts_enabled", false)) {
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
                                            val dur = viewModel.duration.value
                                            val targetMs = if (dur > 0) (currentMs + 10000).coerceAtMost(dur) else (currentMs + 10000)
                                            viewModel.seekTo(targetMs)
                                        } else {
                                            viewModel.toggleFavorite()
                                        }
                                    }
                                )
                            }
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isVinyl && !isEffectiveMvMode) {
                    val isEffectivePlaying = (isPlaying || song.id == -9999L) && isCurrentPage
                    com.codetrio.overdrive.ui.player.themes.VinylDiscArtwork(
                        artworkData = artworkData,
                        isPlaying = isEffectivePlaying,
                        onDoubleTapSeekBackward = {
                            val currentMs = currentPosition
                            viewModel.seekTo((currentMs - 10000).coerceAtLeast(0))
                        },
                        onDoubleTapSeekForward = {
                            val currentMs = currentPosition
                            val dur = duration
                            val targetMs = if (dur > 0) (currentMs + 10000).coerceAtMost(dur) else (currentMs + 10000)
                            viewModel.seekTo(targetMs)
                        },
                        onDoubleTapFavorite = { viewModel.toggleFavorite() },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // 1. Static album art as base layer (hidden during MV playback)
                    if (showStaticArt && artworkData != null && !isError) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(artworkData)
                                .size(coil.size.Size.ORIGINAL)
                                .precision(coil.size.Precision.EXACT)
                                .allowHardware(true)
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
                        ExpressiveArtworkPlaceholder(
                            title = song.title,
                            artist = song.artist,
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (!showStaticArt) {
                        // Clean transparent background behind Music Video so no black box bleeds through
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Transparent)
                        )
                    }

                    // 2. Canvas motion artwork overlay (crossfades in when ready)
                    AnimatedVisibility(
                        visible = showAnimatedArt && isCurrentPage && !isEffectiveMvMode &&
                            (!canvasPrimaryUrl.isNullOrBlank() || !canvasFallbackUrl.isNullOrBlank()),
                        enter = fadeIn(tween(1500)),
                        exit = fadeOut(tween(800)),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CanvasArtworkPlayer(
                            primaryUrl = canvasPrimaryUrl,
                            fallbackUrl = canvasFallbackUrl,
                            isPlaying = isCurrentPage && !isEffectiveMvMode && isPlaying,
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // 3. Music Video Playback overlay
                AnimatedVisibility(
                    visible = isCurrentPage && isEffectiveMvMode,
                    enter = fadeIn(tween(500)),
                    exit = fadeOut(tween(500)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    var showOverlay by remember { androidx.compose.runtime.mutableStateOf(false) }
                    
                    androidx.compose.runtime.LaunchedEffect(showOverlay, isPlaying) {
                        if (showOverlay && isPlaying) {
                            kotlinx.coroutines.delay(3000)
                            showOverlay = false
                        }
                    }

                    val mvRadius = if (isTrueFullscreen || isMvFullscreen) 0.dp else 22.dp

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (isTrueFullscreen) Modifier.background(Color.Black) else Modifier)
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) {
                                if (isTrueFullscreen) {
                                    showOverlay = !showOverlay
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        com.codetrio.overdrive.ui.player.canvas.MusicVideoPlayer(
                            videoUrl = musicVideoUrl,
                            isPlaying = isCurrentPage && isEffectiveMvMode && isPlaying,
                            currentPositionMs = currentPosition.toLong(),
                            onPositionUpdate = { },
                            onDurationUpdate = { durMs ->
                                if (isEffectiveMvMode && durMs > 0) {
                                    viewModel.setDuration(durMs.toInt())
                                }
                            },
                            seekRequest = mvSeekRequest,
                            onSeekRequestConsumed = {
                                viewModel.clearMvSeekRequest()
                            },
                            onPlaybackCompleted = {
                                if (isEffectiveMvMode) {
                                    viewModel.playNextSong()
                                }
                            },
                            onAspectRatioUpdate = { ratio -> 
                                viewModel.setVideoAspectRatio(ratio) 
                            },
                            resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT,
                            cornerRadius = mvRadius,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        androidx.compose.animation.AnimatedVisibility(
                            visible = !isTrueFullscreen || showOverlay || !isPlaying,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier.matchParentSize()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(if (isTrueFullscreen) Color.Black.copy(alpha = 0.6f) else Color.Transparent)
                            ) {
                                if (isTrueFullscreen) {
                                    // Top bar with back button & title
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .fillMaxWidth()
                                            .statusBarsPadding()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        androidx.compose.material3.IconButton(
                                            onClick = { viewModel.setMvFullscreen(false) }
                                        ) {
                                            androidx.compose.material3.Icon(
                                                imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Default.ArrowBack,
                                                contentDescription = "Back",
                                                tint = Color.White
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            androidx.compose.material3.Text(
                                                text = song.title,
                                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                            androidx.compose.material3.Text(
                                                text = song.artist,
                                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.7f),
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    // Play/Pause button in center
                                    androidx.compose.material3.IconButton(
                                        onClick = { if (isPlaying) viewModel.pauseAudio() else viewModel.playAudio() },
                                        modifier = Modifier.align(Alignment.Center).size(80.dp)
                                    ) {
                                        androidx.compose.material3.Icon(
                                            painter = painterResource(id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                                            contentDescription = "Play/Pause",
                                            tint = Color.White,
                                            modifier = Modifier.size(64.dp)
                                        )
                                    }
                                    
                                    // Wavy Slider at bottom
                                    val duration by viewModel.duration.collectAsStateWithLifecycle()
                                    val playbackFormat by viewModel.playbackFormat.collectAsStateWithLifecycle()
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .navigationBarsPadding()
                                            .padding(bottom = 72.dp, start = 32.dp, end = 32.dp)
                                    ) {
                                        com.codetrio.overdrive.ui.player.WavySliderWithLabels(
                                            currentPositionProvider = { currentPosition },
                                            duration = duration,
                                            isPlaying = isPlaying,
                                            onSeekTo = { viewModel.seekTo(it) },
                                            dynamicAccentColor = Color.White,
                                            contentColor = Color.White,
                                            contentSecondary = Color.White.copy(alpha = 0.7f),
                                            isDark = true,
                                            playbackFormat = playbackFormat
                                        )
                                    }
                                }

                                if (!isInPipMode) {
                                    val videoAspectRatio by viewModel.videoAspectRatio.collectAsStateWithLifecycle()
                                    val buttonBoxModifier = if (isTrueFullscreen) {
                                        Modifier
                                            .align(Alignment.BottomEnd)
                                            .navigationBarsPadding()
                                            .padding(16.dp)
                                    } else {
                                        Modifier
                                            .align(Alignment.Center)
                                            .fillMaxWidth()
                                            .aspectRatio(videoAspectRatio.coerceIn(0.5f, 2.5f))
                                            .wrapContentSize(Alignment.BottomEnd)
                                            .padding(12.dp)
                                    }

                                    androidx.compose.material3.IconButton(
                                        onClick = { viewModel.setMvFullscreen(!isMvFullscreen) },
                                        modifier = buttonBoxModifier
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(Color.Black.copy(alpha = 0.6f))
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

        // ── 4. Fixed Vinyl Tonearm Overlay (Stays static across horizontal swipes) ──
        val prefs = remember { context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE) }
        var playerTheme by remember { mutableStateOf(prefs.getString("player_theme", "fluid") ?: "fluid") }
        androidx.compose.runtime.DisposableEffect(prefs) {
            val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
                if (key == "player_theme") {
                    playerTheme = sp.getString(key, "fluid") ?: "fluid"
                }
            }
            prefs.registerOnSharedPreferenceChangeListener(listener)
            onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
        }
        val isVinyl = playerTheme == "vinyl"
        val isMvMode by viewModel.isMvMode.collectAsState()
        val hasMusicVideo by viewModel.hasMusicVideo.collectAsState()
        val musicVideoUrl by viewModel.musicVideoUrl.collectAsState()
        val isEffectiveMvMode = isMvMode && hasMusicVideo && !musicVideoUrl.isNullOrBlank()
        val duration by viewModel.duration.collectAsState()
        val currentPosition by viewModel.currentPosition.collectAsState()
        val isPlaying by viewModel.isPlaying.collectAsState()

        if (isVinyl && !isEffectiveMvMode) {
            val progressFraction = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()) else 0.35f
            val isEffectivePlaying = isPlaying || currentSong.id == -9999L
            com.codetrio.overdrive.ui.player.themes.VinylTonearmOverlay(
                isPlaying = isEffectivePlaying,
                progressFraction = progressFraction,
                showTonearm = showTonearm,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

