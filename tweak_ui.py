import re

def modify_full_player():
    path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
    with open(path, "r") as f:
        content = f.read()

    # 1. Remove Header Row
    # We find the exact block:
    header_row = """                // Header Row (Nav controls + collapse) - Symmetric centering
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

                    Spacer(modifier = Modifier.size(48.dp))

                    Spacer(modifier = Modifier.size(48.dp))
                }"""
    if header_row in content:
        content = content.replace(header_row, "")
        print("Header Row removed")
    else:
        print("Header Row NOT found!")

    # 2. Remove verticalScroll from left pane Column
    scroll_mod = """                        // Left pane: Artwork and Controls
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally // Center aligned for tablet!
                        ) {"""
    new_scroll_mod = """                        // Left pane: Artwork and Controls
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally // Center aligned for tablet!
                        ) {"""
    if scroll_mod in content:
        content = content.replace(scroll_mod, new_scroll_mod)
        print("Vertical scroll removed from left pane")
    else:
        print("Vertical scroll mod NOT found!")

    # 3. Change title font size from 32.sp to 24.sp
    title_text = """                                text = uiState.currentSong?.title ?: "Unknown Title",
                                style = MaterialTheme.typography.headlineMediumEmphasized.copy(
                                    fontFamily = if (playerTheme == "immersion") com.codetrio.overdrive.ui.theme.GoogleSansFlexImmersion else MaterialTheme.typography.headlineMediumEmphasized.fontFamily,
                                    fontSize = 32.sp
                                ),"""
    new_title_text = """                                text = uiState.currentSong?.title ?: "Unknown Title",
                                style = MaterialTheme.typography.headlineMediumEmphasized.copy(
                                    fontFamily = if (playerTheme == "immersion") com.codetrio.overdrive.ui.theme.GoogleSansFlexImmersion else MaterialTheme.typography.headlineMediumEmphasized.fontFamily,
                                    fontSize = 24.sp
                                ),"""
    if title_text in content:
        content = content.replace(title_text, new_title_text)
        print("Title font size changed to 24.sp")
    else:
        print("Title text NOT found!")

    with open(path, "w") as f:
        f.write(content)

def modify_pbs():
    path = "app/src/main/java/com/codetrio/overdrive/ui/PlayerBottomSheetCompose.kt"
    with open(path, "r") as f:
        content = f.read()

    # Update availableHeight and baseY in yEndPx
    old_base_cover = """                        val baseCoverY = if (isTablet) {
                            val availableHeight = containerHeight - with(density) { statusBarTopPx.toDp() + navBarBottomPx.toDp() + (dimens.smallPadding * 2) + 56.dp }
                            val blockHeight = albumArtSizeDp + 280.dp
                            val topOffsetDp = 0.dp
                            val baseY = statusBarTopPx + with(density) { (dimens.smallPadding * 2).toPx() + 56.dp.toPx() }
                            baseY + with(density) { topOffsetDp.toPx() }
                        } else {"""
    new_base_cover = """                        val baseCoverY = if (isTablet) {
                            val availableHeight = containerHeight - with(density) { statusBarTopPx.toDp() + navBarBottomPx.toDp() + (dimens.smallPadding * 2) }
                            val blockHeight = albumArtSizeDp + 280.dp
                            val topOffsetDp = 0.dp
                            val baseY = statusBarTopPx + with(density) { dimens.smallPadding.toPx() }
                            baseY + with(density) { topOffsetDp.toPx() }
                        } else {"""
    if old_base_cover in content:
        content = content.replace(old_base_cover, new_base_cover)
        print("PBS baseY and availableHeight updated")
    else:
        print("PBS baseY NOT found!")

    with open(path, "w") as f:
        f.write(content)

modify_full_player()
modify_pbs()
print("Done tweaking UI")
