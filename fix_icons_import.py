import re

path_full = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path_full, "r") as f:
    content = f.read()

imports = """import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
"""

content = content.replace("package com.codetrio.overdrive.ui.player", f"package com.codetrio.overdrive.ui.player\n\n{imports}")

with open(path_full, "w") as f:
    f.write(content)
print("Icon imports added.")
