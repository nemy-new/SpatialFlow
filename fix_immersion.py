import re

path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

# 1. Box size for immersion mode
target_box = r"""            // Album Art Container Placeholder \(ArtworkPager is rendered at this absolute position\)
            Box\(
                modifier = Modifier\.size\(albumArtSize\)
            \)"""

replacement_box = """            // Album Art Container Placeholder (ArtworkPager is rendered at this absolute position)
            Box(
                modifier = Modifier.size(
                    width = albumArtSize,
                    height = if (playerTheme == "immersion") albumArtSize * 0.6f else albumArtSize
                )
            )"""
content = re.sub(target_box, replacement_box, content)

# 2. Title font
target_title = r"""                    Text\(
                        text = uiState\.currentSong\?\.title \?\: "Unknown Title",
                        style = MaterialTheme\.typography\.headlineMediumEmphasized\.copy\(
                            fontFamily = if \(playerTheme == "immersion"\) com\.codetrio\.overdrive\.ui\.theme\.GoogleSansFlexImmersion else MaterialTheme\.typography\.headlineMediumEmphasized\.fontFamily
                        \),
                        fontWeight = FontWeight\.Bold,
                        color = contentColor,"""

replacement_title = """                    Text(
                        text = uiState.currentSong?.title ?: "Unknown Title",
                        style = MaterialTheme.typography.headlineMediumEmphasized.copy(
                            fontFamily = if (playerTheme == "immersion") com.codetrio.overdrive.ui.theme.GoogleSansFlexImmersion else MaterialTheme.typography.headlineMediumEmphasized.fontFamily,
                            fontSize = if (playerTheme == "immersion") 32.sp else MaterialTheme.typography.headlineMediumEmphasized.fontSize,
                            lineHeight = if (playerTheme == "immersion") 40.sp else MaterialTheme.typography.headlineMediumEmphasized.lineHeight
                        ),
                        fontWeight = FontWeight.Bold,
                        color = contentColor,"""
content = re.sub(target_title, replacement_title, content)

# 3. Artist font
target_artist = r"""                    Text\(
                        text = uiState\.currentSong\?\.artist \?\: "Unknown Artist",
                        style = MaterialTheme\.typography\.bodyMedium\.copy\(
                            fontFamily = if \(playerTheme == "immersion"\) com\.codetrio\.overdrive\.ui\.theme\.GoogleSansFlexImmersion else MaterialTheme\.typography\.bodyMedium\.fontFamily
                        \),
                        color = contentSecondary,"""

replacement_artist = """                    Text(
                        text = uiState.currentSong?.artist ?: "Unknown Artist",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = if (playerTheme == "immersion") com.codetrio.overdrive.ui.theme.GoogleSansFlexImmersion else MaterialTheme.typography.bodyMedium.fontFamily,
                            fontSize = if (playerTheme == "immersion") 20.sp else MaterialTheme.typography.bodyMedium.fontSize,
                            fontWeight = if (playerTheme == "immersion") androidx.compose.ui.text.font.FontWeight.Medium else MaterialTheme.typography.bodyMedium.fontWeight
                        ),
                        color = contentSecondary,"""
content = re.sub(target_artist, replacement_artist, content)


with open(path, "w") as f:
    f.write(content)
print("Changes applied")
