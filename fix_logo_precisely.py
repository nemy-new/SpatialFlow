import re

path2 = "app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt"
with open(path2, "r") as f2:
    content2 = f2.read()

# Remove AppLogoSection call
content2 = content2.replace("        AppLogoSection(isLandscape = isLandscape)\n", "")

# Remove the AppLogoSection method completely
# We know exactly where it ends (before "fun FeedbackScreen")
parts = content2.split("// ── App Logo ────────────────────────────────────────────────────────────────")
if len(parts) > 1:
    before = parts[0]
    after_logo = parts[1]
    
    # split again at the next function definition, which should be FeedbackScreen
    after_logo_parts = after_logo.split("@Composable\nprivate fun FeedbackScreen")
    if len(after_logo_parts) > 1:
        content2 = before + "@Composable\nprivate fun FeedbackScreen" + after_logo_parts[1]
    else:
        print("Couldn't find FeedbackScreen!")
        
with open(path2, "w") as f2:
    f2.write(content2)
print("Removed AppLogoSection from SettingsFragment.kt precisely")
