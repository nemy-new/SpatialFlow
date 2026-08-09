import re

with open('temp_settings_patched.kt', 'r') as f:
    content = f.read()

# 1. 120.dp -> 240.dp
content = content.replace('.padding(bottom = 120.dp)', '.padding(bottom = 240.dp)')

# 2. Add bottom_nav_customize route
route_target = """    composableWithBlur(
        route = SettingsRoute.WhatsNew.route,
        enterTransition = enterAnim,
        exitTransition = exitAnim,
        popEnterTransition = popEnterAnim,
        popExitTransition = popExitAnim
    ) {
        WhatsNewScreen(navController = navController)
    }"""
route_replacement = route_target + """

    composableWithBlur(
        route = "bottom_nav_customize",
        enterTransition = enterAnim,
        exitTransition = exitAnim,
        popEnterTransition = popEnterAnim,
        popExitTransition = popExitAnim
    ) {
        val context = androidx.compose.ui.platform.LocalContext.current
        val activity = context.findActivity() as androidx.activity.ComponentActivity
        val playerSharedViewModel: com.codetrio.overdrive.viewmodel.PlayerSharedViewModel = androidx.lifecycle.viewmodel.compose.viewModel(activity)
        com.codetrio.overdrive.ui.settings.BottomNavCustomizeScreen(
            playerViewModel = playerSharedViewModel,
            onBack = { navController.popBackStack() }
        )
    }"""
content = content.replace(route_target, route_replacement)

# 3. Add settings variables to AppearanceScreen signature
app_sig_target = """    onForceHighRefreshRateChange: (Boolean) -> Unit,
    appLanguage: String,
    onAppLanguageChange: (String) -> Unit"""
app_sig_replacement = app_sig_target + """,
    floatingNavBar: Boolean,
    onFloatingNavBarChange: (Boolean) -> Unit,
    unifiedFloatingBar: Boolean,
    onUnifiedFloatingBarChange: (Boolean) -> Unit"""
content = content.replace(app_sig_target, app_sig_replacement)

# 4. Add items to AppearanceScreen Navigation Bar section
nav_bar_target = """            SettingsHeader("Navigation Bar")
            SettingsGroupCard(buildList {
                add { NavigationBlurRow(navigationBlur, onNavigationBlurChange) }"""
nav_bar_replacement = """            SettingsHeader("Navigation Bar")
            SettingsGroupCard(buildList {
                add { BottomNavCustomizeRow { navController.navigate("bottom_nav_customize") } }
                add { FloatingNavBarRow(floatingNavBar, onFloatingNavBarChange) }
                if (floatingNavBar) {
                    add { UnifiedFloatingBarRow(unifiedFloatingBar, onUnifiedFloatingBarChange) }
                }
                add { NavigationBlurRow(navigationBlur, onNavigationBlurChange) }"""
content = content.replace(nav_bar_target, nav_bar_replacement)

# 5. Add BottomNavCustomizeRow, FloatingNavBarRow, UnifiedFloatingBarRow composables
composables_add = """
@Composable
private fun BottomNavCustomizeRow(onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.setting_customize_bottom_nav), style = MaterialTheme.typography.bodyLarge) },
        supportingContent = { Text(stringResource(R.string.setting_customize_bottom_nav_desc), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingContent = { Icon(androidx.compose.material.icons.Icons.Rounded.ViewList, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp)) },
        trailingContent = { Icon(androidx.compose.material.icons.Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
private fun FloatingNavBarRow(floating: Boolean, onSelect: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.text_floating_nav_bar), style = MaterialTheme.typography.bodyLarge) },
        supportingContent = { Text(stringResource(R.string.text_floating_nav_bar_sub), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingContent = { Switch(checked = floating, onCheckedChange = onSelect) },
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
        modifier = Modifier.clickable { onSelect(!floating) }
    )
}

@Composable
private fun UnifiedFloatingBarRow(unified: Boolean, onSelect: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.text_unified_floating_bar), style = MaterialTheme.typography.bodyLarge) },
        supportingContent = { Text(stringResource(R.string.text_unified_floating_bar_sub), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingContent = { Switch(checked = unified, onCheckedChange = onSelect) },
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
        modifier = Modifier.clickable { onSelect(!unified) }
    )
}
"""
content = content + composables_add

# 6. Add ShowVolumeSliderRow to PlaybackScreen
playback_sig_target = """    ignoreShortAudioDuration: Float,
    onIgnoreShortAudioDurationChange: (Float) -> Unit"""
playback_sig_replacement = playback_sig_target + """,
    showVolumeSlider: Boolean,
    onShowVolumeSliderChange: (Boolean) -> Unit"""
