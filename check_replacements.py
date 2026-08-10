path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

print("Metadata Row present:", "modifier = Modifier.width(albumArtSize)" in content.split("// Metadata row: title/artist")[1][:100])
print("Chips Row present:", "modifier = Modifier" in content.split("// Premium YT Music style horizontal control chips row")[1][:100] and "width(albumArtSize)" in content.split("// Premium YT Music style horizontal control chips row")[1][:100])
print("Slider Box present:", "Box(modifier = Modifier.width(albumArtSize)) {" in content.split("// Premium Wavy Seek Bar (Isolated)")[1][:100])
print("ButtonGroup present:", "modifier = Modifier.width(albumArtSize)," in content.split("androidx.compose.material3.ButtonGroup(")[1][:100])
