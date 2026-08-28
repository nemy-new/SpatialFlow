@file:Suppress("DEPRECATION")
@file:OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class
)

package com.codetrio.overdrive.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.SettingsBackupRestore
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material.icons.rounded.Vibration
import com.codetrio.overdrive.ui.onboarding.OnboardingScreen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.graphics.shapes.Morph
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraphBuilder
import com.codetrio.overdrive.BuildConfig
import com.codetrio.overdrive.MainActivity
import com.codetrio.overdrive.R
import com.codetrio.overdrive.composableWithBlur
import com.codetrio.overdrive.ui.explore.MorphShape
import com.codetrio.overdrive.ui.explore.rememberExpressiveShapeMorph
import com.codetrio.overdrive.ui.explore.shimmerEffect
import com.codetrio.overdrive.ui.player.SleepTimerBottomSheet
import com.codetrio.overdrive.viewmodel.PlayerSharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URLEncoder
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

private val SmoothSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMediumLow
)

// ═══════════════════════════════════════════════════════════════════════════════
// SETTINGS VIEW MODEL
// ═══════════════════════════════════════════════════════════════════════════════

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context: Context = application.applicationContext
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Theme Mode ────────────────────────────────────────────────────────────
    private val _themeMode = MutableStateFlow(
        prefs.getString(KEY_THEME_MODE, "system") ?: "system"
    )
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _darkMode = MutableStateFlow(
        _themeMode.value.let {
            if (it == "system") prefs.getBoolean(KEY_DARK_MODE, true) else it == "dark"
        }
    )
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        val isDark = mode == "dark"
        _darkMode.value = isDark
        prefs.edit {
            putString(KEY_THEME_MODE, mode)
            putBoolean(KEY_DARK_MODE, isDark)
        }
        val nightMode = when (mode) {
            "dark" -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            "light" -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
            else -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    fun setDarkMode(enabled: Boolean) {
        setThemeMode(if (enabled) "dark" else "light")
    }

    // ── YouTube Music Cookies ──────────────────────────────────────────────────
    private val _ytCookies = MutableStateFlow(prefs.getString("yt_cookies", null))
    val ytCookies: StateFlow<String?> = _ytCookies.asStateFlow()

    fun setYtCookies(cookies: String?) {
        _ytCookies.value = cookies
        if (cookies != null) {
            prefs.edit {putString("yt_cookies", cookies)}
            com.codetrio.overdrive.data.innertube.InnerTubeClient.cookie = cookies
        } else {
            prefs.edit {remove("yt_cookies")}
            com.codetrio.overdrive.data.innertube.InnerTubeClient.cookie = null
        }
    }

    // ── Haptics ──────────────────────────────────────────────────────────────
    private val _vibrationStrength =
        MutableStateFlow(prefs.getFloat(KEY_VIBRATION_STRENGTH, 80f))
    val vibrationStrength: StateFlow<Float> = _vibrationStrength.asStateFlow()

    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    val hasHaptics: Boolean = vibrator?.hasVibrator() == true

    fun setVibrationStrength(strength: Float) {
        _vibrationStrength.value = strength
        prefs.edit {putFloat(KEY_VIBRATION_STRENGTH, strength)}
        if (strength > 0 && hasHaptics && vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amplitude = (strength / 100f * 255).toInt().coerceIn(1, 255)
                vibrator.vibrate(VibrationEffect.createOneShot(50, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        }
    }

    // ── Lyrics Translation ───────────────────────────────────────────────────
    private val _lyricsTranslationEnabled = MutableStateFlow(
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(context).getBoolean("enable_lyrics_translation", false)
    )
    val lyricsTranslationEnabled: StateFlow<Boolean> = _lyricsTranslationEnabled.asStateFlow()

    fun setLyricsTranslationEnabled(enabled: Boolean) {
        _lyricsTranslationEnabled.value = enabled
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean("enable_lyrics_translation", enabled)
            .apply()
    }

    private val _lyricsTranslationEngine = MutableStateFlow(
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(context).getString("lyrics_translation_engine", "gemini_api") ?: "gemini_api"
    )
    val lyricsTranslationEngine: StateFlow<String> = _lyricsTranslationEngine.asStateFlow()

    fun setLyricsTranslationEngine(engine: String) {
        _lyricsTranslationEngine.value = engine
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString("lyrics_translation_engine", engine)
            .apply()
    }

    // ── Crossfade ────────────────────────────────────────────────────────────
    private val _crossfadeEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_CROSSFADE_ENABLED, false))
    val crossfadeEnabled: StateFlow<Boolean> = _crossfadeEnabled.asStateFlow()

    private val _crossfadeDuration =
        MutableStateFlow(prefs.getFloat(KEY_CROSSFADE_DURATION, 3f))
    val crossfadeDuration: StateFlow<Float> = _crossfadeDuration.asStateFlow()

    fun setCrossfadeEnabled(enabled: Boolean) {
        _crossfadeEnabled.value = enabled
        prefs.edit {putBoolean(KEY_CROSSFADE_ENABLED, enabled)}
    }

    fun setCrossfadeDuration(duration: Float) {
        _crossfadeDuration.value = duration
        prefs.edit { putFloat(KEY_CROSSFADE_DURATION, duration) }
    }

    // ── Live Updates & Synced Lyrics ─────────────────────────────────────────
    private val _liveUpdatesEnabled =
        MutableStateFlow(prefs.getBoolean("pref_live_updates_enabled", true))
    val liveUpdatesEnabled: StateFlow<Boolean> = _liveUpdatesEnabled.asStateFlow()

    fun setLiveUpdatesEnabled(enabled: Boolean) {
        _liveUpdatesEnabled.value = enabled
        prefs.edit { putBoolean("pref_live_updates_enabled", enabled) }
    }

    // ── Volume Normalization ─────────────────────────────────────────────────
    private val _volumeNormalizationEnabled = MutableStateFlow(prefs.getBoolean(KEY_VOLUME_NORMALIZATION_ENABLED, false))
    val volumeNormalizationEnabled: StateFlow<Boolean> = _volumeNormalizationEnabled.asStateFlow()

    private val _targetLufs = MutableStateFlow(prefs.getFloat(KEY_TARGET_LUFS, -14f))
    val targetLufs: StateFlow<Float> = _targetLufs.asStateFlow()

    fun setVolumeNormalizationEnabled(enabled: Boolean) {
        _volumeNormalizationEnabled.value = enabled
        prefs.edit {putBoolean(KEY_VOLUME_NORMALIZATION_ENABLED, enabled)}
    }

    fun setTargetLufs(lufs: Float) {
        _targetLufs.value = lufs
        prefs.edit {putFloat(KEY_TARGET_LUFS, lufs)}
    }

    // ── Audio Focus ──────────────────────────────────────────────────────────
    private val _audioFocus = MutableStateFlow(prefs.getBoolean(KEY_AUDIO_FOCUS, true))
    val audioFocus: StateFlow<Boolean> = _audioFocus.asStateFlow()

    fun setAudioFocus(enabled: Boolean) {
        _audioFocus.value = enabled
        prefs.edit {putBoolean(KEY_AUDIO_FOCUS, enabled)}
    }

    // ── Autoplay ─────────────────────────────────────────────────────────────
    private val _autoplayEnabled = MutableStateFlow(prefs.getBoolean("autoplay_enabled", true))
    val autoplayEnabled: StateFlow<Boolean> = _autoplayEnabled.asStateFlow()

    private val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        if (key == "autoplay_enabled") {
            val enabled = sharedPreferences.getBoolean("autoplay_enabled", true)
            if (_autoplayEnabled.value != enabled) {
                _autoplayEnabled.value = enabled
            }
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(preferenceListener)
    }

    override fun onCleared() {
        super.onCleared()
        prefs.unregisterOnSharedPreferenceChangeListener(preferenceListener)
    }

    fun setAutoplayEnabled(enabled: Boolean) {
        if (_autoplayEnabled.value == enabled) return
        _autoplayEnabled.value = enabled
        prefs.edit {putBoolean("autoplay_enabled", enabled)}
    }

    // ── Pure AMOLED Black ──────────────────────────────────────────────────
    private val _amoledBlack = MutableStateFlow(prefs.getBoolean(KEY_AMOLED_BLACK, false))
    val amoledBlack: StateFlow<Boolean> = _amoledBlack.asStateFlow()

    fun setAmoledBlack(enabled: Boolean) {
        _amoledBlack.value = enabled
        prefs.edit {putBoolean(KEY_AMOLED_BLACK, enabled)}
    }

    // ── Hide Nav Labels ──────────────────────────────────────────────────
    private val _hideNavLabels = MutableStateFlow(prefs.getBoolean("hide_nav_labels", false))
    val hideNavLabels: StateFlow<Boolean> = _hideNavLabels.asStateFlow()

    fun setHideNavLabels(hide: Boolean) {
        _hideNavLabels.value = hide
        prefs.edit {putBoolean("hide_nav_labels", hide)}
    }

    // ── Hide Nav on Scroll ─────────────────────────────────────────────────
    private val _hideNavOnScroll = MutableStateFlow(prefs.getBoolean("hide_nav_on_scroll", false))
    val hideNavOnScroll: StateFlow<Boolean> = _hideNavOnScroll.asStateFlow()

    fun setHideNavOnScroll(hide: Boolean) {
        _hideNavOnScroll.value = hide
        prefs.edit {putBoolean("hide_nav_on_scroll", hide)}
    }

    // ── App Language ─────────────────────────────────────────────────────────
    private val _appLanguage = MutableStateFlow(prefs.getString("app_language", "system") ?: "system")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    fun setLanguage(langCode: String) {
        _appLanguage.value = langCode
        prefs.edit { putString("app_language", langCode) }
        val localeList = if (langCode == "system" || langCode.isEmpty()) {
            androidx.core.os.LocaleListCompat.getEmptyLocaleList()
        } else {
            androidx.core.os.LocaleListCompat.forLanguageTags(langCode)
        }
        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(localeList)
    }

    private val _floatingNavBar = kotlinx.coroutines.flow.MutableStateFlow(prefs.getBoolean("floating_nav_bar", false))
    val floatingNavBar: kotlinx.coroutines.flow.StateFlow<Boolean> = _floatingNavBar.asStateFlow()
    fun setFloatingNavBar(floating: Boolean) {
        _floatingNavBar.value = floating
        prefs.edit {putBoolean("floating_nav_bar", floating)}
    }
    
    private val _unifiedFloatingBar = kotlinx.coroutines.flow.MutableStateFlow(prefs.getBoolean("unified_floating_bar", false))
    val unifiedFloatingBar: kotlinx.coroutines.flow.StateFlow<Boolean> = _unifiedFloatingBar.asStateFlow()
    fun setUnifiedFloatingBar(unified: Boolean) {
        _unifiedFloatingBar.value = unified
        prefs.edit {putBoolean("unified_floating_bar", unified)}
    }

    private val _showVolumeSlider = kotlinx.coroutines.flow.MutableStateFlow(prefs.getBoolean("show_volume_slider", true))
    val showVolumeSlider: kotlinx.coroutines.flow.StateFlow<Boolean> = _showVolumeSlider.asStateFlow()
    fun setShowVolumeSlider(show: Boolean) {
        _showVolumeSlider.value = show
        prefs.edit {putBoolean("show_volume_slider", show)}
    }



    // ── Dynamic Nav Style ──────────────────────────────────────────────────
    private val _dynamicNavStyle = MutableStateFlow(prefs.getBoolean("dynamic_nav_style", false))
    val dynamicNavStyle: StateFlow<Boolean> = _dynamicNavStyle.asStateFlow()

    fun setDynamicNavStyle(enabled: Boolean) {
        _dynamicNavStyle.value = enabled
        prefs.edit {putBoolean("dynamic_nav_style", enabled)}
    }

    // ── Navigation Blur ──────────────────────────────────────────────────
    private val _navigationBlur = MutableStateFlow(prefs.getBoolean(KEY_NAVIGATION_BLUR, true))
    val navigationBlur: StateFlow<Boolean> = _navigationBlur.asStateFlow()

    fun setNavigationBlur(enabled: Boolean) {
        _navigationBlur.value = enabled
        prefs.edit {putBoolean(KEY_NAVIGATION_BLUR, enabled)}
    }

    // ── Tab Switch Blur ───────────────────────────────────────────────────
    // Controls whether blur is applied on main tab swipes (Explore↔Library).
    // Off by default since tab switches are frequent and GPU-heavy.
    private val _tabSwitchBlur = MutableStateFlow(prefs.getBoolean(KEY_TAB_SWITCH_BLUR, false))
    val tabSwitchBlur: StateFlow<Boolean> = _tabSwitchBlur.asStateFlow()

    fun setTabSwitchBlur(enabled: Boolean) {
        _tabSwitchBlur.value = enabled
        prefs.edit { putBoolean(KEY_TAB_SWITCH_BLUR, enabled) }
    }

    // ── Dynamic Album Theme ────────────────────────────────────────────────
    private val _dynamicAlbumTheme = MutableStateFlow(prefs.getBoolean(KEY_DYNAMIC_ALBUM_THEME, true))
    val dynamicAlbumTheme: StateFlow<Boolean> = _dynamicAlbumTheme.asStateFlow()

    fun setDynamicAlbumTheme(enabled: Boolean) {
        _dynamicAlbumTheme.value = enabled
        prefs.edit {putBoolean(KEY_DYNAMIC_ALBUM_THEME, enabled)}
    }

    // ── Player Theme ──────────────────────────────────────────────────
    private val _playerTheme = MutableStateFlow(prefs.getString(KEY_PLAYER_THEME, "fluid") ?: "fluid")
    val playerTheme: StateFlow<String> = _playerTheme.asStateFlow()

    fun setPlayerTheme(theme: String) {
        _playerTheme.value = theme
        prefs.edit { putString(KEY_PLAYER_THEME, theme) }
    }

    // ── Ignore Short Audio ──────────────────────────────────────────────────
    private val _ignoreShortAudio = MutableStateFlow(prefs.getBoolean(KEY_IGNORE_SHORT_AUDIO, false))
    val ignoreShortAudio: StateFlow<Boolean> = _ignoreShortAudio.asStateFlow()

    private val _ignoreShortAudioDuration = MutableStateFlow(prefs.getFloat(KEY_IGNORE_SHORT_AUDIO_DURATION, 30f))
    val ignoreShortAudioDuration: StateFlow<Float> = _ignoreShortAudioDuration.asStateFlow()

    fun setIgnoreShortAudio(enabled: Boolean) {
        _ignoreShortAudio.value = enabled
        prefs.edit {putBoolean(KEY_IGNORE_SHORT_AUDIO, enabled)}
    }

    fun setIgnoreShortAudioDuration(duration: Float) {
        _ignoreShortAudioDuration.value = duration
        prefs.edit {putFloat(KEY_IGNORE_SHORT_AUDIO_DURATION, duration)}
    }

    // ── Data Saver ──────────────────────────────────────────────────────────
    private val _dataSaver = MutableStateFlow(prefs.getBoolean(KEY_DATA_SAVER, false))
    val dataSaver: StateFlow<Boolean> = _dataSaver.asStateFlow()

    fun setDataSaver(enabled: Boolean) {
        _dataSaver.value = enabled
        prefs.edit {putBoolean(KEY_DATA_SAVER, enabled)}
        com.codetrio.overdrive.data.innertube.NewPipeStreamExtractor.clearCache()
    }

    // ── Audio Quality ────────────────────────────────────────────────────────
    private val _audioQuality = MutableStateFlow(prefs.getString(KEY_AUDIO_QUALITY, "High") ?: "High")
    val audioQuality: StateFlow<String> = _audioQuality.asStateFlow()

    fun setAudioQuality(quality: String) {
        _audioQuality.value = quality
        prefs.edit {putString(KEY_AUDIO_QUALITY, quality)}
        com.codetrio.overdrive.data.innertube.NewPipeStreamExtractor.clearCache()
    }

    // ── Pause Listening History ────────────────────────────────────────────────
    private val _pauseHistory = MutableStateFlow(prefs.getBoolean("pause_history", false))
    val pauseHistory: StateFlow<Boolean> = _pauseHistory.asStateFlow()

    fun setPauseHistory(pause: Boolean) {
        _pauseHistory.value = pause
        prefs.edit {putBoolean("pause_history", pause)}
    }

    // ── High Refresh Rate ────────────────────────────────────────────────────
    private val _forceHighRefreshRate = MutableStateFlow(prefs.getBoolean("force_high_refresh_rate", false))
    val forceHighRefreshRate: StateFlow<Boolean> = _forceHighRefreshRate.asStateFlow()

    fun setForceHighRefreshRate(force: Boolean) {
        _forceHighRefreshRate.value = force
        prefs.edit {putBoolean("force_high_refresh_rate", force)}
    }

    // ── Animated Album Art ───────────────────────────────────────────────────
    private val _showAnimatedArt = MutableStateFlow(prefs.getBoolean("show_animated_art", true))
    val showAnimatedArt: StateFlow<Boolean> = _showAnimatedArt.asStateFlow()

    fun setShowAnimatedArt(show: Boolean) {
        _showAnimatedArt.value = show
        prefs.edit {putBoolean("show_animated_art", show)}
    }

    // ── Haptics Granular ─────────────────────────────────────────────────────
    private val _hapticPlayPause = MutableStateFlow(prefs.getBoolean(KEY_HAPTIC_PLAY_PAUSE, true))
    val hapticPlayPause: StateFlow<Boolean> = _hapticPlayPause.asStateFlow()

    private val _hapticQueue = MutableStateFlow(prefs.getBoolean(KEY_HAPTIC_QUEUE, true))
    val hapticQueue: StateFlow<Boolean> = _hapticQueue.asStateFlow()

    private val _hapticFavorite = MutableStateFlow(prefs.getBoolean(KEY_HAPTIC_FAVORITE, true))
    val hapticFavorite: StateFlow<Boolean> = _hapticFavorite.asStateFlow()

    fun setHapticPlayPause(enabled: Boolean) {
        _hapticPlayPause.value = enabled
        prefs.edit {putBoolean(KEY_HAPTIC_PLAY_PAUSE, enabled)}
    }

    fun setHapticQueue(enabled: Boolean) {
        _hapticQueue.value = enabled
        prefs.edit {putBoolean(KEY_HAPTIC_QUEUE, enabled)}
    }

    fun setHapticFavorite(enabled: Boolean) {
        _hapticFavorite.value = enabled
        prefs.edit {putBoolean(KEY_HAPTIC_FAVORITE, enabled)}
    }



    // ── Library Paths ────────────────────────────────────────────────────────
    private val _libraryPaths = MutableStateFlow(
        (prefs.getString(KEY_LIBRARY_PATHS, "") ?: "").split("||").filter { it.isNotEmpty() }
    )
    val libraryPaths: StateFlow<List<String>> = _libraryPaths.asStateFlow()

    fun addLibraryPath(path: String) {
        val current = _libraryPaths.value
        if (current.contains(path)) return
        val newList = current + path
        _libraryPaths.value = newList
        prefs.edit {putString(KEY_LIBRARY_PATHS, newList.joinToString("||"))}
    }

    fun removeLibraryPath(path: String) {
        val current = _libraryPaths.value
        val newList = current.filter { it != path }
        _libraryPaths.value = newList
        prefs.edit {putString(KEY_LIBRARY_PATHS, newList.joinToString("||"))}
    }

    // ── Hidden Folders ────────────────────────────────────────────────────────
    private val _hiddenFolders = MutableStateFlow(
        (prefs.getString(KEY_HIDDEN_FOLDERS, "") ?: "").split("||").filter { it.isNotEmpty() }
    )
    val hiddenFolders: StateFlow<List<String>> = _hiddenFolders.asStateFlow()

    fun addHiddenFolder(path: String) {
        val current = _hiddenFolders.value
        if (current.contains(path)) return
        val newList = current + path
        _hiddenFolders.value = newList
        prefs.edit {putString(KEY_HIDDEN_FOLDERS, newList.joinToString("||"))}
    }

    fun removeHiddenFolder(path: String) {
        val current = _hiddenFolders.value
        val newList = current.filter { it != path }
        _hiddenFolders.value = newList
        prefs.edit {putString(KEY_HIDDEN_FOLDERS, newList.joinToString("||"))}
    }

    // ── Cache & Downloads ───────────────────────────────────────────────────
    private val _downloadFolder = MutableStateFlow(prefs.getString("download_folder", null))
    val downloadFolder: StateFlow<String?> = _downloadFolder.asStateFlow()

    fun setDownloadFolder(uri: String?) {
        _downloadFolder.value = uri
        if (uri != null) {
            prefs.edit {putString("download_folder", uri)}
        } else {
            prefs.edit {remove("download_folder")}
        }
    }

    // Sizes in MB. -1 or 0 could mean unlimited, or we can use large numbers. We'll use Int (MB). 0 = unlimited.
    private val _songCacheMaxSize = MutableStateFlow(prefs.getInt("song_cache_max_size", 256)) // Default 256MB
    val songCacheMaxSize: StateFlow<Int> = _songCacheMaxSize.asStateFlow()

    fun setSongCacheMaxSize(sizeMb: Int) {
        _songCacheMaxSize.value = sizeMb
        prefs.edit {putInt("song_cache_max_size", sizeMb)}
    }

    private val _imageCacheMaxSize = MutableStateFlow(prefs.getInt("image_cache_max_size", 128)) // Default 128MB
    val imageCacheMaxSize: StateFlow<Int> = _imageCacheMaxSize.asStateFlow()

    fun setImageCacheMaxSize(sizeMb: Int) {
        _imageCacheMaxSize.value = sizeMb
        prefs.edit {putInt("image_cache_max_size", sizeMb)}
    }

    // ── Database & Scanning Tools ─────────────────────────────────────────────
    private val _isLibraryScanning = MutableStateFlow(false)
    val isLibraryScanning: StateFlow<Boolean> = _isLibraryScanning.asStateFlow()

    fun performFullScan(playerSharedViewModel: PlayerSharedViewModel, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLibraryScanning.value = true
            val pathsToScan = mutableListOf<java.io.File>()

            // Resolve configured library paths
            val currentPaths = libraryPaths.value
            currentPaths.forEach { pathUriStr ->
                try {
                    val uri = Uri.parse(pathUriStr)
                    val absolutePath = getPathFromUri(uri)
                    val file = java.io.File(absolutePath)
                    if (file.exists() && file.isDirectory) {
                        pathsToScan.add(file)
                    }
                } catch (e: Exception) {
                    android.util.Log.w("SettingsViewModel", "Invalid library path: $pathUriStr", e)
                }
            }

            // Fallback to standard dirs if no custom paths
            if (pathsToScan.isEmpty()) {
                val musicDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_MUSIC)
                if (musicDir.exists()) pathsToScan.add(musicDir)
                val downloadDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                if (downloadDir.exists()) pathsToScan.add(downloadDir)
            }

            // Find all audio files recursively
            val filesToScan = mutableListOf<String>()
            val audioExtensions = setOf("mp3", "flac", "wav", "m4a", "ogg", "aac", "opus", "wma", "amr")

            pathsToScan.forEach { dir ->
                try {
                    dir.walkTopDown().forEach { file ->
                        if (file.isFile && file.extension.lowercase() in audioExtensions) {
                            filesToScan.add(file.absolutePath)
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SettingsViewModel", "Error walking directory: ${dir.absolutePath}", e)
                }
            }

            if (filesToScan.isNotEmpty()) {
                val latch = java.util.concurrent.CountDownLatch(1)
                val chunkSize = 100
                val chunks = filesToScan.chunked(chunkSize)

                val scannedCount = java.util.concurrent.atomic.AtomicInteger(0)
                chunks.forEach { chunk ->
                    android.media.MediaScannerConnection.scanFile(
                        context,
                        chunk.toTypedArray(),
                        null
                    ) { _, _ ->
                        if (scannedCount.incrementAndGet() >= filesToScan.size) {
                            latch.countDown()
                        }
                    }
                }

                latch.await(5, java.util.concurrent.TimeUnit.SECONDS)
            }

            // Reload the local media files in PlayerSharedViewModel
            playerSharedViewModel.rescanLocalFiles()

            _isLibraryScanning.value = false
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun rebuildDatabase(playerSharedViewModel: PlayerSharedViewModel, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLibraryScanning.value = true

            // 1. Clear song cache
            try {
                val mediaCacheDir = java.io.File(context.cacheDir, "media_cache")
                deleteDir(mediaCacheDir)
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "Failed to clear song cache", e)
            }

            // 2. Clear image cache
            try {
                val imageCacheDir = java.io.File(context.cacheDir, "image_cache")
                deleteDir(imageCacheDir)
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "Failed to clear image cache", e)
            }

            // Update cache size state in ViewModel
            calculateCacheSize()

            // 3. Force perform full scan
            performFullScan(playerSharedViewModel) {
                _isLibraryScanning.value = false
                onComplete()
            }
        }
    }

    // ── Backup & Restore ──────────────────────────────────────────────────────
    fun exportBackup(context: Context, uri: Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val settingsMap = prefs.all
                val favManager = com.codetrio.overdrive.util.FavoritesManager(context)
                val favoritesList = favManager.favoriteIds.toList()

                val db = com.codetrio.overdrive.data.db.MusicDatabase.getDatabase(context)
                val playlistDao = db.playlistDao()

                // Read playlists and songs using Flow.first() extension
                val playlists = playlistDao.getAllPlaylists().first()
                val playlistSongsList = mutableListOf<com.codetrio.overdrive.data.db.PlaylistSongEntity>()
                playlists.forEach { playlist: com.codetrio.overdrive.data.db.PlaylistEntity ->
                    val songs = playlistDao.getSongsForPlaylist(playlist.id).first()
                    playlistSongsList.addAll(songs)
                }

                val backupObject = com.google.gson.JsonObject().apply {
                    addProperty("version", 1)

                    val settingsObj = com.google.gson.JsonObject()
                    settingsMap.forEach { (key, value) ->
                        when (value) {
                            is Boolean -> settingsObj.addProperty(key, value)
                            is Float -> settingsObj.addProperty(key, value)
                            is Int -> settingsObj.addProperty(key, value)
                            is Long -> settingsObj.addProperty(key, value)
                            is String -> settingsObj.addProperty(key, value)
                        }
                    }
                    add("settings", settingsObj)

                    val favArray = com.google.gson.JsonArray()
                    favoritesList.forEach { favArray.add(it) }
                    add("favorites", favArray)

                    val gson = com.google.gson.Gson()
                    add("playlists", gson.toJsonTree(playlists))
                    add("playlist_songs", gson.toJsonTree(playlistSongsList))
                }

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(backupObject.toString().toByteArray())
                }

                withContext(Dispatchers.Main) {
                    onResult(true, "Backup exported successfully")
                }
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "Failed to export backup", e)
                withContext(Dispatchers.Main) {
                    onResult(false, "Failed to export: ${e.localizedMessage}")
                }
            }
        }
    }

    fun importBackup(context: Context, uri: Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                } ?: throw Exception("Could not read backup file")

                val gson = com.google.gson.Gson()
                val backupObject = gson.fromJson(jsonString, com.google.gson.JsonObject::class.java)

                if (backupObject.has("settings")) {
                    val settingsObj = backupObject.getAsJsonObject("settings")
                    prefs.edit {
                        settingsObj.entrySet().forEach { (key, jsonElement) ->
                            if (jsonElement.isJsonPrimitive) {
                                val prim = jsonElement.asJsonPrimitive
                                if (prim.isBoolean) putBoolean(key, prim.asBoolean)
                                else if (prim.isNumber) {
                                    val num = prim.asNumber
                                    when (key) {
                                        KEY_VIBRATION_STRENGTH, KEY_CROSSFADE_DURATION, KEY_TARGET_LUFS -> putFloat(key, num.toFloat())
                                        else -> {
                                            try {
                                                putInt(key, num.toInt())
                                            } catch (_: Exception) {
                                                putFloat(key, num.toFloat())
                                            }
                                        }
                                    }
                                }
                                else if (prim.isString) putString(key, prim.asString)
                            }
                        }
                    }

                    _themeMode.value = prefs.getString(KEY_THEME_MODE, "system") ?: "system"
                    _darkMode.value = if (_themeMode.value == "system") prefs.getBoolean(KEY_DARK_MODE, true) else _themeMode.value == "dark"
                    _amoledBlack.value = prefs.getBoolean(KEY_AMOLED_BLACK, false)
                    _showAnimatedArt.value = prefs.getBoolean("show_animated_art", true)
                    _dynamicAlbumTheme.value = prefs.getBoolean(KEY_DYNAMIC_ALBUM_THEME, true)
                    _vibrationStrength.value = prefs.getFloat(KEY_VIBRATION_STRENGTH, 80f)
                    _crossfadeEnabled.value = prefs.getBoolean(KEY_CROSSFADE_ENABLED, false)
                    _crossfadeDuration.value = prefs.getFloat(KEY_CROSSFADE_DURATION, 3f)
                    _audioFocus.value = prefs.getBoolean(KEY_AUDIO_FOCUS, true)
                    _dataSaver.value = prefs.getBoolean(KEY_DATA_SAVER, false)
                    _audioQuality.value = prefs.getString(KEY_AUDIO_QUALITY, "High") ?: "High"
                    _pauseHistory.value = prefs.getBoolean("pause_history", false)
                    _downloadFolder.value = prefs.getString("download_folder", null)
                    _songCacheMaxSize.value = prefs.getInt("song_cache_max_size", 256)
                    _imageCacheMaxSize.value = prefs.getInt("image_cache_max_size", 128)
                    _hapticPlayPause.value = prefs.getBoolean(KEY_HAPTIC_PLAY_PAUSE, true)
                    _hapticQueue.value = prefs.getBoolean(KEY_HAPTIC_QUEUE, true)
                    _hapticFavorite.value = prefs.getBoolean(KEY_HAPTIC_FAVORITE, true)
                    _volumeNormalizationEnabled.value = prefs.getBoolean(KEY_VOLUME_NORMALIZATION_ENABLED, false)
                    _targetLufs.value = prefs.getFloat(KEY_TARGET_LUFS, -14f)
                    _navigationBlur.value = prefs.getBoolean(KEY_NAVIGATION_BLUR, true)
                    _forceHighRefreshRate.value = prefs.getBoolean("force_high_refresh_rate", false)
                    _playerTheme.value = prefs.getString(KEY_PLAYER_THEME, "fluid") ?: "fluid"
                }

                if (backupObject.has("favorites")) {
                    val favArray = backupObject.getAsJsonArray("favorites")
                    val favManager = com.codetrio.overdrive.util.FavoritesManager(context)
                    favArray.forEach { element ->
                        val idStr = element.asString
                        idStr.toLongOrNull()?.let { idLong ->
                            favManager.setFavorite(idLong, true)
                        }
                    }
                }

                val db = com.codetrio.overdrive.data.db.MusicDatabase.getDatabase(context)
                val playlistDao = db.playlistDao()

                if (backupObject.has("playlists")) {
                    val typeTokenPlaylists = object :
                        com.google.gson.reflect.TypeToken<List<com.codetrio.overdrive.data.db.PlaylistEntity>>() {}.type
                    val playlistsList: List<com.codetrio.overdrive.data.db.PlaylistEntity> =
                        gson.fromJson(backupObject.get("playlists"), typeTokenPlaylists)

                    playlistsList.forEach { playlist ->
                        playlistDao.insertPlaylist(playlist)
                    }
                }

                if (backupObject.has("playlist_songs")) {
                    val typeTokenSongs = object : com.google.gson.reflect.TypeToken<List<com.codetrio.overdrive.data.db.PlaylistSongEntity>>() {}.type
                    val songsList: List<com.codetrio.overdrive.data.db.PlaylistSongEntity> = gson.fromJson(backupObject.get("playlist_songs"), typeTokenSongs)

                    songsList.forEach { song ->
                        playlistDao.insertPlaylistSong(song)
                    }
                }

                withContext(Dispatchers.Main) {
                    onResult(true, "Backup imported successfully")
                }
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "Failed to import backup", e)
                withContext(Dispatchers.Main) {
                    onResult(false, "Import failed: ${e.localizedMessage}")
                }
            }
        }
    }

    // ── Cache ────────────────────────────────────────────────────────────────
    private val _songCacheSize = MutableStateFlow("Calculating...")
    val songCacheSize: StateFlow<String> = _songCacheSize.asStateFlow()

    private val _imageCacheSize = MutableStateFlow("Calculating...")
    val imageCacheSize: StateFlow<String> = _imageCacheSize.asStateFlow()

    fun calculateCacheSize() {
        viewModelScope.launch(Dispatchers.IO) {
            val imageCacheDir = File(context.cacheDir, "image_cache")
            val imageSize = getDirSize(imageCacheDir)
            
            val mediaCacheDir = File(context.cacheDir, "media_cache")
            val songSize = getDirSize(mediaCacheDir)

            _imageCacheSize.value = formatFileSize(imageSize)
            _songCacheSize.value = formatFileSize(songSize)
        }
    }

    fun clearSongCache(onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val mediaCacheDir = File(context.cacheDir, "media_cache")
            deleteDir(mediaCacheDir)
            calculateCacheSize()
            withContext(Dispatchers.Main) { onComplete() }
        }
    }

    fun clearImageCache(onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val imageCacheDir = File(context.cacheDir, "image_cache")
            deleteDir(imageCacheDir)
            calculateCacheSize()
            withContext(Dispatchers.Main) { onComplete() }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private fun getDirSize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0
        var size = 0L
        dir.listFiles()?.forEach { file ->
            size += if (file.isFile) file.length() else getDirSize(file)
        }
        return size
    }

    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups =
            (log10(bytes.toDouble()) / log10(1024.0)).toInt()
                .coerceAtMost(units.size - 1)
        return DecimalFormat("#.##")
            .format(bytes / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }

    private fun deleteDir(dir: File?) {
        if (dir == null || !dir.exists()) return
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) deleteDir(file)
            file.delete()
        }
    }

    companion object {
        const val PREFS_NAME = "AppSettings"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_NAVIGATION_BLUR = "navigation_blur"
        const val KEY_TAB_SWITCH_BLUR = "tab_switch_blur"
        const val KEY_VIBRATION_STRENGTH = "vibration_strength"
        const val KEY_CROSSFADE_ENABLED = "crossfade_enabled"
        const val KEY_CROSSFADE_DURATION = "crossfade_duration"
        const val KEY_AUDIO_FOCUS = "audio_focus"
        const val KEY_LIBRARY_PATHS = "library_paths"
        const val KEY_HIDDEN_FOLDERS = "hidden_folders"
        const val KEY_AMOLED_BLACK = "amoled_black"
        const val KEY_IGNORE_SHORT_AUDIO = "ignore_short_audio"
        const val KEY_IGNORE_SHORT_AUDIO_DURATION = "ignore_short_audio_duration"
        const val KEY_DATA_SAVER = "data_saver"
        const val KEY_AUDIO_QUALITY = "audio_quality"
        const val KEY_SYNC_FREQUENCY = "sync_frequency"
        const val KEY_HAPTIC_PLAY_PAUSE = "haptic_play_pause"
        const val KEY_HAPTIC_QUEUE = "haptic_queue"
        const val KEY_HAPTIC_FAVORITE = "haptic_favorite"
        const val KEY_DYNAMIC_ALBUM_THEME = "dynamic_album_theme"
        const val KEY_VOLUME_NORMALIZATION_ENABLED = "volume_normalization_enabled"
        const val KEY_TARGET_LUFS = "target_lufs"
        const val KEY_PLAYER_THEME = "player_theme"
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// SETTINGS FRAGMENT
// ═══════════════════════════════════════════════════════════════════════════════

// ═══════════════════════════════════════════════════════════════════════════════
// COMPOSE UI — ENTRY POINT
// ═══════════════════════════════════════════════════════════════════════════════

private fun getPathFromUri(uri: Uri): String {
    if (android.provider.DocumentsContract.isTreeUri(uri)) {
        val docId = android.provider.DocumentsContract.getTreeDocumentId(uri)
        val split = docId.split(":")
        if (split.size >= 2) {
            val type = split[0]
            val relativePath = split[1]
            return if ("primary" == type.lowercase()) {
                android.os.Environment.getExternalStorageDirectory().toString() + "/" + relativePath
            } else {
                "/storage/$type/$relativePath"
            }
        }
    }
    return uri.path ?: uri.toString()
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@RequiresApi(Build.VERSION_CODES.Q)
fun NavGraphBuilder.settingsGraph(navController: androidx.navigation.NavController) {
    val enterAnim: androidx.compose.animation.AnimatedContentTransitionScope<androidx.navigation.NavBackStackEntry>.() -> androidx.compose.animation.EnterTransition = {
        slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
        ) + fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.95f, animationSpec = tween(220))
    }

    val exitAnim: androidx.compose.animation.AnimatedContentTransitionScope<androidx.navigation.NavBackStackEntry>.() -> androidx.compose.animation.ExitTransition = {
        slideOutHorizontally(
            targetOffsetX = { -it / 3 },
            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
        ) + fadeOut(animationSpec = tween(220)) + scaleOut(targetScale = 0.95f, animationSpec = tween(220))
    }

    val popEnterAnim: androidx.compose.animation.AnimatedContentTransitionScope<androidx.navigation.NavBackStackEntry>.() -> androidx.compose.animation.EnterTransition = {
        slideInHorizontally(
            initialOffsetX = { -it / 3 },
            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
        ) + fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.95f, animationSpec = tween(220))
    }

    val popExitAnim: androidx.compose.animation.AnimatedContentTransitionScope<androidx.navigation.NavBackStackEntry>.() -> androidx.compose.animation.ExitTransition = {
        slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
        ) + fadeOut(animationSpec = tween(220)) + scaleOut(targetScale = 0.95f, animationSpec = tween(220))
    }

    composableWithBlur(SettingsRoute.Main.route) {
        SettingsMainScreen(navController = navController)
    }

    composableWithBlur(
        route = SettingsRoute.MusicManagement.route,
        enterTransition = enterAnim,
        exitTransition = exitAnim,
        popEnterTransition = popEnterAnim,
        popExitTransition = popExitAnim
    ) {
        val context = LocalContext.current
        val activity = context.findActivity() as androidx.activity.ComponentActivity
        val viewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(activity)

        val libraryPaths by viewModel.libraryPaths.collectAsStateWithLifecycle()
        val songCacheSize by viewModel.songCacheSize.collectAsStateWithLifecycle()
        val imageCacheSize by viewModel.imageCacheSize.collectAsStateWithLifecycle()
        val songCacheMaxSize by viewModel.songCacheMaxSize.collectAsStateWithLifecycle()
        val imageCacheMaxSize by viewModel.imageCacheMaxSize.collectAsStateWithLifecycle()
        val downloadFolder by viewModel.downloadFolder.collectAsStateWithLifecycle()
        val ignoreShortAudio by viewModel.ignoreShortAudio.collectAsStateWithLifecycle()
        val ignoreShortAudioDuration by viewModel.ignoreShortAudioDuration.collectAsStateWithLifecycle()
        val hiddenFolders by viewModel.hiddenFolders.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) { viewModel.calculateCacheSize() }

        val directoryPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocumentTree()
        ) { uri ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                viewModel.addLibraryPath(it.toString())
            }
        }

        val downloadFolderPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocumentTree()
        ) { uri ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val path = getPathFromUri(it)
                viewModel.setDownloadFolder(path)
            }
        }

        val hiddenFolderPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocumentTree()
        ) { uri ->
            uri?.let {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    val path = getPathFromUri(it)
                    viewModel.addHiddenFolder(path)
                } catch (e: Exception) {
                    android.util.Log.e("SettingsScreenContent", "Failed to resolve hidden folder path", e)
                }
            }
        }

        val isScanning by viewModel.isLibraryScanning.collectAsStateWithLifecycle()
        val playerSharedViewModel: PlayerSharedViewModel = androidx.lifecycle.viewmodel.compose.viewModel(activity)

        MusicManagementScreen(
            navController = navController,
            libraryPaths = libraryPaths,
            onRemovePath = { viewModel.removeLibraryPath(it) },
            onAddPathClick = { directoryPickerLauncher.launch(null) },
            songCacheSize = songCacheSize,
            imageCacheSize = imageCacheSize,
            onClearSongCache = {
                viewModel.clearSongCache {
                    (context as? MainActivity)?.showSnackbar("Song cache cleared", 0)
                }
            },
            onClearImageCache = {
                viewModel.clearImageCache {
                    (context as? MainActivity)?.showSnackbar("Image cache cleared", 0)
                }
            },
            songCacheMaxSize = songCacheMaxSize,
            onSongCacheMaxSizeChange = { viewModel.setSongCacheMaxSize(it) },
            imageCacheMaxSize = imageCacheMaxSize,
            onImageCacheMaxSizeChange = { viewModel.setImageCacheMaxSize(it) },
            downloadFolder = downloadFolder,
            onDownloadFolderClick = { downloadFolderPickerLauncher.launch(null) },
            ignoreShortAudio = ignoreShortAudio,
            onIgnoreShortAudioChange = { viewModel.setIgnoreShortAudio(it) },
            ignoreShortAudioDuration = ignoreShortAudioDuration,
            onIgnoreShortAudioDurationChange = { viewModel.setIgnoreShortAudioDuration(it) },
            hiddenFolders = hiddenFolders,
            onAddHiddenFolderClick = { hiddenFolderPickerLauncher.launch(null) },
            onRemoveHiddenFolder = { viewModel.removeHiddenFolder(it) },
            isScanning = isScanning,
            onRescanClick = {
                viewModel.performFullScan(playerSharedViewModel) {
                    (context as? MainActivity)?.showSnackbar("Library rescan complete", 0)
                }
            },
            onRebuildDatabaseClick = {
                viewModel.rebuildDatabase(playerSharedViewModel) {
                    (context as? MainActivity)?.showSnackbar("Database rebuilt successfully", 0)
                }
            }
        )
    }

    composableWithBlur(
        route = SettingsRoute.BackupRestore.route,
        enterTransition = enterAnim,
        exitTransition = exitAnim,
        popEnterTransition = popEnterAnim,
        popExitTransition = popExitAnim
    ) {
        val context = LocalContext.current
        val activity = context.findActivity() as androidx.activity.ComponentActivity
        val viewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(activity)

        val exportBackupLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/json")
        ) { uri ->
            uri?.let {
                viewModel.exportBackup(context, it) { _, msg ->
                    (context as? MainActivity)?.showSnackbar(msg, 0)
                }
            }
        }

        val importBackupLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri ->
            uri?.let {
                viewModel.importBackup(context, it) { _, msg ->
                    (context as? MainActivity)?.showSnackbar(msg, 0)
                }
            }
        }

        BackupRestoreScreen(
            navController = navController,
            onBackupClick = { exportBackupLauncher.launch("overdrive_backup.json") },
            onRestoreClick = { importBackupLauncher.launch(arrayOf("application/json")) }
        )
    }

    composableWithBlur(
        route = SettingsRoute.Account.route,
        enterTransition = enterAnim,
        exitTransition = exitAnim,
        popEnterTransition = popEnterAnim,
        popExitTransition = popExitAnim
    ) {
        val context = LocalContext.current
        val activity = context.findActivity() as androidx.activity.ComponentActivity
        val viewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(activity)
        val ytCookies by viewModel.ytCookies.collectAsStateWithLifecycle()
        val dataSaver by viewModel.dataSaver.collectAsStateWithLifecycle()
        val pauseHistory by viewModel.pauseHistory.collectAsStateWithLifecycle()


        AccountScreen(
            navController = navController,
            ytCookies = ytCookies,
            onYtCookiesChange = { viewModel.setYtCookies(it) },
            dataSaver = dataSaver,
            onDataSaverChange = { viewModel.setDataSaver(it) },
            pauseHistory = pauseHistory,
            onPauseHistoryChange = { viewModel.setPauseHistory(it) }
        )
    }

    composableWithBlur(
        route = SettingsRoute.Appearance.route,
        enterTransition = enterAnim,
        exitTransition = exitAnim,
        popEnterTransition = popEnterAnim,
        popExitTransition = popExitAnim
    ) {
        val context = LocalContext.current
        val activity = context.findActivity() as androidx.activity.ComponentActivity
        val viewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(activity)
        val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
        val amoledBlack by viewModel.amoledBlack.collectAsStateWithLifecycle()
        val showAnimatedArt by viewModel.showAnimatedArt.collectAsStateWithLifecycle()
        val dynamicAlbumTheme by viewModel.dynamicAlbumTheme.collectAsStateWithLifecycle()
        val hideNavLabels by viewModel.hideNavLabels.collectAsStateWithLifecycle()
        val hideNavOnScroll by viewModel.hideNavOnScroll.collectAsStateWithLifecycle()
        val dynamicNavStyle by viewModel.dynamicNavStyle.collectAsStateWithLifecycle()
        val navigationBlur by viewModel.navigationBlur.collectAsStateWithLifecycle()
        val tabSwitchBlur by viewModel.tabSwitchBlur.collectAsStateWithLifecycle()
        val playerTheme by viewModel.playerTheme.collectAsStateWithLifecycle()
        val forceHighRefreshRate by viewModel.forceHighRefreshRate.collectAsStateWithLifecycle()
        val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
        val floatingNavBar by viewModel.floatingNavBar.collectAsStateWithLifecycle()
        val unifiedFloatingBar by viewModel.unifiedFloatingBar.collectAsStateWithLifecycle()

        AppearanceScreen(
            navController = navController,
            themeMode = themeMode,
            onThemeModeChange = { viewModel.setThemeMode(it) },
            amoledBlack = amoledBlack,
            onAmoledBlackChange = { viewModel.setAmoledBlack(it) },
            showAnimatedArt = showAnimatedArt,
            onShowAnimatedArtChange = { viewModel.setShowAnimatedArt(it) },
            dynamicAlbumTheme = dynamicAlbumTheme,
            onDynamicAlbumThemeChange = { viewModel.setDynamicAlbumTheme(it) },
            hideNavLabels = hideNavLabels,
            onHideNavLabelsChange = { viewModel.setHideNavLabels(it) },
            hideNavOnScroll = hideNavOnScroll,
            onHideNavOnScrollChange = { viewModel.setHideNavOnScroll(it) },
            dynamicNavStyle = dynamicNavStyle,
            onDynamicNavStyleChange = { viewModel.setDynamicNavStyle(it) },
            navigationBlur = navigationBlur,
            onNavigationBlurChange = { viewModel.setNavigationBlur(it) },
            tabSwitchBlur = tabSwitchBlur,
            onTabSwitchBlurChange = { viewModel.setTabSwitchBlur(it) },
            playerTheme = playerTheme,
            onPlayerThemeChange = { viewModel.setPlayerTheme(it) },
            forceHighRefreshRate = forceHighRefreshRate,
            onForceHighRefreshRateChange = { viewModel.setForceHighRefreshRate(it) },
            appLanguage = appLanguage,
            onAppLanguageChange = { viewModel.setLanguage(it) },
            floatingNavBar = floatingNavBar,
            onFloatingNavBarChange = { viewModel.setFloatingNavBar(it) },
            unifiedFloatingBar = unifiedFloatingBar,
            onUnifiedFloatingBarChange = { viewModel.setUnifiedFloatingBar(it) }
        )
    }

    composableWithBlur(
        route = SettingsRoute.Playback.route,
        enterTransition = enterAnim,
        exitTransition = exitAnim,
        popEnterTransition = popEnterAnim,
        popExitTransition = popExitAnim
    ) {
        val context = LocalContext.current
        val activity = context.findActivity() as androidx.activity.ComponentActivity
        val viewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(activity)
        val playerSharedViewModel: PlayerSharedViewModel = androidx.lifecycle.viewmodel.compose.viewModel(activity)

        val crossfadeEnabled by viewModel.crossfadeEnabled.collectAsStateWithLifecycle()
        val crossfadeDuration by viewModel.crossfadeDuration.collectAsStateWithLifecycle()
        val audioFocus by viewModel.audioFocus.collectAsStateWithLifecycle()
        val autoplayEnabled by viewModel.autoplayEnabled.collectAsStateWithLifecycle()
        val sleepTimerEndTime by playerSharedViewModel.sleepTimerEndTime.collectAsStateWithLifecycle()
        val sleepTimerMode by playerSharedViewModel.sleepTimerMode.collectAsStateWithLifecycle()
        val audioQuality by viewModel.audioQuality.collectAsStateWithLifecycle()
        val volumeNormalizationEnabled by viewModel.volumeNormalizationEnabled.collectAsStateWithLifecycle()
        val targetLufs by viewModel.targetLufs.collectAsStateWithLifecycle()
        val showVolumeSlider by viewModel.showVolumeSlider.collectAsStateWithLifecycle()
        val lyricsTranslationEnabled by viewModel.lyricsTranslationEnabled.collectAsStateWithLifecycle()
        val lyricsTranslationEngine by viewModel.lyricsTranslationEngine.collectAsStateWithLifecycle()
        val liveUpdatesEnabled by viewModel.liveUpdatesEnabled.collectAsStateWithLifecycle()
        PlaybackScreen(
            navController = navController,
            crossfadeEnabled = crossfadeEnabled,
            onCrossfadeToggle = { viewModel.setCrossfadeEnabled(it) },
            crossfadeDuration = crossfadeDuration,
            onCrossfadeDurationChange = { viewModel.setCrossfadeDuration(it) },
            audioFocus = audioFocus,
            onAudioFocusToggle = { viewModel.setAudioFocus(it) },
            autoplayEnabled = autoplayEnabled,
            onAutoplayToggle = { viewModel.setAutoplayEnabled(it) },
            sleepTimerEndTime = sleepTimerEndTime,
            sleepTimerMode = sleepTimerMode,
            onStartSleepTimer = { playerSharedViewModel.startCustomSleepTimer(it) },
            onCancelSleepTimer = { playerSharedViewModel.cancelSleepTimer() },
            onSetEndOfSong = { enable ->
                if (enable) playerSharedViewModel.setSleepTimerMode(PlayerSharedViewModel.SleepTimerMode.END_OF_SONG)
                else playerSharedViewModel.cancelSleepTimer()
            },
            audioQuality = audioQuality,
            onAudioQualityChange = { viewModel.setAudioQuality(it) },
            volumeNormalizationEnabled = volumeNormalizationEnabled,
            onVolumeNormalizationChange = { viewModel.setVolumeNormalizationEnabled(it) },
            targetLufs = targetLufs,
            onTargetLufsChange = { viewModel.setTargetLufs(it) },
            showVolumeSlider = showVolumeSlider,
            onShowVolumeSliderChange = { viewModel.setShowVolumeSlider(it) },
            lyricsTranslationEnabled = lyricsTranslationEnabled,
            onLyricsTranslationToggle = { viewModel.setLyricsTranslationEnabled(it) },
            lyricsTranslationEngine = lyricsTranslationEngine,
            onLyricsTranslationEngineChange = { viewModel.setLyricsTranslationEngine(it) },
            liveUpdatesEnabled = liveUpdatesEnabled,
            onLiveUpdatesToggle = { viewModel.setLiveUpdatesEnabled(it) }
        )
    }

    composableWithBlur(
        route = SettingsRoute.Haptics.route,
        enterTransition = enterAnim,
        exitTransition = exitAnim,
        popEnterTransition = popEnterAnim,
        popExitTransition = popExitAnim
    ) {
        val context = LocalContext.current
        val activity = context.findActivity() as androidx.activity.ComponentActivity
        val viewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(activity)
        val vibrationStrength by viewModel.vibrationStrength.collectAsStateWithLifecycle()
        val hapticPlayPause by viewModel.hapticPlayPause.collectAsStateWithLifecycle()
        val hapticQueue by viewModel.hapticQueue.collectAsStateWithLifecycle()
        val hapticFavorite by viewModel.hapticFavorite.collectAsStateWithLifecycle()

        HapticsScreen(
            navController = navController,
            hasHaptics = viewModel.hasHaptics,
            vibrationStrength = vibrationStrength,
            onVibrationStrengthChange = { viewModel.setVibrationStrength(it) },
            hapticPlayPause = hapticPlayPause,
            onHapticPlayPauseChange = { viewModel.setHapticPlayPause(it) },
            hapticQueue = hapticQueue,
            onHapticQueueChange = { viewModel.setHapticQueue(it) },
            hapticFavorite = hapticFavorite,
            onHapticFavoriteChange = { viewModel.setHapticFavorite(it) }
        )
    }

    composableWithBlur(
        route = SettingsRoute.About.route,
        enterTransition = enterAnim,
        exitTransition = exitAnim,
        popEnterTransition = popEnterAnim,
        popExitTransition = popExitAnim
    ) {
        val context = LocalContext.current
        AboutScreen(
            navController = navController,
            onCheckUpdate = {
                (context as? Activity)?.let { activity ->
                    (context as? MainActivity)?.updateManager?.checkForUpdate(
                        activity.findViewById(android.R.id.content),
                        BuildConfig.VERSION_NAME
                    )
                }
            },
            onWhatsNew = { navController.navigate(SettingsRoute.WhatsNew.route) },
            onOpenUrl = { url ->
                context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
            }
        )
    }



    composableWithBlur(
        route = SettingsRoute.WhatsNew.route,
        enterTransition = enterAnim,
        exitTransition = exitAnim,
        popEnterTransition = popEnterAnim,
        popExitTransition = popExitAnim
    ) {
        WhatsNewScreen(navController = navController)
    }

    composableWithBlur(
        route = SettingsRoute.CustomizeBottomNav.route,
        enterTransition = enterAnim,
        exitTransition = exitAnim,
        popEnterTransition = popEnterAnim,
        popExitTransition = popExitAnim
    ) {
        val context = androidx.compose.ui.platform.LocalContext.current
        val activity = context.findActivity() as androidx.activity.ComponentActivity
        val playerSharedViewModel: com.codetrio.overdrive.viewmodel.PlayerSharedViewModel = androidx.lifecycle.viewmodel.compose.viewModel(activity)
        com.codetrio.overdrive.ui.settings.BottomNavCustomizeScreen(
            playerViewModel = playerSharedViewModel,
            onBack = { navController.popBackStack() }
        )
    }

    composableWithBlur(
        route = "developer_options",
        enterTransition = enterAnim,
        exitTransition = exitAnim,
        popEnterTransition = popEnterAnim,
        popExitTransition = popExitAnim
    ) {
        DeveloperOptionsScreen(navController = navController)
    }

    composableWithBlur(
        route = "developer_playback_logs",
        enterTransition = enterAnim,
        exitTransition = exitAnim,
        popEnterTransition = popEnterAnim,
        popExitTransition = popExitAnim
    ) {
        com.codetrio.overdrive.ui.settings.PlaybackLogsScreen(navController = navController)
    }

    composableWithBlur(
        route = "developer_performance",
        enterTransition = enterAnim,
        exitTransition = exitAnim,
        popEnterTransition = popEnterAnim,
        popExitTransition = popExitAnim
    ) {
        com.codetrio.overdrive.ui.settings.PerformanceSettingsScreen(navController = navController)
    }

    composableWithBlur(
        route = SettingsRoute.CustomFonts.route,
        enterTransition = enterAnim,
        exitTransition = exitAnim,
        popEnterTransition = popEnterAnim,
        popExitTransition = popExitAnim
    ) {
        com.codetrio.overdrive.ui.settings.CustomFontSettingsScreen(navController = navController)
    }

    composableWithBlur(
        route = "onboarding",
        enterTransition = enterAnim,
        exitTransition = exitAnim,
        popEnterTransition = popEnterAnim,
        popExitTransition = popExitAnim
    ) {
        OnboardingScreen(
            onComplete = {
                navController.popBackStack()
            },
            onNavigateToSignIn = {
                navController.navigate("google_signin")
            }
        )
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// COMPOSE UI — MAIN SETTINGS BODY
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDetailTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

sealed class SettingsRoute(val route: String) {
    object Main : SettingsRoute("settings")
    object MusicManagement : SettingsRoute("music_management")
    object Account : SettingsRoute("account")
    object Appearance : SettingsRoute("appearance")
    object Playback : SettingsRoute("playback")
    object Haptics : SettingsRoute("haptics")
    object About : SettingsRoute("about")
    object Feedback : SettingsRoute("feedback")
    object WhatsNew : SettingsRoute("whats_new")
    object BackupRestore : SettingsRoute("backup_restore")
    object CustomizeBottomNav : SettingsRoute("customize_bottom_nav")
    object CustomFonts : SettingsRoute("custom_fonts")
}

@Composable
private fun SettingsMainScreen(navController: androidx.navigation.NavController) {
    val scrollState = rememberScrollState()
    var showKeyboardShortcutsDialog by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val context = LocalContext.current
    val mainActivity = context as? MainActivity

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset, available: Offset, source: NestedScrollSource
            ): Offset {
                if (!isLandscape && mainActivity != null) {
                    val delta = consumed.y
                    if (delta < -30f) mainActivity.hideBottomNavWithAnimation()
                    else if (delta > 30f) mainActivity.showBottomNavWithAnimation()
                }
                return super.onPostScroll(consumed, available, source)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .nestedScroll(nestedScrollConnection)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
            .padding(bottom = 400.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 16.dp, bottom = 24.dp)
        )

        SettingsGroupCard(
            items = listOf(
                {
                    SettingsCategoryItem(
                        title = stringResource(R.string.settings_cat_account),
                        subtitle = stringResource(R.string.settings_cat_account_sub),
                        icon = Icons.Rounded.AccountCircle,
                        onClick = { navController.navigate(SettingsRoute.Account.route) }
                    )
                },
                {
                    SettingsCategoryItem(
                        title = stringResource(R.string.settings_cat_playback),
                        subtitle = stringResource(R.string.settings_cat_playback_sub),
                        icon = Icons.Rounded.PlayCircle,
                        onClick = { navController.navigate(SettingsRoute.Playback.route) }
                    )
                },
                {
                    SettingsCategoryItem(
                        title = stringResource(R.string.settings_cat_music_mgmt),
                        subtitle = stringResource(R.string.settings_cat_music_mgmt_sub),
                        icon = Icons.Rounded.LibraryMusic,
                        onClick = { navController.navigate(SettingsRoute.MusicManagement.route) }
                    )
                },
                {
                    SettingsCategoryItem(
                        title = stringResource(R.string.settings_cat_appearance),
                        subtitle = stringResource(R.string.settings_cat_appearance_sub),
                        icon = Icons.Rounded.Palette,
                        onClick = { navController.navigate(SettingsRoute.Appearance.route) }
                    )
                },
                {
                    SettingsCategoryItem(
                        title = stringResource(R.string.settings_cat_haptics),
                        subtitle = stringResource(R.string.settings_cat_haptics_sub),
                        icon = Icons.Rounded.Vibration,
                        onClick = { navController.navigate(SettingsRoute.Haptics.route) }
                    )
                },
                {
                    SettingsCategoryItem(
                        title = stringResource(R.string.settings_cat_backup),
                        subtitle = stringResource(R.string.settings_cat_backup_sub),
                        icon = Icons.Rounded.SettingsBackupRestore,
                        onClick = { navController.navigate(SettingsRoute.BackupRestore.route) }
                    )
                },
                {
                    SettingsCategoryItem(
                        title = "キーボードショートカット",
                        subtitle = "物理キーボードでの操作一覧を表示",
                        icon = Icons.Rounded.Keyboard,
                        onClick = { showKeyboardShortcutsDialog = true }
                    )
                },
                {
                    SettingsCategoryItem(
                        title = stringResource(R.string.settings_cat_about),
                        subtitle = stringResource(R.string.settings_cat_about_sub),
                        icon = Icons.Rounded.Info,
                        onClick = { navController.navigate(SettingsRoute.About.route) }
                    )
                }
            )
        )

        if (showKeyboardShortcutsDialog) {
            com.codetrio.overdrive.ui.components.KeyboardShortcutsDialog(
                onDismissRequest = { showKeyboardShortcutsDialog = false }
            )
        }
    }
}

