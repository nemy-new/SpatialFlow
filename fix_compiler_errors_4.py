import re

# Fix FullPlayer.kt
path = "./app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

# Fix FullScreenLyricsOverlay arguments
content = content.replace("syncOffsetMs = uiState.syncOffsetMs,", "syncOffsetMs = 0L,")
content = content.replace("onSyncOffsetChange = { viewModel.updateSyncOffset(it) },", "onSyncOffsetChange = {},")
content = content.replace("playbackSpeed = uiState.playbackSpeed,", "playbackSpeed = 1f,")

# Fix LyricsBottomSheet arguments
target_lbs = """            onCollapse = { viewModel.setLyricsModeEnabled(false) },"""
replacement_lbs = """            onCollapse = { viewModel.setLyricsModeEnabled(false) },
            syncOffsetMs = 0L,
            onSyncOffsetChange = {},"""
content = content.replace(target_lbs, replacement_lbs)

with open(path, "w") as f:
    f.write(content)

print("FullPlayer.kt fixed")
