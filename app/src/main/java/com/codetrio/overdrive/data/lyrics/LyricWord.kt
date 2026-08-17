package com.codetrio.overdrive.data.lyrics

import androidx.compose.runtime.Immutable

/**
 * Represents a single word within a synchronized lyric line.
 * Supports background vocal flags for synchronized pairing.
 */
@Immutable
data class LyricWord @JvmOverloads constructor(
    @JvmField val text: String,
    @JvmField val absoluteStartTimeMs: Long,
    @JvmField val durationMs: Long,
    @JvmField val charRange: IntRange = 0..0,
    @JvmField val isBackground: Boolean = false
)
