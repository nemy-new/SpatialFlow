import re

def fix_full_player():
    path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
    with open(path, "r") as f:
        content = f.read()

    # Update tabletTopOffset
    old_offset = """val tabletTopOffset = if (isTablet) {
                    val minTopOffsetDp = statusBarTopDp + 12.dp
                    val blockHeight = albumArtSize + 280.dp
                    ((screenHeight - blockHeight) / 2f).coerceAtLeast(minTopOffsetDp)
                } else {
                    0.dp
                }"""
    new_offset = """val navBarBottomDp = with(density) { androidx.compose.foundation.layout.WindowInsets.navigationBars.getBottom(this).toDp() }
                val tabletTopOffset = if (isTablet) {
                    val availableHeight = screenHeight - statusBarTopDp - navBarBottomDp - (dimens.smallPadding * 2) - 56.dp
                    val blockHeight = albumArtSize + 280.dp
                    ((availableHeight - blockHeight) / 2f).coerceAtLeast(0.dp)
                } else {
                    0.dp
                }"""
    
    if old_offset in content:
        content = content.replace(old_offset, new_offset)
    else:
        print("old_offset not found in FullPlayer")

    with open(path, "w") as f:
        f.write(content)

def fix_pbs():
    path = "app/src/main/java/com/codetrio/overdrive/ui/PlayerBottomSheetCompose.kt"
    with open(path, "r") as f:
        content = f.read()

    # Need to match yEndPx correctly
    old_y_end = """val yEndPx = remember(isTablet, statusBarTopPx, density, containerHeight, albumArtSizeDp, fullSizePx, isImmersion, isMvMode, targetHeightDp) {
                        val baseCoverY = if (isTablet) {
                            val minTopOffsetDp = with(density) { statusBarTopPx.toDp() } + 12.dp
                            val blockHeight = albumArtSizeDp + 280.dp // Approximate height of controls
                            val topOffsetDp = ((containerHeight - blockHeight) / 2f).coerceAtLeast(minTopOffsetDp)
                            with(density) { topOffsetDp.toPx() }
                        } else {"""
    new_y_end = """val navBarBottomPx = WindowInsets.navigationBars.getBottom(density).toFloat()
                    val dimens = com.codetrio.overdrive.ui.theme.LocalDimens.current
                    
                    val yEndPx = remember(isTablet, statusBarTopPx, navBarBottomPx, dimens, density, containerHeight, albumArtSizeDp, fullSizePx, isImmersion, isMvMode, targetHeightDp) {
                        val baseCoverY = if (isTablet) {
                            val availableHeight = containerHeight - with(density) { statusBarTopPx.toDp() + navBarBottomPx.toDp() + (dimens.smallPadding * 2) + 56.dp }
                            val blockHeight = albumArtSizeDp + 280.dp
                            val topOffsetDp = ((availableHeight - blockHeight) / 2f).coerceAtLeast(0.dp)
                            val baseY = statusBarTopPx + with(density) { (dimens.smallPadding * 2).toPx() + 56.dp.toPx() }
                            baseY + with(density) { topOffsetDp.toPx() }
                        } else {"""

    if old_y_end in content:
        content = content.replace(old_y_end, new_y_end)
    else:
        print("old_y_end not found in PBS")

    old_x_end = """val xEndPx = remember(isTablet, screenWidthPx, fullWidthPx) {
                        if (isTablet) {
                            (screenWidthPx / 4f) - (fullWidthPx / 2f)
                        } else {"""
    new_x_end = """val screenMarginPx = with(density) { dimens.screenMargin.toPx() }
                    val xEndPx = remember(isTablet, screenWidthPx, fullWidthPx, screenMarginPx) {
                        if (isTablet) {
                            (screenWidthPx / 4f) + (screenMarginPx / 2f) - (fullWidthPx / 2f)
                        } else {"""
    if old_x_end in content:
        content = content.replace(old_x_end, new_x_end)
    else:
        print("old_x_end not found in PBS")

    with open(path, "w") as f:
        f.write(content)

fix_full_player()
fix_pbs()
print("Done")
