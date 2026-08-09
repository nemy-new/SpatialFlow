import re

path = "app/src/main/java/com/codetrio/overdrive/MainActivity.kt"
with open(path, "r") as f:
    content = f.read()

# Fix the broken import
content = content.replace("import Icons.Rounded.Home", "import androidx.compose.material.icons.rounded.Home")

with open(path, "w") as f:
    f.write(content)
print("Import fixed.")
