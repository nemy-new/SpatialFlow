import re

file_path = "app/src/main/java/com/codetrio/overdrive/viewmodel/PlayerSharedViewModel.kt"
with open(file_path, "r") as f:
    content = f.read()

target = """                _likesCount.value = "Like"
                _isCurrentSongDisliked.value = false
                _canvasArtwork.value = null"""

replacement = """                _likesCount.value = "Like"
                _isCurrentSongDisliked.value = false
                _canvasArtwork.value = null
                if (song == null) {
                    _musicVideoUrl.value = null
                    _isMvMode.value = false
                }"""

if target in content:
    content = content.replace(target, replacement)
    with open(file_path, "w") as f:
        f.write(content)
    print("Patched successfully")
else:
    print("Target not found")
