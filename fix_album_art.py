import re

path = "app/src/main/java/com/codetrio/overdrive/ui/PlayerBottomSheetCompose.kt"
with open(path, "r") as f:
    content = f.read()

target_zindex = r"""\.zIndex\(if \(isQueueExpanded\) 1f else if \(isLyricsModeEnabled \|\| lyricsArtworkProgress > 0f\) 6f else if \(playerTheme == "immersion" && !isMvMode\) -1f else 3f\)"""
replacement_zindex = """.zIndex(if (isQueueExpanded) 1f else if (isLyricsModeEnabled || lyricsArtworkProgress > 0f) 6f else if (playerTheme == "immersion" && !isMvMode && t > 0.5f) -1f else 3f)"""

content = re.sub(target_zindex, replacement_zindex, content)

with open(path, "w") as f:
    f.write(content)
print("Updated zIndex for ArtworkPager in PlayerBottomSheetCompose.kt")

path2 = "app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt"
with open(path2, "r") as f2:
    content2 = f2.read()

# Remove AppLogoSection call
content2 = re.sub(r"AppLogoSection\(isLandscape = isLandscape\)", "", content2)

# Remove AppLogoSection code block (until end of file or next fun)
target_logo = r"""// ── App Logo ────────────────────────────────────────────────────────────────[\s\S]*?(?=\n\n\n|\Z)"""
content2 = re.sub(target_logo, "", content2)

# Let's also remove the specific implementation just in case regex didn't catch it
target_logo_func = r"""@Composable\nprivate fun AppLogoSection\(isLandscape: Boolean\) \{[\s\S]*?(?=@Composable|// ──)"""
content2 = re.sub(target_logo_func, "", content2)

with open(path2, "w") as f2:
    f2.write(content2)
print("Removed AppLogoSection from SettingsFragment.kt")
