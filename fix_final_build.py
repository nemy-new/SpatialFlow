import re

path = "app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt"
with open(path, "r") as f:
    content = f.read()

target = """    composableWithBlur(
        route = SettingsRoute.CustomizeBottomNav.route,
        enterTransition = enterAnim,
        exitTransition = exitAnim,
        popEnterTransition = popEnterAnim,
        popExitTransition = popExitAnim
    ) {
        val context = androidx.compose.ui.platform.LocalContext.current
        val activity = context.findActivity() as androidx.activity.ComponentActivity
        val playerSharedViewModel: com.codetrio.overdrive.viewmodel.PlayerSharedViewModel = androidx.lifecycle.viewmodel.compose.viewModel(activity)
        com.codetrio.overdrive.ui.settings.BottomNavCustomizeScreen(
            playerViewModel = playerSharedViewModel,
            onBack = { navController.popBackStack() }
        )
    }"""

if target in content:
    content = content.replace(target, "/* " + target + " */")
    with open(path, "w") as f:
        f.write(content)
    print("SettingsFragment fixed")
else:
    print("Target not found in SettingsFragment")


path_fp = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path_fp, "r") as f:
    fp_content = f.read()

# Fix LyricsBottomSheet double syncOffsetMs
target_dup = """            onCollapse = { viewModel.setLyricsModeEnabled(false) },
            syncOffsetMs = 0L,
            onSyncOffsetChange = {},
            syncOffsetMs = 0L,
            onSyncOffsetChange = {},
            modifier = Modifier.fillMaxSize()
"""
if target_dup in fp_content:
    fp_content = fp_content.replace(target_dup, """            onCollapse = { viewModel.setLyricsModeEnabled(false) },
            syncOffsetMs = 0L,
            onSyncOffsetChange = {},
            modifier = Modifier.fillMaxSize()
""")
    with open(path_fp, "w") as f:
        f.write(fp_content)
    print("FullPlayer duplicate arguments fixed")

# If it's a slightly different format
elif fp_content.count("syncOffsetMs = 0L,") > 2:
    fp_content = re.sub(r'syncOffsetMs = 0L,\s*onSyncOffsetChange = \{\},\s*syncOffsetMs = 0L,\s*onSyncOffsetChange = \{\},', 'syncOffsetMs = 0L,\n            onSyncOffsetChange = {},', fp_content)
    with open(path_fp, "w") as f:
        f.write(fp_content)
    print("FullPlayer duplicate arguments fixed (regex)")
