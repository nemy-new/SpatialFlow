import re

path = "app/src/main/java/com/codetrio/overdrive/MainActivity.kt"
with open(path, "r") as f:
    content = f.read()

target = r"""fun getNavLabelForRoute\(route: String\): String \{
    return when \(route\) \{
        "explore" -> "Home"
        "search" -> "Search"
        "library" -> "Library"
        "statistics" -> "Stats"
        "effects" -> "Effects"
        "settings" -> "Settings"
        else -> route.replaceFirstChar \{ it.uppercase\(\) \}
    \}
\}"""

replacement = """@androidx.compose.runtime.Composable
fun getNavLabelForRoute(route: String): String {
    return when (route) {
        "explore" -> androidx.compose.ui.res.stringResource(R.string.tab_explore)
        "search" -> androidx.compose.ui.res.stringResource(R.string.tab_search)
        "library" -> androidx.compose.ui.res.stringResource(R.string.tab_library)
        "statistics" -> androidx.compose.ui.res.stringResource(R.string.tab_statistics)
        "effects" -> androidx.compose.ui.res.stringResource(R.string.tab_effects)
        "settings" -> androidx.compose.ui.res.stringResource(R.string.tab_settings)
        else -> route.replaceFirstChar { it.uppercase() }
    }
}"""

content = re.sub(target, replacement, content)

with open(path, "w") as f:
    f.write(content)
print("Updated getNavLabelForRoute in MainActivity.kt")
