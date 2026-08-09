import re

with open('app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt', 'r') as f:
    content = f.read()

# Update signature
old_sig = """private fun AppearanceScreen(
    navController: androidx.navigation.NavController,
    darkMode: Boolean,"""

new_sig = """private fun AppearanceScreen(
    navController: androidx.navigation.NavController,
    bottomNavTabs: List<com.codetrio.overdrive.model.BottomNavTab> = com.codetrio.overdrive.model.BottomNavTab.DEFAULT_TABS,
    onBottomNavTabsChange: (List<com.codetrio.overdrive.model.BottomNavTab>) -> Unit = {},
    darkMode: Boolean,"""

content = content.replace(old_sig, new_sig)

# Add row under Navigation Bar
old_nav_bar = """            SettingsHeader("Navigation Bar")
            SettingsGroupCard(buildList {
                add { HideNavOnScrollRow(hideNavOnScroll, onHideNavOnScrollChange) }"""

new_nav_bar = """            SettingsHeader("Navigation Bar")
            SettingsGroupCard(buildList {
                add { BottomNavCustomizeRow(bottomNavTabs, onBottomNavTabsChange) }
                add { HideNavOnScrollRow(hideNavOnScroll, onHideNavOnScrollChange) }"""

content = content.replace(old_nav_bar, new_nav_bar)

# Add component at the end of the file
bottom_nav_customize_code = """
@Composable
private fun BottomNavCustomizeRow(
    tabs: List<com.codetrio.overdrive.model.BottomNavTab>,
    onTabsChange: (List<com.codetrio.overdrive.model.BottomNavTab>) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = {
            Text(
                text = stringResource(R.string.setting_customize_bottom_nav),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = stringResource(R.string.setting_customize_bottom_nav_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Rounded.ViewList,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { showDialog = true }
    )

    if (showDialog) {
        val context = LocalContext.current
        var currentTabs by remember { mutableStateOf(tabs.ifEmpty { com.codetrio.overdrive.model.BottomNavTab.DEFAULT_TABS }) }
        
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.customize_bottom_nav_title)) },
            text = {
                LazyColumn {
                    itemsIndexed(currentTabs) { index, tab ->
                        val tabNameRes = when (tab.route) {
                            "explore" -> R.string.tab_explore
                            "search" -> R.string.tab_search
                            "library" -> R.string.tab_library
                            "effects" -> R.string.tab_effects
                            "settings" -> R.string.tab_settings
                            else -> R.string.tab_explore
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(tabNameRes),
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Visible toggle
                            Switch(
                                checked = tab.isVisible,
                                onCheckedChange = { isVisible ->
                                    val newTabs = currentTabs.toMutableList()
                                    newTabs[index] = tab.copy(isVisible = isVisible)
                                    // ensure at least one is visible
                                    if (newTabs.none { it.isVisible }) {
                                        Toast.makeText(context, R.string.error_need_at_least_one_tab, Toast.LENGTH_SHORT).show()
                                    } else {
                                        currentTabs = newTabs
                                        onTabsChange(newTabs)
                                    }
                                }
                            )
                            
                            // Move up
                            IconButton(
                                onClick = {
                                    if (index > 0) {
                                        val newTabs = currentTabs.toMutableList()
                                        java.util.Collections.swap(newTabs, index, index - 1)
                                        currentTabs = newTabs
                                        onTabsChange(newTabs)
                                    }
                                },
                                enabled = index > 0
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Up")
                            }
                            
                            // Move down
                            IconButton(
                                onClick = {
                                    if (index < currentTabs.size - 1) {
                                        val newTabs = currentTabs.toMutableList()
                                        java.util.Collections.swap(newTabs, index, index + 1)
                                        currentTabs = newTabs
                                        onTabsChange(newTabs)
                                    }
                                },
                                enabled = index < currentTabs.size - 1
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = "Down")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        )
    }
}
"""

content = content + bottom_nav_customize_code

with open('app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt', 'w') as f:
    f.write(content)
