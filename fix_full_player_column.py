import re

path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

target_column = """            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = dimens.screenMargin, vertical = dimens.smallPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {"""

replacement_column = """            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets.statusBars)
                    .windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets.navigationBars.only(androidx.compose.foundation.layout.WindowInsetsSides.Vertical))
                    .padding(vertical = dimens.smallPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {"""

content = content.replace(target_column, replacement_column)

target_header = """                if (!isTablet) {
                    // Header Row (Nav controls + collapse) - Symmetric centering
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),"""

replacement_header = """                if (!isTablet) {
                    // Header Row (Nav controls + collapse) - Symmetric centering
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimens.screenMargin)
                            .height(56.dp),"""

content = content.replace(target_header, replacement_header)

with open(path, "w") as f:
    f.write(content)

print("Column modifier fixed")
