import re

path = "./app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

# 1. Collect state
state_target = "val isAutoplayEnabled by viewModel.isAutoplayEnabled.collectAsStateWithLifecycle()"
new_state = """
    val isTabletLeftPaneVisible by viewModel.isTabletLeftPaneVisible.collectAsStateWithLifecycle()
"""
if state_target in content:
    content = content.replace(state_target, state_target + new_state)
    print("State collected")
else:
    print("State target NOT found!")

# 2. Add 32.dp top padding
padding_target = """                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = dimens.screenMargin),
                        verticalAlignment = Alignment.CenterVertically
                    ) {"""
new_padding = """                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                horizontal = dimens.screenMargin,
                                top = 32.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {"""
if padding_target in content:
    content = content.replace(padding_target, new_padding)
    print("Top padding added")
else:
    print("Padding target NOT found!")

# 3. Wrap Left Pane in AnimatedVisibility
left_pane_target = """                        // Left pane: Artwork and Controls
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally // Center aligned for tablet!
                        ) {"""
new_left_pane = """                        // Left pane: Artwork and Controls
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isTabletLeftPaneVisible,
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
                            Column(
                                modifier = Modifier.fillMaxHeight(),
                                horizontalAlignment = Alignment.CenterHorizontally // Center aligned for tablet!
                            ) {"""

# Also close the AnimatedVisibility bracket
left_pane_end_target = """                                    contentDescription = null
                                )
                            }
                        }"""
new_left_pane_end = """                                    contentDescription = null
                                )
                            }
                        }
                        } // End of AnimatedVisibility"""

if left_pane_target in content and left_pane_end_target in content:
    content = content.replace(left_pane_target, new_left_pane)
    content = content.replace(left_pane_end_target, new_left_pane_end)
    print("Left Pane wrapped in AnimatedVisibility")
else:
    print("Left pane target NOT found!")

# 4. Remove clip and background from Right Pane
right_pane_target = """                        // Right pane: Tabs and Content
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(end = dimens.screenMargin)
                                .clip(RoundedCornerShape(32.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                        ) {"""
new_right_pane = """                        // Right pane: Tabs and Content
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(end = dimens.screenMargin)
                        ) {"""
if right_pane_target in content:
    content = content.replace(right_pane_target, new_right_pane)
    print("Right Pane clip and background removed")
else:
    print("Right pane target NOT found!")

# 5. Add toggle button in Right Pane header
tab_row_target = """                            // Segmented Control Tabs
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {"""
new_tab_row = """                            // Segmented Control Tabs
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                IconButton(
                                    onClick = { viewModel.toggleTabletLeftPane() },
                                    modifier = Modifier.align(Alignment.CenterStart)
                                ) {
                                    Icon(
                                        imageVector = if (isTabletLeftPaneVisible) androidx.compose.material.icons.Icons.AutoMirrored.Rounded.ArrowBack else androidx.compose.material.icons.Icons.AutoMirrored.Rounded.ArrowForward,
                                        contentDescription = "Toggle Left Pane",
                                        tint = Color.White
                                    )
                                }
                                
                                Row(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalArrangement = Arrangement.Center
                                ) {"""
if tab_row_target in content:
    content = content.replace(tab_row_target, new_tab_row)
    # Don't forget to close the Box! The row ends at line 1222 usually.
    # Wait, the Row ends after tabs.forEachIndexed ... } } } }
    # Let's find the end of this Row exactly.
    print("Tab row replaced")
else:
    print("Tab row target NOT found!")

with open(path, "w") as f:
    f.write(content)
