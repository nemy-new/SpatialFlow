import re

with open('app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt', 'r') as f:
    content = f.read()

# Fix VolumeNormalizationRow signature
vn_sig_target = """private fun VolumeNormalizationRow(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    targetLufs: Float,
    onTargetLufsChange: (Float) -> Unit,
    showVolumeSlider: Boolean,
    onShowVolumeSliderChange: (Boolean) -> Unit
) {"""
vn_sig_replacement = """private fun VolumeNormalizationRow(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    targetLufs: Float,
    onTargetLufsChange: (Float) -> Unit
) {"""

content = content.replace(vn_sig_target, vn_sig_replacement)

with open('app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt', 'w') as f:
    f.write(content)

print("Fixed VolumeNormalizationRow signature")
