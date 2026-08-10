import re

path = "app/src/main/java/com/codetrio/overdrive/ui/PlayerBottomSheetCompose.kt"
with open(path, "r") as f:
    content = f.read()

target = r"""                        \} else \{
                            val controlsHeightDp = 300\.dp
                            val totalGroupHeightDp = albumArtHeightDp \+ controlsHeightDp
                            val availableHeightDp = screenHeight\.dp - with\(density\) \{ statusBarTopPx\.toDp\(\) \}
                            val phoneTopOffsetDp = with\(density\) \{ statusBarTopPx\.toDp\(\) \} \+ \(\(availableHeightDp - totalGroupHeightDp\) / 2f\)\.coerceAtLeast\(16\.dp\)
                            with\(density\) \{ phoneTopOffsetDp\.toPx\(\) \}
                        \}"""

replacement = """                        } else {
                            val controlsHeightDp = 420.dp
                            val totalGroupHeightDp = albumArtHeightDp + controlsHeightDp
                            val availableHeightDp = screenHeight.dp - with(density) { statusBarTopPx.toDp() }
                            val phoneTopOffsetDp = with(density) { statusBarTopPx.toDp() } + ((availableHeightDp - totalGroupHeightDp) / 2f).coerceAtLeast(16.dp)
                            with(density) { phoneTopOffsetDp.toPx() }
                        }"""

content_new = re.sub(target, replacement, content)

if content_new != content:
    with open(path, "w") as f:
        f.write(content_new)
    print("Fixed album art offset for phone.")
else:
    print("Warning: regex didn't match in PlayerBottomSheetCompose.")
