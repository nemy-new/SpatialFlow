package com.codetrio.overdrive.ui.player.themes

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * High-performance real-time audio energy data model for music visualizers.
 */
data class VisualizerAudioData(
    val subBass: Float = 0f,
    val bass: Float = 0f,
    val mid: Float = 0f,
    val high: Float = 0f,
    val overallEnergy: Float = 0f,
    val timestampMs: Long = 0L
)

/**
 * Shared event bus that receives instantaneous audio frequency energy updates
 * directly from AudioPlaybackService's real-time DSP pipeline.
 */
object PlayerVisualizerBus {
    private val _audioData = MutableStateFlow(VisualizerAudioData())
    val audioData: StateFlow<VisualizerAudioData> = _audioData.asStateFlow()

    fun update(subBass: Float, bass: Float, mid: Float, high: Float) {
        val overall = ((subBass * 0.35f + bass * 0.35f + mid * 0.20f + high * 0.10f)).coerceIn(0f, 1f)
        _audioData.value = VisualizerAudioData(
            subBass = subBass.coerceIn(0f, 1f),
            bass = bass.coerceIn(0f, 1f),
            mid = mid.coerceIn(0f, 1f),
            high = high.coerceIn(0f, 1f),
            overallEnergy = overall,
            timestampMs = System.currentTimeMillis()
        )
    }

    fun reset() {
        _audioData.value = VisualizerAudioData()
    }
}
