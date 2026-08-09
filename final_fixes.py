import re

# Fix FullPlayer.kt
path_full = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path_full, "r") as f:
    content = f.read()

target_wavy = """                            // Wavy Slider
                            WavySliderWithLabels(
                currentPositionProvider = currentPositionProvider,"""

replacement_wavy = """                            // Wavy Slider
                            Box(modifier = Modifier.width(albumArtSize).padding(horizontal = 32.dp)) {
                                WavySliderWithLabels(
                currentPositionProvider = currentPositionProvider,"""

content = content.replace(target_wavy, replacement_wavy)

# Close the Box after WavySliderWithLabels
target_wavy_end = """                playbackFormat = uiState.playbackFormat
            )

                            Spacer(modifier = Modifier.height(16.dp))"""

replacement_wavy_end = """                playbackFormat = uiState.playbackFormat
            )
                            }

                            Spacer(modifier = Modifier.height(16.dp))"""

content = content.replace(target_wavy_end, replacement_wavy_end)

with open(path_full, "w") as f:
    f.write(content)
print("FullPlayer fixed")

# Fix MainActivity.kt
path_main = "app/src/main/java/com/codetrio/overdrive/MainActivity.kt"
with open(path_main, "r") as f:
    content = f.read()

target_nav_rail = """                        if (showNavRail) {
                            androidx.compose.material3.NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ) {"""

replacement_nav_rail = """                        val playerExpansionFraction by playerViewModel.playerExpansionFraction.collectAsStateWithLifecycle(initialValue = 0f)
                        if (showNavRail) {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .width(80.dp * (1f - playerExpansionFraction))
                                    .alpha(1f - playerExpansionFraction)
                            ) {
                            androidx.compose.material3.NavigationRail(
                            modifier = Modifier.requiredWidth(80.dp).offset(x = -80.dp * playerExpansionFraction),
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ) {"""

content = content.replace(target_nav_rail, replacement_nav_rail)

target_nav_rail_end = """                                )
                            }
                        }
                        }

                        LaunchedEffect(Unit) {"""

replacement_nav_rail_end = """                                )
                            }
                        }
                            }
                        }

                        LaunchedEffect(Unit) {"""

content = content.replace(target_nav_rail_end, replacement_nav_rail_end)

with open(path_main, "w") as f:
    f.write(content)
print("MainActivity fixed")

