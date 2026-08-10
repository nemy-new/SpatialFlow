import re

path = "app/src/main/java/com/codetrio/overdrive/ui/PlayerBottomSheetCompose.kt"
with open(path, "r") as f:
    content = f.read()

target_zindex = r"""\.zIndex\(if \(isQueueExpanded\) 1f else if \(isLyricsModeEnabled \|\| lyricsArtworkProgress > 0f\) 6f else if \(playerTheme == "immersion" && !isMvMode && t > 0\.5f\) -1f else 3f\)"""
replacement_zindex = """.zIndex(if (isQueueExpanded) 1f else if (isLyricsModeEnabled || lyricsArtworkProgress > 0f) 6f else if (playerTheme == "immersion" && !isMvMode && playerContentExpansionFraction.value > 0.5f) -1f else 3f)"""

content = re.sub(target_zindex, replacement_zindex, content)

with open(path, "w") as f:
    f.write(content)
print("Fixed zIndex with playerContentExpansionFraction.value")
