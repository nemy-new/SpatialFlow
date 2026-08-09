import re

with open('app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt', 'r') as f:
    content = f.read()

# 1. Appearance route fixes
# Remove the faulty ones added to SettingsRoute.WhatsNew if any? Wait, where were they added?
# In my previous script, I did:
# graph_collect_target = """        val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()"""
# Let's find exactly where it is.
# They are at line 1208. Let's just do a clean replacement using regex.

import re

# Find the AppearanceScreen block in settingsGraph
appearance_block_pattern = r"(val appLanguage by viewModel\.appLanguage\.collectAsStateWithLifecycle\(\)\n)(        val floatingNavBar by viewModel\.floatingNavBar\.collectAsStateWithLifecycle\(\)\n        val unifiedFloatingBar by viewModel\.unifiedFloatingBar\.collectAsStateWithLifecycle\(\)\n        val showVolumeSlider by viewModel\.showVolumeSlider\.collectAsStateWithLifecycle\(\)\n)?(\n        AppearanceScreen\()"

def repl_app(m):
    return m.group(1) + """        val floatingNavBar by viewModel.floatingNavBar.collectAsStateWithLifecycle()
        val unifiedFloatingBar by viewModel.unifiedFloatingBar.collectAsStateWithLifecycle()
""" + m.group(3)

content = re.sub(appearance_block_pattern, repl_app, content)

# Find the PlaybackScreen block
playback_block_pattern = r"(val targetLufs by viewModel\.targetLufs\.collectAsStateWithLifecycle\(\)\n)(\s*PlaybackScreen\()"
def repl_pb(m):
    return m.group(1) + "        val showVolumeSlider by viewModel.showVolumeSlider.collectAsStateWithLifecycle()\n" + m.group(2)

content = re.sub(playback_block_pattern, repl_pb, content)

# Ensure MusicManagementScreen (line 1114) doesn't have showVolumeSlider added to it!
# Wait! line 1114 in the error was: "SettingsFragment.kt:1114:13 No value passed for parameter 'showVolumeSlider'."
# 1114 was actually `PlaybackScreen(navController = navController, ...)`?
# Let's check line 1114 in SettingsFragment.kt
# Actually, I'll just write it and let the compiler tell me if anything is wrong.
with open('app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt', 'w') as f:
    f.write(content)

print("Fixed")
