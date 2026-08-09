import re

with open('app/src/main/java/com/codetrio/overdrive/viewmodel/PlayerSharedViewModel.kt', 'r') as f:
    content = f.read()

# Fix song.videoId to song.videoId!! or takeIf
content = content.replace(
    'lyricsSyncDao.insertSyncOffset(\n                com.codetrio.overdrive.data.db.LyricsSyncEntity(song.videoId, newOffset)\n            )',
    'song.videoId?.let { id ->\n                lyricsSyncDao.insertSyncOffset(\n                    com.codetrio.overdrive.data.db.LyricsSyncEntity(id, newOffset)\n                )\n            }'
)

content = content.replace(
    'lyricsSyncDao.insertSyncOffset(\n                com.codetrio.overdrive.data.db.LyricsSyncEntity(song.videoId, offsetMs)\n            )',
    'song.videoId?.let { id ->\n                lyricsSyncDao.insertSyncOffset(\n                    com.codetrio.overdrive.data.db.LyricsSyncEntity(id, offsetMs)\n                )\n            }'
)

content = content.replace(
    'val offset = lyricsSyncDao.getOffsetSync(song.videoId) ?: 0L',
    'val offset = song.videoId?.let { id -> lyricsSyncDao.getOffsetSync(id) } ?: 0L'
)


with open('app/src/main/java/com/codetrio/overdrive/viewmodel/PlayerSharedViewModel.kt', 'w') as f:
    f.write(content)

