import re

file_path = "app/src/main/java/com/codetrio/overdrive/viewmodel/PlayerSharedViewModel.kt"
with open(file_path, "r") as f:
    content = f.read()

target = """                if (song != null) {
                    playedShuffledIds.add(song.id)"""

replacement = """                if (song != null) {
                    if (!song.videoId.isNullOrEmpty()) {
                        _musicVideoUrl.value = "innertube://${song.videoId}"
                    } else {
                        _musicVideoUrl.value = null
                        _isMvMode.value = false
                    }
                    playedShuffledIds.add(song.id)"""

if target in content:
    content = content.replace(target, replacement)
    with open(file_path, "w") as f:
        f.write(content)
    print("Patched successfully")
else:
    print("Target not found")
