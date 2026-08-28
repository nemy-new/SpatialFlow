package com.codetrio.overdrive.ui.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import com.codetrio.overdrive.data.lyrics.LyricLine
import com.codetrio.overdrive.data.lyrics.LyricsResult
import com.codetrio.overdrive.model.SongItem
import com.codetrio.overdrive.ui.player.canvas.CanvasArtwork

/**
 * Button-controlled, spring-animated Lyrics Sheet container.
 * Sits transparently on top of the shared AppleMusicBackground canvas for 100% visual parity.
 */
@Composable
fun LyricsBottomSheet(
    visible: Boolean,
    currentSong: SongItem?,
    syncedLyrics: List<LyricLine>?,
    plainLyrics: String?,
    translatedPlainLyrics: String? = null,
    isLoading: Boolean,
    lyricsError: Throwable?,
    currentPositionProvider: () -> Int,
    contentReady: Boolean = true,
    playerBackgroundColor: Color,
    canvasArtwork: CanvasArtwork? = null,
    contentColor: Color,
    contentSecondary: Color,
    dynamicAccentColor: Color,
    onRetryLyrics: () -> Unit,
    onFetchLyrics: () -> Unit,
    onSeekTo: (Int) -> Unit,
    providerResults: Map<String, LyricsResult>,
    selectedProvider: String?,
    onProviderSelected: (String) -> Unit,
    syncOffsetMs: Long,
    onSyncOffsetChange: (Long) -> Unit,
    isPlaying: Boolean = false,
    playbackSpeed: Float = 1f,
    onPlayPauseClick: () -> Unit = {},
    duration: Long,
    onCollapse: () -> Unit,
    onToggleTranslation: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Intercept system back button while Lyrics Sheet is visible
    BackHandler(enabled = visible) {
        onCollapse()
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 380f)
        ) + fadeIn(
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 380f)
        ),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 380f)
        ) + fadeOut(
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 380f)
        ),
        modifier = modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            FullScreenLyricsOverlay(
                currentSong = currentSong,
                syncedLyrics = syncedLyrics,
                plainLyrics = plainLyrics,
                translatedPlainLyrics = translatedPlainLyrics,
                isLoading = isLoading,
                lyricsError = lyricsError,
                currentPositionProvider = currentPositionProvider,
                contentReady = contentReady,
                playerBackgroundColor = playerBackgroundColor,
                canvasArtwork = canvasArtwork,
                contentColor = contentColor,
                contentSecondary = contentSecondary,
                dynamicAccentColor = dynamicAccentColor,
                onRetryLyrics = onRetryLyrics,
                onFetchLyrics = onFetchLyrics,
                onSeekTo = onSeekTo,
                providerResults = providerResults,
                selectedProvider = selectedProvider,
                onProviderSelected = onProviderSelected,
                syncOffsetMs = syncOffsetMs,
                onSyncOffsetChange = onSyncOffsetChange,
                isPlaying = isPlaying,
                playbackSpeed = playbackSpeed,
                onPlayPauseClick = onPlayPauseClick,
                duration = duration,
                onCollapse = onCollapse,
                onToggleTranslation = onToggleTranslation,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
