import re

path = "./app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

# Add Toggle button in Metadata row
target = """            // Metadata row: title/artist
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {"""

replacement = """            // Metadata row: title/artist
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isTablet) {
                    androidx.compose.material3.IconButton(
                        onClick = { viewModel.toggleTabletLeftPane() },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = if (isTabletLeftPaneVisible) androidx.compose.material.icons.Icons.AutoMirrored.Rounded.ArrowBack else androidx.compose.material.icons.Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = "Toggle Left Pane",
                            tint = contentColor
                        )
                    }
                }"""

if target in content:
    content = content.replace(target, replacement)
    print("Toggle button added")
else:
    print("Toggle button target NOT found")

with open(path, "w") as f:
    f.write(content)
