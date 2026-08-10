import re

path2 = "app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt"
with open(path2, "r") as f2:
    content2 = f2.read()

# 1. Remove Feedback category from Settings list
target_feedback_category = r"""                \{
                    SettingsCategoryItem\(
                        title = stringResource\(R\.string\.settings_cat_feedback\),
                        subtitle = stringResource\(R\.string\.settings_cat_feedback_sub\),
                        icon = Icons\.Rounded\.BugReport,
                        onClick = \{ navController\.navigate\(SettingsRoute\.Feedback\.route\) \}
                    \)
                \},
"""
content2 = re.sub(target_feedback_category, "", content2)

# 2. Remove FeedbackScreen navigation graph
target_feedback_route = r"""    composableWithBlur\(
        route = SettingsRoute\.Feedback\.route,
        enterTransition = enterAnim,
        exitTransition = exitAnim,
        popEnterTransition = popEnterAnim,
        popExitTransition = popExitAnim
    \) \{
        FeedbackScreen\(navController = navController\)
    \}"""
content2 = re.sub(target_feedback_route, "", content2)

# 3. Remove FeedbackScreen composable
target_feedback_screen = r"""// ── Feedback & Bug Reports ──────────────────────────────────────────────────[\s\S]*?(?=\n// ──)"""
content2 = re.sub(target_feedback_screen, "", content2)

# 4. Remove DonateCard and CreditsCard calls from AboutScreen
content2 = re.sub(r"            DonateCard\(onOpenUrl\)\n            \n            Spacer\(modifier = Modifier\.height\(16\.dp\)\)\n            \n            // Credits Card\n            CreditsCard\(onOpenUrl\)", "", content2)

# 5. Remove DonateCard and CreditsCard composables
target_donate = r"""@Composable\nprivate fun DonateCard\(onOpenUrl: \(String\) -> Unit\) \{[\s\S]*?(?=@Composable\nprivate fun CreditsCard|// ──)"""
content2 = re.sub(target_donate, "", content2)

target_credits = r"""@Composable\nprivate fun CreditsCard\(onOpenUrl: \(String\) -> Unit\) \{[\s\S]*?(?=@Composable|// ──)"""
content2 = re.sub(target_credits, "", content2)

with open(path2, "w") as f2:
    f2.write(content2)
print("Removed Developer/Support/Telegram references")
