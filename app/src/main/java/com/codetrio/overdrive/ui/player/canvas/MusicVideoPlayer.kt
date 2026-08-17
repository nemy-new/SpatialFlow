@file:OptIn(androidx.media3.common.util.UnstableApi::class)
@file:Suppress("OPT_IN_ARGUMENT_IS_NOT_MARKER")

package com.codetrio.overdrive.ui.player.canvas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.compose.ContentFrame
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import java.util.Locale

/**
 * Renders a Music Video using ExoPlayer on a TextureView surface.
 * Audio is ON, loop is OFF, and it synchronizes with the main player's position when started/stopped.
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
    onAspectRatioUpdate: (Float) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val url = videoUrl?.takeIf { it.isNotBlank() } ?: return

    var currentUrl by remember(url) { mutableStateOf(url) }
    var isVideoReady by remember(url) { mutableStateOf(false) }
    val shouldPlay by rememberUpdatedState(isPlaying)
    val onPlaybackCompletedState by rememberUpdatedState(onPlaybackCompleted)

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

    val exoPlayer = remember(url, mediaSourceFactory) {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                trackSelectionParameters = trackSelectionParameters
                    .buildUpon()
                    // DISABLE AUDIO FOR MV TO PREVENT OVERLAP
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
                    onAspectRatioUpdate(ratio)
                }
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    onPlaybackCompletedState()
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            if (exoPlayer.playbackState == Player.STATE_ENDED) exoPlayer.seekTo(0)
            if (exoPlayer.playbackState == Player.STATE_IDLE && exoPlayer.mediaItemCount > 0) exoPlayer.prepare()
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    // Sync with the master audio position
    LaunchedEffect(currentPositionMs) {
        val diff = kotlin.math.abs(exoPlayer.currentPosition - currentPositionMs)
        // Only seek if we drift by more than 1 second to avoid micro-stutters
        if (diff > 1000L) {
            exoPlayer.seekTo(currentPositionMs)
        }
    }

    LaunchedEffect(seekRequest) {
        seekRequest?.let { targetMs ->
            exoPlayer.seekTo(targetMs)
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
            // Sync position back to main player when disposed (removed to not overwrite audio service)
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
        exoPlayer.seekTo(currentPositionMs)
        exoPlayer.prepare()
        if (shouldPlay) exoPlayer.play()
    }

    Box(modifier = modifier.background(Color.Black)) {
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

private fun Int.toContentScale(): ContentScale = when (this) {
    AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> ContentScale.Crop
    AspectRatioFrameLayout.RESIZE_MODE_FILL -> ContentScale.FillBounds
    AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH -> ContentScale.FillWidth
    AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT -> ContentScale.FillHeight
    else -> ContentScale.Fit
}
