import re

path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

# 1. Metadata Row
target_metadata = """        val rightPaneContent: @Composable () -> Unit = {
            // Metadata row: title/artist
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {"""

replacement_metadata = """        val rightPaneContent: @Composable () -> Unit = {
            // Metadata row: title/artist
            Row(
                modifier = Modifier.width(albumArtSize),
                verticalAlignment = Alignment.CenterVertically
            ) {"""

content = content.replace(target_metadata, replacement_metadata)

# 2. Chips Row
target_chips = """            // Premium YT Music style horizontal control chips row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .layout { measurable, constraints ->
                        val pad = 20.dp.roundToPx()
                        val placeable = measurable.measure(
                            constraints.copy(
                                maxWidth = constraints.maxWidth + 2 * pad
                            )
                        )
                        layout(placeable.width - 2 * pad, placeable.height) {
                            placeable.place(-pad, 0)
                        }
                    }
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(12.dp))"""

replacement_chips = """            // Premium YT Music style horizontal control chips row
            Row(
                modifier = Modifier
                    .width(albumArtSize)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {"""

content = content.replace(target_chips, replacement_chips)

# 3. Wavy Slider
target_slider = """            // Premium Wavy Seek Bar (Isolated)
            WavySliderWithLabels(
                currentPositionProvider = currentPositionProvider,"""

replacement_slider = """            // Premium Wavy Seek Bar (Isolated)
            Box(modifier = Modifier.width(albumArtSize)) {
                WavySliderWithLabels(
                    currentPositionProvider = currentPositionProvider,"""

content = content.replace(target_slider, replacement_slider)

target_slider_end = """                playbackFormat = uiState.playbackFormat
            )

            Spacer(modifier = Modifier.height(16.dp))

            androidx.compose.material3.ButtonGroup(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),"""

replacement_slider_end = """                playbackFormat = uiState.playbackFormat
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            androidx.compose.material3.ButtonGroup(
                modifier = Modifier.width(albumArtSize),"""

content = content.replace(target_slider_end, replacement_slider_end)

with open(path, "w") as f:
    f.write(content)

print("Alignment fixed")
