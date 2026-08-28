package com.codetrio.overdrive.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codetrio.overdrive.data.lyrics.LyricsResult
import com.codetrio.overdrive.model.SongItem
import com.codetrio.overdrive.ui.theme.GoogleSansFlex
import com.codetrio.overdrive.ui.theme.GoogleSansFlexNonRounded

/**
 * High-legibility, Apple Music-styled Plain (Static/Un-synced) Lyrics View.
 * Renders original lyrics with high contrast and translated subtext beneath each line.
 */
@Composable
fun PlainLyricsView(
    plainLyrics: String,
    translatedPlainLyrics: String? = null,
    currentSong: SongItem?,
    selectedProvider: String?,
    providerResults: Map<String, LyricsResult>,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    val originalLines = remember(plainLyrics) { plainLyrics.lines() }
    val translatedLines = remember(translatedPlainLyrics) { translatedPlainLyrics?.lines() }
    val lyricsFontFamily = com.codetrio.overdrive.ui.theme.rememberCustomFontFamily(com.codetrio.overdrive.data.font.FontTarget.LYRICS)

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            originalLines.forEachIndexed { index, originalLine ->
                val trimmed = originalLine.trim()
                if (trimmed.isBlank()) {
                    Spacer(modifier = Modifier.height(18.dp))
                } else {
                    val translation = translatedLines?.getOrNull(index)?.trim()?.takeIf { it.isNotBlank() }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Original Lyric Line (Bold, Primary Prominence)
                        Text(
                            text = trimmed,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = lyricsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp,
                                lineHeight = 29.sp
                            ),
                            color = contentColor.copy(alpha = 0.95f),
                            softWrap = true
                        )

                        // Translated Subtext (Smooth, High Legibility)
                        if (!translation.isNullOrBlank()) {
                            Text(
                                text = translation,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = lyricsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 17.sp,
                                    lineHeight = 23.sp
                                ),
                                color = contentColor.copy(alpha = 0.70f),
                                softWrap = true
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Metadata Footer
        LyricsMetadataFooter(
            currentSong = currentSong,
            selectedProvider = selectedProvider,
            providerResults = providerResults,
            contentColor = contentColor
        )
    }
}
