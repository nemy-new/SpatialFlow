def fix_pbs():
    file_path = "app/src/main/java/com/codetrio/overdrive/ui/PlayerBottomSheetCompose.kt"
    with open(file_path, "r") as f:
        content = f.read()
    
    # insert val isTablet = screenWidth >= 600 before val isLandscape
    if "val isTablet =" not in content:
        content = content.replace("val isLandscape = LocalConfiguration.current", "val isTablet = screenWidth >= 600\n                    val isLandscape = LocalConfiguration.current")
    with open(file_path, "w") as f:
        f.write(content)

def fix_fp():
    file_path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
    with open(file_path, "r") as f:
        content = f.read()

    # insert val isTablet = configuration.screenWidthDp >= 600 before val isLandscape
    if "val isTablet =" not in content:
        content = content.replace("val isLandscape = configuration.orientation", "val isTablet = configuration.screenWidthDp >= 600\n        val isLandscape = configuration.orientation")
    with open(file_path, "w") as f:
        f.write(content)

fix_pbs()
fix_fp()
print("Fixed!")
