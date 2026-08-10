import re

# Fix FullPlayer.kt
path_full = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path_full, "r") as f:
    content = f.read()

target_top_offset = """        val minTopOffset = statusBarTopDp + 68.dp // Removed 16.dp extra gap

        // Calculate top offset to perfectly match yEndPx in PlayerBottomSheetCompose
        val topOffset = ((screenHeight - albumArtSize) / 2f - 220.dp).coerceAtLeast(minTopOffset)"""

replacement_top_offset = """        val controlsHeightDp = 300.dp
        val totalGroupHeightDp = albumArtSize + controlsHeightDp
        val availableHeightDp = screenHeight - statusBarTopDp
        val topOffset = statusBarTopDp + ((availableHeightDp - totalGroupHeightDp) / 2f).coerceAtLeast(16.dp)"""

content = content.replace(target_top_offset, replacement_top_offset)

# In FullPlayerPhoneLayout, use topSpacerHeight
target_phone_spacer = """        @Composable
        fun FullPlayerPhoneLayout() {
            Spacer(modifier = Modifier.height(topOffset - (statusBarTopDp + 68.dp)))"""

replacement_phone_spacer = """        @Composable
        fun FullPlayerPhoneLayout() {
            val rowTopAbsolute = statusBarTopDp + dimens.smallPadding + 56.dp
            val phoneTopSpacerHeight = (topOffset - rowTopAbsolute).coerceAtLeast(0.dp)
            Spacer(modifier = Modifier.height(phoneTopSpacerHeight))"""

content = content.replace(target_phone_spacer, replacement_phone_spacer)

# Also fix LyricsBottomSheet to have a weight
target_lyrics = """            LyricsBottomSheet(
                visible = isLyricsModeEnabled,"""

replacement_lyrics = """            Spacer(modifier = Modifier.weight(1f, fill = false))
            LyricsBottomSheet(
                visible = isLyricsModeEnabled,"""

content = content.replace(target_lyrics, replacement_lyrics)

with open(path_full, "w") as f:
    f.write(content)


# Fix PlayerBottomSheetCompose.kt
path_compose = "app/src/main/java/com/codetrio/overdrive/ui/PlayerBottomSheetCompose.kt"
with open(path_compose, "r") as f:
    content = f.read()

target_y_end = """                        } else {
                            val minTopOffsetDp = with(density) { statusBarTopPx.toDp() } + 68.dp
                            val topOffsetDp = ((containerHeight - albumArtHeightDp) / 2f - 220.dp).coerceAtLeast(minTopOffsetDp)
                            with(density) { topOffsetDp.toPx() }
                        }"""

replacement_y_end = """                        } else {
                            val controlsHeightDp = 300.dp
                            val totalGroupHeightDp = albumArtHeightDp + controlsHeightDp
                            val availableHeightDp = screenHeight.dp - with(density) { statusBarTopPx.toDp() }
                            val phoneTopOffsetDp = with(density) { statusBarTopPx.toDp() } + ((availableHeightDp - totalGroupHeightDp) / 2f).coerceAtLeast(16.dp)
                            with(density) { phoneTopOffsetDp.toPx() }
                        }"""

content = content.replace(target_y_end, replacement_y_end)

with open(path_compose, "w") as f:
    f.write(content)

print("Layout fixed")
