import re

def fix_full_player():
    path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
    with open(path, "r") as f:
        content = f.read()

    # 1. Left align Title
    content = content.replace("textAlign = androidx.compose.ui.text.style.TextAlign.Center,",
                              "textAlign = androidx.compose.ui.text.style.TextAlign.Start,")

    # 2. Set tabletTopOffset to 0
    old_offset = """val tabletTopOffset = if (isTablet) {
                    val availableHeight = screenHeight - statusBarTopDp - navBarBottomDp - (dimens.smallPadding * 2) - 56.dp
                    val blockHeight = albumArtSize + 280.dp
                    ((availableHeight - blockHeight) / 2f).coerceAtLeast(0.dp)
                } else {
                    0.dp
                }"""
    new_offset = """val tabletTopOffset = 0.dp"""
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

    # Change yEndPx topOffsetDp to 0 for tablets
    old_y_end = """val topOffsetDp = ((availableHeight - blockHeight) / 2f).coerceAtLeast(0.dp)"""
    new_y_end = """val topOffsetDp = 0.dp"""
    if old_y_end in content:
        content = content.replace(old_y_end, new_y_end)
    else:
        print("old_y_end not found in PBS")

    with open(path, "w") as f:
        f.write(content)

fix_full_player()
fix_pbs()
print("Done fixing alignments")
