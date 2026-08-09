import re

path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

# 1. Erase the NOW PLAYING header row
header_start = content.find("// Header Row (Nav controls + collapse) - Symmetric centering")
header_end = content.find("val controlsHeightDp = 268.dp")
if header_start != -1 and header_end != -1:
    header_block = content[header_start:header_end]
    content = content.replace(header_block, "")
    print("Removed NOW PLAYING header")

# 2. Add Spacer before Row, remove Spacer inside Left Column, adjust Right Column height
# Find where isTablet block starts
tablet_block_start_str = "if (isTablet) {"
tablet_block_start = content.find(tablet_block_start_str)

target_tablet = """                if (isTablet) {
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
                            Spacer(modifier = Modifier.height(tabletTopOffset))"""

replacement_tablet = """                val rowTopAbsolute = statusBarTopDp + dimens.smallPadding
                val topSpacerHeight = (tabletTopOffset - rowTopAbsolute).coerceAtLeast(0.dp)

                if (isTablet) {
                    Spacer(modifier = Modifier.height(topSpacerHeight))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimens.screenMargin),
                        verticalAlignment = Alignment.Top // Align tops perfectly
                    ) {
                        // Left pane: Artwork and Controls
                        Column(
                            modifier = Modifier
                                .weight(1f),
                            horizontalAlignment = Alignment.Start
                        ) {"""

if target_tablet in content:
    content = content.replace(target_tablet, replacement_tablet)
    print("Fixed left pane top spacer and alignment")
else:
    print("Could not find target_tablet block")

# 3. Set Right Pane height to match Left Pane
target_right_pane = """                        // Right pane: Tabs and Content
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(end = dimens.screenMargin)
                                .clip(RoundedCornerShape(32.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f))
                        ) {"""

replacement_right_pane = """                        // Right pane: Tabs and Content
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(totalGroupHeightDp) // Match left pane height precisely
                                .padding(end = dimens.screenMargin)
                                .clip(RoundedCornerShape(32.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f))
                        ) {"""

if target_right_pane in content:
    content = content.replace(target_right_pane, replacement_right_pane)
    print("Fixed right pane height")
else:
    print("Could not find target_right_pane block")

# Let's also check the `} else {` block to make sure phone layout doesn't break.
# In the `} else {` block, we had `Spacer(modifier = Modifier.height(topOffset - (statusBarTopDp + 68.dp)))`
# Since we removed the 56.dp header, `topOffset` might need adjustment.
# `topOffset` is `((screenHeight - albumArtSize) / 2f - 220.dp).coerceAtLeast(minTopOffset)`
# Wait, `topOffset` (for phone) calculation is earlier in the file.
# The `else` block spacer:
# `Spacer(modifier = Modifier.height(topOffset - (statusBarTopDp + 68.dp)))`
# This was compensating for the 56.dp header + 12.dp padding.
# If we removed the header, we should probably change it to:
# `Spacer(modifier = Modifier.height((topOffset - rowTopAbsolute).coerceAtLeast(0.dp)))`

else_block_target = """                } else {
                    Spacer(modifier = Modifier.height(topOffset - (statusBarTopDp + 68.dp)))"""

else_block_replacement = """                } else {
                    Spacer(modifier = Modifier.height((topOffset - rowTopAbsolute).coerceAtLeast(0.dp)))"""

if else_block_target in content:
    content = content.replace(else_block_target, else_block_replacement)
    print("Fixed else block spacer")

with open(path, "w") as f:
    f.write(content)
print("Done")
