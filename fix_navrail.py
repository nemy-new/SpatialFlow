import re

path = "app/src/main/java/com/codetrio/overdrive/MainActivity.kt"
with open(path, "r") as f:
    content = f.read()

# Fix NavigationRail visibility
target = """                    if (isTablet) {
                        val navBackStackEntry by currentNavController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination

                        androidx.compose.material3.NavigationRail("""

replacement = """                    if (isTablet) {
                        val navBackStackEntry by currentNavController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        
                        val showNavRail = currentDestination?.route in listOf("explore", "library", "effects", "settings")

                        if (showNavRail) {
                            androidx.compose.material3.NavigationRail("""

if target in content:
    content = content.replace(target, replacement)
    
    # We also need to close the `if (showNavRail)` block.
    # The NavigationRail block ends where `LaunchedEffect` begins.
    target_end = """                        }

                        LaunchedEffect(Unit) {"""
    replacement_end = """                        }
                        }

                        LaunchedEffect(Unit) {"""
    content = content.replace(target_end, replacement_end)
    with open(path, "w") as f:
        f.write(content)
    print("MainActivity.kt fixed")
else:
    print("Target not found in MainActivity.kt")
