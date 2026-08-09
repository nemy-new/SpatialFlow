import re

path_full = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path_full, "r") as f:
    content_full = f.read()

target_left_pane = """                        // Left pane: Artwork and Controls
                        Column(
                            modifier = Modifier
                                .weight(1f),
                            horizontalAlignment = Alignment.Start
                        ) {"""

replacement_left_pane = """                        // Left pane: Artwork and Controls
                        Column(
                            modifier = Modifier
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {"""

if target_left_pane in content_full:
    content_full = content_full.replace(target_left_pane, replacement_left_pane)
    with open(path_full, "w") as f:
        f.write(content_full)
    print("FullPlayer.kt aligned to CenterHorizontally")
else:
    print("Could not find left pane in FullPlayer.kt")


path_sheet = "app/src/main/java/com/codetrio/overdrive/ui/PlayerBottomSheetCompose.kt"
with open(path_sheet, "r") as f:
    content_sheet = f.read()

target_x = """                    val xEndPx = remember(isTablet, screenWidthPx, fullSizePx, density) {
                        if (isTablet) {
                            with(density) { 16.dp.toPx() } // Match left pane dimens.screenMargin exactly
                        } else {
                            (screenWidthPx - fullSizePx) / 2f
                        }
                    }"""

replacement_x = """                    val xEndPx = remember(isTablet, screenWidthPx, fullSizePx, density) {
                        if (isTablet) {
                            val screenMarginPx = with(density) { 16.dp.toPx() }
                            val availableWidthPx = screenWidthPx - (2 * screenMarginPx)
                            val leftPaneWidthPx = availableWidthPx / 2f
                            screenMarginPx + (leftPaneWidthPx - fullSizePx) / 2f
                        } else {
                            (screenWidthPx - fullSizePx) / 2f
                        }
                    }"""

if target_x in content_sheet:
    content_sheet = content_sheet.replace(target_x, replacement_x)
    with open(path_sheet, "w") as f:
        f.write(content_sheet)
    print("PlayerBottomSheetCompose.kt xEndPx updated for centered album art")
else:
    print("Could not find xEndPx in PlayerBottomSheetCompose.kt")

