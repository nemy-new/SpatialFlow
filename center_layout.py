import os

def main():
    player_compose_path = './app/src/main/java/com/codetrio/overdrive/ui/PlayerBottomSheetCompose.kt'
    with open(player_compose_path, 'r') as f:
        content = f.read()
        
    old_base_cover_y = """                        val baseCoverY = if (isTablet) {
                            statusBarTopPx + with(density) { 24.dp.toPx() }
                        } else {"""
                        
    new_base_cover_y = """                        val baseCoverY = if (isTablet) {
                            val controlsHeightDp = 268.dp
                            val totalGroupHeightDp = albumArtSizeDp + controlsHeightDp
                            val availableHeightDp = screenHeight.dp - with(density) { statusBarTopPx.toDp() }
                            val tabletTopOffsetDp = with(density) { statusBarTopPx.toDp() } + ((availableHeightDp - totalGroupHeightDp) / 2f).coerceAtLeast(16.dp)
                            with(density) { tabletTopOffsetDp.toPx() }
                        } else {"""
                        
    content = content.replace(old_base_cover_y, new_base_cover_y)
    
    with open(player_compose_path, 'w') as f:
        f.write(content)

    full_player_path = './app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt'
    with open(full_player_path, 'r') as f:
        content = f.read()
        
    old_tablet_top_offset = """                val tabletHeaderHeightDp = statusBarTopDp + 12.dp
                val tabletAvailableHeightDp = screenHeight - tabletHeaderHeightDp
                val tabletCenterYDp = tabletHeaderHeightDp + (tabletAvailableHeightDp / 2f)
                val tabletTopOffset = tabletCenterYDp - (albumArtSize / 2f)"""
                
    new_tablet_top_offset = """                val controlsHeightDp = 268.dp
                val totalGroupHeightDp = albumArtSize + controlsHeightDp
                val availableHeightDp = screenHeight - statusBarTopDp
                val tabletTopOffset = statusBarTopDp + ((availableHeightDp - totalGroupHeightDp) / 2f).coerceAtLeast(16.dp)"""
                
    content = content.replace(old_tablet_top_offset, new_tablet_top_offset)
    
    old_spacer = "Spacer(modifier = Modifier.height(statusBarTopDp + 24.dp))"
    new_spacer = "Spacer(modifier = Modifier.height(tabletTopOffset))"
    
    content = content.replace(old_spacer, new_spacer)
    
    old_lyrics = """                                            onPlayPauseClick = onPlayPauseClick,
                                            duration = uiState.duration.toLong(),
                                            modifier = Modifier.fillMaxSize()
                                        )"""
                                        
    new_lyrics = """                                            onPlayPauseClick = onPlayPauseClick,
                                            duration = uiState.duration.toLong(),
                                            isEmbedded = true,
                                            modifier = Modifier.fillMaxSize()
                                        )"""
                                        
    content = content.replace(old_lyrics, new_lyrics)
    
    with open(full_player_path, 'w') as f:
        f.write(content)
        
    print("Success")

if __name__ == "__main__":
    main()
