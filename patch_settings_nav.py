import re

with open('app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt', 'r') as f:
    content = f.read()

nav_route = """    composableWithBlur(
        route = "bottom_nav_customize",
        enterTransition = enterAnim,
        exitTransition = exitAnim,
        popEnterTransition = popEnterAnim,
        popExitTransition = popExitAnim
    ) {
        com.codetrio.overdrive.ui.settings.BottomNavCustomizeScreen(
            playerViewModel = playerViewModel,
            onBack = { navController.popBackStack() }
        )
    }
}"""

content = content.replace("    }\n}", "    }\n\n" + nav_route)

with open('app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt', 'w') as f:
    f.write(content)
