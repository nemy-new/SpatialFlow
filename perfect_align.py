def fix_full_player():
    path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
    with open(path, "r") as f:
        content = f.read()
    
    # 1. Update tabletTopOffset
    old_offset = """val tabletTopOffset = if (isTablet) {
                    val minTopOffsetDp = statusBarTopDp + 12.dp
                    val blockHeight = albumArtSize + 280.dp
                    ((screenHeight - blockHeight) / 2f).coerceAtLeast(minTopOffsetDp)
                } else {
                    0.dp
                }"""
    
    new_offset = """val navBarBottomDp = with(density) { androidx.compose.foundation.layout.WindowInsets.navigationBars.getBottom(density).toDp() }
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

    # 2. Remove horizontal padding from the tablet Row
    old_row = """Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = dimens.screenMargin),
                        verticalAlignment = Alignment.CenterVertically
                    )"""
    new_row = """Row(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    )"""
    if old_row in content:
        content = content.replace(old_row, new_row)
    else:
        print("old_row not found in FullPlayer")
        
    with open(path, "w") as f:
        f.write(content)

def fix_pbs():
    path = "app/src/main/java/com/codetrio/overdrive/ui/PlayerBottomSheetCompose.kt"
    with open(path, "r") as f:
        content = f.read()

    # 1. Add dimens for calculation
    # We will insert it inside the remember block for yEndPx and xEndPx, but we need it from outside
    old_density = "val statusBarTopPx = WindowInsets.statusBars.getTop(density).toFloat()"
    new_density = """val statusBarTopPx = WindowInsets.statusBars.getTop(density).toFloat()
                    val navBarBottomPx = WindowInsets.navigationBars.getBottom(density).toFloat()
                    val dimens = com.codetrio.overdrive.ui.theme.LocalDimens.current"""
    if old_density in content:
        content = content.replace(old_density, new_density)
    else:
        print("old_density not found in PBS")

    # 2. Update xEndPx to account for screenMargin
    old_xend = """val xEndPx = remember(isTablet, screenWidthPx, fullWidthPx) {
                        if (isTablet) {
                            (screenWidthPx / 4f) - (fullWidthPx / 2f)
                        } else {"""
    new_xend = """val screenMarginPx = with(density) { dimens.screenMargin.toPx() }
                    val xEndPx = remember(isTablet, screenWidthPx, fullWidthPx, screenMarginPx) {
                        if (isTablet) {
                            (screenWidthPx / 4f) + (screenMarginPx / 2f) - (fullWidthPx / 2f)
                        } else {"""
    if old_xend in content:
        content = content.replace(old_xend, new_xend)
    else:
        print("old_xend not found in PBS")

    # 3. Update yEndPx for perfect alignment
    # Note: I need to check exactly what the current yEndPx code is.
