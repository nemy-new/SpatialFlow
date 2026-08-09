import re

path = "./app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

# Replace the isTablet block in FullPlayer.kt
# We want to conditionally render the lyrics OR the album art inside the left Box.
target_tablet = """                if (isTablet) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(modifier = Modifier.size(albumArtSize))
                        }
                        Column(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            rightPaneContent()
                        }
                    }
                }"""

replacement_tablet = """                if (isTablet) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(top = 32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = !isLyricsModeEnabled,
                                enter = androidx.compose.animation.fadeIn(),
                                exit = androidx.compose.animation.fadeOut()
                            ) {
                                Box(modifier = Modifier.size(albumArtSize))
                            }
                            
                            androidx.compose.animation.AnimatedVisibility(
                                visible = isLyricsModeEnabled,
                                enter = androidx.compose.animation.fadeIn(),
                                exit = androidx.compose.animation.fadeOut()
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
                                    syncOffsetMs = uiState.syncOffsetMs,
                                    onSyncOffsetChange = { viewModel.updateSyncOffset(it) },
                                    isPlaying = uiState.isPlaying,
                                    playbackSpeed = uiState.playbackSpeed,
                                    onPlayPauseClick = onPlayPauseClick,
                                    duration = uiState.duration.toLong(),
                                    onCollapse = { viewModel.setLyricsModeEnabled(false) },
                                    isEmbedded = true,
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
                                )
                            }
                        }
                        Column(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            rightPaneContent()
                        }
                    }
                }"""

content = content.replace(target_tablet, replacement_tablet)

# Conditionally render LyricsBottomSheet only on phones
target_lyrics = """        LyricsBottomSheet(
            visible = isLyricsModeEnabled,"""

replacement_lyrics = """        if (!isTablet) {
            LyricsBottomSheet(
                visible = isLyricsModeEnabled,"""

content = content.replace(target_lyrics, replacement_lyrics)

# Add closing brace for the if statement at the end of LyricsBottomSheet
# Since LyricsBottomSheet call ends right before sliding queue drawer
target_queue = """        // --- CUSTOM EMBEDDED SLIDING PLAY QUEUE ---"""
replacement_queue = """        }

        // --- CUSTOM EMBEDDED SLIDING PLAY QUEUE ---"""

content = content.replace(target_queue, replacement_queue)

# Also need to make sure AnimatedVisibility doesn't fade out the whole screen when lyrics mode is enabled on tablet
# The main column is wrapped in AnimatedVisibility(visible = !isLyricsModeEnabled)
# So on tablets, it would fade out!
target_anim_main = """        AnimatedVisibility(
            visible = !isLyricsModeEnabled,"""

replacement_anim_main = """        AnimatedVisibility(
            visible = isTablet || !isLyricsModeEnabled,"""
            
content = content.replace(target_anim_main, replacement_anim_main)

with open(path, "w") as f:
    f.write(content)

print("FullPlayer fixed")
