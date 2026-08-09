import re
path = "app/src/main/java/com/codetrio/overdrive/MainActivity.kt"
with open(path, "r") as f:
    content = f.read()

imports = """import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.alpha
"""

# Revert my bad sed attempt
content = content.replace(".androidx.compose.foundation.layout.width", ".width")

# Add imports after package declaration
content = content.replace("package com.codetrio.overdrive", f"package com.codetrio.overdrive\n\n{imports}")

with open(path, "w") as f:
    f.write(content)
