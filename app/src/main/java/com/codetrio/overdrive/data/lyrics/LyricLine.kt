package com.codetrio.overdrive.data.lyrics

import androidx.compose.runtime.Immutable

/**
 * Data model for a single line of synced lyrics.
 * Supports standard line-sync and enhanced word-by-word sync.
 *
 * Uses @JvmField for direct field access from Java (LyricsAdapter, LrcParser callers).
 * Uses @JvmOverloads to generate Java-visible constructor overloads.
 */
@Immutable
data class LyricLine @JvmOverloads constructor(
    @JvmField val startTimeMs: Long,
    @JvmField val content: String,
    @JvmField val isInterlude: Boolean = false,
    @JvmField val isWordByWord: Boolean = false,
    @JvmField val words: List<LyricWord> = emptyList(),
    @JvmField val isBackground: Boolean = false,
    @JvmField val backgroundContent: String? = null,
    @JvmField val backgroundWords: List<LyricWord> = emptyList(),
    @JvmField val translatedContent: String? = null

) : Comparable<LyricLine> {

    override fun compareTo(other: LyricLine): Int {
        return this.startTimeMs.compareTo(other.startTimeMs)
    }
}

