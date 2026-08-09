import re

# 1. Patch PlayerSharedViewModel.kt
vm_file = "app/src/main/java/com/codetrio/overdrive/viewmodel/PlayerSharedViewModel.kt"
with open(vm_file, "r") as f:
    content = f.read()

# Replace the currentSong.collect block
old_block = """                if (song != null) {
                    if (!song.videoId.isNullOrEmpty()) {
                        _musicVideoUrl.value = "innertube://${song.videoId}"
                    } else {
                        _musicVideoUrl.value = null
                        _isMvMode.value = false
                    }
                    playedShuffledIds.add(song.id)"""

new_block = """                if (song != null) {
                    if (!song.videoId.isNullOrEmpty()) {
                        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            val mvUrl = com.codetrio.overdrive.data.innertube.NewPipeStreamExtractor.getVideoStreamUrl(song.videoId)
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                if (_currentSong.value?.id == song.id) {
                                    _musicVideoUrl.value = mvUrl
                                    if (mvUrl == null) {
                                        _isMvMode.value = false
                                    }
                                }
                            }
                        }
                    } else {
                        _musicVideoUrl.value = null
                        _isMvMode.value = false
                    }
                    playedShuffledIds.add(song.id)"""

content = content.replace(old_block, new_block)
with open(vm_file, "w") as f:
    f.write(content)

# 2. Patch ArtworkPager.kt
art_file = "app/src/main/java/com/codetrio/overdrive/ui/player/ArtworkPager.kt"
with open(art_file, "r") as f:
    content = f.read()

old_tap = """                        detectTapGestures(
                            onDoubleTap = { offset ->
                                val width = size.width"""

new_tap = """                        detectTapGestures(
                            onTap = {
                                val prefs = context.getSharedPreferences("AppSettings", android.content.Context.MODE_PRIVATE)
                                if (prefs.getBoolean("debug_toasts", false)) {
                                    val s = songList.getOrNull(currentSongIndex)
                                    android.widget.Toast.makeText(context, "Song ID: ${s?.id}\\nVideo ID: ${s?.videoId}", android.widget.Toast.LENGTH_LONG).show()
                                }
                            },
                            onDoubleTap = { offset ->
                                val width = size.width"""

content = content.replace(old_tap, new_tap)
with open(art_file, "w") as f:
    f.write(content)

print("Patch applied successfully.")
