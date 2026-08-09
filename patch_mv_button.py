import re

file_path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(file_path, "r") as f:
    content = f.read()

# Remove MV button from the old position
old_mv_button = """                if (musicVideoUrl != null) {
                    PillChip(
                        icon = painterResource(id = R.drawable.ic_music_video),
                        label = "MV",
                        isSelected = isMvMode,
                        onClick = { viewModel.toggleMvMode() },
                        contentColor = contentColor,
                        accentColor = dynamicAccentColor,
                        isDark = isDark,
                        customBackgroundColor = pillBg
                    )
                }

"""

if old_mv_button in content:
    content = content.replace(old_mv_button, "")
else:
    print("Failed to find old MV button")

# Insert MV button before SplitLikeDislikeChip
like_button_target = """                SplitLikeDislikeChip(
                    isLiked = uiState.isCurrentSongFavorite,
                    isDisliked = uiState.isCurrentSongDisliked,"""

new_mv_button_plus_like = """                if (musicVideoUrl != null) {
                    PillChip(
                        icon = painterResource(id = R.drawable.ic_music_video),
                        label = "MV",
                        isSelected = isMvMode,
                        onClick = { viewModel.toggleMvMode() },
                        contentColor = contentColor,
                        accentColor = dynamicAccentColor,
                        isDark = isDark,
                        customBackgroundColor = pillBg
                    )
                }

                SplitLikeDislikeChip(
                    isLiked = uiState.isCurrentSongFavorite,
                    isDisliked = uiState.isCurrentSongDisliked,"""

if like_button_target in content:
    content = content.replace(like_button_target, new_mv_button_plus_like)
else:
    print("Failed to find SplitLikeDislikeChip")

with open(file_path, "w") as f:
    f.write(content)
print("Patched FullPlayer.kt")
