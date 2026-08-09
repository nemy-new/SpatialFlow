import re

path = "./app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

# Fix 1: Left pane target
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

# We need to find the end of the left pane column. The left pane column ends right before:
# "                        // Right pane: Tabs and Content"
end_regex = r"(\s+)(\} // End of left column)?(\s+)// Right pane: Tabs and Content"
# Wait, I can just use regex substitution.
pattern = r"(\s+)(// Right pane: Tabs and Content)"
replacement = r"\1}\2" # Close the AnimatedVisibility

if left_pane_target in content:
    content = content.replace(left_pane_target, new_left_pane)
    content = re.sub(pattern, replacement, content, count=1)
    print("Left pane wrapped in AnimatedVisibility")

# Fix 2: Tab row box closure
# The Tab row is now:
#                             Box(...) {
#                                 IconButton(...)
#                                 Row(...) { ... } // Original row
# We need to close the Box where the original Row ended.
# The original Row ended right before:
# "                            // Content area based on selected tab"
tab_end_pattern = r"(\s+)(// Content area based on selected tab)"
tab_end_replacement = r"\1}\1\2"

if "IconButton" in content: # Means we already replaced the start
    content = re.sub(tab_end_pattern, tab_end_replacement, content, count=1)
    print("Tab row Box closed")


with open(path, "w") as f:
    f.write(content)
