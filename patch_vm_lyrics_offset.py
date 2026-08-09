import re

path = "app/src/main/java/com/codetrio/overdrive/viewmodel/PlayerSharedViewModel.kt"
with open(path, "r") as f:
    content = f.read()

target = """    private val _isPlayerExpanded = MutableStateFlow(false)
    val isPlayerExpanded: StateFlow<Boolean> = _isPlayerExpanded.asStateFlow()"""

replacement = """    private val _isPlayerExpanded = MutableStateFlow(false)
    val isPlayerExpanded: StateFlow<Boolean> = _isPlayerExpanded.asStateFlow()
    
    private val _currentLyricsOffsetMs = MutableStateFlow(0L)
    val currentLyricsOffsetMs: StateFlow<Long> = _currentLyricsOffsetMs.asStateFlow()
    
    fun setLyricsOffset(offset: Long) {
        _currentLyricsOffsetMs.value = offset
    }"""

if target in content:
    content = content.replace(target, replacement)
    with open(path, "w") as f:
        f.write(content)
    print("ViewModel patched")
else:
    print("Target not found")
