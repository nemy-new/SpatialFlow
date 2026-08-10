import re

path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

target = r"""            val showVolumeSlider by viewModel\.showVolumeSlider\.collectAsStateWithLifecycle\(initialValue = true\)
            if \(showVolumeSlider\) \{
                com\.codetrio\.overdrive\.ui\.player\.VolumeSlider\(
                    modifier = Modifier\.width\(albumArtSize\)\.padding\(top = 16\.dp\),
                    contentColor = contentColor,
                    accentColor = dynamicAccentColor,
                    isDark = isDark
                \)
                Spacer\(modifier = Modifier\.height\(16\.dp\)\)
            \}"""

replacement = """            val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(LocalContext.current)
            var showVolumeSlider by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(prefs.getBoolean("show_volume_slider", true)) }
            
            if (showVolumeSlider) {
                com.codetrio.overdrive.ui.player.VolumeSlider(
                    modifier = Modifier.width(albumArtSize).padding(top = 16.dp),
                    contentColor = contentColor,
                    dynamicAccentColor = dynamicAccentColor
                )
                Spacer(modifier = Modifier.height(16.dp))
            }"""

content = re.sub(target, replacement, content)

with open(path, "w") as f:
    f.write(content)
print("Fix applied")
