import re

path_full = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path_full, "r") as f:
    content = f.read()

# 1. Remove the 32.dp padding from the WavySlider Box
target_wavy_box = "Box(modifier = Modifier.width(albumArtSize).padding(horizontal = 32.dp)) {"
replacement_wavy_box = "Box(modifier = Modifier.width(albumArtSize)) {"
content = content.replace(target_wavy_box, replacement_wavy_box)

# 2. Update the Right Pane Tabs to be hideable and remove the third tab
target_tabs_block = """                            // Segmented Control Tabs
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                val tabs = listOf("次のコンテンツ", "歌詞", "関連コンテンツ")
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(Color.Black.copy(alpha = 0.2f))
                                        .padding(4.dp)
                                ) {
                                    tabs.forEachIndexed { index, title ->
                                        val isSelected = tabletRightPaneTab == index
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent)
                                                .clickable { tabletRightPaneTab = index }
                                                .padding(horizontal = 24.dp, vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = title,
                                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }"""

replacement_tabs_block = """                            // Segmented Control Tabs
                            var isTabsVisible by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(true) }
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = isTabsVisible,
                                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically(),
                                    modifier = Modifier.align(Alignment.Center)
                                ) {
                                    val tabs = listOf("次のコンテンツ", "歌詞")
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(Color.Black.copy(alpha = 0.2f))
                                            .padding(4.dp)
                                    ) {
                                        tabs.forEachIndexed { index, title ->
                                            val isSelected = tabletRightPaneTab == index
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent)
                                                    .clickable { tabletRightPaneTab = index }
                                                    .padding(horizontal = 24.dp, vertical = 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = title,
                                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                                    fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                androidx.compose.material3.IconButton(
                                    onClick = { isTabsVisible = !isTabsVisible },
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                ) {
                                    Icon(
                                        painter = painterResource(id = if (isTabsVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility),
                                        contentDescription = "Toggle Tabs",
                                        tint = Color.White.copy(alpha = 0.5f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }"""

content = content.replace(target_tabs_block, replacement_tabs_block)

with open(path_full, "w") as f:
    f.write(content)
print("Fixes applied.")
