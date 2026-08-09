import re

path = "app/src/main/java/com/codetrio/overdrive/viewmodel/PlayerSharedViewModel.kt"
with open(path, "r") as f:
    content = f.read()

injection = """
    // MV and Artwork states
    private val _isMvFullscreen = MutableStateFlow(false)
    val isMvFullscreen: StateFlow<Boolean> = _isMvFullscreen.asStateFlow()
    fun setMvFullscreen(value: Boolean) { _isMvFullscreen.value = value }

    private val _isInPipMode = MutableStateFlow(false)
    val isInPipMode: StateFlow<Boolean> = _isInPipMode.asStateFlow()

    private val _isMvMode = MutableStateFlow(false)
    val isMvMode: StateFlow<Boolean> = _isMvMode.asStateFlow()

    private val _musicVideoUrl = MutableStateFlow<String?>(null)
    val musicVideoUrl: StateFlow<String?> = _musicVideoUrl.asStateFlow()

    private val _mvSeekRequest = MutableStateFlow<Long?>(null)
    val mvSeekRequest: StateFlow<Long?> = _mvSeekRequest.asStateFlow()
    fun clearMvSeekRequest() { _mvSeekRequest.value = null }

    val currentSongArtwork = MutableStateFlow<ByteArray?>(null)
"""

if "_isMvFullscreen" not in content:
    content = content.replace("override fun onCleared() {", injection + "\n    override fun onCleared() {")
    with open(path, "w") as f:
        f.write(content)
    print("Injected missing states")
else:
    print("Already injected")
