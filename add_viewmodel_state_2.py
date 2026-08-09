import re

path = "./app/src/main/java/com/codetrio/overdrive/viewmodel/PlayerSharedViewModel.kt"
with open(path, "r") as f:
    content = f.read()

new_state = """
    private val _isTabletLeftPaneVisible = kotlinx.coroutines.flow.MutableStateFlow(true)
    val isTabletLeftPaneVisible: kotlinx.coroutines.flow.StateFlow<Boolean> = _isTabletLeftPaneVisible.asStateFlow()
    fun toggleTabletLeftPane() {
        _isTabletLeftPaneVisible.value = !_isTabletLeftPaneVisible.value
    }
"""

target = "val selectedProvider get() = lyricsController.selectedProvider"
if target in content:
    content = content.replace(target, target + new_state)
    print("Added to ViewModel")
else:
    print("Target NOT found in ViewModel!")

with open(path, "w") as f:
    f.write(content)
