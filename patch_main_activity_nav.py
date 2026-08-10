import re

path = "app/src/main/java/com/codetrio/overdrive/MainActivity.kt"
with open(path, "r") as f:
    content = f.read()

# Add a composable helper at the end of the file to observe SharedPreferences
helper_code = """
@Composable
fun rememberBottomNavTabs(context: android.content.Context): androidx.compose.runtime.State<List<com.codetrio.overdrive.model.BottomNavTab>> {
    val prefs = remember { context.getSharedPreferences("AppSettings", android.content.Context.MODE_PRIVATE) }
    val tabsState = remember { androidx.compose.runtime.mutableStateOf(com.codetrio.overdrive.model.BottomNavTab.parse(prefs.getString("bottom_nav_tabs", null))) }
    
    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "bottom_nav_tabs") {
                tabsState.value = com.codetrio.overdrive.model.BottomNavTab.parse(sharedPreferences.getString(key, null))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    return tabsState
}

fun getNavIconForRoute(route: String): Any {
    return when (route) {
        "explore" -> androidx.compose.material.icons.Icons.Rounded.Home
        "search" -> androidx.compose.material.icons.Icons.Rounded.Search
        "library" -> com.codetrio.overdrive.R.drawable.ic_library_music
        "statistics" -> com.codetrio.overdrive.R.drawable.ic_analytics
        "effects" -> com.codetrio.overdrive.R.drawable.ic_equalizer
        "settings" -> com.codetrio.overdrive.R.drawable.ic_settings
        else -> androidx.compose.material.icons.Icons.Rounded.Home
    }
}

fun getNavLabelForRoute(route: String): String {
    return when (route) {
        "explore" -> "Home"
        "search" -> "Search"
        "library" -> "Library"
        "statistics" -> "Stats"
        "effects" -> "Effects"
        "settings" -> "Settings"
        else -> route.replaceFirstChar { it.uppercase() }
    }
}
"""

if "rememberBottomNavTabs" not in content:
    content += helper_code

target1 = """                            val items = listOf<Triple<String, String, Any>>(
                                Triple("explore", "Home", Icons.Rounded.Home),
                                Triple("search", "Search", Icons.Rounded.Search),
                                Triple("library", "Library", R.drawable.ic_library_music),
                                Triple("effects", "Effects", R.drawable.ic_equalizer),
                                Triple("settings", "Settings", R.drawable.ic_settings)
                            )"""

replacement1 = """                            val tabsState by rememberBottomNavTabs(LocalContext.current)
                            val items = tabsState.filter { it.isVisible }.map { Triple(it.route, getNavLabelForRoute(it.route), getNavIconForRoute(it.route)) }"""

content = content.replace(target1, replacement1)

with open(path, "w") as f:
    f.write(content)
print("MainActivity patched.")
