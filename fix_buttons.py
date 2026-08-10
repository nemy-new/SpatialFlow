import re

path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

# 1. Height adjustment (0.85f -> 0.95f to push it down significantly)
# 0.85f was still too high? The user said "あまりにも上すぎます" when it was 0.85f? No, the user saw 0.6f. 
# But to be safe, let's just make it 0.92f.
content = content.replace(
    'height = if (playerTheme == "immersion") albumArtSize * 0.85f else albumArtSize',
    'height = if (playerTheme == "immersion") albumArtSize * 0.92f else albumArtSize'
)

# 2. Previous Button
target_prev = r"""                            colors = androidx\.compose\.material3\.ButtonDefaults\.buttonColors\(
                                containerColor = contentColor\.copy\(alpha = if \(isDark\) 0\.08f else 0\.06f\),
                                contentColor = contentColor
                            \),"""

replacement_prev = """                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = if (playerTheme == "immersion") Color.White.copy(alpha = 0.15f) else contentColor.copy(alpha = if (isDark) 0.08f else 0.06f),
                                contentColor = if (playerTheme == "immersion") Color.White else contentColor
                            ),"""
content = re.sub(target_prev, replacement_prev, content)

# 3. Play Button
target_play = r"""                            colors = androidx\.compose\.material3\.ButtonDefaults\.buttonColors\(
                                containerColor = dynamicAccentColor,
                                contentColor = if \(isDark\) Color\(0xFF1C1B1F\) else Color\.White
                            \),"""

replacement_play = """                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = if (playerTheme == "immersion") Color.White.copy(alpha = 0.25f) else dynamicAccentColor,
                                contentColor = if (playerTheme == "immersion") Color.White else (if (isDark) Color(0xFF1C1B1F) else Color.White)
                            ),"""
content = re.sub(target_play, replacement_play, content)

# 4. Next Button
target_next = r"""                            colors = androidx\.compose\.material3\.ButtonDefaults\.buttonColors\(
                                containerColor = contentColor\.copy\(alpha = if \(isDark\) 0\.08f else 0\.06f\),
                                contentColor = contentColor
                            \),"""

replacement_next = """                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = if (playerTheme == "immersion") Color.White.copy(alpha = 0.15f) else contentColor.copy(alpha = if (isDark) 0.08f else 0.06f),
                                contentColor = if (playerTheme == "immersion") Color.White else contentColor
                            ),"""
content = re.sub(target_next, replacement_next, content)

with open(path, "w") as f:
    f.write(content)
print("Changes applied")
