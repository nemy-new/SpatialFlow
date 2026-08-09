import re

path = "./app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

# Fix state collection
target_state = "val isTabletLeftPaneVisible by viewModel.isTabletLeftPaneVisible.collectAsStateWithLifecycle()"
replacement_state = "val isTabletLeftPaneVisibleState = viewModel.isTabletLeftPaneVisible.collectAsStateWithLifecycle()"
if target_state in content:
    content = content.replace(target_state, replacement_state)
    print("State collection fixed")
else:
    print("State collection target NOT found")

# Fix AnimatedVisibility usage
target_anim = "visible = isTabletLeftPaneVisible,"
replacement_anim = "visible = isTabletLeftPaneVisibleState.value,"
if target_anim in content:
    content = content.replace(target_anim, replacement_anim)
    print("AnimatedVisibility fixed")
else:
    print("AnimatedVisibility target NOT found")

# Fix icon and toggle usage
target_icon = "imageVector = if (isTabletLeftPaneVisible) androidx.compose.material.icons.Icons.AutoMirrored.Rounded.ArrowBack else androidx.compose.material.icons.Icons.AutoMirrored.Rounded.ArrowForward,"
replacement_icon = "imageVector = if (isTabletLeftPaneVisibleState.value) androidx.compose.material.icons.automirrored.rounded.ArrowBack else androidx.compose.material.icons.automirrored.rounded.ArrowForward,"
if target_icon in content:
    content = content.replace(target_icon, replacement_icon)
    print("Icon fixed")
else:
    print("Icon target NOT found")

with open(path, "w") as f:
    f.write(content)
