import re

path_full = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path_full, "r") as f:
    content = f.read()

target = "painter = painterResource(id = if (isTabsVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility)"
replacement = "imageVector = if (isTabsVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility"
content = content.replace(target, replacement)

with open(path_full, "w") as f:
    f.write(content)
print("Icons fixed.")
