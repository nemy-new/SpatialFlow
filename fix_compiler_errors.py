import re

def fix_full_player():
    path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
    with open(path, "r") as f:
        content = f.read()

    # Add import
    if "import androidx.compose.foundation.layout.navigationBars" not in content:
        content = content.replace("import androidx.compose.foundation.layout.statusBars",
                                  "import androidx.compose.foundation.layout.statusBars\nimport androidx.compose.foundation.layout.navigationBars")
        # If statusBars import doesn't exist, just put it at the top
        if "import androidx.compose.foundation.layout.navigationBars" not in content:
            content = content.replace("import androidx.compose.foundation.layout.fillMaxSize",
                                      "import androidx.compose.foundation.layout.fillMaxSize\nimport androidx.compose.foundation.layout.navigationBars")

    # Fix navigationBars usage
    content = content.replace("androidx.compose.foundation.layout.WindowInsets.navigationBars", "androidx.compose.foundation.layout.WindowInsets.Companion.navigationBars")
    # Actually, if we import it, we can just use WindowInsets.navigationBars!
    content = content.replace("androidx.compose.foundation.layout.WindowInsets.Companion.navigationBars", "androidx.compose.foundation.layout.WindowInsets.navigationBars")

    with open(path, "w") as f:
        f.write(content)

def fix_pbs():
    path = "app/src/main/java/com/codetrio/overdrive/ui/PlayerBottomSheetCompose.kt"
    with open(path, "r") as f:
        content = f.read()

    # Add import
    if "import androidx.compose.foundation.layout.navigationBars" not in content:
        content = content.replace("import androidx.compose.foundation.layout.statusBars",
                                  "import androidx.compose.foundation.layout.statusBars\nimport androidx.compose.foundation.layout.navigationBars")
        if "import androidx.compose.foundation.layout.navigationBars" not in content:
             content = content.replace("import androidx.compose.foundation.layout.fillMaxSize",
                                      "import androidx.compose.foundation.layout.fillMaxSize\nimport androidx.compose.foundation.layout.navigationBars")

    # Move dimens and navBarBottomPx to before xEndPx
    # Currently they are inside yEndPx remember block
    old_y_end = """val navBarBottomPx = WindowInsets.navigationBars.getBottom(density).toFloat()
                    val dimens = com.codetrio.overdrive.ui.theme.LocalDimens.current
                    
                    val yEndPx = remember(isTablet"""
    new_y_end = """val yEndPx = remember(isTablet"""
    if old_y_end in content:
        content = content.replace(old_y_end, new_y_end)
    
    # Now put them before xEndPx
    old_x_end = """val screenMarginPx = with(density) { dimens.screenMargin.toPx() }
                    val xEndPx = remember(isTablet"""
    new_x_end = """val navBarBottomPx = WindowInsets.navigationBars.getBottom(density).toFloat()
                    val dimens = com.codetrio.overdrive.ui.theme.LocalDimens.current
                    val screenMarginPx = with(density) { dimens.screenMargin.toPx() }
                    val xEndPx = remember(isTablet"""
    if old_x_end in content:
        content = content.replace(old_x_end, new_x_end)

    with open(path, "w") as f:
        f.write(content)

fix_full_player()
fix_pbs()
print("Fixed compiler errors")
