package com.codetrio.overdrive.data.lyrics.providers

import android.media.MediaMetadataRetriever
import android.util.Log
import com.codetrio.overdrive.data.lyrics.LrcParser
import com.codetrio.overdrive.data.lyrics.LyricsNormalizer
import com.codetrio.overdrive.data.lyrics.LyricsResult
import com.codetrio.overdrive.data.lyrics.PaxsenixLyrics
import com.codetrio.overdrive.data.lyrics.TrackMetadata
import java.io.File

/**
 * Reads local lyrics from sidecar files (.lrc, .ttml, .srt) or embedded ID3 tags (USLT/SYLT).
 * Highest trust level since lyrics come from the local file itself.
 */
class EmbeddedLyricsProvider : LyricsProvider {

    companion object {
        private const val TAG = "EmbeddedLyricsProvider"
    }

    override fun getName(): String = "Local File / Tag"

    override fun getPriority(): Int = 0 // Absolute highest priority — local file, no network needed

    override fun search(track: TrackMetadata): LyricsResult? {
        if (track.filePath.isEmpty()) return null

        val audioFile = File(track.filePath)
        var rawContent: String? = null
        var sourceLabel = "Embedded Tag"

        // 1. Check sidecar files in the same directory (.lrc, .ttml, .srt)
        if (audioFile.exists()) {
            val parent = audioFile.parentFile
            val baseName = audioFile.nameWithoutExtension
            if (parent != null) {
                val lrcFile = File(parent, "$baseName.lrc")
                val ttmlFile = File(parent, "$baseName.ttml")
                val srtFile = File(parent, "$baseName.srt")

                val sidecar = when {
                    lrcFile.exists() -> lrcFile
                    ttmlFile.exists() -> ttmlFile
                    srtFile.exists() -> srtFile
                    else -> null
                }

                if (sidecar != null) {
                    try {
                        val text = sidecar.readText(Charsets.UTF_8).trim()
                        if (text.isNotBlank()) {
                            rawContent = text
                            sourceLabel = "Sidecar (${sidecar.extension.uppercase()})"
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to read sidecar file ${sidecar.absolutePath}: ${e.message}")
                    }
                }
            }
        }

        // 2. If no sidecar file found, check embedded ID3 metadata tag
        if (rawContent == null) {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(track.filePath)
                try {
                    rawContent = retriever.extractMetadata(29) // METADATA_KEY_LYRICS
                } catch (e: Exception) {
                    Log.w(TAG, "Could not extract lyrics metadata key: ${e.message}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed reading file: ${track.filePath} — ${e.message}")
            } finally {
                try {
                    retriever.release()
                } catch (_: Exception) {
                }
            }
        }

        if (rawContent.isNullOrBlank()) return null

        val trimmed = rawContent.trim()
        val parsedLines = LrcParser.parse(trimmed)
        val isSynced = parsedLines.isNotEmpty()
        val isWordByWord = isSynced && (parsedLines.any { it.isWordByWord } || PaxsenixLyrics.isWordByWord(trimmed))

        val result = LyricsResult(
            providerName = sourceLabel,
            matchedTitle = track.cleanedTitle,
            matchedArtist = track.cleanedArtist,
            matchedDuration = track.durationMs / 1000f,
            isSynced = isSynced,
            isWordByWord = isWordByWord
        )

        if (isSynced) {
            result.syncedLyrics = trimmed
            result.plainLyrics = null
        } else {
            result.syncedLyrics = null
            result.plainLyrics = LyricsNormalizer.extractCleanPlainText(trimmed)
        }

        Log.d(TAG, "Found local lyrics ($sourceLabel) for: ${track.cleanedTitle} (synced=$isSynced, wordByWord=$isWordByWord)")
        return result
    }
}
