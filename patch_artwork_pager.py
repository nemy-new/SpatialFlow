import re

with open('app/src/main/java/com/codetrio/overdrive/ui/player/ArtworkPager.kt', 'r') as f:
    content = f.read()

# Add imports if missing
if 'import androidx.compose.foundation.gestures.detectTapGestures' not in content:
    content = content.replace(
        'import androidx.compose.ui.Modifier',
        'import androidx.compose.foundation.gestures.detectTapGestures\nimport androidx.compose.ui.input.pointer.pointerInput\nimport androidx.compose.ui.Modifier'
    )

box_pattern = r"(\s+)(Box\(\s+modifier = Modifier\.fillMaxSize\(\),\s+contentAlignment = Alignment\.Center\s+\)\s+\{\s+// 1\. Static album art as base layer)"

def box_replacer(match):
    indent = match.group(1)
    new_box = """Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isCurrentPage) {
                        if (!isCurrentPage) return@pointerInput
                        detectTapGestures(
                            onDoubleTap = { offset ->
                                val width = size.width
                                if (offset.x < width * 0.33f) {
                                    val currentMs = currentPosition
                                    viewModel.seekTo((currentMs - 10000).coerceAtLeast(0))
                                } else if (offset.x > width * 0.66f) {
                                    val currentMs = currentPosition
                                    val duration = viewModel.duration.value
                                    val targetMs = if (duration > 0) (currentMs + 10000).coerceAtMost(duration) else (currentMs + 10000)
                                    viewModel.seekTo(targetMs)
                                } else {
                                    viewModel.toggleFavorite()
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // 1. Static album art as base layer"""
    return indent + new_box

content = re.sub(box_pattern, box_replacer, content)

with open('app/src/main/java/com/codetrio/overdrive/ui/player/ArtworkPager.kt', 'w') as f:
    f.write(content)

print("ArtworkPager patched.")
