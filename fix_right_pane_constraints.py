path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

target_right = """                        // Right pane: Dedicated to Lyrics
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isLyricsModeEnabled,
                            enter = androidx.compose.animation.expandHorizontally(
                                animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.88f, stiffness = 380f)
                            ) + androidx.compose.animation.fadeIn(
                                animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.88f, stiffness = 380f)
                            ),
                            exit = androidx.compose.animation.shrinkHorizontally(
                                animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.88f, stiffness = 380f)
                            ) + androidx.compose.animation.fadeOut(
                                animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.88f, stiffness = 380f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .width(rightPaneWidthDp)
                                    .fillMaxHeight()"""

replacement_right = """                        // Right pane: Dedicated to Lyrics
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isLyricsModeEnabled,
                            enter = androidx.compose.animation.expandHorizontally(
                                animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.88f, stiffness = 380f)
                            ) + androidx.compose.animation.fadeIn(
                                animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.88f, stiffness = 380f)
                            ),
                            exit = androidx.compose.animation.shrinkHorizontally(
                                animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.88f, stiffness = 380f)
                            ) + androidx.compose.animation.fadeOut(
                                animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.88f, stiffness = 380f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .requiredWidth(rightPaneWidthDp)
                                    .height(totalGroupHeightDp)"""

content = content.replace(target_right, replacement_right)

with open(path, "w") as f:
    f.write(content)
print("Updated right pane constraints")
