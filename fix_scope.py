import re

path = "./app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

# Remove line 115
content = re.sub(r"^\s*val isTabletLeftPaneVisible by viewModel\.isTabletLeftPaneVisible\.collectAsStateWithLifecycle\(\)\n", "", content, flags=re.MULTILINE)

# Replace usage in IconButton
target_icon = "imageVector = if (isTabletLeftPaneVisible) Icons.AutoMirrored.Rounded.ArrowBack else Icons.AutoMirrored.Rounded.ArrowForward,"
replacement_icon = "imageVector = if (viewModel.isTabletLeftPaneVisible.collectAsStateWithLifecycle().value) Icons.AutoMirrored.Rounded.ArrowBack else Icons.AutoMirrored.Rounded.ArrowForward,"
content = content.replace(target_icon, replacement_icon)

# Replace usage in AnimatedVisibility
target_anim = "visible = isTabletLeftPaneVisible,"
replacement_anim = "visible = viewModel.isTabletLeftPaneVisible.collectAsStateWithLifecycle().value,"
content = content.replace(target_anim, replacement_anim)

with open(path, "w") as f:
    f.write(content)

print("Scope fixed")
