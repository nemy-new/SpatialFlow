import re

path1 = "app/src/main/java/com/codetrio/overdrive/ui/PlayerBottomSheetCompose.kt"
with open(path1, "r") as f:
    content1 = f.read()

# Fix lyricsArtworkProgress to not shrink on tablet
target_progress = """        val lyricsArtworkProgress by animateFloatAsState(
            targetValue = if (isLyricsModeEnabled) 1f else 0f,
            animationSpec = spring(dampingRatio = 0.88f, stiffness = 380f),
            label = "LyricsArtworkSharedElement"
        )"""

replacement_progress = """        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val isTabletTopLevel = configuration.screenWidthDp >= 600
        val lyricsArtworkProgress by animateFloatAsState(
            targetValue = if (isLyricsModeEnabled && !isTabletTopLevel) 1f else 0f,
            animationSpec = spring(dampingRatio = 0.88f, stiffness = 380f),
            label = "LyricsArtworkSharedElement"
        )"""

content1 = content1.replace(target_progress, replacement_progress)

# Fix xEndPx to animate and handle isLyricsModeEnabled for tablet
target_xendpx = """                    val xEndPx = remember(isTablet, screenWidthPx, fullSizePx, density, dimens) {
                        if (isTablet) {
                            val screenMarginPx = with(density) { dimens.screenMargin.toPx() }
                            val availableWidthPx = screenWidthPx - (2 * screenMarginPx)
                            val leftPaneWidthPx = availableWidthPx / 2f
                            screenMarginPx + (leftPaneWidthPx - fullSizePx) / 2f
                        } else {
                            (screenWidthPx - fullSizePx) / 2f
                        }
                    }"""

replacement_xendpx = """                    val xEndPxTarget = remember(isTablet, isLyricsModeEnabled, screenWidthPx, fullSizePx, density, dimens) {
                        if (isTablet) {
                            if (isLyricsModeEnabled) {
                                val screenMarginPx = with(density) { dimens.screenMargin.toPx() }
                                val availableWidthPx = screenWidthPx - (2 * screenMarginPx)
                                val leftPaneWidthPx = availableWidthPx / 2f
                                screenMarginPx + (leftPaneWidthPx - fullSizePx) / 2f
                            } else {
                                (screenWidthPx - fullSizePx) / 2f
                            }
                        } else {
                            (screenWidthPx - fullSizePx) / 2f
                        }
                    }
                    val xEndPx by animateFloatAsState(
                        targetValue = xEndPxTarget,
                        animationSpec = spring(dampingRatio = 0.88f, stiffness = 380f),
                        label = "AlbumArtXPosition"
                    )"""

content1 = content1.replace(target_xendpx, replacement_xendpx)

with open(path1, "w") as f:
    f.write(content1)


path2 = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path2, "r") as f:
    content2 = f.read()

# In FullPlayer.kt, we need to add leftPaneFraction and replace weight(1f) with fillMaxWidth(leftPaneFraction)
target_left_pane = """                        // Left pane: Artwork and Controls
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(totalGroupHeightDp),"""

replacement_left_pane = """                        val leftPaneFraction by androidx.compose.animation.core.animateFloatAsState(
                            targetValue = if (isLyricsModeEnabled) 0.5f else 1f,
                            animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.88f, stiffness = 380f),
                            label = "LeftPaneFraction"
                        )
                        // Left pane: Artwork and Controls
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(leftPaneFraction)
                                .height(totalGroupHeightDp),"""

content2 = content2.replace(target_left_pane, replacement_left_pane)

# For the Right pane, we should wrap it in if statement and remove weight(1f)
target_right_pane = """                        // Right pane: Dedicated to Lyrics
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(end = dimens.screenMargin)
                                .clip(RoundedCornerShape(32.dp))
                                .background(Color.Transparent)
                        ) {"""

replacement_right_pane = """                        // Right pane: Dedicated to Lyrics
                        if (isLyricsModeEnabled || leftPaneFraction < 0.99f) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth() // Fills the remaining width of the Row
                                    .fillMaxHeight()
                                    .padding(end = dimens.screenMargin)
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(Color.Transparent)
                                    .graphicsLayer { alpha = 1f - ((leftPaneFraction - 0.5f) * 2f).coerceIn(0f, 1f) } // Fade out gracefully
                            ) {"""

content2 = content2.replace(target_right_pane, replacement_right_pane)

# Find the closing brace for the Right pane Column
# Right pane is from:
#                             Box(modifier = Modifier.fillMaxSize()) {
#                                 val syncOffsetMs by viewModel.currentLyricsOffsetMs.collectAsStateWithLifecycle()
# ...
#                                 )
#                             }
#                         }

# Wait, regex is safer here
pattern = r'(FullScreenLyricsOverlay\([\s\S]*?duration = duration\n                                \)\n                            \}\n                        \})'
replacement = r'\1\n                        }'

content2 = re.sub(pattern, replacement, content2)

with open(path2, "w") as f:
    f.write(content2)

print("Update scripts finished")
