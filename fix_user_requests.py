import re

# 1. Modify FullPlayer.kt to remove the smartphone Header Row
path_full = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path_full, "r") as f:
    content = f.read()

# We want to remove this block:
#                 if (!isTablet) {
#                     // Header Row (Nav controls + collapse) - Symmetric centering
#                     Row(
# ...
#                     }
#                 }
target_header = r"""                if \(!isTablet\) \{
                    // Header Row \(Nav controls \+ collapse\) - Symmetric centering
                    Row\(
                        modifier = Modifier
                            \.fillMaxWidth\(\)
                            \.padding\(horizontal = dimens\.screenMargin\)
                            \.height\(56\.dp\),
                        horizontalArrangement = Arrangement\.SpaceBetween,
                        verticalAlignment = Alignment\.CenterVertically
                    \) \{
                        IconButton\(onClick = onCollapse\) \{
                            Icon\(
                                painter = painterResource\(id = R\.drawable\.ic_keyboard_arrow_down\),
                                contentDescription = "Collapse Player",
                                tint = contentColor\.copy\(alpha = 0\.8f\),
                                modifier = Modifier\.size\(28\.dp\)
                            \)
                        \}
    
                        if \(!hasCanvas \|\| isLyricsModeEnabled\) \{
                            Text\(
                                text = "NOW PLAYING",
                                style = MaterialTheme\.typography\.labelLarge,
                                fontWeight = FontWeight\.Bold,
                                color = contentSecondary
                            \)
                        \} else \{
                            Spacer\(modifier = Modifier\.size\(48\.dp\)\)
                        \}
    
                        Spacer\(modifier = Modifier\.size\(48\.dp\)\)
                    \}
                \}"""

# Replace it with nothing, but we should make sure we don't break the layout spacer logic.
# Wait, since we are doing `rowTopAbsolute = statusBarTopDp + dimens.smallPadding + 56.dp` in FullPlayerPhoneLayout,
# and we are now DELETING the 56.dp Row, we MUST adjust `rowTopAbsolute` or keep a Spacer(height=56.dp) so the layout stays identical!
# Actually, the user just says "remove the symbol and NOW PLAYING text". 
# It's better to just keep a `Spacer(modifier = Modifier.height(56.dp))` so that the layout math is unchanged.

replacement_header = """                if (!isTablet) {
                    Spacer(modifier = Modifier.height(56.dp))
                }"""

content_new = re.sub(target_header, replacement_header, content)

if content_new == content:
    print("WARNING: Header Row not found or not replaced in FullPlayer.kt")
else:
    print("Header Row replaced in FullPlayer.kt")
    with open(path_full, "w") as f:
        f.write(content_new)

# 2. Modify PlayerBottomSheetCompose.kt to hide the small album art in immersion mode
path_compose = "app/src/main/java/com/codetrio/overdrive/ui/PlayerBottomSheetCompose.kt"
with open(path_compose, "r") as f:
    content2 = f.read()

target_is_mv = """                    val isMvFullscreen by viewModel.isMvFullscreen.collectAsStateWithLifecycle()"""
replacement_is_mv = """                    val isMvMode by viewModel.isMvMode.collectAsStateWithLifecycle()
                    val isMvFullscreen by viewModel.isMvFullscreen.collectAsStateWithLifecycle()"""

content2_new = content2.replace(target_is_mv, replacement_is_mv)
if content2_new == content2:
    print("WARNING: isMvFullscreen not found in PlayerBottomSheetCompose.kt")

target_z_index = """.zIndex(if (isQueueExpanded) 1f else if (isLyricsModeEnabled || lyricsArtworkProgress > 0f) 6f else 3f)"""
replacement_z_index = """.zIndex(if (isQueueExpanded) 1f else if (isLyricsModeEnabled || lyricsArtworkProgress > 0f) 6f else if (playerTheme == "immersion" && !isMvMode) -1f else 3f)"""

content2_new2 = content2_new.replace(target_z_index, replacement_z_index)
if content2_new2 == content2_new:
    print("WARNING: zIndex not found in PlayerBottomSheetCompose.kt")
else:
    print("zIndex replaced in PlayerBottomSheetCompose.kt")
    with open(path_compose, "w") as f:
        f.write(content2_new2)

print("Done")
