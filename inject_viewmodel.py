import re

with open('app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt', 'r') as f:
    content = f.read()

target = """    fun setLanguage(langCode: String) {
        _appLanguage.value = langCode
        prefs.edit { putString("app_language", langCode) }
        val localeList = if (langCode == "system" || langCode.isEmpty()) {
            androidx.core.os.LocaleListCompat.getEmptyLocaleList()
        } else {
            androidx.core.os.LocaleListCompat.forLanguageTags(langCode)
        }
        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(localeList)
    }"""
    
replacement = target + """

    private val _floatingNavBar = kotlinx.coroutines.flow.MutableStateFlow(prefs.getBoolean("floating_nav_bar", false))
    val floatingNavBar: kotlinx.coroutines.flow.StateFlow<Boolean> = _floatingNavBar.asStateFlow()
    fun setFloatingNavBar(floating: Boolean) {
        _floatingNavBar.value = floating
        prefs.edit {putBoolean("floating_nav_bar", floating)}
    }
    
    private val _unifiedFloatingBar = kotlinx.coroutines.flow.MutableStateFlow(prefs.getBoolean("unified_floating_bar", false))
    val unifiedFloatingBar: kotlinx.coroutines.flow.StateFlow<Boolean> = _unifiedFloatingBar.asStateFlow()
    fun setUnifiedFloatingBar(unified: Boolean) {
        _unifiedFloatingBar.value = unified
        prefs.edit {putBoolean("unified_floating_bar", unified)}
    }

    private val _showVolumeSlider = kotlinx.coroutines.flow.MutableStateFlow(prefs.getBoolean("show_volume_slider", true))
    val showVolumeSlider: kotlinx.coroutines.flow.StateFlow<Boolean> = _showVolumeSlider.asStateFlow()
    fun setShowVolumeSlider(show: Boolean) {
        _showVolumeSlider.value = show
        prefs.edit {putBoolean("show_volume_slider", show)}
    }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt', 'w') as f:
    f.write(content)
print("Injected viewmodel flows")
