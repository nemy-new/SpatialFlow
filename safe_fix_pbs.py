import re

path = "./app/src/main/java/com/codetrio/overdrive/ui/PlayerBottomSheetCompose.kt"
with open(path, "r") as f:
    content = f.read()

target = """                    ) {
                        ArtworkPager(
                            viewModel = viewModel,
                            currentSong = currentSong!!,
                            songList = songList,
                            currentSongIndex = uiState.currentSongIndex,
                            context = context,
                            userScrollEnabled = playerContentExpansionFraction.value > 0.95f && !isLyricsModeEnabled && !isQueueExpanded,
                            allowCanvas = playerContentExpansionFraction.value > 0.95f && !isLyricsModeEnabled && lyricsArtworkProgress == 0f,
                            modifier = Modifier.fillMaxSize()
                        )
                    }"""

replacement = """                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = !(isTablet && isLyricsModeEnabled),
                            enter = androidx.compose.animation.fadeIn(),
                            exit = androidx.compose.animation.fadeOut(),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            ArtworkPager(
                                viewModel = viewModel,
                                currentSong = currentSong!!,
                                songList = songList,
                                currentSongIndex = uiState.currentSongIndex,
                                context = context,
                                userScrollEnabled = playerContentExpansionFraction.value > 0.95f && !isLyricsModeEnabled && !isQueueExpanded,
                                allowCanvas = playerContentExpansionFraction.value > 0.95f && !isLyricsModeEnabled && lyricsArtworkProgress == 0f,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }"""

content = content.replace(target, replacement)

with open(path, "w") as f:
    f.write(content)

print("Safely fixed PlayerBottomSheetCompose")
