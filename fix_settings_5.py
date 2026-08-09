import re

with open('app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt', 'r') as f:
    content = f.read()

content = content.replace("androidx.compose.material.icons.automirrored.rounded.List", "androidx.compose.material.icons.Icons.AutoMirrored.Rounded.List")

with open('app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt', 'w') as f:
    f.write(content)
print("Fixed icon path")
