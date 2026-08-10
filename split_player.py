import re

path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    text = f.read()

def find_matching_brace(s, start_idx):
    count = 0
    for i in range(start_idx, len(s)):
        if s[i] == '{':
            count += 1
        elif s[i] == '}':
            count -= 1
            if count == 0:
                return i
    return -1

# 1. Identify the rightPaneContent definition
right_pane_start = text.find("val rightPaneContent: @Composable () -> Unit = {")
right_pane_brace_idx = text.find("{", right_pane_start)
right_pane_end = find_matching_brace(text, right_pane_brace_idx)

right_pane_body = text[right_pane_brace_idx+1:right_pane_end].strip()

# 2. Identify the isTablet block
tablet_start = text.find("if (isTablet) {\n                    Spacer(modifier = Modifier.height(topSpacerHeight))")
tablet_brace_idx = text.find("{", tablet_start)
tablet_end = find_matching_brace(text, tablet_brace_idx)
tablet_body = text[tablet_brace_idx+1:tablet_end].strip()

# 3. Identify the AnimatedVisibility block which wraps them
anim_start = text.find("AnimatedVisibility(\n            visible = isTablet || !isLyricsModeEnabled,")
anim_brace_idx = text.find("{", anim_start)
anim_end = find_matching_brace(text, anim_brace_idx)

# We want to replace the inside of AnimatedVisibility with just:
# if (isTablet) {
#     FullPlayerTabletLayout(...)
# } else {
#     FullPlayerPhoneLayout(...)
# }
# But wait, what are the parameters?
# Let's just use Kotlin nested functions to avoid parameter hell entirely!
# If we define them inside the `FullPlayer` function right before `AnimatedVisibility`,
# they capture all the scope.

# But the user asked for "FullPlayerPhoneLayout" and "FullPlayerTabletLayout" functions.
nested_funcs = f"""
        @Composable
        fun FullPlayerPhoneLayout() {{
            Spacer(modifier = Modifier.height(topOffset - (statusBarTopDp + 68.dp)))

            // Album Art Container Placeholder (ArtworkPager is rendered at this absolute position)
            Box(
                modifier = Modifier.size(albumArtSize)
            )

            Spacer(modifier = Modifier.height(28.dp))

            {right_pane_body}
            
            // Lyrics Bottom Sheet for Phone
            LyricsBottomSheet(
                visible = isLyricsModeEnabled,
                currentSong = uiState.currentSong,
                syncedLyrics = syncedLyrics,
                plainLyrics = plainLyrics,
                isLoading = isLyricsLoading,
                lyricsError = lyricsError,
                currentPositionProvider = currentPositionProvider,
                contentReady = true,
                playerBackgroundColor = playerBackgroundColor,
                canvasArtwork = canvasArtwork,
                contentColor = contentColor,
                contentSecondary = contentSecondary,
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
                onCollapse = {{ viewModel.setLyricsModeEnabled(false) }},
                syncOffsetMs = 0L,
                onSyncOffsetChange = {{}},
                modifier = Modifier.fillMaxSize()
            )
        }}

        @Composable
        fun FullPlayerTabletLayout() {{
            {tablet_body}
        }}
"""

# Wait, `LyricsBottomSheet` is in the `if (!isTablet)` block below. 
# Let's replace the whole `AnimatedVisibility` block and the subsequent `if (!isTablet)` for Lyrics.
# Actually, `rightPaneContent` was defined earlier. Let's delete it.

# Delete rightPaneContent
new_text = text[:right_pane_start] + text[right_pane_end+1:]

# Now replace the `AnimatedVisibility` block.
anim_start_new = new_text.find("AnimatedVisibility(\n            visible = isTablet || !isLyricsModeEnabled,")
anim_brace_idx_new = new_text.find("{", anim_start_new)
anim_end_new = find_matching_brace(new_text, anim_brace_idx_new)

# Let's see what is inside the Column inside AnimatedVisibility
# We will just replace the Column's children
col_start = new_text.find("Column(\n                modifier = Modifier\n                    .fillMaxSize()", anim_start_new)
col_brace = new_text.find("{", col_start)
col_end = find_matching_brace(new_text, col_brace)

col_inner = f"""
                if (!isTablet) {{
                    // Header Row (Nav controls + collapse) - Symmetric centering
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimens.screenMargin)
                            .height(56.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {{
                        IconButton(onClick = onCollapse) {{
                            Icon(
                                painter = painterResource(id = R.drawable.ic_keyboard_arrow_down),
                                contentDescription = "Collapse Player",
                                tint = contentColor.copy(alpha = 0.8f),
                                modifier = Modifier.size(28.dp)
                            )
                        }}
    
                        if (!hasCanvas || isLyricsModeEnabled) {{
                            Text(
                                text = "NOW PLAYING",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = contentSecondary
                            )
                        }} else {{
                            Spacer(modifier = Modifier.size(48.dp))
                        }}
    
                        Spacer(modifier = Modifier.size(48.dp))
                    }}
                }}

                val controlsHeightDp = 300.dp
                val totalGroupHeightDp = albumArtSize + controlsHeightDp
                val availableHeightDp = screenHeight - statusBarTopDp
                val tabletTopOffset = statusBarTopDp + ((availableHeightDp - totalGroupHeightDp) / 2f).coerceAtLeast(16.dp)
                val rowTopAbsolute = statusBarTopDp + dimens.smallPadding
                val topSpacerHeight = (tabletTopOffset - rowTopAbsolute).coerceAtLeast(0.dp)

{nested_funcs}

                if (isTablet) {{
                    FullPlayerTabletLayout()
                }} else {{
                    FullPlayerPhoneLayout()
                }}
"""

new_text = new_text[:col_brace+1] + col_inner + new_text[col_end:]

# Now we need to remove the trailing `if (!isTablet) { LyricsBottomSheet(...) }` because we moved it into PhoneLayout
lyrics_bottom_sheet = """if (!isTablet) {
            LyricsBottomSheet(
            visible = isLyricsModeEnabled,
            currentSong = uiState.currentSong,
            syncedLyrics = syncedLyrics,
            plainLyrics = plainLyrics,
            isLoading = isLyricsLoading,
            lyricsError = lyricsError,
            currentPositionProvider = currentPositionProvider,
            contentReady = true,
            playerBackgroundColor = playerBackgroundColor,
            canvasArtwork = canvasArtwork,
            contentColor = contentColor,
            contentSecondary = contentSecondary,
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
            syncOffsetMs = 0L,
            onSyncOffsetChange = {},
            modifier = Modifier.fillMaxSize()
        )
        }"""
        
# Find the exact text ignoring whitespace differences
def remove_block(t, start_str):
    idx = t.find(start_str)
    if idx != -1:
        brace = t.find("{", idx)
        end = find_matching_brace(t, brace)
        return t[:idx] + t[end+1:]
    return t

new_text = remove_block(new_text, "if (!isTablet) {\n            LyricsBottomSheet(")

with open(path, "w") as f:
    f.write(new_text)

print("Refactored successfully")
