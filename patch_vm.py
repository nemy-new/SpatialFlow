import re

with open('app/src/main/java/com/codetrio/overdrive/viewmodel/PlayerSharedViewModel.kt', 'r') as f:
    content = f.read()

# 1. Remove the bad lyricsError and the init block that was put too high
content = re.sub(r'    val lyricsError: StateFlow<Throwable\?> = _lyricsError\.asStateFlow\(\)\n', '', content)
content = re.sub(r'    init \{\n        // Collect current song changes and fetch offset\n        bgScope\.launch \{\n            _currentSong\.collectLatest \{ song ->\n                if \(song != null\) \{\n                    val offset = lyricsSyncDao\.getOffsetSync\(song\.videoId\) \?: 0L\n                    _currentLyricsOffsetMs\.value = offset\n                \} else \{\n                    _currentLyricsOffsetMs\.value = 0L\n                \}\n            \}\n        \}\n    \}\n', '', content)

# 2. Add the init block back where it's safe (e.g. at the bottom of the other init block at 1059)
# We will just find the real init block and append our launch to it
real_init_match = re.search(r'    init \{\n        bgScope\.launch \{\n            while \(true\) \{\n', content)
if real_init_match:
    insertion = """
        bgScope.launch {
            _currentSong.collectLatest { song ->
                if (song != null) {
                    val offset = lyricsSyncDao.getOffsetSync(song.videoId) ?: 0L
                    _currentLyricsOffsetMs.value = offset
                } else {
                    _currentLyricsOffsetMs.value = 0L
                }
            }
        }
"""
    content = content.replace(real_init_match.group(0), "    init {" + insertion + "        bgScope.launch {\n            while (true) {\n")

with open('app/src/main/java/com/codetrio/overdrive/viewmodel/PlayerSharedViewModel.kt', 'w') as f:
    f.write(content)

