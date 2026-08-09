import os
import re

file_path = "app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt"
with open(file_path, "r") as f:
    content = f.read()

viewModel_code = """
    // ── Floating Nav Bar ──────────────────────────────────────────────────
    private val _floatingNavBar = MutableStateFlow(prefs.getBoolean("floating_nav_bar", false))
    val floatingNavBar: StateFlow<Boolean> = _floatingNavBar.asStateFlow()

    fun setFloatingNavBar(floating: Boolean) {
        _floatingNavBar.value = floating
        prefs.edit {putBoolean("floating_nav_bar", floating)}
    }
"""
content = re.sub(
    r'(    // ── Hide Nav Labels ──────────────────────────────────────────────────)',
    viewModel_code + r'\n\1',
    content
)

content = re.sub(
    r'(val hideNavLabels by viewModel\.hideNavLabels\.collectAsStateWithLifecycle\(\))',
    r'\1\n        val floatingNavBar by viewModel.floatingNavBar.collectAsStateWithLifecycle()',
    content
)

content = re.sub(
    r'(hideNavLabels = hideNavLabels,)',
    r'\1\n                            floatingNavBar = floatingNavBar,\n                            onFloatingNavBarChange = { viewModel.setFloatingNavBar(it) },',
    content
)

content = re.sub(
    r'(hideNavLabels: Boolean,\n    onHideNavLabelsChange: \(Boolean\) -> Unit,)',
    r'\1\n    floatingNavBar: Boolean,\n    onFloatingNavBarChange: (Boolean) -> Unit,',
    content
)

content = re.sub(
    r'(add \{ HideNavLabelsRow\(hideNavLabels, onHideNavLabelsChange\) \})',
    r'\1\n                add { FloatingNavBarRow(floatingNavBar, onFloatingNavBarChange) }',
    content
)

composable_code = """
@Composable
private fun FloatingNavBarRow(floating: Boolean, onSelect: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_floating_nav_bar), style = MaterialTheme.typography.bodyLarge) },
        supportingContent = { Text(androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_floating_nav_bar_sub), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingContent = { Switch(checked = floating, onCheckedChange = onSelect) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { onSelect(!floating) }
    )
}
"""
content = content + composable_code
with open(file_path, "w") as f:
    f.write(content)

file_path = "app/src/main/java/com/codetrio/overdrive/MainActivity.kt"
with open(file_path, "r") as f:
    content = f.read()

content = re.sub(
    r'(val hideNavLabels by prefs\.observeKey\("hide_nav_labels", false\))',
    r'\1\n                        val floatingNavBar by prefs.observeKey("floating_nav_bar", false)',
    content
)

old_nav_modifier = """                                .graphicsLayer {
                                    val currentBase = animatedBaseTranslationState.value
                                    val currentFraction = playerExpansionFractionState.value
                                    val translationY = (currentBase + (totalSlideDistPx * currentFraction)).coerceAtMost(totalSlideDistPx)
                                    this.translationY = translationY
                                    this.alpha = if (translationY >= totalSlideDistPx - 1f) 0f else 1f
                                }
                                .height(navBarHeight + bottomPadding)"""

new_nav_modifier = """                                .graphicsLayer {
                                    val currentBase = animatedBaseTranslationState.value
                                    val currentFraction = playerExpansionFractionState.value
                                    val translationY = (currentBase + (totalSlideDistPx * currentFraction)).coerceAtMost(totalSlideDistPx)
                                    this.translationY = translationY
                                    this.alpha = if (translationY >= totalSlideDistPx - 1f) 0f else 1f
                                }
                                .then(
                                    if (floatingNavBar) {
                                        Modifier
                                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp + bottomPadding)
                                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(32.dp))
                                            .height(navBarHeight)
                                    } else {
                                        Modifier.height(navBarHeight + bottomPadding)
                                    }
                                )"""

content = content.replace(old_nav_modifier, new_nav_modifier)
with open(file_path, "w") as f:
    f.write(content)

file_path = "app/src/main/res/values/strings.xml"
with open(file_path, "r") as f:
    content = f.read()
content = content.replace("</resources>", '    <string name="text_floating_nav_bar">Floating Navigation Bar</string>\n    <string name="text_floating_nav_bar_sub">Displays the navigation bar in a floating style</string>\n</resources>')
with open(file_path, "w") as f:
    f.write(content)

file_path = "app/src/main/res/values-ja/strings.xml"
with open(file_path, "r") as f:
    content = f.read()
content = content.replace("</resources>", '    <string name="text_floating_nav_bar">フローティングナビゲーションバー</string>\n    <string name="text_floating_nav_bar_sub">ボトムバーをフローティング式で表示します</string>\n</resources>')
with open(file_path, "w") as f:
    f.write(content)

print("Done")
