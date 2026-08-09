import re

path_full = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path_full, "r") as f:
    content_full = f.read()

# 1. Update controlsHeightDp to 300.dp
content_full = content_full.replace("val controlsHeightDp = 268.dp", "val controlsHeightDp = 300.dp")

# 2. Add height(totalGroupHeightDp) to Left Pane to force exact match
target_left_pane = """                        // Left pane: Artwork and Controls
                        Column(
                            modifier = Modifier
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {"""
replacement_left_pane = """                        // Left pane: Artwork and Controls
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(totalGroupHeightDp), // Match exact height for bottom alignment
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {"""
content_full = content_full.replace(target_left_pane, replacement_left_pane)

# 3. Decrease Title Font Size (32.sp -> 28.sp)
target_title = """                                    fontSize = 32.sp
                                ),
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,"""
replacement_title = """                                    fontSize = 28.sp
                                ),
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,"""
content_full = content_full.replace(target_title, replacement_title)

# 4. Wrap Wavy Slider in Box(width = albumArtSize)
# The wavy slider starts with WavySliderWithLabels( and ends with playbackFormat = uiState.playbackFormat \n )
wavy_start = content_full.find("                            WavySliderWithLabels(")
wavy_end_str = "playbackFormat = uiState.playbackFormat\n            )"
wavy_end = content_full.find(wavy_end_str) + len(wavy_end_str)

if wavy_start != -1 and wavy_end != -1:
    original_wavy = content_full[wavy_start:wavy_end]
    wrapped_wavy = f"                            Box(modifier = Modifier.width(albumArtSize)) {{\n    {original_wavy}\n                            }}"
    content_full = content_full[:wavy_start] + wrapped_wavy + content_full[wavy_end:]
    print("Wrapped wavy slider")

# 5. Change the spacer before ButtonGroup to weight(1f) so ButtonGroup sits at the absolute bottom
target_spacer_before_buttons = """                            Spacer(modifier = Modifier.height(16.dp))

                            androidx.compose.material3.ButtonGroup("""
replacement_spacer_before_buttons = """                            Spacer(modifier = Modifier.weight(1f)) // Push buttons to absolute bottom

                            androidx.compose.material3.ButtonGroup("""
content_full = content_full.replace(target_spacer_before_buttons, replacement_spacer_before_buttons)

with open(path_full, "w") as f:
    f.write(content_full)
print("FullPlayer.kt updated")

# Now update PlayerBottomSheetCompose.kt
path_sheet = "app/src/main/java/com/codetrio/overdrive/ui/PlayerBottomSheetCompose.kt"
with open(path_sheet, "r") as f:
    content_sheet = f.read()

content_sheet = content_sheet.replace("val controlsHeightDp = 268.dp", "val controlsHeightDp = 300.dp")

with open(path_sheet, "w") as f:
    f.write(content_sheet)
print("PlayerBottomSheetCompose.kt updated")

