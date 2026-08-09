import re

path = "./app/src/main/java/com/codetrio/overdrive/viewmodel/PlayerSharedViewModel.kt"
with open(path, "r") as f:
    content = f.read()

# Add isTabletLeftPaneVisible state
new_state = """
    private val _isTabletLeftPaneVisible = MutableStateFlow(true)
    val isTabletLeftPaneVisible: StateFlow<Boolean> = _isTabletLeftPaneVisible.asStateFlow()

    fun toggleTabletLeftPane() {
        _isTabletLeftPaneVisible.value = !_isTabletLeftPaneVisible.value
    }
"""

# Insert it after `val isLyricsLoading` or similar
target = "val isLyricsLoading: StateFlow<Boolean> = _isLyricsLoading.asStateFlow()"
if target in content:
    content = content.replace(target, target + new_state)
    print("Added to ViewModel")
else:
    print("Target NOT found in ViewModel!")

with open(path, "w") as f:
    f.write(content)
