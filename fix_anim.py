import re

path = "./app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

target = """                        Box(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(modifier = Modifier.size(albumArtSize))
                        }"""

replacement = """                        androidx.compose.animation.AnimatedVisibility(
                            visible = isTabletLeftPaneVisibleState.value,
                            modifier = Modifier.weight(1f),
                            enter = androidx.compose.animation.expandHorizontally(
                                expandFrom = Alignment.End,
                                animationSpec = androidx.compose.animation.core.spring(
                                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                                )
                            ) + androidx.compose.animation.fadeIn(),
                            exit = androidx.compose.animation.shrinkHorizontally(
                                shrinkTowards = Alignment.End,
                                animationSpec = androidx.compose.animation.core.spring(
                                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                                )
                            ) + androidx.compose.animation.fadeOut()
                        ) {
                            Box(
                                modifier = Modifier.fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(modifier = Modifier.size(albumArtSize))
                            }
                        }"""

if target in content:
    content = content.replace(target, replacement)
    print("AnimatedVisibility injected")
else:
    print("AnimatedVisibility target NOT found")

with open(path, "w") as f:
    f.write(content)