@Composable
private fun SettingsCategoryItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        },
        supportingContent = {
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
private fun MusicManagementScreen(
    navController: androidx.navigation.NavController,
    libraryPaths: List<String>,
    onRemovePath: (String) -> Unit,
    onAddPathClick: () -> Unit,
    songCacheSize: String,
    imageCacheSize: String,
    onClearSongCache: () -> Unit,
    onClearImageCache: () -> Unit,
    ignoreShortAudio: Boolean,
    onIgnoreShortAudioChange: (Boolean) -> Unit,
    ignoreShortAudioDuration: Float,
    onIgnoreShortAudioDurationChange: (Float) -> Unit,
    songCacheMaxSize: Int,
    onSongCacheMaxSizeChange: (Int) -> Unit,
    imageCacheMaxSize: Int,
    onImageCacheMaxSizeChange: (Int) -> Unit,
    downloadFolder: String?,
    onDownloadFolderClick: () -> Unit,
    hiddenFolders: List<String>,
    onAddHiddenFolderClick: () -> Unit,
    onRemoveHiddenFolder: (String) -> Unit,
    isScanning: Boolean,
    onRescanClick: () -> Unit,
    onRebuildDatabaseClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = { SettingsDetailTopBar(stringResource(R.string.settings_cat_music_mgmt)) { navController.popBackStack() } }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 400.dp)
            ) {
                SettingsHeader(stringResource(R.string.settings_header_library))
                SettingsGroupCard(buildList {
                    add { LibrarySourceHeader() }
                    libraryPaths.forEach { path ->
                        add { LibraryPathRow(path, onRemovePath) }
                    }
                    if (libraryPaths.isEmpty()) {
                        add { LibraryPathRow(stringResource(R.string.setting_music_source_default), null) }
                    }
                    add { AddMorePathRow(onAddPathClick) }
                })

                SettingsHeader(stringResource(R.string.settings_audio_filtering))
                SettingsGroupCard(buildList {
                    add {
                        IgnoreShortAudioRow(ignoreShortAudio, onIgnoreShortAudioChange)
                    }
                    if (ignoreShortAudio) {
                        add {
                            IgnoreShortAudioDurationRow(ignoreShortAudioDuration, onIgnoreShortAudioDurationChange)
                        }
                    }
                })

                SettingsHeader(stringResource(R.string.settings_hidden_folders))
                SettingsGroupCard(buildList {
                    hiddenFolders.forEach { folder ->
                        add { HiddenFolderRow(folder, onRemoveHiddenFolder) }
                    }
                    if (hiddenFolders.isEmpty()) {
                        add {
                            ListItem(
                                headlineContent = { Text(stringResource(R.string.settings_no_folders_blacklisted), style = MaterialTheme.typography.bodyLarge) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }
                    }
                    add {
                        AddHiddenFolderRow(onAddHiddenFolderClick)
                    }
                })

                SettingsHeader(stringResource(R.string.settings_downloads))
                SettingsGroupCard(listOf(
                    { DownloadFolderRow(downloadFolder, onDownloadFolderClick) }
                ))

                SettingsHeader(stringResource(R.string.settings_header_storage))
                SettingsGroupCard(buildList {
                    add { SongCacheSizeRow(songCacheSize, songCacheMaxSize, onSongCacheMaxSizeChange, onClearSongCache) }
                    add { ImageCacheSizeRow(imageCacheSize, imageCacheMaxSize, onImageCacheMaxSizeChange, onClearImageCache) }
                })

                SettingsHeader(stringResource(R.string.settings_database_scanning))
                SettingsGroupCard(buildList {
                    add {
                        ListItem(
                            onClick = onRescanClick,
                            content = {
                                Column {
                                    Text(stringResource(R.string.text_full_library_rescan), style = MaterialTheme.typography.bodyLarge)
                                    Text(stringResource(R.string.text_scan_local_storage_for_new_music_files), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            leadingContent = {
                                Icon(androidx.compose.material.icons.Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.primary)
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                    add {
                        ListItem(
                            onClick = onRebuildDatabaseClick,
                            content = {
                                Column {
                                    Text(stringResource(R.string.text_rebuild_database), style = MaterialTheme.typography.bodyLarge)
                                    Text(stringResource(R.string.text_clear_media_image_caches_then_full_re_index), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            leadingContent = {
                                Icon(androidx.compose.material.icons.Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                })
            }
        }

        if (isScanning) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.text_scanning_syncing_library),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BackupRestoreScreen(
    navController: androidx.navigation.NavController,
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit
) {
    Scaffold(
        topBar = { SettingsDetailTopBar(stringResource(R.string.settings_cat_backup)) { navController.popBackStack() } }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 400.dp)
        ) {
            SettingsHeader(stringResource(R.string.settings_backup_restore))
            SettingsGroupCard(buildList {
                add {
                    ListItem(
                        onClick = onBackupClick,
                        content = {
                            Column {
                                Text(stringResource(R.string.text_export_settings_library), style = MaterialTheme.typography.bodyLarge)
                                Text(stringResource(R.string.text_export_playlists_favorites_and_preferences_to_json), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        leadingContent = {
                            Icon(
                                painter = painterResource(R.drawable.ic_download),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
                add {
                    ListItem(
                        onClick = onRestoreClick,
                        content = {
                            Column {
                                Text(stringResource(R.string.text_import_settings_library), style = MaterialTheme.typography.bodyLarge)
                                Text(stringResource(R.string.text_restore_playlists_favorites_and_preferences_from_json), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        leadingContent = {
                            Icon(
                                painter = painterResource(R.drawable.ic_download),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp).graphicsLayer { rotationZ = 180f }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            })
        }
    }
}

@Composable
private fun IgnoreShortAudioRow(checked: Boolean, onToggle: (Boolean) -> Unit) {
    ListItem(
        onClick = { onToggle(!checked) },
        content = {
            Column {
                Text(
                    text = stringResource(R.string.setting_ignore_short_audio),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.setting_ignore_short_audio_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onToggle
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun IgnoreShortAudioDurationRow(
    value: Float, onValueChange: (Float) -> Unit
) {
    ListItem(
        onClick = { },
        content = {
            Column {
                Text(
                    text = stringResource(R.string.setting_min_duration_threshold),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.setting_ignore_tracks_under_sec, value.toInt()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        supportingContent = {
            Slider(
                value = value.coerceIn(10f, 120f),
                onValueChange = onValueChange,
                valueRange = 10f..120f,
                steps = 10,
                modifier = Modifier.fillMaxWidth()
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun HiddenFolderRow(path: String, onRemove: (String) -> Unit) {
    ListItem(
        onClick = { },
        content = {
            Text(
                text = path.substringAfterLast("/"),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = path,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_folder_open),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingContent = {
            Text(
                text = stringResource(R.string.text_remove),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onRemove(path) }
                    .padding(8.dp)
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun AddHiddenFolderRow(onClick: () -> Unit) {
    ListItem(
        onClick = onClick,
        content = {
            Text(
                text = stringResource(R.string.setting_add_hidden_folder),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun AccountScreen(
    navController: androidx.navigation.NavController,
    ytCookies: String?,
    onYtCookiesChange: (String?) -> Unit,
    dataSaver: Boolean,
    onDataSaverChange: (Boolean) -> Unit,
    pauseHistory: Boolean,
    onPauseHistoryChange: (Boolean) -> Unit
) {
    Scaffold(
        topBar = { SettingsDetailTopBar(stringResource(R.string.settings_cat_account)) { navController.popBackStack() } }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 400.dp)
        ) {
            SettingsHeader(stringResource(R.string.yt_music_account))
            SettingsGroupCard(listOf(
                {
                    var showLoginDialog by remember { mutableStateOf(false) }

                    if (showLoginDialog) {
                        YouTubeMusicLoginDialog(
                            onDismissRequest = { showLoginDialog = false },
                            onLoginSuccess = { cookies ->
                                onYtCookiesChange(cookies)
                                showLoginDialog = false
                            }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (ytCookies != null) stringResource(R.string.logged_in_yt) else stringResource(R.string.guest_mode),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (ytCookies != null) {
                                Text(
                                    text = stringResource(R.string.profile_spatialflow),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = if (ytCookies != null) stringResource(R.string.accessing_personalized) else stringResource(R.string.no_account_linked),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                if (ytCookies != null) {
                                    onYtCookiesChange(null)
                                } else {
                                    showLoginDialog = true
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (ytCookies != null) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                                contentColor = if (ytCookies != null) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Text(if (ytCookies != null) stringResource(R.string.log_out) else stringResource(R.string.log_in))
                        }
                    }
                }
            ))

            SettingsHeader(stringResource(R.string.data_saving))
            SettingsGroupCard(listOf(
                { DataSaverRow(dataSaver, onDataSaverChange) }
            ))

            SettingsHeader(stringResource(R.string.sync_privacy))
            SettingsGroupCard(listOf(
                { PauseHistoryRow(pauseHistory, onPauseHistoryChange) },
                { ManualSyncRow() }
            ))
        }
    }
}


@Composable
private fun DataSaverRow(checked: Boolean, onToggle: (Boolean) -> Unit) {
    ListItem(
        onClick = { onToggle(!checked) },
        content = {
            Column {
                Text(
                    text = stringResource(R.string.text_data_saver_wi_fi_only),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.text_restrict_streaming_and_high_res_cover_art_downloads_to_wi_fi_only),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onToggle
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun PauseHistoryRow(checked: Boolean, onToggle: (Boolean) -> Unit) {
    ListItem(
        onClick = { onToggle(!checked) },
        content = {
            Column {
                Text(
                    text = stringResource(R.string.pause_history_title),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.pause_history_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onToggle
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun ManualSyncRow() {
    var isSyncing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ListItem(
        onClick = {
            if (!isSyncing) {
                isSyncing = true
                scope.launch {
                    delay(2000) // Simulate sync
                    isSyncing = false
                }
            }
        },
        content = {
            Column {
                Text(text = stringResource(R.string.text_manual_sync), style = MaterialTheme.typography.bodyLarge)
                Text(text = stringResource(R.string.text_sync_playlists_and_favorites_with_server), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        trailingContent = {
            if (isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Sync Now",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun ThemeModeSelector(
    selectedMode: String,
    onModeSelect: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val options = listOf(
        Triple("system", stringResource(R.string.onboarding_mode_system), Icons.Filled.SettingsBrightness),
        Triple("dark", stringResource(R.string.onboarding_mode_dark), Icons.Filled.DarkMode),
        Triple("light", stringResource(R.string.onboarding_mode_light), Icons.Filled.LightMode)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        options.forEach { (mode, label, icon) ->
            val isSelected = selectedMode == mode
            val targetContainerColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "themeContainer_$mode"
            )
            val targetBorderColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "themeBorder_$mode"
            )

            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onModeSelect(mode)
                },
                shape = RoundedCornerShape(22.dp),
                color = targetContainerColor,
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = targetBorderColor
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }

                    if (isSelected) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(16.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppearanceScreen(
    navController: androidx.navigation.NavController,
    themeMode: String,
    onThemeModeChange: (String) -> Unit,
    amoledBlack: Boolean,
    onAmoledBlackChange: (Boolean) -> Unit,
    showAnimatedArt: Boolean,
    onShowAnimatedArtChange: (Boolean) -> Unit,
    dynamicAlbumTheme: Boolean,
    onDynamicAlbumThemeChange: (Boolean) -> Unit,
    hideNavLabels: Boolean,
    onHideNavLabelsChange: (Boolean) -> Unit,
    hideNavOnScroll: Boolean,
    onHideNavOnScrollChange: (Boolean) -> Unit,
    dynamicNavStyle: Boolean,
    onDynamicNavStyleChange: (Boolean) -> Unit,
    navigationBlur: Boolean,
    onNavigationBlurChange: (Boolean) -> Unit,
    tabSwitchBlur: Boolean,
    onTabSwitchBlurChange: (Boolean) -> Unit,
    playerTheme: String,
    onPlayerThemeChange: (String) -> Unit,
    forceHighRefreshRate: Boolean,
    onForceHighRefreshRateChange: (Boolean) -> Unit,
    appLanguage: String,
    onAppLanguageChange: (String) -> Unit,
    floatingNavBar: Boolean,
    onFloatingNavBarChange: (Boolean) -> Unit,
    unifiedFloatingBar: Boolean,
    onUnifiedFloatingBarChange: (Boolean) -> Unit
) {
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isEffectiveDark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemDark
    }

    Scaffold(
        topBar = { SettingsDetailTopBar(stringResource(R.string.settings_cat_appearance)) { navController.popBackStack() } }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 400.dp)
        ) {
            SettingsHeader(stringResource(R.string.settings_header_theme_mode))
            ThemeModeSelector(
                selectedMode = themeMode,
                onModeSelect = onThemeModeChange
            )

            AnimatedVisibility(
                visible = isEffectiveDark,
                enter = fadeIn(tween(220)) + expandVertically(spring(stiffness = Spring.StiffnessMediumLow)),
                exit = fadeOut(tween(180)) + shrinkVertically(spring(stiffness = Spring.StiffnessMediumLow))
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    AmoledBlackRow(amoledBlack, onAmoledBlackChange)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            SettingsHeader(stringResource(R.string.settings_header_general))
            SettingsGroupCard(buildList {
                add { AppLanguageRow(appLanguage, onAppLanguageChange) }
                add { HighRefreshRateRow(forceHighRefreshRate, onForceHighRefreshRateChange) }
            })

            SettingsHeader(stringResource(R.string.settings_visual_effects))
            SettingsGroupCard(buildList {
                add {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.setting_animated_album_art), style = MaterialTheme.typography.bodyLarge) },
                        supportingContent = { Text(stringResource(R.string.setting_animated_album_art_desc), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        trailingContent = { Switch(checked = showAnimatedArt, onCheckedChange = onShowAnimatedArtChange) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { onShowAnimatedArtChange(!showAnimatedArt) }
                    )
                }
                add { CustomFontSettingsRow { navController.navigate(SettingsRoute.CustomFonts.route) } }
                add { PlayerThemeRow(playerTheme, onPlayerThemeChange) }
                add { DynamicAlbumThemeRow(dynamicAlbumTheme, onDynamicAlbumThemeChange) }
                add { NavigationBlurRow(navigationBlur, onNavigationBlurChange) }
                // Only show tab-blur toggle when blur is enabled globally
                if (navigationBlur) {
                    add { TabSwitchBlurRow(tabSwitchBlur, onTabSwitchBlurChange) }
                }
            })

            SettingsHeader(stringResource(R.string.settings_navigation_bar))
            SettingsGroupCard(buildList {
                add { BottomNavCustomizeRow { navController.navigate(SettingsRoute.CustomizeBottomNav.route) } }
                add { HideNavOnScrollRow(hideNavOnScroll, onHideNavOnScrollChange) }
                add { HideNavLabelsRow(hideNavLabels, onHideNavLabelsChange) }
                add { DynamicNavStyleRow(dynamicNavStyle, onDynamicNavStyleChange) }
                add { FloatingNavBarRow(floatingNavBar, onFloatingNavBarChange) }
                if (floatingNavBar) {
                    add { UnifiedFloatingBarRow(unifiedFloatingBar, onUnifiedFloatingBarChange) }
                }
            })

            SettingsHeader(stringResource(R.string.settings_header_performance))
            SettingsGroupCard(buildList {
                add { PerformanceModeRow() }
                add { DisableBlurRow() }
            })
        }
    }
}

@Composable
private fun CustomFontSettingsRow(onClick: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(R.string.setting_custom_fonts),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = stringResource(R.string.setting_custom_fonts_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                Icons.Rounded.FontDownload,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun PlayerThemeRow(currentTheme: String, onThemeChange: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    val themeText = stringResource(com.codetrio.overdrive.ui.player.themes.PlayerThemeType.fromId(currentTheme).titleRes)

    ListItem(
        headlineContent = {
            Text(
                text = stringResource(R.string.setting_player_theme),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = themeText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Rounded.Palette,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { showDialog = true }
    )

    if (showDialog) {
        com.codetrio.overdrive.ui.player.PlayerThemeBottomSheet(
            onDismissRequest = { showDialog = false },
            currentTheme = currentTheme,
            onThemeSelect = { selectedTheme ->
                onThemeChange(selectedTheme)
            }
        )
    }
}

@Composable
private fun PerformanceModeRow() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE) }
    var currentMode by remember { mutableStateOf(prefs.getString(com.codetrio.overdrive.util.PerformanceManager.KEY_PERFORMANCE_MODE, "auto") ?: "auto") }
    var showDialog by remember { mutableStateOf(false) }

    val modeLabel = when (currentMode) {
        "low_end" -> stringResource(R.string.performance_mode_lite)
        "high_quality" -> stringResource(R.string.performance_mode_high)
        else -> stringResource(R.string.performance_mode_auto)
    }

    ListItem(
        headlineContent = {
            Text(stringResource(R.string.setting_performance_mode), style = MaterialTheme.typography.bodyLarge)
        },
        supportingContent = {
            Text(modeLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Rounded.Speed,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingContent = {
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { showDialog = true }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.setting_performance_mode)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        stringResource(R.string.setting_performance_mode_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val modes = listOf(
                        "auto" to stringResource(R.string.performance_mode_auto),
                        "high_quality" to stringResource(R.string.performance_mode_high),
                        "low_end" to stringResource(R.string.performance_mode_lite)
                    )

                    modes.forEach { (modeKey, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentMode = modeKey
                                    prefs.edit().putString(com.codetrio.overdrive.util.PerformanceManager.KEY_PERFORMANCE_MODE, modeKey).apply()
                                    showDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentMode == modeKey,
                                onClick = {
                                    currentMode = modeKey
                                    prefs.edit().putString(com.codetrio.overdrive.util.PerformanceManager.KEY_PERFORMANCE_MODE, modeKey).apply()
                                    showDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.action_close))
                }
            }
        )
    }
}

@Composable
private fun DisableBlurRow() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE) }
    var disableBlur by remember { mutableStateOf(prefs.getBoolean(com.codetrio.overdrive.util.PerformanceManager.KEY_DISABLE_BLUR, false)) }

    ListItem(
        headlineContent = {
            Text(stringResource(R.string.setting_disable_realtime_blur), style = MaterialTheme.typography.bodyLarge)
        },
        supportingContent = {
            Text(stringResource(R.string.setting_disable_realtime_blur_desc), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingContent = {
            Switch(
                checked = disableBlur,
                onCheckedChange = {
                    disableBlur = it
                    prefs.edit().putBoolean(com.codetrio.overdrive.util.PerformanceManager.KEY_DISABLE_BLUR, it).apply()
                }
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable {
            val newVal = !disableBlur
            disableBlur = newVal
            prefs.edit().putBoolean(com.codetrio.overdrive.util.PerformanceManager.KEY_DISABLE_BLUR, newVal).apply()
        }
    )
}

@Composable
private fun AppLanguageRow(currentLang: String, onSelect: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    val langLabel = when (currentLang) {
        "ja" -> stringResource(R.string.lang_japanese)
        "en" -> stringResource(R.string.lang_english)
        else -> stringResource(R.string.lang_system)
    }

    ListItem(
        headlineContent = {
            Text(
                text = stringResource(R.string.setting_app_language),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = langLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Rounded.Language,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { showDialog = true }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = stringResource(R.string.setting_app_language)) },
            text = {
                Column {
                    val options = listOf(
                        "system" to stringResource(R.string.lang_system),
                        "ja" to stringResource(R.string.lang_japanese),
                        "en" to stringResource(R.string.lang_english)
                    )
                    options.forEach { (code, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(code)
                                    showDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            RadioButton(
                                selected = (currentLang == code),
                                onClick = {
                                    onSelect(code)
                                    showDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun NavigationBlurRow(checked: Boolean, onToggle: (Boolean) -> Unit) {
    ListItem(
        onClick = { onToggle(!checked) },
        content = {
            Column {
                Text(
                    text = stringResource(R.string.setting_navigation_blur),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.setting_navigation_blur_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Rounded.Opacity,
                contentDescription = null,
                tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onToggle
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun TabSwitchBlurRow(checked: Boolean, onToggle: (Boolean) -> Unit) {
    ListItem(
        onClick = { onToggle(!checked) },
        content = {
            Column {
                Text(
                    text = stringResource(R.string.setting_tab_switch_blur),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.setting_tab_switch_blur_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Rounded.Opacity,
                contentDescription = null,
                tint = if (checked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onToggle
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun DynamicNavStyleRow(checked: Boolean, onToggle: (Boolean) -> Unit) {
    ListItem(
        onClick = { onToggle(!checked) },
        content = {
            Column {
                Text(
                    text = stringResource(R.string.setting_dynamic_nav_style),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.setting_dynamic_nav_style_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onToggle
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun HideNavLabelsRow(checked: Boolean, onToggle: (Boolean) -> Unit) {
    ListItem(
        onClick = { onToggle(!checked) },
        content = {
            Column {
                Text(
                    text = stringResource(R.string.setting_hide_nav_labels),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.setting_hide_nav_labels_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onToggle
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun HideNavOnScrollRow(checked: Boolean, onToggle: (Boolean) -> Unit) {
    ListItem(
        onClick = { onToggle(!checked) },
        content = {
            Column {
                Text(
                    text = stringResource(R.string.setting_hide_nav_on_scroll),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.setting_hide_nav_on_scroll_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onToggle
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun DynamicAlbumThemeRow(checked: Boolean, onToggle: (Boolean) -> Unit) {
    ListItem(
        onClick = { onToggle(!checked) },
        content = {
            Column {
                Text(
                    text = stringResource(R.string.setting_dynamic_colors),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.setting_dynamic_colors_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Rounded.Palette,
                contentDescription = null,
                tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onToggle
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun AmoledBlackRow(checked: Boolean, onToggle: (Boolean) -> Unit) {
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(R.string.setting_pure_black),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        supportingContent = {
            Text(
                text = stringResource(R.string.setting_pure_black_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Surface(
                shape = CircleShape,
                color = if (checked) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Contrast,
                        contentDescription = null,
                        tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onToggle
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { onToggle(!checked) }
    )
}

@Composable
private fun HighRefreshRateRow(checked: Boolean, onToggle: (Boolean) -> Unit) {
    val supportedHighestFps = com.codetrio.overdrive.util.rememberSupportedHighestFps()
    val isHighRefreshRateSupported = supportedHighestFps > 60.5f

    ListItem(
        onClick = { if (isHighRefreshRateSupported) onToggle(!checked) },
        content = {
            Column {
                Text(
                    text = stringResource(R.string.setting_force_high_refresh_rate),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = if (isHighRefreshRateSupported) {
                        stringResource(R.string.setting_max_supported_fps, supportedHighestFps.roundToInt())
                    } else {
                        stringResource(R.string.setting_not_supported_on_device)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onToggle,
                enabled = isHighRefreshRateSupported
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun PlaybackScreen(
    navController: androidx.navigation.NavController,
    crossfadeEnabled: Boolean,
    onCrossfadeToggle: (Boolean) -> Unit,
    crossfadeDuration: Float,
    onCrossfadeDurationChange: (Float) -> Unit,
    audioFocus: Boolean,
    onAudioFocusToggle: (Boolean) -> Unit,
    autoplayEnabled: Boolean,
    onAutoplayToggle: (Boolean) -> Unit,
    sleepTimerEndTime: Long,
    sleepTimerMode: PlayerSharedViewModel.SleepTimerMode,
    onStartSleepTimer: (Int) -> Unit,
    onCancelSleepTimer: () -> Unit,
    onSetEndOfSong: (Boolean) -> Unit,
    audioQuality: String,
    onAudioQualityChange: (String) -> Unit,
    volumeNormalizationEnabled: Boolean,
    onVolumeNormalizationChange: (Boolean) -> Unit,
    targetLufs: Float,
    onTargetLufsChange: (Float) -> Unit,
    showVolumeSlider: Boolean,
    onShowVolumeSliderChange: (Boolean) -> Unit,
    lyricsTranslationEnabled: Boolean,
    onLyricsTranslationToggle: (Boolean) -> Unit,
    lyricsTranslationEngine: String,
    onLyricsTranslationEngineChange: (String) -> Unit,
    liveUpdatesEnabled: Boolean,
    onLiveUpdatesToggle: (Boolean) -> Unit
) {
    Scaffold(
        topBar = { SettingsDetailTopBar(stringResource(R.string.settings_cat_playback)) { navController.popBackStack() } }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 400.dp)
        ) {
            SettingsHeader(stringResource(R.string.setting_live_updates))
            SettingsGroupCard(listOf(
                { LiveUpdatesRow(liveUpdatesEnabled, onLiveUpdatesToggle) }
            ))

            SettingsHeader(stringResource(R.string.setting_crossfade))
            SettingsGroupCard(listOf(
                { CrossfadeRow(crossfadeEnabled, onCrossfadeToggle, crossfadeDuration, onCrossfadeDurationChange) }
            ))

            SettingsHeader(stringResource(R.string.setting_streaming_quality))
            SettingsGroupCard(listOf(
                { AudioQualityRow(audioQuality, onAudioQualityChange) }
            ))
            
            SettingsHeader(stringResource(R.string.settings_audio_focus))
            SettingsGroupCard(listOf(
                { AudioFocusRow(audioFocus, onAudioFocusToggle) }
            ))

            SettingsHeader(stringResource(R.string.text_volume_bar))
            SettingsGroupCard(listOf(
                { ShowVolumeSliderRow(showVolumeSlider, onShowVolumeSliderChange) }
            ))

            SettingsHeader(stringResource(R.string.pref_lyrics_translation_title))
            SettingsGroupCard(listOf(
                { LyricsTranslationRow(lyricsTranslationEnabled, onLyricsTranslationToggle) },
                {
                    AnimatedVisibility(visible = lyricsTranslationEnabled) {
                        LyricsEngineRow(lyricsTranslationEngine, onLyricsTranslationEngineChange)
                    }
                }
            ))

            SettingsHeader(stringResource(R.string.settings_queue_autoplay))
            SettingsGroupCard(listOf(
                { AutoplayRow(autoplayEnabled, onAutoplayToggle) }
            ))

            SettingsHeader(stringResource(R.string.settings_volume_controls))
            SettingsGroupCard(listOf(
                { VolumeNormalizationRow(volumeNormalizationEnabled, onVolumeNormalizationChange, targetLufs, onTargetLufsChange) }
            ))

            SettingsHeader(stringResource(R.string.setting_sleep_timer))
            SettingsGroupCard(listOf(
                { SleepTimerSection(sleepTimerEndTime, sleepTimerMode, onStartSleepTimer, onCancelSleepTimer, onSetEndOfSong) }
            ))
        }
    }
}



@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AudioQualityRow(
    currentQuality: String,
    onQualityChange: (String) -> Unit
) {
    val options = listOf(
        "High" to stringResource(R.string.quality_high),
        "Normal" to stringResource(R.string.quality_normal),
        "Data Saver" to stringResource(R.string.quality_data_saver)
    )
    
    ListItem(
        headlineContent = {
            Column {
                Text(
                    text = stringResource(R.string.settings_streaming_quality),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.text_high_uses_more_data_data_saver_uses_lowest_bitrate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    options.forEach { (key, label) ->
                        val isSelected = key == currentQuality
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = { onQualityChange(key) }
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun VolumeNormalizationRow(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    targetLufs: Float,
    onTargetLufsChange: (Float) -> Unit
) {
    val options = listOf(
        Triple("Quiet", stringResource(R.string.loudness_quiet), -19f),
        Triple("Normal", stringResource(R.string.loudness_normal), -14f),
        Triple("Loud", stringResource(R.string.loudness_loud), -11f)
    )

    Column {
        ListItem(
            headlineContent = {
                Text(stringResource(R.string.settings_volume_normalization), style = MaterialTheme.typography.bodyLarge)
            },
            supportingContent = {
                Text(
                    stringResource(R.string.settings_volume_normalization_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingContent = {
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )

        AnimatedVisibility(visible = enabled) {
            ListItem(
                headlineContent = {
                    Column {
                        Text(
                            text = stringResource(R.string.text_target_loudness),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            options.forEach { (_, label, lufs) ->
                                val isSelected = lufs == targetLufs
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    onClick = { onTargetLufsChange(lufs) }
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
        }
    }
}

@Composable
private fun HapticsScreen(
    navController: androidx.navigation.NavController,
    hasHaptics: Boolean,
    vibrationStrength: Float,
    onVibrationStrengthChange: (Float) -> Unit,
    hapticPlayPause: Boolean,
    onHapticPlayPauseChange: (Boolean) -> Unit,
    hapticQueue: Boolean,
    onHapticQueueChange: (Boolean) -> Unit,
    hapticFavorite: Boolean,
    onHapticFavoriteChange: (Boolean) -> Unit
) {
    Scaffold(
        topBar = { SettingsDetailTopBar(stringResource(R.string.settings_cat_haptics)) { navController.popBackStack() } }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 400.dp)
        ) {
            SettingsHeader(stringResource(R.string.settings_haptics_title))
            SettingsGroupCard(buildList {
                if (!hasHaptics) {
                    add { HapticsNotSupported() }
                }
                add { VibrationStrengthRow(vibrationStrength, onVibrationStrengthChange, hasHaptics) }
            })

            SettingsHeader(stringResource(R.string.settings_granular_interactions))
            SettingsGroupCard(buildList {
                add { HapticToggleRow(stringResource(R.string.haptics_play_pause), stringResource(R.string.haptics_play_pause_desc), hapticPlayPause, onHapticPlayPauseChange, hasHaptics) }
                add { HapticToggleRow(stringResource(R.string.haptics_queue_reorder), stringResource(R.string.haptics_queue_reorder_desc), hapticQueue, onHapticQueueChange, hasHaptics) }
                add { HapticToggleRow(stringResource(R.string.haptics_favorite), stringResource(R.string.haptics_favorite_desc), hapticFavorite, onHapticFavoriteChange, hasHaptics) }
            })
        }
    }
}

@Composable
private fun HapticToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    enabled: Boolean
) {
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.38f,
        animationSpec = SmoothSpring,
        label = "haptic_row_alpha"
    )

    ListItem(
        onClick = { if (enabled) onToggle(!checked) },
        content = {
            Column(modifier = Modifier.graphicsLayer { this.alpha = alpha }) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onToggle,
                enabled = enabled
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
// ── Whats New ───────────────────────────────────────────────────────────────

@Composable
private fun WhatsNewScreen(navController: androidx.navigation.NavController) {
    val releases by androidx.compose.runtime.produceState<List<com.codetrio.overdrive.update.GitHubReleaseClient.ReleaseInfo>?>(initialValue = null) {
        withContext(Dispatchers.IO) {
            val client = com.codetrio.overdrive.update.GitHubReleaseClient("nemy-new", "OverDrive")
            value = client.getAllReleases()
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            androidx.compose.material3.LargeTopAppBar(
                title = { Text(stringResource(R.string.settings_whats_new)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.text_back))
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        if (releases == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (releases!!.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.whats_new_no_releases), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp)
            ) {
                val groupedReleases = releases!!.groupBy { release ->
                    release.tagName.removePrefix("v").removePrefix("V").substringBefore('.')
                }

                groupedReleases.forEach { (majorVersion, majorReleases) ->
                    item(key = "header_$majorVersion") {
                        Text(
                            text = stringResource(R.string.whats_new_version_series, majorVersion),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }

                    itemsIndexed(majorReleases, key = { _, rel -> rel.tagName }) { _, release ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = getSettingsSegmentedShape(index = 0, count = 2),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            text = release.tagName,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                )
                            }

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = getSettingsSegmentedShape(index = 1, count = 2),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val rawChangelog = release.changelog ?: ""
                                    val lines = rawChangelog.split("\n")
                                    
                                    lines.forEach { line ->
                                        val trimmed = line.trim()
                                        if (trimmed.isEmpty()) return@forEach
                                        
                                        // Try to parse line as an Image or GIF
                                        val imgUrl = when {
                                            trimmed.startsWith("![") -> {
                                                Regex("!\\s*\\[.*?\\]\\s*\\((.*?)\\)").find(trimmed)?.groupValues?.getOrNull(1)
                                            }
                                            trimmed.startsWith("<img", ignoreCase = true) -> {
                                                Regex("<img.*?src=[\"'](.*?)[\"'].*?>", RegexOption.IGNORE_CASE).find(trimmed)?.groupValues?.getOrNull(1)
                                            }
                                            trimmed.startsWith("http") && trimmed.contains(Regex("\\.(gif|png|jpe?g|webp)", RegexOption.IGNORE_CASE)) -> {
                                                trimmed
                                            }
                                            else -> null
                                        }

                                        if (imgUrl != null) {
                                            var isImgLoading by remember(imgUrl) { mutableStateOf(true) }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .defaultMinSize(minHeight = 120.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                            ) {
                                                coil.compose.AsyncImage(
                                                    model = coil.request.ImageRequest.Builder(LocalContext.current)
                                                        .data(imgUrl.trim())
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = "Release Image",
                                                    onState = { state ->
                                                        isImgLoading = state !is coil.compose.AsyncImagePainter.State.Success
                                                    },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .wrapContentHeight(),
                                                    contentScale = androidx.compose.ui.layout.ContentScale.FillWidth
                                                )
                                                if (isImgLoading) {
                                                    Box(
                                                        modifier = Modifier
                                                            .matchParentSize()
                                                            .shimmerEffect()
                                                    )
                                                }
                                            }
                                            return@forEach
                                        }

                                        // Clean bold markdown markers (e.g. **bold**)
                                        val cleanLine = trimmed
                                            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
                                            .replace(Regex("__(.*?)__"), "$1")
                                        
                                        val primaryColor = MaterialTheme.colorScheme.primary

                                        when {
                                            // 1. Headers: #, ##, ###, ####
                                            cleanLine.startsWith("#") -> {
                                                val cleanHeader = cleanLine.replace(Regex("^#+\\s*"), "")
                                                Text(
                                                    text = cleanHeader,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = primaryColor,
                                                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                                                )
                                            }
                                            // 2. Callout Boxes: Important:, Note:, Warning:
                                            cleanLine.startsWith("Important:", ignoreCase = true) ||
                                            cleanLine.startsWith("Note:", ignoreCase = true) ||
                                            cleanLine.startsWith("Warning:", ignoreCase = true) -> {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(androidx.compose.foundation.layout.IntrinsicSize.Min)
                                                        .padding(vertical = 4.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(primaryColor.copy(alpha = 0.08f))
                                                        .padding(vertical = 8.dp, horizontal = 12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .width(4.dp)
                                                            .fillMaxHeight()
                                                            .background(primaryColor, RoundedCornerShape(2.dp))
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Text(
                                                        text = cleanLine,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        lineHeight = 20.sp,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                            // 3. Bullet points (List items)
                                            cleanLine.startsWith("•") || cleanLine.startsWith("-") || cleanLine.startsWith("*") -> {
                                                val cleanBullet = cleanLine
                                                    .removePrefix("•")
                                                    .removePrefix("-")
                                                    .removePrefix("*")
                                                    .trim()
                                                
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                                                    verticalAlignment = Alignment.Top
                                                 ) {
                                                     Text(
                                                         text = "•",
                                                         style = MaterialTheme.typography.bodyMedium,
                                                         color = primaryColor,
                                                         modifier = Modifier.padding(end = 8.dp)
                                                     )
                                                     Text(
                                                         text = cleanBullet,
                                                         style = MaterialTheme.typography.bodyMedium,
                                                         color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                         modifier = Modifier.weight(1f),
                                                         lineHeight = 20.sp
                                                     )
                                                 }
                                             }
                                             // 4. Horizontal Dividers: ---
                                             cleanLine == "---" -> {
                                                 Box(
                                                     modifier = Modifier
                                                         .fillMaxWidth()
                                                         .padding(vertical = 8.dp)
                                                         .height(1.dp)
                                                         .background(MaterialTheme.colorScheme.outlineVariant)
                                                 )
                                             }
                                             // 5. Standard paragraph text
                                             else -> {
                                                 Text(
                                                     text = cleanLine,
                                                     style = MaterialTheme.typography.bodyMedium,
                                                     color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                     lineHeight = 20.sp
                                                 )
                                             }
                                         }
                                     }
                                 }
                             }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutScreen(
    navController: androidx.navigation.NavController,
    onCheckUpdate: () -> Unit,
    onWhatsNew: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = androidx.compose.runtime.remember { context.getSharedPreferences("AppSettings", android.content.Context.MODE_PRIVATE) }
    var isDeveloperMode by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(prefs.getBoolean("developer_mode", false)) }
    var tapCount by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }
    var currentToast by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<android.widget.Toast?>(null) }
    
    fun handleVersionTap() {
        if (isDeveloperMode) return
        tapCount++
        currentToast?.cancel()
        if (tapCount >= 7) {
            isDeveloperMode = true
            prefs.edit().putBoolean("developer_mode", true).apply()
            val t = android.widget.Toast.makeText(context, context.getString(R.string.developer_mode_enabled), android.widget.Toast.LENGTH_SHORT)
            currentToast = t
            t.show()
        } else if (tapCount >= 3) {
            val t = android.widget.Toast.makeText(context, context.getString(R.string.developer_steps_away, 7 - tapCount), android.widget.Toast.LENGTH_SHORT)
            currentToast = t
            t.show()
        }
    }

    Scaffold(
        topBar = { SettingsDetailTopBar(stringResource(R.string.settings_cat_about)) { navController.popBackStack() } }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 400.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Hero App Logo (Elegant Box)
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_applogo),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(72.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                
                // Version Pills
                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = BuildConfig.VERSION_NAME,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clickable(
                                    interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null
                                ) { handleVersionTap() }
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
                
                // Action Buttons
                androidx.compose.material3.ButtonGroup(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp)
                ) {
                    Button(
                        onClick = onCheckUpdate,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Rounded.Update, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.settings_updates), style = MaterialTheme.typography.labelLarge)
                    }
                    Button(
                        onClick = onWhatsNew,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Rounded.Info, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.settings_whats_new), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            
            if (isDeveloperMode) {
                Spacer(modifier = Modifier.height(32.dp))
                SettingsHeader(stringResource(R.string.settings_developer_options))
                SettingsGroupCard(
                    items = listOf {
                        SettingsCategoryItem(
                            icon = Icons.Rounded.Info,
                            title = stringResource(R.string.settings_developer_options),
                            subtitle = stringResource(R.string.settings_developer_options_desc),
                            onClick = { navController.navigate("developer_options") }
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            

        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// COMPOSE UI — COMPONENTS
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun SettingsHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 24.dp, bottom = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsGroupCard(items: List<@Composable () -> Unit>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        items.forEachIndexed { index, item ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = getSettingsSegmentedShape(index = index, count = items.size),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                item()
            }
        }
    }
}

private fun getSettingsSegmentedShape(index: Int, count: Int): Shape {
    val outer = 32.dp
    val inner = 4.dp
    return when {
        count <= 1 -> RoundedCornerShape(outer)
        index == 0 -> RoundedCornerShape(topStart = outer, topEnd = outer, bottomStart = inner, bottomEnd = inner)
        index == count - 1 -> RoundedCornerShape(topStart = inner, topEnd = inner, bottomStart = outer, bottomEnd = outer)
        else -> RoundedCornerShape(inner)
    }
}



// ── Haptics ─────────────────────────────────────────────────────────────────

@Composable
private fun HapticsNotSupported() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.settings_haptics_not_supported),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun VibrationStrengthRow(
    value: Float, onValueChange: (Float) -> Unit, enabled: Boolean
) {
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.38f,
        animationSpec = SmoothSpring,
        label = "haptic_alpha"
    )

    ListItem(
        onClick = { /* Slider handles interaction */ },
        content = {
            Text(
                text = stringResource(R.string.setting_vibration_strength),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
            )
        },
        supportingContent = {
            Slider(
                value = value.coerceIn(0f, 100f),
                onValueChange = onValueChange,
                enabled = enabled,
                valueRange = 0f..100f,
                steps = 3,
                modifier = Modifier.fillMaxWidth()
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

// ── Live Updates ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LiveUpdatesRow(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    ListItem(
        onClick = { onToggle(!enabled) },
        content = {
            Text(
                text = stringResource(R.string.setting_live_updates),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = stringResource(R.string.setting_live_updates_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Switch(
                checked = enabled,
                onCheckedChange = onToggle
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

// ── Crossfade ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CrossfadeRow(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    duration: Float,
    onDurationChange: (Float) -> Unit
) {
    val offText = stringResource(R.string.text_off)
    ListItem(
        onClick = { onToggle(!enabled) },
        verticalAlignment = Alignment.CenterVertically,
        content = {
            Text(
                text = stringResource(R.string.setting_crossfade),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Column {
                AnimatedContent(
                    targetState = if (enabled) "${duration.toInt()}s" else offText,
                    transitionSpec = {
                        fadeIn(spring(stiffness = Spring.StiffnessMedium)) togetherWith
                            fadeOut(spring(stiffness = Spring.StiffnessMedium))
                    },
                    label = "crossfade_label"
                ) { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                AnimatedVisibility(visible = enabled) {
                    Slider(
                        value = duration,
                        onValueChange = onDurationChange,
                        enabled = enabled,
                        valueRange = 0f..12f,
                        steps = 11,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        },
        trailingContent = { Switch(checked = enabled, onCheckedChange = onToggle) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

// ── Audio Focus ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AudioFocusRow(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    val enabledDesc = stringResource(R.string.setting_audio_focus_desc_enabled)
    val disabledDesc = stringResource(R.string.setting_audio_focus_desc_disabled)

    ListItem(
        onClick = { onToggle(!enabled) },
        content = {
            Column {
                Text(
                    text = stringResource(R.string.setting_audio_focus),
                    style = MaterialTheme.typography.bodyLarge
                )
                AnimatedContent(
                    targetState = enabled,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220, delayMillis = 90)) togetherWith
                        fadeOut(animationSpec = tween(90))
                    },
                    label = "audioFocusDesc"
                ) { isEnabled ->
                    Text(
                        text = if (isEnabled) enabledDesc else disabledDesc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_audio_focus),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingContent = { Switch(checked = enabled, onCheckedChange = onToggle) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

// ── Autoplay ─────────────────────────────────────────────────────────────

@Composable
private fun LyricsTranslationRow(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    ListItem(
        onClick = { onToggle(!enabled) },
        content = {
            Column {
                Text(
                    text = stringResource(R.string.pref_lyrics_translation_title),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.pref_lyrics_translation_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingContent = {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Rounded.Translate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingContent = { Switch(checked = enabled, onCheckedChange = onToggle) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun LyricsEngineRow(currentEngine: String, onEngineChange: (String) -> Unit) {
    val options = listOf(
        "mlkit" to stringResource(R.string.ml_model_engine_mlkit),
        "aicore" to stringResource(R.string.ml_model_engine_aicore)
    )
    
    ListItem(
        headlineContent = {
            Column {
                Text(
                    text = stringResource(R.string.ml_model_engine_title),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.ml_model_engine_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    options.forEach { (id, label) ->
                        val isSelected = id == currentEngine
                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = { onEngineChange(id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun AutoplayRow(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    ListItem(
        onClick = { onToggle(!enabled) },
        content = {
            Column {
                Text(
                    text = stringResource(R.string.text_autoplay),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.text_similar_songs_will_play_next),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_queue_music),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingContent = { Switch(checked = enabled, onCheckedChange = onToggle) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}


// ── Sleep Timer ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SleepTimerSection(
    endTime: Long, 
    mode: PlayerSharedViewModel.SleepTimerMode,
    onStart: (Int) -> Unit, 
    onCancel: () -> Unit,
    onSetEndOfSong: (Boolean) -> Unit
) {
    var showBottomSheet by remember { mutableStateOf(false) }

    val remaining by remember(endTime) {
        derivedStateOf { endTime - System.currentTimeMillis() }
    }

    Column {
        ListItem(
            onClick = { showBottomSheet = true },
            content = {
                Text(
                    text = stringResource(R.string.setting_sleep_timer),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            supportingContent = {
                val supportingText = when (mode) {
                    PlayerSharedViewModel.SleepTimerMode.OFF -> stringResource(R.string.setting_sleep_timer_off)
                    PlayerSharedViewModel.SleepTimerMode.END_OF_SONG -> stringResource(R.string.setting_sleep_timer_end_song)
                    PlayerSharedViewModel.SleepTimerMode.END_OF_QUEUE -> stringResource(R.string.setting_sleep_timer_end_queue)
                    PlayerSharedViewModel.SleepTimerMode.CUSTOM -> {
                        if (remaining > 0) stringResource(R.string.setting_sleep_timer_remaining, remaining / 60000)
                        else stringResource(R.string.setting_sleep_timer_off)
                    }
                }
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.ic_timer),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )

        if (showBottomSheet) {
            SleepTimerBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sleepTimerEndTime = endTime,
                sleepTimerMode = mode,
                onStartTimer = { mins ->
                    onStart(mins)
                },
                onCancelTimer = {
                    onCancel()
                },
                onSetEndOfSong = { enable ->
                    onSetEndOfSong(enable)
                }
            )
        }
    }
}

// ── Library ─────────────────────────────────────────────────────────────────

@Composable
private fun LibrarySourceHeader() {
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(R.string.setting_music_source),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_folder_open),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LibraryPathRow(path: String, onRemove: ((String) -> Unit)?) {
    ListItem(
        onClick = { },
        content = {
            Text(
                text = path.substringAfterLast(":"),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            Icon(
                painter = painterResource(
                    if (onRemove != null) R.drawable.ic_folder_music else R.drawable.ic_folder_open
                ),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingContent = if (onRemove != null) {
            {
                Text(
                    text = stringResource(R.string.text_remove),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onRemove(path) }
                        .padding(8.dp)
                )
            }
        } else null,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AddMorePathRow(onClick: () -> Unit) {
    ListItem(
        onClick = onClick,
        content = {
            Text(
                text = stringResource(R.string.setting_add_more_path),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

// ── Storage ─────────────────────────────────────────────────────────────────

@Composable
private fun DownloadFolderRow(path: String?, onClick: () -> Unit) {
    val notSetText = stringResource(R.string.setting_not_set)
    ListItem(
        onClick = onClick,
        content = {
            Column {
                Text(
                    text = stringResource(R.string.setting_download_location),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = path ?: notSetText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_folder_open),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun CacheSizeRow(
    title: String,
    currentSize: String, // from calculation, e.g. "120 MB"
    maxSize: Int,
    onMaxSizeChange: (Int) -> Unit,
    onClear: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val unlimitedText = stringResource(R.string.setting_unlimited)

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.setting_max_cache_size, title)) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    listOf(100, 500, 1024, 2048, 0).forEach { sizeOption ->
                        val text = if (sizeOption == 0) unlimitedText else if (sizeOption >= 1024) "${sizeOption / 1024} GB" else "$sizeOption MB"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onMaxSizeChange(sizeOption)
                                    showDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (maxSize == sizeOption),
                                onClick = {
                                    onMaxSizeChange(sizeOption)
                                    showDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = text, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    // Extract numerical value roughly to show progress
    val currentBytes = parseCacheSize(currentSize)
    val maxBytes = if (maxSize == 0) 0L else maxSize * 1024L * 1024L
    val progress = if (maxBytes > 0) (currentBytes.toFloat() / maxBytes).coerceIn(0f, 1f) else 0f

    ListItem(
        onClick = { showDialog = true },
        content = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedContent(
                        targetState = currentSize,
                        transitionSpec = {
                            fadeIn(spring(stiffness = Spring.StiffnessMedium)) togetherWith
                                fadeOut(spring(stiffness = Spring.StiffnessMedium))
                        },
                        label = "cache_size"
                    ) { size ->
                        Text(
                            text = size,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    val maxText = if (maxSize == 0) unlimitedText else if (maxSize >= 1024) "${maxSize / 1024} GB" else "$maxSize MB"
                    Text(
                        text = stringResource(R.string.setting_max_label, maxText),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (maxBytes > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(50)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        },
        trailingContent = {
            IconButton(onClick = onClear) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.setting_clear_cache),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

// Very basic string parsing for cache size
private fun parseCacheSize(sizeStr: String): Long {
    try {
        val split = sizeStr.trim().split(" ")
        if (split.size != 2) return 0L
        val value = split[0].toFloatOrNull() ?: return 0L
        val unit = split[1].uppercase()
        return when (unit) {
            "KB" -> (value * 1024).toLong()
            "MB" -> (value * 1024 * 1024).toLong()
            "GB" -> (value * 1024 * 1024 * 1024).toLong()
            "B" -> value.toLong()
            else -> 0L
        }
    } catch (_: Exception) {
        return 0L
    }
}

@Composable
private fun SongCacheSizeRow(
    cacheSize: String,
    maxSize: Int,
    onMaxSizeChange: (Int) -> Unit,
    onClear: () -> Unit
) {
    CacheSizeRow(
        title = stringResource(R.string.setting_song_cache),
        currentSize = cacheSize,
        maxSize = maxSize,
        onMaxSizeChange = onMaxSizeChange,
        onClear = onClear
    )
}

@Composable
private fun ImageCacheSizeRow(
    cacheSize: String,
    maxSize: Int,
    onMaxSizeChange: (Int) -> Unit,
    onClear: () -> Unit
) {
    CacheSizeRow(
        title = stringResource(R.string.setting_image_cache),
        currentSize = cacheSize,
        maxSize = maxSize,
        onMaxSizeChange = onMaxSizeChange,
        onClear = onClear
    )
}

// ── Credits ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SocialIconButton(icon: Int, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), CircleShape)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── App Logo ────────────────────────────────────────────────────────────────

@Composable
private fun AppLogoSection(isLandscape: Boolean) {
    val iconSize by animateDpAsState(
        targetValue = if (isLandscape) 160.dp else 250.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "logo_size"
    )
    val fontSize by animateFloatAsState(
        targetValue = if (isLandscape) 32f else 44f,
        animationSpec = SmoothSpring,
        label = "logo_text_size"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (isLandscape) 8.dp else 16.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_applogo),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(iconSize)
        )
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            fontSize = fontSize.sp,
            letterSpacing = 0.sp
        )
        Text(
            text = "© 2025 Shubham Karande",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeMusicLoginDialog(
    onDismissRequest: () -> Unit,
    onLoginSuccess: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.primary)
            }
        },
        title = {
            Text(
                text = stringResource(R.string.yt_login_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                Text(
                    text = stringResource(R.string.yt_login_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                ) {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                                
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        val cookieManager = CookieManager.getInstance()
                                        val cookies = cookieManager.getCookie("https://music.youtube.com")
                                        if (cookies != null && cookies.contains("SAPISID") && cookies.contains("HSID")) {
                                            onLoginSuccess(cookies)
                                        }
                                    }
                                }
                                
                                // Load Google login continuing to YouTube Music
                                loadUrl("https://accounts.google.com/ServiceLogin?service=youtube&passive=true&continue=https://music.youtube.com/")
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        },
        shape = RoundedCornerShape(28.dp),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}

// ── Feedback & Bug Reports ──────────────────────────────────────────────────

@Composable
private fun FeedbackScreen(navController: androidx.navigation.NavController) {
    val context = LocalContext.current
    val debugInfo = remember {
        """
        Device: ${Build.MANUFACTURER} ${Build.MODEL}
        Android: ${Build.VERSION.RELEASE}
        SDK: ${Build.VERSION.SDK_INT}
        App Version: ${BuildConfig.VERSION_NAME}
        """.trimIndent()
    }

    Scaffold(
        topBar = { SettingsDetailTopBar(stringResource(R.string.settings_feedback_bug_reports)) { navController.popBackStack() } }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 400.dp)
        ) {
            SettingsHeader(stringResource(R.string.settings_report_request))
            SettingsGroupCard(
                items = listOf(
                    {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.settings_report_a_bug), style = MaterialTheme.typography.bodyLarge) },
                            supportingContent = { Text(stringResource(R.string.settings_open_github_desc), style = MaterialTheme.typography.bodyMedium) },
                            leadingContent = { Icon(Icons.Rounded.BugReport, null, tint = MaterialTheme.colorScheme.error) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable {
                                val title = URLEncoder.encode("[Bug] ", "UTF-8")
                                val body = URLEncoder.encode("## Description\n\n\n## Device Information\n$debugInfo\n\n## Steps to Reproduce\n1.\n2.\n3.\n", "UTF-8")
                                val url = "https://github.com/nemy-new/OverDrive/issues/new?title=$title&body=$body"
                                context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                            }
                        )
                    },
                    {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.settings_request_feature), style = MaterialTheme.typography.bodyLarge) },
                            supportingContent = { Text(stringResource(R.string.settings_suggest_feature_desc), style = MaterialTheme.typography.bodyMedium) },
                            leadingContent = { Icon(Icons.Rounded.OpenInNew, null, tint = MaterialTheme.colorScheme.primary) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable {
                                val title = URLEncoder.encode("[Feature] ", "UTF-8")
                                val body = URLEncoder.encode("## Feature Description\n\n\n## Why is this needed?\n\n", "UTF-8")
                                val url = "https://github.com/nemy-new/OverDrive/issues/new?title=$title&body=$body"
                                context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                            }
                        )
                    }
                )
            )

            val prefs = context.getSharedPreferences("AppSettings", android.content.Context.MODE_PRIVATE)
            var debugToastsEnabled by remember { mutableStateOf(prefs.getBoolean("debug_toasts_enabled", false)) }

            SettingsHeader(stringResource(R.string.settings_debug_information))
            SettingsGroupCard(
                items = listOf(
                    {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.pref_debug_toasts), style = MaterialTheme.typography.bodyLarge) },
                            supportingContent = { Text(stringResource(R.string.pref_debug_toasts_desc), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            trailingContent = { Switch(checked = debugToastsEnabled, onCheckedChange = { 
                                debugToastsEnabled = it
                                prefs.edit().putBoolean("debug_toasts_enabled", it).apply()
                            }) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable { 
                                debugToastsEnabled = !debugToastsEnabled
                                prefs.edit().putBoolean("debug_toasts_enabled", debugToastsEnabled).apply()
                            }
                        )
                    },
                    {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.settings_copy_debug_info), style = MaterialTheme.typography.bodyLarge) },
                            supportingContent = { Text(stringResource(R.string.settings_copy_debug_desc), style = MaterialTheme.typography.bodyMedium) },
                            leadingContent = { Icon(Icons.Rounded.ContentCopy, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable {
                                val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Debug Info", debugInfo)
                                clipboardManager.setPrimaryClip(clip)
                            }
                        )
                    },
                    {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.settings_export_logs), style = MaterialTheme.typography.bodyLarge) },
                            supportingContent = { Text(stringResource(R.string.settings_share_logs_desc), style = MaterialTheme.typography.bodyMedium) },
                            leadingContent = { Icon(Icons.Rounded.Description, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, debugInfo)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Export Logs"))
                            }
                        )
                    },
                    {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.settings_simulate_crash), style = MaterialTheme.typography.bodyLarge) },
                            supportingContent = { Text(stringResource(R.string.settings_force_crash_desc), style = MaterialTheme.typography.bodyMedium) },
                            leadingContent = { Icon(Icons.Rounded.BugReport, null, tint = MaterialTheme.colorScheme.error) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable {
                                throw RuntimeException("Simulated App Crash: The user triggered a test exception.")
                            }
                        )
                    }
                )
            )
        }
    }
}

@Composable
private fun BottomNavCustomizeRow(onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.setting_customize_bottom_nav), style = MaterialTheme.typography.bodyLarge) },
        supportingContent = { Text(stringResource(R.string.setting_customize_bottom_nav_desc), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingContent = { Icon(androidx.compose.material.icons.Icons.Rounded.Palette, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp)) },
        trailingContent = { Icon(androidx.compose.material.icons.Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
private fun FloatingNavBarRow(floating: Boolean, onSelect: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.text_floating_nav_bar), style = MaterialTheme.typography.bodyLarge) },
        supportingContent = { Text(stringResource(R.string.text_floating_nav_bar_sub), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingContent = { Switch(checked = floating, onCheckedChange = onSelect) },
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
        modifier = Modifier.clickable { onSelect(!floating) }
    )
}

@Composable
private fun UnifiedFloatingBarRow(unified: Boolean, onSelect: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.text_unified_floating_bar), style = MaterialTheme.typography.bodyLarge) },
        supportingContent = { Text(stringResource(R.string.text_unified_floating_bar_sub), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingContent = { Switch(checked = unified, onCheckedChange = onSelect) },
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
        modifier = Modifier.clickable { onSelect(!unified) }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ShowVolumeSliderRow(checked: Boolean, onToggle: (Boolean) -> Unit) {
    ListItem(
        onClick = { onToggle(!checked) },
        content = {
            Text(stringResource(R.string.text_volume_bar), style = MaterialTheme.typography.bodyLarge)
        },
        supportingContent = {
            Text(
                stringResource(R.string.text_show_m3e_expressive_volume_slider_below_playback_controls),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = { Switch(checked = checked, onCheckedChange = onToggle) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun DeveloperOptionsScreen(navController: androidx.navigation.NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = androidx.compose.runtime.remember { context.getSharedPreferences("AppSettings", android.content.Context.MODE_PRIVATE) }
    var isDeveloperMode by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(prefs.getBoolean("developer_mode", false)) }

    Scaffold(
        topBar = { SettingsDetailTopBar(stringResource(R.string.settings_developer_options)) { navController.popBackStack() } }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 200.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingsHeader(stringResource(R.string.developer_playback_diagnostics))
            var showPlayerStats by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(prefs.getBoolean("show_player_stats", false)) }
            var showPlayerThemeChip by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(prefs.getBoolean("show_player_theme_chip", false)) }
            SettingsGroupCard(
                items = listOf(
                    {
                        androidx.compose.material3.ListItem(
                            headlineContent = { Text(stringResource(R.string.developer_show_player_stats)) },
                            supportingContent = { Text(stringResource(R.string.developer_show_player_stats_desc)) },
                            leadingContent = {
                                Icon(androidx.compose.ui.res.painterResource(R.drawable.ic_stats), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            trailingContent = {
                                androidx.compose.material3.Switch(
                                    checked = showPlayerStats,
                                    onCheckedChange = { checked ->
                                        showPlayerStats = checked
                                        prefs.edit().putBoolean("show_player_stats", checked).apply()
                                    }
                                )
                            },
                            modifier = Modifier.clickable {
                                val next = !showPlayerStats
                                showPlayerStats = next
                                prefs.edit().putBoolean("show_player_stats", next).apply()
                            }
                        )
                    },
                    {
                        androidx.compose.material3.ListItem(
                            headlineContent = { Text(stringResource(R.string.developer_show_player_theme_chip)) },
                            supportingContent = { Text(stringResource(R.string.developer_show_player_theme_chip_desc)) },
                            leadingContent = {
                                Icon(Icons.Rounded.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            trailingContent = {
                                androidx.compose.material3.Switch(
                                    checked = showPlayerThemeChip,
                                    onCheckedChange = { checked ->
                                        showPlayerThemeChip = checked
                                        prefs.edit().putBoolean("show_player_theme_chip", checked).apply()
                                    }
                                )
                            },
                            modifier = Modifier.clickable {
                                val next = !showPlayerThemeChip
                                showPlayerThemeChip = next
                                prefs.edit().putBoolean("show_player_theme_chip", next).apply()
                            }
                        )
                    },
                    {
                        androidx.compose.material3.ListItem(
                            headlineContent = { Text(stringResource(R.string.developer_playback_logs)) },
                            supportingContent = { Text(stringResource(R.string.developer_playback_logs_desc)) },
                            leadingContent = {
                                Icon(Icons.Rounded.BugReport, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            trailingContent = {
                                Icon(Icons.Default.ChevronRight, contentDescription = null)
                            },
                            modifier = Modifier.clickable {
                                navController.navigate("developer_playback_logs")
                            }
                        )
                    }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsHeader(stringResource(R.string.developer_setup_and_onboarding))
            SettingsGroupCard(
                items = listOf(
                    {
                        androidx.compose.material3.ListItem(
                            headlineContent = { Text(stringResource(R.string.developer_start_onboarding)) },
                            supportingContent = { Text(stringResource(R.string.developer_start_onboarding_desc)) },
                            leadingContent = {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            trailingContent = {
                                Icon(Icons.Default.ChevronRight, contentDescription = null)
                            },
                            modifier = Modifier.clickable {
                                navController.navigate("onboarding")
                            }
                        )
                    }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsHeader(stringResource(R.string.developer_performance_tuning))
            SettingsGroupCard(
                items = listOf(
                    {
                        androidx.compose.material3.ListItem(
                            headlineContent = { Text(stringResource(R.string.developer_performance)) },
                            supportingContent = { Text(stringResource(R.string.developer_performance_desc)) },
                            leadingContent = {
                                Icon(androidx.compose.ui.res.painterResource(R.drawable.ic_equalizer), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            trailingContent = {
                                Icon(Icons.Default.ChevronRight, contentDescription = null)
                            },
                            modifier = Modifier.clickable {
                                navController.navigate("developer_performance")
                            }
                        )
                    }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsHeader(stringResource(R.string.developer_controls))
            SettingsGroupCard(
                items = listOf(
                    {
                        androidx.compose.material3.ListItem(
                            headlineContent = { Text(stringResource(R.string.developer_disable)) },
                            supportingContent = { Text(stringResource(R.string.developer_disable_desc)) },
                            leadingContent = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            },
                            modifier = Modifier.clickable {
                                isDeveloperMode = false
                                prefs.edit().putBoolean("developer_mode", false).apply()
                                navController.popBackStack()
                            }
                        )
                    }
                )
            )
        }
    }
}
