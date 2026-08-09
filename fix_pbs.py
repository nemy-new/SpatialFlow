import re

path = "./app/src/main/java/com/codetrio/overdrive/ui/PlayerBottomSheetCompose.kt"
with open(path, "r") as f:
    content = f.read()

target = """                    Box(
                        modifier = Modifier
                            .graphicsLayer {"""

replacement = """                    androidx.compose.animation.AnimatedVisibility(
                        visible = !(isTablet && isLyricsModeEnabled),
                        enter = androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.fadeOut(),
                        modifier = Modifier.graphicsLayer {
                            // Apply the same Z-index logic as the Box below to the AnimatedVisibility container
                            val isQueueExpanded = uiState.isQueueExpanded
                            this.alpha = if (isQueueExpanded) 0f else 1f
                            this.translationY = if (isQueueExpanded) 100f else 0f
                        }.zIndex(if (uiState.isQueueExpanded) 1f else if (isLyricsModeEnabled) 6f else 3f)
                    ) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {"""

content = content.replace(target, replacement)

# We need to close the AnimatedVisibility bracket
target_close = """                        ArtworkPager(
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

replacement_close = """                        ArtworkPager(
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

content = content.replace(target_close, replacement_close)

with open(path, "w") as f:
    f.write(content)

print("PlayerBottomSheetCompose fixed")
