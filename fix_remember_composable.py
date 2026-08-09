import re

path_bottom_sheet = "app/src/main/java/com/codetrio/overdrive/ui/PlayerBottomSheetCompose.kt"
with open(path_bottom_sheet, "r") as f:
    content = f.read()

# 1. Add `val dimens = com.codetrio.overdrive.ui.theme.LocalDimens.current` before `val xEndPx`
target = "val xEndPx = remember(isTablet, screenWidthPx, fullSizePx, density) {"
replacement = "val dimens = com.codetrio.overdrive.ui.theme.LocalDimens.current\n                    val xEndPx = remember(isTablet, screenWidthPx, fullSizePx, density, dimens) {"
content = content.replace(target, replacement)

# 2. Fix the line inside remember block
target_inside = "val screenMarginPx = with(density) { com.codetrio.overdrive.ui.theme.LocalDimens.current.screenMargin.toPx() }"
replacement_inside = "val screenMarginPx = with(density) { dimens.screenMargin.toPx() }"
content = content.replace(target_inside, replacement_inside)

with open(path_bottom_sheet, "w") as f:
    f.write(content)
print("Composable inside remember fixed.")
