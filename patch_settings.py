import re

file_path = "app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt"
with open(file_path, "r") as f:
    content = f.read()

# 1. Add state flow
state_flow_target = """    private val _floatingNavBar = MutableStateFlow(prefs.getBoolean("floating_nav_bar", false))
    val floatingNavBar: StateFlow<Boolean> = _floatingNavBar.asStateFlow()

    fun setFloatingNavBar(floating: Boolean) {
        _floatingNavBar.value = floating
        prefs.edit {putBoolean("floating_nav_bar", floating)}
    }"""

state_flow_replacement = """    private val _floatingNavBar = MutableStateFlow(prefs.getBoolean("floating_nav_bar", false))
    val floatingNavBar: StateFlow<Boolean> = _floatingNavBar.asStateFlow()

    fun setFloatingNavBar(floating: Boolean) {
        _floatingNavBar.value = floating
        prefs.edit {putBoolean("floating_nav_bar", floating)}
    }

    private val _unifiedFloatingBar = MutableStateFlow(prefs.getBoolean("unified_floating_bar", false))
    val unifiedFloatingBar: StateFlow<Boolean> = _unifiedFloatingBar.asStateFlow()

    fun setUnifiedFloatingBar(unified: Boolean) {
        _unifiedFloatingBar.value = unified
        prefs.edit {putBoolean("unified_floating_bar", unified)}
    }"""

if state_flow_target in content:
    content = content.replace(state_flow_target, state_flow_replacement)
else:
    print("Failed to find state_flow_target")

# 2. Add UnifiedFloatingBarRow
composable_target = """@Composable
private fun FloatingNavBarRow(floating: Boolean, onSelect: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_floating_nav_bar), style = MaterialTheme.typography.bodyLarge) },
        supportingContent = { Text(androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_floating_nav_bar_sub), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingContent = { Switch(checked = floating, onCheckedChange = onSelect) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { onSelect(!floating) }
    )
}"""

composable_replacement = """@Composable
private fun FloatingNavBarRow(floating: Boolean, onSelect: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_floating_nav_bar), style = MaterialTheme.typography.bodyLarge) },
        supportingContent = { Text(androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_floating_nav_bar_sub), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingContent = { Switch(checked = floating, onCheckedChange = onSelect) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { onSelect(!floating) }
    )
}

@Composable
private fun UnifiedFloatingBarRow(unified: Boolean, onSelect: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_unified_floating_bar), style = MaterialTheme.typography.bodyLarge) },
        supportingContent = { Text(androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_unified_floating_bar_sub), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingContent = { Switch(checked = unified, onCheckedChange = onSelect) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { onSelect(!unified) }
    )
}"""

if composable_target in content:
    content = content.replace(composable_target, composable_replacement)
else:
    print("Failed to find composable_target")

# 3. Add to list
list_target = """                add { FloatingNavBarRow(floatingNavBar, onFloatingNavBarChange) }"""
list_replacement = """                add { FloatingNavBarRow(floatingNavBar, onFloatingNavBarChange) }
                if (floatingNavBar) {
                    add { UnifiedFloatingBarRow(unifiedFloatingBar, onUnifiedFloatingBarChange) }
                }"""

if list_target in content:
    content = content.replace(list_target, list_replacement)
else:
    print("Failed to find list_target")

# 4. Add collectAsState and handlers
collect_target = """    val floatingNavBar by viewModel.floatingNavBar.collectAsStateWithLifecycle()
    val onFloatingNavBarChange: (Boolean) -> Unit = { viewModel.setFloatingNavBar(it) }"""

collect_replacement = """    val floatingNavBar by viewModel.floatingNavBar.collectAsStateWithLifecycle()
    val onFloatingNavBarChange: (Boolean) -> Unit = { viewModel.setFloatingNavBar(it) }

    val unifiedFloatingBar by viewModel.unifiedFloatingBar.collectAsStateWithLifecycle()
    val onUnifiedFloatingBarChange: (Boolean) -> Unit = { viewModel.setUnifiedFloatingBar(it) }"""

if collect_target in content:
    content = content.replace(collect_target, collect_replacement)
else:
    print("Failed to find collect_target")

with open(file_path, "w") as f:
    f.write(content)

print("Done")
