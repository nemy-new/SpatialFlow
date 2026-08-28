@file:OptIn(androidx.media3.common.util.UnstableApi::class)
@file:Suppress("OPT_IN_ARGUMENT_IS_NOT_MARKER")

package com.codetrio.overdrive.ui.player.canvas

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.compose.ContentFrame
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private const val TAG = "MusicVideoPlayer"

/**
 * High-performance, stutter-free Music Video player with Continuous Micro-Adjustment AV Sync.
 * Audio is decoded and rendered exclusively by the master AudioPlaybackService player,
 * while this player renders hardware-accelerated video frames synchronized seamlessly to within 25ms.
 */
@Composable
fun MusicVideoPlayer(
    videoUrl: String?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    onPositionUpdate: (Long) -> Unit,
    modifier: Modifier = Modifier,
    resizeMode: Int = AspectRatioFrameLayout.RESIZE_MODE_FIT,
    onDurationUpdate: (Long) -> Unit = {},
    seekRequest: Long? = null,
    onSeekRequestConsumed: () -> Unit = {},
    onPlaybackCompleted: () -> Unit = {},
    onAspectRatioUpdate: (Float) -> Unit = {},
    cornerRadius: Dp = 22.dp
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val url = videoUrl?.takeIf { it.isNotBlank() } ?: return

    var currentUrl by remember(url) { mutableStateOf(url) }
    var isVideoReady by remember(url) { mutableStateOf(false) }
    val shouldPlay by rememberUpdatedState(isPlaying)
    val currentPositionMsState by rememberUpdatedState(currentPositionMs)
    val onPlaybackCompletedState by rememberUpdatedState(onPlaybackCompleted)
    val onAspectRatioUpdateState by rememberUpdatedState(onAspectRatioUpdate)
    val onDurationUpdateState by rememberUpdatedState(onDurationUpdate)
    var internalAspectRatio by remember { mutableStateOf(16f / 9f) }

    val cacheDataSourceFactory = remember(context) {
        val entryPoint = dagger.hilt.android.EntryPointAccessors.fromApplication(
            context,
            com.codetrio.overdrive.di.MediaEntryPoint::class.java
        )
        entryPoint.cacheDataSourceFactory()
    }

    val mediaSourceFactory = remember(cacheDataSourceFactory) {
        DefaultMediaSourceFactory(context).setDataSourceFactory(cacheDataSourceFactory)
    }

    // High-performance ExoPlayer with optimized buffer and hardware decoder settings
    val exoPlayer = remember(url, mediaSourceFactory) {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 15_000,
                /* maxBufferMs = */ 60_000,
                /* bufferForPlaybackMs = */ 500,
                /* bufferForPlaybackAfterRebufferMs = */ 1_500
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .setBackBuffer(10_000, true)
            .build()

        val renderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
            .setEnableDecoderFallback(true)
            .setAllowedVideoJoiningTimeMs(5000)

        ExoPlayer.Builder(context, renderersFactory)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .setSeekParameters(SeekParameters.CLOSEST_SYNC)
            .build()
            .apply {
                trackSelectionParameters = trackSelectionParameters
                    .buildUpon()
                    // DISABLE AUDIO ON MV PLAYER: Master audio plays from AudioPlaybackService
                    .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true)
                    .setForceHighestSupportedBitrate(true)
                    .build()
                volume = 0f
                repeatMode = Player.REPEAT_MODE_OFF
                playWhenReady = isPlaying
            }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                if (videoSize.width > 0 && videoSize.height > 0) {
                    val ratio = videoSize.width.toFloat() / videoSize.height.toFloat()
                    internalAspectRatio = ratio
                    onAspectRatioUpdateState(ratio)
                }
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    isVideoReady = true
                    if (exoPlayer.duration > 0) {
                        onDurationUpdateState(exoPlayer.duration)
                    }
                } else if (playbackState == Player.STATE_ENDED) {
                    onPlaybackCompletedState()
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    // Play/Pause synchronization
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            if (exoPlayer.playbackState == Player.STATE_ENDED) {
                exoPlayer.seekTo(0)
            }
            if (exoPlayer.playbackState == Player.STATE_IDLE && exoPlayer.mediaItemCount > 0) {
                exoPlayer.prepare()
            }
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    // ── Continuous Micro-Adjustment AV Sync Loop ──
    // Seamlessly synchronizes video clock to master audio clock within ±25ms without causing stutter
    LaunchedEffect(exoPlayer) {
        var lastTargetSpeed = 1.0f
        while (isActive) {
            delay(100L)

            if (!shouldPlay || exoPlayer.playbackState != Player.STATE_READY) {
                if (lastTargetSpeed != 1.0f) {
                    exoPlayer.playbackParameters = PlaybackParameters(1.0f)
                    lastTargetSpeed = 1.0f
                }
                continue
            }

            val masterPos = currentPositionMsState
            val videoPos = exoPlayer.currentPosition
            val driftMs = masterPos - videoPos

            // If major desync (> 350ms), perform an instantaneous sync seek
            if (kotlin.math.abs(driftMs) > 350L) {
                exoPlayer.seekTo(masterPos)
                if (lastTargetSpeed != 1.0f) {
                    exoPlayer.playbackParameters = PlaybackParameters(1.0f)
                    lastTargetSpeed = 1.0f
                }
            } else {
                // Micro speed adjustments (±2% to ±6%) to smoothly eliminate drift imperceptibly
                val newSpeed = when {
                    driftMs > 120L -> 1.06f  // Video is lagging slightly -> speed up +6%
                    driftMs > 25L -> 1.02f   // Video is lagging imperceptibly -> speed up +2%
                    driftMs < -120L -> 0.94f // Video is leading slightly -> slow down -6%
                    driftMs < -25L -> 0.98f  // Video is leading imperceptibly -> slow down -2%
                    else -> 1.0f            // Within 25ms: Perfect Lip-Sync
                }

                if (newSpeed != lastTargetSpeed) {
                    exoPlayer.playbackParameters = PlaybackParameters(newSpeed)
                    lastTargetSpeed = newSpeed
                }
            }
        }
    }

    // Direct Seek Bar user request
    LaunchedEffect(seekRequest) {
        seekRequest?.let { targetMs ->
            exoPlayer.seekTo(targetMs)
            exoPlayer.playbackParameters = PlaybackParameters(1.0f)
            onPositionUpdate(targetMs)
            onSeekRequestConsumed()
        }
    }

    // ── Lifecycle: pause/resume with app lifecycle ──
    DisposableEffect(exoPlayer, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            val appPrefs = context.getSharedPreferences("AppSettings", android.content.Context.MODE_PRIVATE)
            val mvBehavior = appPrefs.getString("mv_background_behavior", "pip") ?: "pip"
            
            if (event == Lifecycle.Event.ON_START || event == Lifecycle.Event.ON_RESUME) {
                if (shouldPlay) exoPlayer.play()
            } else if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                if (mvBehavior != "audio") {
                    exoPlayer.pause()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose { 
            lifecycleOwner.lifecycle.removeObserver(observer) 
            exoPlayer.release() 
        }
    }

    // ── Load new URL into player ──
    LaunchedEffect(currentUrl, exoPlayer) {
        val normalized = currentUrl.trim()
        isVideoReady = false
        
        val streamUrlToPlay = if (normalized.startsWith("innertube://")) {
            val videoId = normalized.removePrefix("innertube://")
            com.codetrio.overdrive.data.innertube.NewPipeStreamExtractor.getVideoStreamUrl(videoId) ?: return@LaunchedEffect
        } else {
            normalized
        }

        val mediaItem = MediaItem.Builder()
            .setUri(streamUrlToPlay)
            .build()

        exoPlayer.stop()
        exoPlayer.setMediaItem(mediaItem)
        if (currentPositionMsState > 0) {
            exoPlayer.seekTo(currentPositionMsState)
        }
        exoPlayer.prepare()
        if (shouldPlay) {
            exoPlayer.play()
        }
    }

    val isFit = resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT
    val shape = if (cornerRadius > 0.dp) RoundedCornerShape(cornerRadius) else null

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val videoBoxModifier = if (isFit) {
            Modifier
                .fillMaxWidth()
                .aspectRatio(internalAspectRatio)
                .then(
                    if (shape != null) Modifier.clip(shape) else Modifier
                )
        } else {
            Modifier
                .fillMaxSize()
                .then(
                    if (shape != null) Modifier.clip(shape) else Modifier
                )
        }

        Box(
            modifier = videoBoxModifier
                .background(Color.Black)
        ) {
            ContentFrame(
                player = exoPlayer,
                surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
                contentScale = resizeMode.toContentScale(),
                keepContentOnReset = false,
                shutter = {},
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

private fun Int.toContentScale(): ContentScale = when (this) {
    AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> ContentScale.Crop
    AspectRatioFrameLayout.RESIZE_MODE_FILL -> ContentScale.FillBounds
    AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH -> ContentScale.FillWidth
    AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT -> ContentScale.FillHeight
    else -> ContentScale.Fit
}
