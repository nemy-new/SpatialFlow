import re

def replace_in_file(filepath, replacements):
    with open(filepath, 'r') as f:
        content = f.read()
    
    for old, new in replacements:
        content = content.replace(old, new)
        
    with open(filepath, 'w') as f:
        f.write(content)

replace_in_file("app/src/main/java/com/codetrio/overdrive/ui/library/LibraryScreen.kt", [
    ('Text("Play All")', 'Text(stringResource(R.string.action_play_all))')
])

replace_in_file("app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt", [
    ('Text("Debug Toasts")', 'Text(stringResource(R.string.pref_debug_toasts))'),
    ('Text("Show diagnostic toast messages")', 'Text(stringResource(R.string.pref_debug_toasts_desc))'),
    ('Text("MV Background Behavior")', 'Text(stringResource(R.string.pref_mv_behavior))'),
    ('Text("Picture-in-Picture")', 'Text(stringResource(R.string.pref_mv_pip))'),
    ('Text("Background Audio")', 'Text(stringResource(R.string.pref_mv_bg_audio))')
])

