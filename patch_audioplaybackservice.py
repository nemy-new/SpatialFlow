import re

file_path = "app/src/main/java/com/codetrio/overdrive/service/AudioPlaybackService.kt"
with open(file_path, "r") as f:
    content = f.read()

target = """        if (uri.scheme == "innertube") {
            val videoId = uri.host ?: return"""
replacement = """        if (uri.scheme == "innertube") {
            val videoId = uri.host ?: uri.authority ?: uri.toString().substringAfter("innertube://")
            if (videoId.isEmpty()) return"""

if target in content:
    content = content.replace(target, replacement)
    print("Patched AudioPlaybackService.kt")
else:
    print("Could not find target string in AudioPlaybackService.kt")

with open(file_path, "w") as f:
    f.write(content)

