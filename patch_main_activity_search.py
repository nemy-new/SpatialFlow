import re

with open('app/src/main/java/com/codetrio/overdrive/MainActivity.kt', 'r') as f:
    content = f.read()

# Fix items list in both NavigationRail and NavigationBar
old_items = """                            val items = listOf(
                                Triple("explore", stringResource(id = R.string.tab_explore), R.drawable.ic_explore),
                                Triple("library", stringResource(id = R.string.tab_library), R.drawable.ic_library_music),
                                Triple("effects", stringResource(id = R.string.tab_effects), R.drawable.ic_equalizer),
                                Triple("settings", stringResource(id = R.string.tab_settings), R.drawable.ic_settings)
                            )"""

new_items = """                            val items = listOf(
                                Triple("explore", stringResource(id = R.string.tab_explore), R.drawable.ic_explore),
                                Triple("search", stringResource(id = R.string.tab_search), R.drawable.ic_search),
                                Triple("library", stringResource(id = R.string.tab_library), R.drawable.ic_library_music),
                                Triple("effects", stringResource(id = R.string.tab_effects), R.drawable.ic_equalizer),
                                Triple("settings", stringResource(id = R.string.tab_settings), R.drawable.ic_settings)
                            )"""

content = content.replace(old_items, new_items)

# Update routeIndices
old_route_indices = """                            val routeIndices = remember {
                                mapOf(
                                    "explore" to 0,
                                    "library" to 1,
                                    "effects" to 2,
                                    "settings" to 3,
                                    "onboarding" to -1
                                )
                            }"""

new_route_indices = """                            val routeIndices = remember {
                                mapOf(
                                    "explore" to 0,
                                    "search" to 1,
                                    "library" to 2,
                                    "effects" to 3,
                                    "settings" to 4,
                                    "onboarding" to -1
                                )
                            }"""

content = content.replace(old_route_indices, new_route_indices)

with open('app/src/main/java/com/codetrio/overdrive/MainActivity.kt', 'w') as f:
    f.write(content)

