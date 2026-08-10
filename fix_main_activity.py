import re

path = "app/src/main/java/com/codetrio/overdrive/MainActivity.kt"
with open(path, "r") as f:
    content = f.read()

# Add imports
if "import androidx.compose.ui.platform.LocalContext" not in content:
    content = content.replace("import androidx.compose.ui.Modifier", "import androidx.compose.ui.Modifier\nimport androidx.compose.ui.platform.LocalContext\nimport androidx.compose.runtime.DisposableEffect")

# Fix Analytics
content = content.replace("androidx.compose.material.icons.Icons.Rounded.Analytics", "androidx.compose.material.icons.Icons.Rounded.Info")

with open(path, "w") as f:
    f.write(content)
print("MainActivity fixed")
