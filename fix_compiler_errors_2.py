import re

def fix_file(path):
    with open(path, "r") as f:
        content = f.read()

    # Change fully qualified WindowInsets.navigationBars to just WindowInsets.navigationBars
    content = content.replace("androidx.compose.foundation.layout.WindowInsets.navigationBars", "WindowInsets.navigationBars")

    with open(path, "w") as f:
        f.write(content)

fix_file("app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt")
fix_file("app/src/main/java/com/codetrio/overdrive/ui/PlayerBottomSheetCompose.kt")
print("Fixed fully qualified extension property")
