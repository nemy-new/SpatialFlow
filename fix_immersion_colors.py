import re

path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

# 1. Box size for immersion mode (0.6f -> 0.85f)
content = content.replace(
    'height = if (playerTheme == "immersion") albumArtSize * 0.6f else albumArtSize',
    'height = if (playerTheme == "immersion") albumArtSize * 0.85f else albumArtSize'
)

# 2. dynamicAccentColor logic
target_accent = r"""    val dynamicAccentColor = remember\(accentColor, isTextColorDark\) \{
        val hsl = FloatArray\(3\)"""

replacement_accent = """    val dynamicAccentColor = remember(accentColor, isTextColorDark, playerTheme) {
        if (playerTheme == "immersion") return@remember Color.White
        val hsl = FloatArray(3)"""
content = re.sub(target_accent, replacement_accent, content)

with open(path, "w") as f:
    f.write(content)
print("Changes applied")
