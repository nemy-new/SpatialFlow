path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

target_left = """                        val leftPaneFraction by androidx.compose.animation.core.animateFloatAsState(
                            targetValue = if (isLyricsModeEnabled) 0.5f else 1f,
                            animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.88f, stiffness = 380f),
                            label = "LeftPaneFraction"
                        )
                        // Left pane: Artwork and Controls
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(leftPaneFraction)
                                .height(totalGroupHeightDp),"""

replacement_left = """                        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
                        val availableWidthDp = configuration.screenWidthDp.dp - (dimens.screenMargin * 2)
                        val rightPaneWidthDp = availableWidthDp / 2f
                        // Left pane: Artwork and Controls
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(totalGroupHeightDp),"""

content = content.replace(target_left, replacement_left)

target_right = """                        // Right pane: Dedicated to Lyrics
                        if (isLyricsModeEnabled || leftPaneFraction < 0.99f) {
                            Column(
                                modifier = Modifier
                                    .weight(1f) // Fills the remaining width of the Row safely
                                    .fillMaxHeight()
                                    .padding(end = dimens.screenMargin)
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(Color.Transparent)
                                    .graphicsLayer { alpha = 1f - ((leftPaneFraction - 0.5f) * 2f).coerceIn(0f, 1f) } // Fade out gracefully
                            ) {"""

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
                                    .width(rightPaneWidthDp)
                                    .fillMaxHeight()
                                    .padding(end = dimens.screenMargin)
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(Color.Transparent)
                            ) {"""

content = content.replace(target_right, replacement_right)

with open(path, "w") as f:
    f.write(content)
print("Updated FullPlayer.kt")