content = content.replace(playback_sig_target, playback_sig_replacement)

playback_card_target = """                add { IgnoreShortAudioRow(ignoreShortAudio, onIgnoreShortAudioChange) }
                if (ignoreShortAudio) {
                    add { IgnoreShortAudioDurationRow(ignoreShortAudioDuration, onIgnoreShortAudioDurationChange) }
                }
            })"""
playback_card_replacement = playback_card_target + """
            Spacer(modifier = Modifier.height(16.dp))
            SettingsHeader("Controls")
            SettingsGroupCard(buildList {
                add { ShowVolumeSliderRow(showVolumeSlider, onShowVolumeSliderChange) }
            })"""
content = content.replace(playback_card_target, playback_card_replacement)

volume_composable = """
@Composable
private fun ShowVolumeSliderRow(checked: Boolean, onToggle: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.text_volume_bar), style = MaterialTheme.typography.bodyLarge) },
        supportingContent = { Text(stringResource(R.string.text_show_m3e_expressive_volume_slider_below_playback_controls), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onToggle) },
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
        modifier = Modifier.clickable { onToggle(!checked) }
    )
}
"""
content = content + volume_composable

# 7. Add state variables in SettingsMainScreen
collect_target = """    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val onAppLanguageChange: (String) -> Unit = { viewModel.setLanguage(it) }"""
collect_replacement = collect_target + """
    val floatingNavBar by viewModel.floatingNavBar.collectAsStateWithLifecycle()
    val onFloatingNavBarChange: (Boolean) -> Unit = { viewModel.setFloatingNavBar(it) }
    val unifiedFloatingBar by viewModel.unifiedFloatingBar.collectAsStateWithLifecycle()
    val onUnifiedFloatingBarChange: (Boolean) -> Unit = { viewModel.setUnifiedFloatingBar(it) }
    val showVolumeSlider by viewModel.showVolumeSlider.collectAsStateWithLifecycle()
    val onShowVolumeSliderChange: (Boolean) -> Unit = { viewModel.setShowVolumeSlider(it) }"""
content = content.replace(collect_target, collect_replacement)

call_appearance_target = """            onForceHighRefreshRateChange = onForceHighRefreshRateChange,
            appLanguage = appLanguage,
            onAppLanguageChange = onAppLanguageChange
        )"""
call_appearance_replacement = """            onForceHighRefreshRateChange = onForceHighRefreshRateChange,
            appLanguage = appLanguage,
            onAppLanguageChange = onAppLanguageChange,
            floatingNavBar = floatingNavBar,
            onFloatingNavBarChange = onFloatingNavBarChange,
            unifiedFloatingBar = unifiedFloatingBar,
            onUnifiedFloatingBarChange = onUnifiedFloatingBarChange
        )"""
content = content.replace(call_appearance_target, call_appearance_replacement)

call_playback_target = """            onIgnoreShortAudioDurationChange = onIgnoreShortAudioDurationChange
        )"""
call_playback_replacement = """            onIgnoreShortAudioDurationChange = onIgnoreShortAudioDurationChange,
            showVolumeSlider = showVolumeSlider,
            onShowVolumeSliderChange = onShowVolumeSliderChange
        )"""
content = content.replace(call_playback_target, call_playback_replacement)

# 8. Add MutableStateFlows in SettingsViewModel
viewmodel_target = """    private val _appLanguage = MutableStateFlow(prefs.getString("app_language", "system") ?: "system")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    fun setLanguage(langCode: String) {
        _appLanguage.value = langCode
        prefs.edit { putString("app_language", langCode) }
        // Let MainActivity handle locale updates dynamically.
    }"""
viewmodel_replacement = viewmodel_target + """
    private val _floatingNavBar = MutableStateFlow(prefs.getBoolean("floating_nav_bar", false))
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
    }

    private val _showVolumeSlider = MutableStateFlow(prefs.getBoolean("show_volume_slider", true))
    val showVolumeSlider: StateFlow<Boolean> = _showVolumeSlider.asStateFlow()
    fun setShowVolumeSlider(show: Boolean) {
        _showVolumeSlider.value = show
        prefs.edit {putBoolean("show_volume_slider", show)}
    }"""
content = content.replace(viewmodel_target, viewmodel_replacement)

# Import ViewList icon
import_target = """import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.ViewCarousel"""
import_replacement = """import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.material.icons.rounded.ViewList"""
content = content.replace(import_target, import_replacement)

with open('app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt', 'w') as f:
    f.write(content)
print("SettingsFragment generated successfully!")
