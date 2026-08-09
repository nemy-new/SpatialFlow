import re

file_path = "app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt"
with open(file_path, "r") as f:
    content = f.read()

# 1. Fix bottom padding in all screens
content = content.replace('.padding(bottom = 240.dp)', '.padding(bottom = 400.dp)')

# 2. Add Floating Navigation and Unified Floating Bar to AppearanceScreen
appearance_target = """            SettingsHeader("Navigation Bar")
            SettingsGroupCard(buildList {
                add { HideNavOnScrollRow(hideNavOnScroll, onHideNavOnScrollChange) }
                add { HideNavLabelsRow(hideNavLabels, onHideNavLabelsChange) }
                add { DynamicNavStyleRow(dynamicNavStyle, onDynamicNavStyleChange) }
            })"""

appearance_replacement = """            SettingsHeader("Navigation Bar")
            SettingsGroupCard(buildList {
                add { HideNavOnScrollRow(hideNavOnScroll, onHideNavOnScrollChange) }
                add { HideNavLabelsRow(hideNavLabels, onHideNavLabelsChange) }
                add { DynamicNavStyleRow(dynamicNavStyle, onDynamicNavStyleChange) }
                add { FloatingNavBarRow(floatingNavBar, onFloatingNavBarChange) }
                if (floatingNavBar) {
                    add { UnifiedFloatingBarRow(unifiedFloatingBar, onUnifiedFloatingBarChange) }
                }
            })"""
content = content.replace(appearance_target, appearance_replacement)

# 3. Add ShowVolumeSliderRow to PlaybackScreen
playback_target = """            SettingsHeader("Audio Focus")
            SettingsGroupCard(listOf(
                { AudioFocusRow(audioFocus, onAudioFocusToggle) }
            ))"""

playback_replacement = """            SettingsHeader("Audio Focus")
            SettingsGroupCard(listOf(
                { AudioFocusRow(audioFocus, onAudioFocusToggle) }
            ))

            SettingsHeader(stringResource(R.string.text_volume_bar))
            SettingsGroupCard(listOf(
                { ShowVolumeSliderRow(showVolumeSlider, onShowVolumeSliderChange) }
            ))"""
content = content.replace(playback_target, playback_replacement)

# 4. Add DebugToasts setting to FeedbackScreen
feedback_target = """            SettingsHeader("Debug Information")
            SettingsGroupCard(
                items = listOf("""

feedback_replacement = """            val prefs = context.getSharedPreferences("AppSettings", android.content.Context.MODE_PRIVATE)
            var debugToastsEnabled by remember { mutableStateOf(prefs.getBoolean("debug_toasts_enabled", false)) }

            SettingsHeader("Debug Information")
            SettingsGroupCard(
                items = listOf(
                    {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.pref_debug_toasts), style = MaterialTheme.typography.bodyLarge) },
                            supportingContent = { Text(stringResource(R.string.pref_debug_toasts_desc), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            trailingContent = { Switch(checked = debugToastsEnabled, onCheckedChange = { 
                                debugToastsEnabled = it
                                prefs.edit().putBoolean("debug_toasts_enabled", it).apply()
                            }) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable { 
                                debugToastsEnabled = !debugToastsEnabled
                                prefs.edit().putBoolean("debug_toasts_enabled", debugToastsEnabled).apply()
                            }
                        )
                    },"""
content = content.replace(feedback_target, feedback_replacement)

with open(file_path, "w") as f:
    f.write(content)
print("Settings patched successfully.")
