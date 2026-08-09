import re

path = "./app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

# Fix state collection back to by
content = content.replace("val isTabletLeftPaneVisibleState = viewModel.isTabletLeftPaneVisible.collectAsStateWithLifecycle()", "val isTabletLeftPaneVisible by viewModel.isTabletLeftPaneVisible.collectAsStateWithLifecycle()")

# Fix usages
content = content.replace("isTabletLeftPaneVisibleState.value", "isTabletLeftPaneVisible")

# Fix icons
content = content.replace("androidx.compose.material.icons.Icons.AutoMirrored.Rounded.ArrowBack", "androidx.compose.material.icons.Icons.AutoMirrored.Rounded.ArrowBack".replace("androidx.compose.material.icons.", ""))
content = content.replace("androidx.compose.material.icons.Icons.AutoMirrored.Rounded.ArrowForward", "androidx.compose.material.icons.Icons.AutoMirrored.Rounded.ArrowForward".replace("androidx.compose.material.icons.", ""))

# Add import for Icons if not present (though Icons is usually present)
import_statement = "import androidx.compose.material.icons.automirrored.rounded.ArrowBack\nimport androidx.compose.material.icons.automirrored.rounded.ArrowForward\n"
if "import androidx.compose.material.icons.automirrored.rounded.ArrowBack" not in content:
    content = content.replace("import androidx.compose.material.icons.Icons", import_statement + "import androidx.compose.material.icons.Icons")

with open(path, "w") as f:
    f.write(content)

print("Fixed syntax errors")
