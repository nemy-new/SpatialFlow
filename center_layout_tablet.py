import re

def update_player_bottom_sheet():
    file_path = "app/src/main/java/com/codetrio/overdrive/ui/PlayerBottomSheetCompose.kt"
    with open(file_path, "r") as f:
        content = f.read()

    # 1. Update albumArtSizeDp
    old_size = "val albumArtSizeDp = androidx.compose.ui.unit.min((screenWidth * 0.9f).dp, (screenHeight * 0.45f).dp)"
    new_size = """val isTablet = screenWidth >= 600
                    val isLandscape = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
                    val albumArtSizeDp = remember(isTablet, screenWidth, screenHeight, isLandscape) {
                        if (isTablet) {
                            val maxArtWidth = (screenWidth / 2f) - 48f
                            val maxArtHeight = screenHeight * 0.45f
                            androidx.compose.ui.unit.min(maxArtWidth.dp, maxArtHeight.dp)
                        } else {
                            androidx.compose.ui.unit.min((screenWidth * 0.9f).dp, (screenHeight * 0.45f).dp)
                        }
                    }"""
    content = content.replace(old_size, new_size)

    # 2. Update isTablet definition lower down (remove it)
    content = content.replace("val isTablet = screenWidth >= 600", "")

    # 3. Update yEndPx for tablet
    old_y_end = """val baseCoverY = if (isTablet) {
                            statusBarTopPx + with(density) { 36.dp.toPx() }
                        } else {"""
    new_y_end = """val baseCoverY = if (isTablet) {
                            val minTopOffsetDp = with(density) { statusBarTopPx.toDp() } + 12.dp
                            val blockHeight = albumArtSizeDp + 280.dp // Approximate height of controls
                            val topOffsetDp = ((containerHeight - blockHeight) / 2f).coerceAtLeast(minTopOffsetDp)
                            with(density) { topOffsetDp.toPx() }
                        } else {"""
    content = content.replace(old_y_end, new_y_end)

    with open(file_path, "w") as f:
        f.write(content)

def update_full_player():
    file_path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
    with open(file_path, "r") as f:
        content = f.read()

    # 1. Update albumArtSize
    old_art_size_block = """val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val screenHeight = configuration.screenHeightDp.dp
        val albumArtSize = androidx.compose.ui.unit.min(screenWidth * 0.9f, screenHeight * 0.45f)"""
    new_art_size_block = """val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val screenHeight = configuration.screenHeightDp.dp
        val isTablet = configuration.screenWidthDp >= 600
        val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
        
        val albumArtSize = if (isTablet) {
            val maxArtWidth = (configuration.screenWidthDp / 2f) - 48f
            val maxArtHeight = configuration.screenHeightDp * 0.45f
            androidx.compose.ui.unit.min(maxArtWidth.dp, maxArtHeight.dp)
        } else {
            androidx.compose.ui.unit.min(screenWidth * 0.9f, screenHeight * 0.45f)
        }"""
    content = content.replace(old_art_size_block, new_art_size_block)

    # 2. Update isTablet lower down
    content = content.replace("val isTablet = configuration.screenWidthDp >= 600", "")

    # 3. Update tabletTopOffset
    old_offset = "val tabletTopOffset = statusBarTopDp + 36.dp"
    new_offset = """val tabletTopOffset = if (isTablet) {
                    val minTopOffsetDp = statusBarTopDp + 12.dp
                    val blockHeight = albumArtSize + 280.dp
                    ((screenHeight - blockHeight) / 2f).coerceAtLeast(minTopOffsetDp)
                } else {
                    0.dp
                }"""
    content = content.replace(old_offset, new_offset)

    with open(file_path, "w") as f:
        f.write(content)

update_player_bottom_sheet()
update_full_player()
print("Success")
