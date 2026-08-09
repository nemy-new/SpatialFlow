import re

with open('app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt', 'r') as f:
    content = f.read()

# 1. Remove showVolumeSlider from MusicManagementScreen parameters
mms_sig_target = """    onIgnoreShortAudioDurationChange: (Float) -> Unit,
    showVolumeSlider: Boolean,
    onShowVolumeSliderChange: (Boolean) -> Unit,
    songCacheMaxSize: Int"""
mms_sig_replacement = """    onIgnoreShortAudioDurationChange: (Float) -> Unit,
    songCacheMaxSize: Int"""
content = content.replace(mms_sig_target, mms_sig_replacement)

# 2. Remove showVolumeSlider from MusicManagementScreen call
mms_call_target = """            onIgnoreShortAudioDurationChange = { viewModel.setIgnoreShortAudioDuration(it) },
            showVolumeSlider = showVolumeSlider,
            onShowVolumeSliderChange = { viewModel.setShowVolumeSlider(it) },
            hiddenFolders = hiddenFolders"""
mms_call_replacement = """            onIgnoreShortAudioDurationChange = { viewModel.setIgnoreShortAudioDuration(it) },
            hiddenFolders = hiddenFolders"""
content = content.replace(mms_call_target, mms_call_replacement)

# 3. Move the ShowVolumeSliderRow from MusicManagementScreen body to PlaybackScreen body
# Wait, MusicManagementScreen body currently has:
mms_body_target = """                if (ignoreShortAudio) {
                    add { IgnoreShortAudioDurationRow(ignoreShortAudioDuration, onIgnoreShortAudioDurationChange) }
                }
            })
            Spacer(modifier = Modifier.height(16.dp))
            SettingsHeader("Controls")
            SettingsGroupCard(buildList {
                add { ShowVolumeSliderRow(showVolumeSlider, onShowVolumeSliderChange) }
            })"""
mms_body_replacement = """                if (ignoreShortAudio) {
                    add { IgnoreShortAudioDurationRow(ignoreShortAudioDuration, onIgnoreShortAudioDurationChange) }
                }
            })"""
content = content.replace(mms_body_target, mms_body_replacement)


# 4. Add showVolumeSlider to PlaybackScreen parameters
pb_sig_target = """    targetLufs: Float,
    onTargetLufsChange: (Float) -> Unit
) {"""
pb_sig_replacement = """    targetLufs: Float,
    onTargetLufsChange: (Float) -> Unit,
    showVolumeSlider: Boolean,
    onShowVolumeSliderChange: (Boolean) -> Unit
) {"""
content = content.replace(pb_sig_target, pb_sig_replacement)

# 5. Add showVolumeSlider to PlaybackScreen call
pb_call_target = """            targetLufs = targetLufs,
            onTargetLufsChange = { viewModel.setTargetLufs(it) }
        )
    }"""
pb_call_replacement = """            targetLufs = targetLufs,
            onTargetLufsChange = { viewModel.setTargetLufs(it) },
            showVolumeSlider = showVolumeSlider,
            onShowVolumeSliderChange = { viewModel.setShowVolumeSlider(it) }
        )
    }"""
content = content.replace(pb_call_target, pb_call_replacement)

# 6. Add ShowVolumeSliderRow to PlaybackScreen body
pb_body_target = """                { VolumeNormalizationRow(volumeNormalizationEnabled, onVolumeNormalizationChange, targetLufs, onTargetLufsChange) }
            })"""
pb_body_replacement = """                { VolumeNormalizationRow(volumeNormalizationEnabled, onVolumeNormalizationChange, targetLufs, onTargetLufsChange) }
            })
            Spacer(modifier = Modifier.height(16.dp))
            SettingsHeader("Controls")
            SettingsGroupCard(buildList {
                add { ShowVolumeSliderRow(showVolumeSlider, onShowVolumeSliderChange) }
            })"""
content = content.replace(pb_body_target, pb_body_replacement)

# Also fix the Unresolved reference 'List'
list_target = """import androidx.compose.material.icons.automirrored.rounded.List"""
list_replacement = """import androidx.compose.material.icons.automirrored.rounded.List"""
if list_target not in content:
    content = content.replace("import androidx.compose.material.icons.rounded.ViewCarousel", "import androidx.compose.material.icons.rounded.ViewCarousel\nimport androidx.compose.material.icons.automirrored.rounded.List")

with open('app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt', 'w') as f:
    f.write(content)

print("Fixed again")
