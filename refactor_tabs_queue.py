import re

path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

# 1. Replace the Left-aligned Metadata with a Row containing Metadata and Queue Button
metadata_target = """                            // Left-aligned Metadata
                            Text(
                                text = uiState.currentSong?.title ?: "Unknown Title",
                                style = MaterialTheme.typography.headlineMediumEmphasized.copy(
                                    fontFamily = if (playerTheme == "immersion") com.codetrio.overdrive.ui.theme.GoogleSansFlexImmersion else MaterialTheme.typography.headlineMediumEmphasized.fontFamily,
                                    fontSize = 28.sp
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
                                    .clickable {
                                        val song = uiState.currentSong
                                        if (song != null) {
                                            onArtistClick(song.artistId, song.artist)
                                        }
                                    }
                            )"""

metadata_replacement = """                            // Left-aligned Metadata + Queue Button
                            Row(
                                modifier = Modifier.width(albumArtSize),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = uiState.currentSong?.title ?: "Unknown Title",
                                        style = MaterialTheme.typography.headlineMediumEmphasized.copy(
                                            fontFamily = if (playerTheme == "immersion") com.codetrio.overdrive.ui.theme.GoogleSansFlexImmersion else MaterialTheme.typography.headlineMediumEmphasized.fontFamily,
                                            fontSize = 28.sp
                                        ),
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        color = contentColor,
                                        maxLines = 1,
                                        modifier = Modifier.basicMarqueeWithFadedEdges()
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
                                            .clickable {
                                                val song = uiState.currentSong
                                                if (song != null) {
                                                    onArtistClick(song.artistId, song.artist)
                                                }
                                            }
                                    )
                                }
                                
                                androidx.compose.material3.IconButton(
                                    onClick = { viewModel.setQueueExpanded(true) },
                                    modifier = Modifier.padding(start = 16.dp)
                                ) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Rounded.QueueMusic,
                                        contentDescription = "Queue",
                                        tint = contentColor.copy(alpha = 0.75f),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }"""
content = content.replace(metadata_target, metadata_replacement)

# 2. Refactor Right Pane: Remove tabs completely, just show FullScreenLyricsOverlay
right_pane_target = """                        // Right pane: Tabs and Content
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(end = dimens.screenMargin)
                                .clip(RoundedCornerShape(32.dp))
                                .background(Color.Transparent)
                        ) {
                            // Segmented Control Tabs
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
                                        imageVector = if (isTabsVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = "Toggle Tabs",
                                        tint = Color.White.copy(alpha = 0.5f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Tab Content
                            Box(modifier = Modifier.fillMaxSize()) {
                                when (tabletRightPaneTab) {
                                    0 -> { // Queue
                                        SlidingQueueDrawer(
                                            isQueueExpanded = true,
                                            onQueueExpandedChange = { viewModel.setQueueExpanded(it) },
                                            songList = songList,
                                            currentSongIndex = currentSongIndex,
                                            isShuffleEnabled = isShuffleEnabled,
                                            repeatMode = repeatMode,
                                            sleepTimerMode = sleepTimerMode,
                                            onReorderQueue = { from, to -> viewModel.reorderQueue(from, to) },
                                            onPlaySongAtIndex = { index -> viewModel.playSongAtIndex(index) },
                                            onRemoveSongAtIndex = { index -> viewModel.removeSongAtIndex(index) },
                                            onToggleShuffle = { viewModel.toggleShuffle() },
                                            onToggleLoopMode = { viewModel.toggleLoopMode() },
                                            onShowSleepTimerDialog = { showSleepTimerDialog = true },
                                            playerBackgroundColor = playerBackgroundColor.toArgb(),
                                            dynamicAccentColor = dynamicAccentColor,
                                            isDark = isDark,
                                            isAutoplayEnabled = isAutoplayEnabled,
                                            onAutoplayToggle = onAutoplayToggle,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    1 -> { // Lyrics
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
                                            isPlaying = uiState.isPlaying,
                                            onPlayPauseClick = onPlayPauseClick,
                                            duration = uiState.duration.toLong(),
                                            onCollapse = { viewModel.setLyricsModeEnabled(false) },
                                            syncOffsetMs = syncOffsetMs,
                                            onSyncOffsetChange = { viewModel.setLyricsOffset(it) },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    2 -> { // Related
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "関連コンテンツは現在利用できません",
                                                color = Color.White.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }
                        }"""

right_pane_replacement = """                        // Right pane: Dedicated to Lyrics
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(end = dimens.screenMargin)
                                .clip(RoundedCornerShape(32.dp))
                                .background(Color.Transparent)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
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
                                    isPlaying = uiState.isPlaying,
                                    onPlayPauseClick = onPlayPauseClick,
                                    duration = uiState.duration.toLong(),
                                    onCollapse = { viewModel.setLyricsModeEnabled(false) },
                                    syncOffsetMs = syncOffsetMs,
                                    onSyncOffsetChange = { viewModel.setLyricsOffset(it) },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }"""
content = content.replace(right_pane_target, right_pane_replacement)

# 3. Allow SlidingQueueDrawer to render globally on tablet too (so it can be toggled by the new Queue Button)
queue_drawer_target = """        // --- CUSTOM EMBEDDED SLIDING PLAY QUEUE ---
        if (!isTablet) {
            SlidingQueueDrawer("""

queue_drawer_replacement = """        // --- CUSTOM EMBEDDED SLIDING PLAY QUEUE ---
        if (true) {
            SlidingQueueDrawer("""
content = content.replace(queue_drawer_target, queue_drawer_replacement)

content = content.replace("import androidx.compose.material.icons.rounded.Visibility\nimport androidx.compose.material.icons.rounded.VisibilityOff\n", "import androidx.compose.material.icons.rounded.Visibility\nimport androidx.compose.material.icons.rounded.VisibilityOff\nimport androidx.compose.material.icons.rounded.QueueMusic\n")

with open(path, "w") as f:
    f.write(content)
print("Right pane refactored to lyrics only, and Queue button added.")
