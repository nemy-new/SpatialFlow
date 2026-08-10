import re

path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

target = r"""            val prefs = androidx\.preference\.PreferenceManager\.getDefaultSharedPreferences\(LocalContext\.current\)
            var showVolumeSlider by androidx\.compose\.runtime\.remember \{ androidx\.compose\.runtime\.mutableStateOf\(prefs\.getBoolean\("show_volume_slider", true\)\) \}
            
            if \(showVolumeSlider\) \{
                com\.codetrio\.overdrive\.ui\.player\.VolumeSlider\(
                    modifier = Modifier\.width\(albumArtSize\)\.padding\(top = 16\.dp\),
                    contentColor = contentColor,
                    dynamicAccentColor = dynamicAccentColor
                \)
                Spacer\(modifier = Modifier\.height\(16\.dp\)\)
            \}"""

replacement = """            val appPrefs = LocalContext.current.getSharedPreferences("AppSettings", android.content.Context.MODE_PRIVATE)
            var showVolumeSlider by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(appPrefs.getBoolean("show_volume_slider", true)) }
            
            androidx.compose.runtime.DisposableEffect(appPrefs) {
                val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                    if (key == "show_volume_slider") {
                        showVolumeSlider = sharedPreferences.getBoolean(key, true)
                    }
                }
                appPrefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose {
                    appPrefs.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }

            if (showVolumeSlider) {
                com.codetrio.overdrive.ui.player.VolumeSlider(
                    modifier = Modifier.width(albumArtSize).padding(top = 16.dp),
                    contentColor = contentColor,
                    dynamicAccentColor = dynamicAccentColor
                )
            } else {
                Spacer(modifier = Modifier.width(albumArtSize).padding(top = 16.dp).height(24.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))"""

content = re.sub(target, replacement, content)

with open(path, "w") as f:
    f.write(content)
print("Updated FullPlayer.kt to listen to show_volume_slider from AppSettings and maintain layout height")
