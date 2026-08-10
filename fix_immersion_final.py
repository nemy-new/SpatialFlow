import re

path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

# 1. Height adjustment (0.92f -> 0.98f to push it down significantly)
content = content.replace(
    'height = if (playerTheme == "immersion") albumArtSize * 0.92f else albumArtSize',
    'height = if (playerTheme == "immersion") albumArtSize * 0.98f else albumArtSize'
)

# 2. Fix global contentColor and contentSecondary for immersion
target_colors = r"""    val contentColor = if \(isTextColorDark\) Color\(0xFF1C1B1F\) else Color\.White
    val contentSecondary = if \(isTextColorDark\) Color\(0xFF1C1B1F\)\.copy\(alpha = 0\.6f\) else Color\.White\.copy\(alpha = 0\.6f\)"""

replacement_colors = """    val contentColor = if (playerTheme == "immersion") Color.White else (if (isTextColorDark) Color(0xFF1C1B1F) else Color.White)
    val contentSecondary = if (playerTheme == "immersion") Color.White.copy(alpha = 0.75f) else (if (isTextColorDark) Color(0xFF1C1B1F).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.6f))"""
content = re.sub(target_colors, replacement_colors, content)

with open(path, "w") as f:
    f.write(content)
print("Changes applied")
