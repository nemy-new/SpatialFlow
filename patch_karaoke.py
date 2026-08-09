import re

with open('app/src/main/java/com/codetrio/overdrive/ui/player/KaraokeLyricsView.kt', 'r') as f:
    content = f.read()

replacement = """    val currentPositionProviderState by rememberUpdatedState(currentPositionProvider)
    val isPlayingProviderState by rememberUpdatedState(isPlayingProvider)
    val playbackSpeedProviderState by rememberUpdatedState(playbackSpeedProvider)

    val listState = rememberLazyListState()"""

content = content.replace("    val listState = rememberLazyListState()", replacement)

content = content.replace("val isPlaying = isPlayingProvider()", "val isPlaying = isPlayingProviderState()")
content = content.replace("val speed = playbackSpeedProvider()", "val speed = playbackSpeedProviderState()")
content = content.replace("val actualMs = currentPositionProvider().toFloat()", "val actualMs = currentPositionProviderState().toFloat()")

with open('app/src/main/java/com/codetrio/overdrive/ui/player/KaraokeLyricsView.kt', 'w') as f:
    f.write(content)

