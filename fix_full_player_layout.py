import re

path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

# The target starts at `if (isTablet) {` around line 842
# and ends at the closing brace of the `else` block around line 915.

target_pattern = r"if\s*\(isTablet\)\s*\{\s*Row\(\s*modifier = Modifier\.fillMaxSize\(\),\s*verticalAlignment = Alignment\.CenterVertically\s*\)\s*\{[\s\S]*?\} else \{\s*Spacer\(modifier = Modifier\.height\(topOffset - \(statusBarTopDp \+ 68\.dp\)\)\)\s*// Album Art Container Placeholder[\s\S]*?Box\(\s*modifier = Modifier\.size\(albumArtSize\)\s*\)\s*Spacer\(modifier = Modifier\.height\(28\.dp\)\)\s*rightPaneContent\(\)\s*\}"

replacement = """
                // Unified layout: Controls always at the bottom
                Spacer(modifier = Modifier.height(topOffset - (statusBarTopDp + 68.dp)))

                // Album Art Container Placeholder (ArtworkPager is rendered at this absolute position)
                Box(
                    modifier = Modifier.size(albumArtSize)
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isTablet && isLyricsModeEnabled,
                        enter = androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.fadeOut(),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        FullScreenLyricsOverlay(
                            currentSong = uiState.currentSong,
                            syncedLyrics = syncedLyrics,
                            plainLyrics = plainLyrics,
                            isLoading = isLyricsLoading,
                            lyricsError = lyricsError,
                            currentPositionProvider = currentPositionProvider,
                            contentReady = true,
                            playerBackgroundColor = androidx.compose.ui.graphics.Color.Transparent,
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
                            isPlaying = uiState.isPlaying,
                            playbackSpeed = 1f,
                            onPlayPauseClick = onPlayPauseClick,
                            duration = uiState.duration.toLong(),
                            onCollapse = { viewModel.setLyricsModeEnabled(false) },
                            isEmbedded = true,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                rightPaneContent()
"""

new_content = re.sub(target_pattern, replacement, content)

if new_content == content:
    print("Failed to replace layout!")
else:
    with open(path, "w") as f:
        f.write(new_content)
    print("FullPlayer layout fixed!")
