import re

def fix_file(path):
    with open(path, "r") as f:
        content = f.read()

    # Add import for WindowInsets
    if "import androidx.compose.foundation.layout.WindowInsets" not in content:
        content = content.replace("import androidx.compose.foundation.layout.fillMaxSize",
                                  "import androidx.compose.foundation.layout.fillMaxSize\nimport androidx.compose.foundation.layout.WindowInsets")

    with open(path, "w") as f:
        f.write(content)

fix_file("app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt")
fix_file("app/src/main/java/com/codetrio/overdrive/ui/PlayerBottomSheetCompose.kt")
print("Fixed WindowInsets import")
