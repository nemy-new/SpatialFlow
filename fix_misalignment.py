import re

path_bottom_sheet = "app/src/main/java/com/codetrio/overdrive/ui/PlayerBottomSheetCompose.kt"
with open(path_bottom_sheet, "r") as f:
    content = f.read()

target = "val screenMarginPx = with(density) { 16.dp.toPx() }"
replacement = "val screenMarginPx = with(density) { com.codetrio.overdrive.ui.theme.LocalDimens.current.screenMargin.toPx() }"

content = content.replace(target, replacement)

with open(path_bottom_sheet, "w") as f:
    f.write(content)
print("Alignment fixed.")
