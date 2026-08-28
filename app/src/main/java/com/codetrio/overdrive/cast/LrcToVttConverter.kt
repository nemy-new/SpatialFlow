package com.codetrio.overdrive.cast

import com.codetrio.overdrive.data.lyrics.LyricLine
import com.codetrio.overdrive.data.lyrics.LrcParser
import java.util.Locale

/**
 * Utility to convert synchronized lyrics (LRC string or List<LyricLine>) into standard WebVTT format.
 * This WebVTT format is fully compliant with Google Cast / Chromecast standard subtitle rendering.
 */
object LrcToVttConverter {

    /**
     * Converts a raw LRC string into standard WebVTT format.
     */
    fun convertLrcToVtt(lrcContent: String): String {
        val lines = LrcParser.parse(lrcContent)
        return convertLinesToVtt(lines)
    }

    /**
     * Converts a parsed list of [LyricLine] into standard WebVTT format.
     */
    fun convertLinesToVtt(lines: List<LyricLine>): String {
        if (lines.isEmpty()) {
            return "WEBVTT\n\n"
        }

        val sortedLines = lines.sortedBy { it.startTimeMs }
        val sb = StringBuilder()
        sb.append("WEBVTT\n\n")

        for (i in sortedLines.indices) {
            val current = sortedLines[i]
            val startMs = current.startTimeMs.coerceAtLeast(0L)
            
            // Calculate end time: start of next line or startMs + 4.5s
            val nextStartMs = if (i + 1 < sortedLines.size) {
                sortedLines[i + 1].startTimeMs
            } else {
                startMs + 5000L
            }
            val endMs = if (nextStartMs > startMs) {
                minOf(nextStartMs, startMs + 8000L)
            } else {
                startMs + 4000L
            }

            val text = current.content.trim()
            if (text.isEmpty() || current.isInterlude) continue

            val startTimeStr = formatVttTimestamp(startMs)
            val endTimeStr = formatVttTimestamp(endMs)

            sb.append("${i + 1}\n")
            sb.append("$startTimeStr --> $endTimeStr\n")
            sb.append(text)

            // If there's a translated line, append it as a second line
            current.translatedContent?.trim()?.let { trans ->
                if (trans.isNotEmpty()) {
                    sb.append("\n").append(trans)
                }
            }

            sb.append("\n\n")
        }

        return sb.toString()
    }

    /**
     * Formats milliseconds into WebVTT timestamp: `HH:MM:SS.mmm`
     */
    fun formatVttTimestamp(ms: Long): String {
        val hours = ms / 3_600_000L
        val minutes = (ms % 3_600_000L) / 60_000L
        val seconds = (ms % 60_000L) / 1000L
        val millis = ms % 1000L

        return String.format(Locale.US, "%02d:%02d:%02d.%03d", hours, minutes, seconds, millis)
    }
}
