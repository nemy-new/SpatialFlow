path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

target = """                        // Right pane: Dedicated to Lyrics
                        if (isLyricsModeEnabled || leftPaneFraction < 0.99f) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth() // Fills the remaining width of the Row
                                    .fillMaxHeight()"""

replacement = """                        // Right pane: Dedicated to Lyrics
                        if (isLyricsModeEnabled || leftPaneFraction < 0.99f) {
                            Column(
                                modifier = Modifier
                                    .weight(1f) // Fills the remaining width of the Row safely
                                    .fillMaxHeight()"""

content = content.replace(target, replacement)

target_launched_effect = """    val hasLyrics = !syncedLyrics.isNullOrEmpty() || !plainLyrics.isNullOrBlank()
    val currentSongId = uiState.currentSong?.id"""

replacement_launched_effect = """    val hasLyrics = !syncedLyrics.isNullOrEmpty() || !plainLyrics.isNullOrBlank()
    
    androidx.compose.runtime.LaunchedEffect(hasLyrics) {
        onLyricsModeChanged(hasLyrics)
    }
    
    val currentSongId = uiState.currentSong?.id"""

content = content.replace(target_launched_effect, replacement_launched_effect)

with open(path, "w") as f:
    f.write(content)
print("Right pane fixed")
