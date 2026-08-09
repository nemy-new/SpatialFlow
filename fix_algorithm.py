import re

path = "app/src/main/java/com/codetrio/overdrive/ui/PlayerBottomSheetCompose.kt"
with open(path, "r") as f:
    content = f.read()

target_x = """                    val xEndPx = remember(isTablet, screenWidthPx, fullSizePx) {
                        if (isTablet) {
                            (screenWidthPx / 4f) - (fullSizePx / 2f)
                        } else {
                            (screenWidthPx - fullSizePx) / 2f
                        }
                    }"""

replacement_x = """                    val xEndPx = remember(isTablet, screenWidthPx, fullSizePx, density) {
                        if (isTablet) {
                            with(density) { 16.dp.toPx() } // Match left pane dimens.screenMargin exactly
                        } else {
                            (screenWidthPx - fullSizePx) / 2f
                        }
                    }"""

content = content.replace(target_x, replacement_x)

target_y = """                    val yEndPx = remember(isTablet, statusBarTopPx, density, containerHeight, albumArtSizeDp, fullSizePx) {
                        if (isTablet) {
                            val headerHeightPx = statusBarTopPx + with(density) { 68.dp.toPx() }
                            val containerHeightPx = with(density) { containerHeight.toPx() }
                            val availableHeightPx = containerHeightPx - headerHeightPx
                            val centerY = headerHeightPx + (availableHeightPx / 2f)
                            centerY - (fullSizePx / 2f)
                        } else {"""

replacement_y = """                    val yEndPx = remember(isTablet, statusBarTopPx, density, containerHeight, albumArtSizeDp, fullSizePx, screenHeight) {
                        if (isTablet) {
                            val controlsHeightDp = 268.dp
                            val totalGroupHeightDp = albumArtSizeDp + controlsHeightDp
                            val availableHeightDp = screenHeight.dp - with(density) { statusBarTopPx.toDp() }
                            val tabletTopOffsetDp = with(density) { statusBarTopPx.toDp() } + ((availableHeightDp - totalGroupHeightDp) / 2f).coerceAtLeast(16.dp)
                            with(density) { tabletTopOffsetDp.toPx() }
                        } else {"""

content = content.replace(target_y, replacement_y)

with open(path, "w") as f:
    f.write(content)
print("Algorithm updated")
