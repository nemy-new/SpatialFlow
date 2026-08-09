import re

with open('app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt', 'r') as f:
    content = f.read()

# Fix ViewList import
content = content.replace("androidx.compose.material.icons.Icons.Rounded.ViewList", "androidx.compose.material.icons.automirrored.rounded.List")

# The collect targets were added to SettingsMainScreen, but actually they should be in settingsGraph
collect_target = """    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val onAppLanguageChange: (String) -> Unit = { viewModel.setLanguage(it) }

    val floatingNavBar by viewModel.floatingNavBar.collectAsStateWithLifecycle()
    val onFloatingNavBarChange: (Boolean) -> Unit = { viewModel.setFloatingNavBar(it) }
    val unifiedFloatingBar by viewModel.unifiedFloatingBar.collectAsStateWithLifecycle()
    val onUnifiedFloatingBarChange: (Boolean) -> Unit = { viewModel.setUnifiedFloatingBar(it) }
    val showVolumeSlider by viewModel.showVolumeSlider.collectAsStateWithLifecycle()
    val onShowVolumeSliderChange: (Boolean) -> Unit = { viewModel.setShowVolumeSlider(it) }"""

# Revert from SettingsMainScreen
content = content.replace(collect_target, """    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val onAppLanguageChange: (String) -> Unit = { viewModel.setLanguage(it) }""")

# And add to settingsGraph! Let's find where appLanguage is collected in settingsGraph.
graph_collect_target = """        val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()"""
graph_collect_replacement = graph_collect_target + """
        val floatingNavBar by viewModel.floatingNavBar.collectAsStateWithLifecycle()
        val unifiedFloatingBar by viewModel.unifiedFloatingBar.collectAsStateWithLifecycle()
        val showVolumeSlider by viewModel.showVolumeSlider.collectAsStateWithLifecycle()"""
content = content.replace(graph_collect_target, graph_collect_replacement)

# Fix AppearanceScreen call in settingsGraph
appearance_call_target = """            onForceHighRefreshRateChange = { viewModel.setForceHighRefreshRate(it) },
            appLanguage = appLanguage,
            onAppLanguageChange = { viewModel.setLanguage(it) }
        )"""
appearance_call_replacement = appearance_call_target.replace("onAppLanguageChange = { viewModel.setLanguage(it) }", """onAppLanguageChange = { viewModel.setLanguage(it) },
            floatingNavBar = floatingNavBar,
            onFloatingNavBarChange = { viewModel.setFloatingNavBar(it) },
            unifiedFloatingBar = unifiedFloatingBar,
            onUnifiedFloatingBarChange = { viewModel.setUnifiedFloatingBar(it) }""")
content = content.replace(appearance_call_target, appearance_call_replacement)

# Fix PlaybackScreen call in settingsGraph
playback_call_target = """            onIgnoreShortAudioDurationChange = { viewModel.setIgnoreShortAudioDuration(it) }
        )"""
playback_call_replacement = """            onIgnoreShortAudioDurationChange = { viewModel.setIgnoreShortAudioDuration(it) },
            showVolumeSlider = showVolumeSlider,
            onShowVolumeSliderChange = { viewModel.setShowVolumeSlider(it) }
        )"""
content = content.replace(playback_call_target, playback_call_replacement)


with open('app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt', 'w') as f:
    f.write(content)
print("Done")
