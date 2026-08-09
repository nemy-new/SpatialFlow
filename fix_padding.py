import re

path = "./app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

target = """                        modifier = Modifier.fillMaxSize().padding(top = 32.dp),"""
replacement = """                        modifier = Modifier.fillMaxSize(),"""

content = content.replace(target, replacement)

with open(path, "w") as f:
    f.write(content)

print("Padding removed")
