import re

path_full = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path_full, "r") as f:
    content = f.read()

# 1. Add horizontal padding to the WavySlider Box
target_wavy_box = "Box(modifier = Modifier.width(albumArtSize)) {\n                                WavySliderWithLabels"
replacement_wavy_box = "Box(modifier = Modifier.width(albumArtSize).padding(horizontal = 32.dp)) {\n                                WavySliderWithLabels"
content = content.replace(target_wavy_box, replacement_wavy_box)

# 2. Make right pane background transparent
target_right_pane_bg = """.padding(end = dimens.screenMargin)
                                .clip(RoundedCornerShape(32.dp))
                                .background(Color.Black.copy(alpha = 0.4f))"""
replacement_right_pane_bg = """.padding(end = dimens.screenMargin)
                                .clip(RoundedCornerShape(32.dp))
                                .background(Color.Transparent)"""
content = content.replace(target_right_pane_bg, replacement_right_pane_bg)

# 3. Add space below ButtonGroup in Left Pane
# The ButtonGroup ends with `menuContent = {}\n                                            )\n                                        }\n                        }`
target_button_group_end = """                                                menuContent = {}
                                            )
                                        }
                        }

                        // Right pane: Tabs and Content"""
replacement_button_group_end = """                                                menuContent = {}
                                            )
                                        }
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // Right pane: Tabs and Content"""
content = content.replace(target_button_group_end, replacement_button_group_end)

with open(path_full, "w") as f:
    f.write(content)
print("Fixes applied successfully")

