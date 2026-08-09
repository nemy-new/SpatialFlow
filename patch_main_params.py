import re

file_path = "app/src/main/java/com/codetrio/overdrive/MainActivity.kt"
with open(file_path, "r") as f:
    content = f.read()

target1 = """                        val floatingNavBar by prefs.observeKey("floating_nav_bar", false)"""
replacement1 = """                        val floatingNavBar by prefs.observeKey("floating_nav_bar", false)
                        val unifiedFloatingBar by prefs.observeKey("unified_floating_bar", false)"""
if target1 in content: content = content.replace(target1, replacement1)

target2 = """                            shape = if (floatingNavBar) androidx.compose.foundation.shape.RoundedCornerShape(32.dp) else androidx.compose.ui.graphics.RectangleShape,"""
replacement2 = """                            shape = if (floatingNavBar && unifiedFloatingBar) {
                                androidx.compose.foundation.shape.RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 40.dp, bottomEnd = 40.dp)
                            } else if (floatingNavBar) {
                                androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
                            } else {
                                androidx.compose.ui.graphics.RectangleShape
                            },"""
if target2 in content: content = content.replace(target2, replacement2)

target3 = """                            NavigationBar(
                                modifier = Modifier.height(if (floatingNavBar) navBarHeight else navBarHeight + bottomPadding),
                                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                                tonalElevation = 0.dp
                            ) {"""
replacement3 = """                            NavigationBar(
                                modifier = Modifier.height(if (floatingNavBar) navBarHeight else navBarHeight + bottomPadding),
                                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                                tonalElevation = 0.dp,
                                windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
                            ) {"""
if target3 in content: content = content.replace(target3, replacement3)

with open(file_path, "w") as f:
    f.write(content)

print("Done patching main")
