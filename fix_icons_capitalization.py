import re

path = "app/src/main/java/com/codetrio/overdrive/MainActivity.kt"
with open(path, "r") as f:
    content = f.read()

content = content.replace("androidx.compose.material.icons.Icons.Rounded.Home", "androidx.compose.material.icons.rounded.Home")

with open(path, "w") as f:
    f.write(content)
print("Updated icons.")
