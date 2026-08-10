import re

path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

# 1. Phone controls too low: change controlsHeightDp to 420.dp
content = content.replace("val controlsHeightDp = 300.dp", "val controlsHeightDp = 420.dp")

# 2. Auto lyrics display on phone -> turn off. 
# Look for LaunchedEffect(currentSongId, hasLyrics)
target_effect = r"""    androidx\.compose\.runtime\.LaunchedEffect\(currentSongId, hasLyrics\) \{
        onLyricsModeChanged\(hasLyrics\)
    \}"""

replacement_effect = """    androidx.compose.runtime.LaunchedEffect(currentSongId, hasLyrics) {
        val config = context.resources.configuration
        if (config.screenWidthDp >= 600) {
            onLyricsModeChanged(hasLyrics)
        }
    }"""
content = re.sub(target_effect, replacement_effect, content)

# 3. Title font for smartphone layout
target_title = r"""                    Text\(
                        text = uiState\.currentSong\?\.title \?\: "Unknown Title",
                        style = MaterialTheme\.typography\.headlineMediumEmphasized,
                        fontWeight = FontWeight\.Bold,"""

replacement_title = """                    Text(
                        text = uiState.currentSong?.title ?: "Unknown Title",
                        style = MaterialTheme.typography.headlineMediumEmphasized.copy(
                            fontFamily = if (playerTheme == "immersion") com.codetrio.overdrive.ui.theme.GoogleSansFlexImmersion else MaterialTheme.typography.headlineMediumEmphasized.fontFamily
                        ),
                        fontWeight = FontWeight.Bold,"""
content = re.sub(target_title, replacement_title, content)

# 3b. Artist font for smartphone layout
target_artist = r"""                    Text\(
                        text = uiState\.currentSong\?\.artist \?\: "Unknown Artist",
                        style = MaterialTheme\.typography\.bodyMedium,
                        color = contentSecondary,"""

replacement_artist = """                    Text(
                        text = uiState.currentSong?.artist ?: "Unknown Artist",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = if (playerTheme == "immersion") com.codetrio.overdrive.ui.theme.GoogleSansFlexImmersion else MaterialTheme.typography.bodyMedium.fontFamily
                        ),
                        color = contentSecondary,"""
content = re.sub(target_artist, replacement_artist, content)

# 4. Volume slider for phone layout
# We will insert it just before the "Swipe Up / Click Chevron Up Indicator to expand Queue"
target_queue = r"""            // Swipe Up / Click Chevron Up Indicator to expand Queue"""
replacement_queue = """            val showVolumeSlider by viewModel.showVolumeSlider.collectAsStateWithLifecycle(initialValue = true)
            if (showVolumeSlider) {
                com.codetrio.overdrive.ui.player.VolumeSlider(
                    modifier = Modifier.width(albumArtSize).padding(top = 16.dp),
                    contentColor = contentColor,
                    accentColor = dynamicAccentColor,
                    isDark = isDark
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Swipe Up / Click Chevron Up Indicator to expand Queue"""
content = re.sub(target_queue, replacement_queue, content, count=1) # only in phone layout

with open(path, "w") as f:
    f.write(content)
print("Changes applied")
