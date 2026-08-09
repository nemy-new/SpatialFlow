import re

with open('app/src/main/java/com/codetrio/overdrive/MainActivity.kt', 'r') as f:
    content = f.read()

# 1. Inject bottomNavTabs state
old_tablet = """                val isTablet = windowSizeClass?.widthSizeClass != androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Compact"""

new_tablet = """                val bottomNavTabs by playerViewModel.bottomNavTabs.collectAsStateWithLifecycle()
                
                val allTabs = mapOf(
                    "explore" to Triple("explore", stringResource(id = R.string.tab_explore), R.drawable.ic_explore),
                    "search" to Triple("search", stringResource(id = R.string.tab_search), R.drawable.ic_search),
                    "library" to Triple("library", stringResource(id = R.string.tab_library), R.drawable.ic_library_music),
                    "effects" to Triple("effects", stringResource(id = R.string.tab_effects), R.drawable.ic_equalizer),
                    "settings" to Triple("settings", stringResource(id = R.string.tab_settings), R.drawable.ic_settings)
                )
                
                val isTablet = windowSizeClass?.widthSizeClass != androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Compact"""

content = content.replace(old_tablet, new_tablet)

# 2. Update items in NavigationRail (around line 326)
old_rail_items = """                            val items = listOf(
                                Triple("explore", stringResource(id = R.string.tab_explore), R.drawable.ic_explore),
                                Triple("search", stringResource(id = R.string.tab_search), R.drawable.ic_search),
                                Triple("library", stringResource(id = R.string.tab_library), R.drawable.ic_library_music),
                                Triple("effects", stringResource(id = R.string.tab_effects), R.drawable.ic_equalizer),
                                Triple("settings", stringResource(id = R.string.tab_settings), R.drawable.ic_settings)
                            )"""

new_rail_items = """                            val items = bottomNavTabs.filter { it.isVisible }.mapNotNull { allTabs[it.route] }"""

content = content.replace(old_rail_items, new_rail_items)

# 3. Update items in NavigationBar (around line 461)
# Note: we can just replace all occurrences of `old_rail_items` with `new_rail_items` since they are identical.
content = content.replace(old_rail_items, new_rail_items)

# 4. Update routeIndices
old_route_indices = """                            val routeIndices = remember {
                                mapOf(
                                    "explore" to 0,
                                    "library" to 1,
                                    "effects" to 2,
                                    "settings" to 3,
                                    "music_management" to 4,
                                    "account" to 4,
                                    "appearance" to 4,
                                    "playback" to 4,
                                    "haptics" to 4,
                                    "about" to 4,
                                    "feedback" to 4,
                                    "whats_new" to 4,
                                    "backup_restore" to 4
                                )
                            }"""

new_route_indices = """                            val routeIndices = remember(bottomNavTabs) {
                                val map = mutableMapOf<String, Int>()
                                var index = 0
                                for (tab in bottomNavTabs) {
                                    if (tab.isVisible) {
                                        map[tab.route] = index++
                                    }
                                }
                                val settingsIndex = 100
                                map["music_management"] = settingsIndex
                                map["account"] = settingsIndex
                                map["appearance"] = settingsIndex
                                map["playback"] = settingsIndex
                                map["haptics"] = settingsIndex
                                map["about"] = settingsIndex
                                map["feedback"] = settingsIndex
                                map["whats_new"] = settingsIndex
                                map["backup_restore"] = settingsIndex
                                map
                            }"""

content = content.replace(old_route_indices, new_route_indices)

with open('app/src/main/java/com/codetrio/overdrive/MainActivity.kt', 'w') as f:
    f.write(content)
