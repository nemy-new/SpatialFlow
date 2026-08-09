import re

def main():
    with open('./app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt', 'r') as f:
        content = f.read()

    # The exact block to replace
    target_block = """                if (isTablet) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(modifier = Modifier.size(albumArtSize))
                        }
                        Column(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            rightPaneContent()
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(topOffset - (statusBarTopDp + 68.dp)))

                    // Album Art Container Placeholder (ArtworkPager is rendered at this absolute position)
                    Box(
                        modifier = Modifier.size(albumArtSize)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    rightPaneContent()
                }
            }
        }

        LyricsBottomSheet("""

    if target_block not in content:
        # We might have already replaced it but the script failed later. Let's checkout original
        import subprocess
        subprocess.run(["git", "checkout", "./app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"])
        with open('./app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt', 'r') as f:
            content = f.read()

    # Find where to insert tab state
    anchor_tabs = "val isTablet = configuration.screenWidthDp >= 600"
    tab_state_code = "\n        var tabletRightPaneTab by remember { androidx.compose.runtime.mutableIntStateOf(1) } // 0: Queue, 1: Lyrics, 2: Related\n"
    content = content.replace(anchor_tabs, anchor_tabs + tab_state_code)

    # Add necessary imports if not present
    imports = [
        "import androidx.compose.ui.draw.clip",
        "import androidx.compose.foundation.shape.RoundedCornerShape",
        "import androidx.compose.foundation.background",
        "import androidx.compose.foundation.clickable",
        "import androidx.compose.foundation.layout.padding",
        "import androidx.compose.foundation.layout.fillMaxWidth",
        "import androidx.compose.ui.unit.sp",
        "import androidx.lifecycle.compose.collectAsStateWithLifecycle"
    ]
    for imp in imports:
        if imp not in content:
            content = content.replace("import androidx.compose.ui.Alignment", f"import androidx.compose.ui.Alignment\n{imp}")

    # Find the Chips row block dynamically
    chips_start_str = "Row(\n                modifier = Modifier\n                    .fillMaxWidth()\n                    .layout { measurable, constraints ->"
    chips_start = content.find(chips_start_str)
    chips_end = content.find("Spacer(modifier = Modifier.height(24.dp))", chips_start)
    chips_code = content[chips_start:chips_end].strip()

    # Find the Wavy Slider dynamically
    slider_start_str = "WavySliderWithLabels("
    slider_start = content.find(slider_start_str)
    slider_end = content.find("Spacer(modifier = Modifier.height(16.dp))", slider_start)
    slider_code = content[slider_start:slider_end].strip()

    # Build the new block
    new_block = f"""                val tabletHeaderHeightDp = statusBarTopDp + 12.dp
                val tabletAvailableHeightDp = screenHeight - tabletHeaderHeightDp
                val tabletCenterYDp = tabletHeaderHeightDp + (tabletAvailableHeightDp / 2f)
                val tabletTopOffset = tabletCenterYDp - (albumArtSize / 2f)

                if (isTablet) {{
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = dimens.screenMargin),
                        verticalAlignment = Alignment.CenterVertically
                    ) {{
                        // Left pane: Artwork and Controls
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.Start // Left aligned for tablet!
                        ) {{
                            Spacer(modifier = Modifier.height(tabletTopOffset - (statusBarTopDp + 68.dp)))
                            Box(modifier = Modifier.size(albumArtSize))
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Left-aligned Metadata
                            Text(
                                text = uiState.currentSong?.title ?: "Unknown Title",
                                style = MaterialTheme.typography.headlineMediumEmphasized.copy(
                                    fontFamily = if (playerTheme == "immersion") com.codetrio.overdrive.ui.theme.GoogleSansFlexImmersion else MaterialTheme.typography.headlineMediumEmphasized.fontFamily,
                                    fontSize = 32.sp
                                ),
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = contentColor,
                                maxLines = 1,
                                modifier = Modifier.basicMarqueeWithFadedEdges().width(albumArtSize)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiState.currentSong?.artist ?: "Unknown Artist",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = if (playerTheme == "immersion") com.codetrio.overdrive.ui.theme.GoogleSansFlexImmersion else MaterialTheme.typography.bodyMedium.fontFamily,
                                    fontSize = 18.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                ),
                                color = contentColor.copy(alpha = 0.75f),
                                maxLines = 1,
                                modifier = Modifier
                                    .basicMarqueeWithFadedEdges()
                                    .width(albumArtSize)
                                    .clickable {{
                                        val song = uiState.currentSong
                                        if (song != null) {{
                                            onArtistClick(song.artistId, song.artist)
                                        }}
                                    }}
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Chips Row
                            Box(modifier = Modifier.width(albumArtSize)) {{
                                {chips_code}
                            }}

                            Spacer(modifier = Modifier.height(24.dp))

                            // Wavy Slider
                            {slider_code}

                            Spacer(modifier = Modifier.height(16.dp))

                            // Circular Playback Controls
                            Row(
                                modifier = Modifier.width(albumArtSize),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {{
                                // Previous
                                androidx.compose.material3.IconButton(
                                    onClick = onPreviousClick,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(contentColor.copy(alpha = if (isDark) 0.08f else 0.06f))
                                ) {{
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_skip_previous),
                                        contentDescription = "Previous Song",
                                        tint = contentColor,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }}

                                Spacer(modifier = Modifier.width(32.dp))

                                // Play/Pause
                                androidx.compose.material3.IconButton(
                                    onClick = onPlayPauseClick,
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(dynamicAccentColor)
                                ) {{
                                    Icon(
                                        painter = painterResource(id = if (uiState.isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                                        contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                                        tint = if (isDark) Color(0xFF1C1B1F) else Color.White,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }}

                                Spacer(modifier = Modifier.width(32.dp))

                                // Next
                                androidx.compose.material3.IconButton(
                                    onClick = onNextClick,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(contentColor.copy(alpha = if (isDark) 0.08f else 0.06f))
                                ) {{
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_skip_next),
                                        contentDescription = "Next Song",
                                        tint = contentColor,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }}
                            }}
                        }}

                        // Right pane: Tabs and Content
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(end = dimens.screenMargin)
                                .clip(RoundedCornerShape(32.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                        ) {{
                            // Segmented Control Tabs
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {{
                                val tabs = listOf("次のコンテンツ", "歌詞", "関連コンテンツ")
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(Color.Black.copy(alpha = 0.2f))
                                        .padding(4.dp)
                                ) {{
                                    tabs.forEachIndexed {{ index, title ->
                                        val isSelected = tabletRightPaneTab == index
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent)
                                                .clickable {{ tabletRightPaneTab = index }}
                                                .padding(horizontal = 24.dp, vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {{
                                            Text(
                                                text = title,
                                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                                                fontSize = 14.sp
                                            )
                                        }}
                                    }}
                                }}
                            }}

                            // Tab Content
                            Box(modifier = Modifier.fillMaxSize()) {{
                                when (tabletRightPaneTab) {{
                                    0 -> {{ // Queue
                                        SlidingQueueDrawer(
                                            isQueueExpanded = true,
                                            onQueueExpandedChange = {{ viewModel.setQueueExpanded(it) }},
                                            songList = songList,
                                            currentSongIndex = currentSongIndex,
                                            isShuffleEnabled = isShuffleEnabled,
                                            repeatMode = repeatMode,
                                            sleepTimerMode = sleepTimerMode,
                                            onReorderQueue = {{ from, to -> viewModel.reorderQueue(from, to) }},
                                            onPlaySongAtIndex = {{ index -> viewModel.playSongAtIndex(index) }},
                                            onRemoveSongAtIndex = {{ index -> viewModel.removeSongAtIndex(index) }},
                                            onToggleShuffle = {{ viewModel.toggleShuffle() }},
                                            onToggleLoopMode = {{ viewModel.toggleLoopMode() }},
                                            onShowSleepTimerDialog = {{ showSleepTimerDialog = true }},
                                            playerBackgroundColor = playerBackgroundColor.toArgb(),
                                            dynamicAccentColor = dynamicAccentColor,
                                            isDark = isDark,
                                            isAutoplayEnabled = isAutoplayEnabled,
                                            onAutoplayToggle = onAutoplayToggle,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }}
                                    1 -> {{ // Lyrics
                                        val syncOffsetMs by viewModel.currentLyricsOffsetMs.collectAsStateWithLifecycle()
                                        FullScreenLyricsOverlay(
                                            currentSong = uiState.currentSong,
                                            syncedLyrics = syncedLyrics,
                                            plainLyrics = plainLyrics,
                                            isLoading = isLyricsLoading,
                                            lyricsError = lyricsError,
                                            currentPositionProvider = currentPositionProvider,
                                            contentReady = true,
                                            playerBackgroundColor = Color.Transparent, // Transparent so the rounded box shows
                                            canvasArtwork = canvasArtwork,
                                            contentColor = Color.White,
                                            contentSecondary = Color.White.copy(alpha = 0.6f),
                                            dynamicAccentColor = dynamicAccentColor,
                                            onRetryLyrics = onRetryLyrics,
                                            onFetchLyrics = onFetchLyrics,
                                            onSeekTo = onSeekTo,
                                            providerResults = providerResults,
                                            selectedProvider = selectedProvider,
                                            onProviderSelected = onProviderSelected,
                                            syncOffsetMs = syncOffsetMs,
                                            onSyncOffsetChange = {{ viewModel.setLyricsOffset(it) }},
                                            isPlaying = uiState.isPlaying,
                                            onPlayPauseClick = onPlayPauseClick,
                                            duration = uiState.duration.toLong(),
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }}
                                    2 -> {{ // Related (Placeholder for now)
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {{
                                            Text("関連コンテンツ", color = Color.White.copy(alpha = 0.5f))
                                        }}
                                    }}
                                }}
                            }}
                        }}
                    }}
                }} else {{
                    Spacer(modifier = Modifier.height(topOffset - (statusBarTopDp + 68.dp)))

                    // Album Art Container Placeholder (ArtworkPager is rendered at this absolute position)
                    Box(
                        modifier = Modifier.size(albumArtSize)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    rightPaneContent()
                }}
            }}
        }}

        if (!isTablet) {{
            LyricsBottomSheet("""

    content = content.replace(target_block, new_block)
    
    content = content.replace("""onCollapse = { viewModel.setLyricsModeEnabled(false) },
            modifier = Modifier.fillMaxSize()
        )""", """onCollapse = { viewModel.setLyricsModeEnabled(false) },
            syncOffsetMs = 0L,
            onSyncOffsetChange = {},
            modifier = Modifier.fillMaxSize()
        )""")
        
    # Wrap SlidingQueueDrawer in `if (!isTablet)` safely
    queue_drawer_marker = "// --- CUSTOM EMBEDDED SLIDING PLAY QUEUE ---\n        SlidingQueueDrawer("
    queue_drawer_new = "// --- CUSTOM EMBEDDED SLIDING PLAY QUEUE ---\n        if (!isTablet) {\n            SlidingQueueDrawer("
    content = content.replace(queue_drawer_marker, queue_drawer_new)
    
    # Close `if (!isTablet)` before SleepTimerBottomSheet
    timer_dialog = "// --- Standalone Sleep Timer Bottom Sheet ---"
    content = content.replace(timer_dialog, "        }\n\n        " + timer_dialog)
    
    with open('./app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt', 'w') as f:
        f.write(content)
        
    print("Success")

if __name__ == "__main__":
    main()
