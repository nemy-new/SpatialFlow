import re

def fix_file(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    # Remove the landscape padding offset
    content = content.replace("            // Offset for Navigation Rail in landscape (80dp + margin)\n", "")
    content = content.replace("                // Offset for Navigation Rail in landscape (80dp + margin)\n", "")
    content = content.replace("            .then(if (isLandscape) Modifier.padding(start = 88.dp) else Modifier)\n", "")
    content = content.replace("                .then(if (isLandscape) Modifier.padding(start = 88.dp) else Modifier)\n", "")

    with open(filepath, "w") as f:
        f.write(content)
    print(f"Fixed {filepath}")

fix_file("app/src/main/java/com/codetrio/overdrive/ui/EffectsScreen.kt")
fix_file("app/src/main/java/com/codetrio/overdrive/ui/explore/ExploreScreen.kt")
fix_file("app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt")
