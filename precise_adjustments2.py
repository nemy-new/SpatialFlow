import re

path_full = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path_full, "r") as f:
    content_full = f.read()

# 1. Delete the Header Row for tablets
target_header_row = """                // Header Row (Nav controls + collapse) - Symmetric centering
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onCollapse) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_keyboard_arrow_down),
                            contentDescription = "Collapse Player",
                            tint = contentColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    if (!hasCanvas || isLyricsModeEnabled) {
                        Text(
                            text = "NOW PLAYING",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = contentSecondary
                        )
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }

                    Spacer(modifier = Modifier.size(48.dp))
                }"""

replacement_header_row = """                if (!isTablet) {
                    // Header Row (Nav controls + collapse) - Symmetric centering
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onCollapse) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_keyboard_arrow_down),
                                contentDescription = "Collapse Player",
                                tint = contentColor.copy(alpha = 0.8f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
    
                        if (!hasCanvas || isLyricsModeEnabled) {
                            Text(
                                text = "NOW PLAYING",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = contentSecondary
                            )
                        } else {
                            Spacer(modifier = Modifier.size(48.dp))
                        }
    
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }"""
content_full = content_full.replace(target_header_row, replacement_header_row)

# 2. Add Top Spacer and align Row to Top, remove Spacer before Album Art, set Left Pane to CenterHorizontally and exact height
target_tablet_row = """                val controlsHeightDp = 268.dp
                val totalGroupHeightDp = albumArtSize + controlsHeightDp
                val availableHeightDp = screenHeight - statusBarTopDp
                val tabletTopOffset = statusBarTopDp + ((availableHeightDp - totalGroupHeightDp) / 2f).coerceAtLeast(16.dp)

                if (isTablet) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = dimens.screenMargin),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left pane: Artwork and Controls
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.Start // Left aligned for tablet!
                        ) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Box(modifier = Modifier.size(albumArtSize))"""

replacement_tablet_row = """                val controlsHeightDp = 300.dp
                val totalGroupHeightDp = albumArtSize + controlsHeightDp
                val availableHeightDp = screenHeight - statusBarTopDp
                val tabletTopOffset = statusBarTopDp + ((availableHeightDp - totalGroupHeightDp) / 2f).coerceAtLeast(16.dp)
                val rowTopAbsolute = statusBarTopDp + dimens.smallPadding
                val topSpacerHeight = (tabletTopOffset - rowTopAbsolute).coerceAtLeast(0.dp)

                if (isTablet) {
                    Spacer(modifier = Modifier.height(topSpacerHeight))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimens.screenMargin),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Left pane: Artwork and Controls
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(totalGroupHeightDp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(modifier = Modifier.size(albumArtSize))"""

content_full = content_full.replace(target_tablet_row, replacement_tablet_row)

# 3. Fix Title Font Size
target_title = """                                    fontSize = 32.sp
                                ),
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,"""
replacement_title = """                                    fontSize = 28.sp
                                ),
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,"""
content_full = content_full.replace(target_title, replacement_title)

# 4. Wrap WavySliderWithLabels and set weight(1f) for the spacer before ButtonGroup
target_wavy_block = """                            // Wavy Slider
                            WavySliderWithLabels(
                                currentPositionProvider = currentPositionProvider,
                                duration = uiState.duration,
                                isPlaying = uiState.isPlaying,
                                onSeekTo = onSeekTo,
                                dynamicAccentColor = dynamicAccentColor,
                                contentColor = contentColor,
                                contentSecondary = contentSecondary,
                                isDark = isDark,
                                playbackFormat = uiState.playbackFormat
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            androidx.compose.material3.ButtonGroup("""

replacement_wavy_block = """                            // Wavy Slider
                            Box(modifier = Modifier.width(albumArtSize)) {
                                WavySliderWithLabels(
                                    currentPositionProvider = currentPositionProvider,
                                    duration = uiState.duration,
                                    isPlaying = uiState.isPlaying,
                                    onSeekTo = onSeekTo,
                                    dynamicAccentColor = dynamicAccentColor,
                                    contentColor = contentColor,
                                    contentSecondary = contentSecondary,
                                    isDark = isDark,
                                    playbackFormat = uiState.playbackFormat
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f)) // Push buttons to absolute bottom

                            androidx.compose.material3.ButtonGroup("""
content_full = content_full.replace(target_wavy_block, replacement_wavy_block)

# Fix right pane
target_right_pane = """                        // Right pane: Queue/Lyrics/Related (Tabs)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(start = 32.dp)
                        ) {"""
replacement_right_pane = """                        // Right pane: Queue/Lyrics/Related (Tabs)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(totalGroupHeightDp)
                                .padding(start = 32.dp)
                        ) {"""
content_full = content_full.replace(target_right_pane, replacement_right_pane)

with open(path_full, "w") as f:
    f.write(content_full)
print("FullPlayer.kt completely updated")

path_sheet = "app/src/main/java/com/codetrio/overdrive/ui/PlayerBottomSheetCompose.kt"
with open(path_sheet, "r") as f:
    content_sheet = f.read()

content_sheet = content_sheet.replace("val controlsHeightDp = 268.dp", "val controlsHeightDp = 300.dp")

# Make sure xEndPx uses the correct logic in PlayerBottomSheetCompose.kt
target_x = """                    val xEndPx = remember(isTablet, screenWidthPx, fullSizePx) {
                        if (isTablet) {
                            (screenWidthPx / 4f) - (fullSizePx / 2f)
                        } else {
                            (screenWidthPx - fullSizePx) / 2f
                        }
                    }"""

replacement_x = """                    val xEndPx = remember(isTablet, screenWidthPx, fullSizePx, density) {
                        if (isTablet) {
                            val screenMarginPx = with(density) { 16.dp.toPx() }
                            val availableWidthPx = screenWidthPx - (2 * screenMarginPx)
                            val leftPaneWidthPx = availableWidthPx / 2f
                            screenMarginPx + (leftPaneWidthPx - fullSizePx) / 2f
                        } else {
                            (screenWidthPx - fullSizePx) / 2f
                        }
                    }"""

if target_x in content_sheet:
    content_sheet = content_sheet.replace(target_x, replacement_x)
else:
    print("WARNING: Could not find target_x in PlayerBottomSheetCompose.kt")

with open(path_sheet, "w") as f:
    f.write(content_sheet)
print("PlayerBottomSheetCompose.kt updated")

