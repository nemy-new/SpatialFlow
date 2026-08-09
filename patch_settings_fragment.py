import re

with open('app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt', 'r') as f:
    content = f.read()

# Replace usage in AppearanceScreen
old_call = """                add { BottomNavCustomizeRow(bottomNavTabs, onBottomNavTabsChange) }"""
new_call = """                add { BottomNavCustomizeRow { navController.navigate("bottom_nav_customize") } }"""
content = content.replace(old_call, new_call)

# Replace the definition of BottomNavCustomizeRow
old_def_pattern = r"""@Composable\nprivate fun BottomNavCustomizeRow\(\n    tabs: List<com\.codetrio\.overdrive\.model\.BottomNavTab>,\n    onTabsChange: \(List<com\.codetrio\.overdrive\.model\.BottomNavTab>\) -> Unit\n\) \{.*?\n\}\n"""
new_def = """@Composable
private fun BottomNavCustomizeRow(onClick: () -> Unit) {
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
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
        modifier = Modifier.clickable { onClick() }
    )
}
"""

content = re.sub(old_def_pattern, new_def, content, flags=re.DOTALL)

with open('app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt', 'w') as f:
    f.write(content)
