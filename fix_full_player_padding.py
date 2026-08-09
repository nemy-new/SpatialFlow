path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

old_row = """Row(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    )"""
new_row = """Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = dimens.screenMargin),
                        verticalAlignment = Alignment.CenterVertically
                    )"""
if old_row in content:
    content = content.replace(old_row, new_row)
else:
    print("old_row not found")

with open(path, "w") as f:
    f.write(content)
