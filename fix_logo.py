import re

path2 = "app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt"
with open(path2, "r") as f2:
    content2 = f2.read()

# Remove AppLogoSection call
content2 = re.sub(r"AppLogoSection\(isLandscape = isLandscape\)", "", content2)

# Properly remove AppLogoSection definition without wiping out other composables
target_logo_func = r"""// ── App Logo ────────────────────────────────────────────────────────────────

@Composable
private fun AppLogoSection\(isLandscape: Boolean\) \{[\s\S]*?            \)
        \}
    \}
\}"""
content2 = re.sub(target_logo_func, "", content2)

with open(path2, "w") as f2:
    f2.write(content2)
print("Removed AppLogoSection from SettingsFragment.kt properly")
