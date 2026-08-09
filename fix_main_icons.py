import re

path = "app/src/main/java/com/codetrio/overdrive/MainActivity.kt"
with open(path, "r") as f:
    content = f.read()

# Replace the items list to explicitly use <String, String, Any> and correct the Home icon import
target_1 = """                            val items = listOf(
                                Triple("explore", "Home", androidx.compose.material.icons.Icons.Rounded.Home),
                                Triple("library", "Library", R.drawable.ic_library_music),
                                Triple("effects", "Effects", R.drawable.ic_equalizer),
                                Triple("settings", "Settings", R.drawable.ic_settings)
                            )"""

replacement_1 = """                            val items = listOf<Triple<String, String, Any>>(
                                Triple("explore", "Home", androidx.compose.material.icons.Icons.Rounded.Home),
                                Triple("library", "Library", R.drawable.ic_library_music),
                                Triple("effects", "Effects", R.drawable.ic_equalizer),
                                Triple("settings", "Settings", R.drawable.ic_settings)
                            )"""

content = content.replace(target_1, replacement_1)

with open(path, "w") as f:
    f.write(content)
print("Updated lists.")
