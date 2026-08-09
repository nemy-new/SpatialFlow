import re

with open('app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt', 'r') as f:
    content = f.read()

content = content.replace("androidx.compose.material.icons.Icons.AutoMirrored.Rounded.List", "androidx.compose.material.icons.Icons.Rounded.List")
content = content.replace("import androidx.compose.material.icons.automirrored.rounded.List", "import androidx.compose.material.icons.rounded.List")

with open('app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt', 'w') as f:
    f.write(content)
