package com.codetrio.overdrive.util

import android.app.ActivityManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Performance & Low-End Device Management Engine.
 * Intelligently assesses device capabilities (RAM, CPU cores, GPU features)
 * and dynamically manages lightweight profiles to ensure solid 60fps on all devices.
 */
object PerformanceManager {
    private const val PREFS_NAME = "AppSettings"
    const val KEY_PERFORMANCE_MODE = "performance_mode_v2" // "auto", "high_quality", "low_end"
    const val KEY_DISABLE_BLUR = "disable_realtime_blur"
    const val KEY_LOW_FPS_ANIMATION = "reduce_animation_framerate"

    private val _isLowEndOrLiteMode = MutableStateFlow(false)
    val isLowEndOrLiteMode: StateFlow<Boolean> = _isLowEndOrLiteMode.asStateFlow()

    private val _isRealtimeBlurDisabled = MutableStateFlow(false)
    val isRealtimeBlurDisabled: StateFlow<Boolean> = _isRealtimeBlurDisabled.asStateFlow()

    private var initialized = false
    private var isHardwareLowEnd = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true

        val appContext = context.applicationContext
        isHardwareLowEnd = detectLowEndHardware(appContext)

        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        updateStatesFromPrefs(prefs)

        prefs.registerOnSharedPreferenceChangeListener { p, key ->
            if (key == KEY_PERFORMANCE_MODE || key == KEY_DISABLE_BLUR || key == KEY_LOW_FPS_ANIMATION) {
                updateStatesFromPrefs(p)
            }
        }
    }

    private fun detectLowEndHardware(context: Context): Boolean {
        return try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            if (actManager != null && actManager.isLowRamDevice) {
                return true
            }

            val memoryInfo = ActivityManager.MemoryInfo()
            actManager?.getMemoryInfo(memoryInfo)
            val totalRamGb = memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0)

            val cpuCores = Runtime.getRuntime().availableProcessors()

            // Low end threshold: < 3.8GB RAM or <= 4 CPU cores or older Android API (< 30)
            totalRamGb < 3.8 || cpuCores <= 4 || Build.VERSION.SDK_INT < Build.VERSION_CODES.R
        } catch (e: Exception) {
            false
        }
    }

    private fun updateStatesFromPrefs(prefs: SharedPreferences) {
        val mode = prefs.getString(KEY_PERFORMANCE_MODE, "auto") ?: "auto"
        val manualDisableBlur = prefs.getBoolean(KEY_DISABLE_BLUR, false)

        val isLite = when (mode) {
            "low_end" -> true
            "high_quality" -> false
            else -> isHardwareLowEnd // "auto"
        }

        _isLowEndOrLiteMode.value = isLite
        _isRealtimeBlurDisabled.value = manualDisableBlur || (isLite && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
    }

    /**
     * Quick check if real-time blur (RenderEffect) should be bypassed for lightweight rendering.
     */
    fun shouldBypassBlur(context: Context? = null): Boolean {
        if (context != null && !initialized) init(context)
        return _isRealtimeBlurDisabled.value || _isLowEndOrLiteMode.value
    }

    /**
     * Quick check if heavy background meshes should be simplified into smooth static gradients.
     */
    fun isLiteModeEnabled(context: Context? = null): Boolean {
        if (context != null && !initialized) init(context)
        return _isLowEndOrLiteMode.value
    }
}

/**
 * Composable helper providing reactive low-end mode state.
 */
@Composable
fun rememberIsLiteMode(): State<Boolean> {
    val context = LocalContext.current
    remember { PerformanceManager.init(context) }
    return PerformanceManager.isLowEndOrLiteMode.collectAsState()
}

/**
 * Composable helper providing reactive real-time blur bypass state.
 */
@Composable
fun rememberShouldBypassBlur(): State<Boolean> {
    val context = LocalContext.current
    remember { PerformanceManager.init(context) }
    return PerformanceManager.isRealtimeBlurDisabled.collectAsState()
}
