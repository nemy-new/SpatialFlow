import re

file_path = "app/src/main/java/com/codetrio/overdrive/MainActivity.kt"
with open(file_path, "r") as f:
    content = f.read()

# 1. Add floatingNavBar variable
content = re.sub(
    r'(val hideNavLabels by prefs\.observeKey\("hide_nav_labels", false\))',
    r'\1\n                        val floatingNavBar by prefs.observeKey("floating_nav_bar", false)',
    content
)

# 2. Replace NavigationBar block
old_mod = """                        val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

                        NavigationBar(
                            modifier = Modifier
                                .graphicsLayer {
                                    val currentBase = animatedBaseTranslationState.value
                                    val currentFraction = playerExpansionFractionState.value
                                    val translationY = (currentBase + (totalSlideDistPx * currentFraction)).coerceAtMost(totalSlideDistPx)
                                    this.translationY = translationY
                                    this.alpha = if (translationY >= totalSlideDistPx - 1f) 0f else 1f
                                }
                                .height(navBarHeight + bottomPadding)
                                .onGloballyPositioned { coordinates ->
                                    val height = coordinates.size.height.toFloat()
                                    playerViewModel.setBottomNavHeight(height)
                                },
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            tonalElevation = navElevation
                        ) {"""

new_mod = """                        val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

                        androidx.compose.material3.Surface(
                            modifier = Modifier
                                .graphicsLayer {
                                    val currentBase = animatedBaseTranslationState.value
                                    val currentFraction = playerExpansionFractionState.value
                                    val translationY = (currentBase + (totalSlideDistPx * currentFraction)).coerceAtMost(totalSlideDistPx)
                                    this.translationY = translationY
                                    this.alpha = if (translationY >= totalSlideDistPx - 1f) 0f else 1f
                                }
                                .onGloballyPositioned { coordinates ->
                                    val height = coordinates.size.height.toFloat()
                                    playerViewModel.setBottomNavHeight(height)
                                }
                                .then(
                                    if (floatingNavBar) {
                                        Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp + bottomPadding, top = 8.dp)
                                    } else {
                                        Modifier
                                    }
                                ),
                            shape = if (floatingNavBar) androidx.compose.foundation.shape.RoundedCornerShape(32.dp) else androidx.compose.ui.graphics.RectangleShape,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            tonalElevation = navElevation,
                            shadowElevation = if (floatingNavBar) 12.dp else navElevation
                        ) {
                            NavigationBar(
                                modifier = Modifier.height(if (floatingNavBar) navBarHeight else navBarHeight + bottomPadding),
                                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                                tonalElevation = 0.dp
                            ) {"""

if old_mod in content:
    content = content.replace(old_mod, new_mod)
    with open(file_path, "w") as f:
        f.write(content)
    print("Success")
else:
    print("Failed to find block")
