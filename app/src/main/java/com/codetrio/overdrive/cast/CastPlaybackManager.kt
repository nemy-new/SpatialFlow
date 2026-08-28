package com.codetrio.overdrive.cast

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.images.WebImage
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import com.google.android.gms.cast.MediaTrack
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

sealed class CastState {
    object Disconnected : CastState()
    object Connecting : CastState()
    data class Connected(val deviceName: String) : CastState()
}

data class CastDeviceItem(
    val id: String,
    val name: String,
    val description: String? = null,
    val isSelected: Boolean = false,
    val isConnecting: Boolean = false,
    val isTv: Boolean = false,
    val isSpeaker: Boolean = false,
    val isGroup: Boolean = false,
    val isPhone: Boolean = false,
    val volume: Float = 1f
)

@Singleton
class CastPlaybackManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localLyricsServer: LocalLyricsServer
) {
    companion object {
        private const val TAG = "CastPlaybackManager"
        private const val PROGRESS_INTERVAL_MS = 250L
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var castContext: CastContext? = null
    private var currentCastSession: CastSession? = null
    private var tickerJob: Job? = null
    private var mediaRouter: MediaRouter? = null

    private val _castState = MutableStateFlow<CastState>(CastState.Disconnected)
    val castState: StateFlow<CastState> = _castState.asStateFlow()

    private val _availableDevices = MutableStateFlow<List<CastDeviceItem>>(emptyList())
    val availableDevices: StateFlow<List<CastDeviceItem>> = _availableDevices.asStateFlow()

    private val _currentVolume = MutableStateFlow(1.0f)
    val currentVolume: StateFlow<Float> = _currentVolume.asStateFlow()

    private val _isCastPlaying = MutableStateFlow(false)
    val isCastPlaying: StateFlow<Boolean> = _isCastPlaying.asStateFlow()

    private val _castPositionMs = MutableStateFlow(0L)
    val castPositionMs: StateFlow<Long> = _castPositionMs.asStateFlow()

    private val _castDurationMs = MutableStateFlow(0L)
    val castDurationMs: StateFlow<Long> = _castDurationMs.asStateFlow()

    private val mediaRouterCallback = object : MediaRouter.Callback() {
        override fun onRouteAdded(router: MediaRouter, route: MediaRouter.RouteInfo) {
            updateAvailableRoutes()
        }
        override fun onRouteRemoved(router: MediaRouter, route: MediaRouter.RouteInfo) {
            updateAvailableRoutes()
        }
        override fun onRouteChanged(router: MediaRouter, route: MediaRouter.RouteInfo) {
            updateAvailableRoutes()
        }
        override fun onRouteSelected(router: MediaRouter, route: MediaRouter.RouteInfo, reason: Int) {
            updateAvailableRoutes()
        }
        override fun onRouteUnselected(router: MediaRouter, route: MediaRouter.RouteInfo, reason: Int) {
            updateAvailableRoutes()
        }
        override fun onRouteVolumeChanged(router: MediaRouter, route: MediaRouter.RouteInfo) {
            updateAvailableRoutes()
        }
    }

    var onCastSessionConnected: ((startPosMs: Long) -> Unit)? = null
    var onCastSessionDisconnected: ((lastPosMs: Long) -> Unit)? = null
    var onRemotePlaybackStateChanged: ((isPlaying: Boolean) -> Unit)? = null
    var onSongFinished: (() -> Unit)? = null

    private val progressListener = RemoteMediaClient.ProgressListener { progressMs, durationMs ->
        _castPositionMs.value = progressMs
        if (durationMs > 0) {
            _castDurationMs.value = durationMs
        }
    }

    private val remoteMediaClientCallback = object : RemoteMediaClient.Callback() {
        override fun onStatusUpdated() {
            val rmc = currentCastSession?.remoteMediaClient ?: return
            val isPlaying = rmc.isPlaying
            val prevPlaying = _isCastPlaying.value
            _isCastPlaying.value = isPlaying
            
            val pos = rmc.approximateStreamPosition
            val dur = rmc.streamDuration
            if (pos >= 0) _castPositionMs.value = pos
            if (dur > 0) _castDurationMs.value = dur

            if (prevPlaying != isPlaying) {
                onRemotePlaybackStateChanged?.invoke(isPlaying)
            }

            // Check if playback ended normally -> trigger auto-advance to next song
            val mediaStatus = rmc.mediaStatus
            if (mediaStatus != null) {
                val playerState = mediaStatus.playerState
                val idleReason = mediaStatus.idleReason
                if (playerState == MediaStatus.PLAYER_STATE_IDLE && idleReason == MediaStatus.IDLE_REASON_FINISHED) {
                    Log.d(TAG, "⚡ Google Cast track finished (IDLE_REASON_FINISHED). Triggering auto-advance to next track.")
                    onSongFinished?.invoke()
                }
            }
        }

        override fun onMetadataUpdated() {
            val rmc = currentCastSession?.remoteMediaClient ?: return
            val dur = rmc.streamDuration
            if (dur > 0) _castDurationMs.value = dur
        }
    }

    private val sessionManagerListener = object : SessionManagerListener<CastSession> {
        override fun onSessionStarting(session: CastSession) {
            Log.d(TAG, "Cast session starting...")
            _castState.value = CastState.Connecting
        }

        override fun onSessionStarted(session: CastSession, sessionId: String) {
            val deviceName = session.castDevice?.friendlyName ?: "Cast Device"
            Log.d(TAG, "Cast session started: $sessionId on device $deviceName")
            currentCastSession = session
            _castState.value = CastState.Connected(deviceName)
            
            session.remoteMediaClient?.registerCallback(remoteMediaClientCallback)
            session.remoteMediaClient?.addProgressListener(progressListener, PROGRESS_INTERVAL_MS)
            startTicker()
            
            onCastSessionConnected?.invoke(_castPositionMs.value)
            updateAvailableRoutes()
        }

        override fun onSessionStartFailed(session: CastSession, error: Int) {
            Log.w(TAG, "Cast session start failed with error: $error")
            cleanupSession()
            _castState.value = CastState.Disconnected
        }

        override fun onSessionEnding(session: CastSession) {
            Log.d(TAG, "Cast session ending...")
        }

        override fun onSessionEnded(session: CastSession, error: Int) {
            Log.d(TAG, "Cast session ended (error: $error)")
            val lastPos = session.remoteMediaClient?.approximateStreamPosition ?: _castPositionMs.value
            cleanupSession()
            _castState.value = CastState.Disconnected
            _isCastPlaying.value = false
            onCastSessionDisconnected?.invoke(lastPos)
        }

        override fun onSessionResuming(session: CastSession, sessionId: String) {
            _castState.value = CastState.Connecting
            updateAvailableRoutes()
        }

        override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
            val deviceName = session.castDevice?.friendlyName ?: "Cast Device"
            Log.d(TAG, "Cast session resumed on device $deviceName")
            currentCastSession = session
            _castState.value = CastState.Connected(deviceName)
            session.remoteMediaClient?.registerCallback(remoteMediaClientCallback)
            session.remoteMediaClient?.addProgressListener(progressListener, PROGRESS_INTERVAL_MS)
            startTicker()
            updateAvailableRoutes()
        }

        override fun onSessionResumeFailed(session: CastSession, error: Int) {
            cleanupSession()
            _castState.value = CastState.Disconnected
        }

        override fun onSessionSuspended(session: CastSession, reason: Int) {
            Log.d(TAG, "Cast session suspended: $reason")
        }
    }

    init {
        initializeCastSafely()
    }

    private fun initializeCastSafely() {
        try {
            castContext = CastContext.getSharedInstance(context)
            castContext?.sessionManager?.addSessionManagerListener(sessionManagerListener, CastSession::class.java)
            mediaRouter = MediaRouter.getInstance(context)
            startScanning()

            val currentSession = castContext?.sessionManager?.currentCastSession
            if (currentSession != null && currentSession.isConnected) {
                currentCastSession = currentSession
                val deviceName = currentSession.castDevice?.friendlyName ?: "Cast Device"
                _castState.value = CastState.Connected(deviceName)
                currentSession.remoteMediaClient?.registerCallback(remoteMediaClientCallback)
                currentSession.remoteMediaClient?.addProgressListener(progressListener, PROGRESS_INTERVAL_MS)
                startTicker()
            }
            updateAvailableRoutes()
        } catch (e: Exception) {
            Log.w(TAG, "Google Cast framework is not available or failed to initialize: ${e.message}")
            castContext = null
            _castState.value = CastState.Disconnected
        }
    }

    fun startScanning() {
        try {
            val router = mediaRouter ?: return
            val selector = castContext?.mergedSelector ?: return
            router.addCallback(selector, mediaRouterCallback, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY)
            updateAvailableRoutes()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start MediaRouter scanning: ${e.message}")
        }
    }

    fun stopScanning() {
        try {
            mediaRouter?.removeCallback(mediaRouterCallback)
        } catch (_: Exception) {}
    }

    fun updateAvailableRoutes() {
        val router = mediaRouter ?: return
        val selector = castContext?.mergedSelector
        val routes = router.routes
        val selectedRoute = router.selectedRoute
        val isCastActive = isConnected

        val items = mutableListOf<CastDeviceItem>()

        // 1. Local Phone Route (Always at the top)
        val defaultRoute = router.defaultRoute
        val isPhoneSelected = !isCastActive || selectedRoute == defaultRoute
        items.add(
            CastDeviceItem(
                id = defaultRoute.id,
                name = "この端末 (スマートフォン)",
                description = if (isPhoneSelected) "現在再生中" else "内部スピーカー / イヤホン",
                isSelected = isPhoneSelected,
                isConnecting = false,
                isPhone = true,
                volume = 1f
            )
        )

        // 2. Discoverable Cast Routes
        for (route in routes) {
            if (route == defaultRoute) continue
            if (selector != null && !route.matchesSelector(selector)) continue

            val isSelected = isCastActive && (route.id == selectedRoute.id || route.name == currentDeviceName)
            val isConnecting = _castState.value is CastState.Connecting && route.id == selectedRoute.id

            val nameLower = route.name.lowercase()
            val isTv = nameLower.contains("tv") || nameLower.contains("bravia") || nameLower.contains("vizio") ||
                    nameLower.contains("fire") || nameLower.contains("chromecast with google tv") ||
                    route.deviceType == MediaRouter.RouteInfo.DEVICE_TYPE_TV
            val isGroup = nameLower.contains("group") || nameLower.contains("グループ") || nameLower.contains("pair")
            val isSpeaker = route.deviceType == MediaRouter.RouteInfo.DEVICE_TYPE_SPEAKER ||
                    nameLower.contains("speaker") || nameLower.contains("nest") || nameLower.contains("home") ||
                    nameLower.contains("audio")

            val currentVol = if (route.volumeMax > 0) route.volume.toFloat() / route.volumeMax.toFloat() else 1f

            items.add(
                CastDeviceItem(
                    id = route.id,
                    name = route.name,
                    description = route.description ?: if (isTv) "Android TV / Google TV" else if (isSpeaker) "Google Cast オーディオ" else "Google Cast デバイス",
                    isSelected = isSelected,
                    isConnecting = isConnecting,
                    isTv = isTv,
                    isSpeaker = isSpeaker,
                    isGroup = isGroup,
                    isPhone = false,
                    volume = currentVol
                )
            )
        }

        _availableDevices.value = items
    }

    fun selectDevice(deviceId: String) {
        val router = mediaRouter ?: return
        val defaultRoute = router.defaultRoute
        if (deviceId == defaultRoute.id) {
            // Disconnect from Cast and switch back to Phone
            disconnect()
            return
        }

        val targetRoute = router.routes.firstOrNull { it.id == deviceId }
        if (targetRoute != null) {
            Log.d(TAG, "Selecting Cast device route: ${targetRoute.name} (${targetRoute.id})")
            _castState.value = CastState.Connecting
            updateAvailableRoutes()
            router.selectRoute(targetRoute)
        }
    }

    fun setDeviceVolume(volume: Float) {
        try {
            val clamped = volume.coerceIn(0f, 1f)
            _currentVolume.value = clamped
            currentCastSession?.setVolume(clamped.toDouble())
            val router = mediaRouter
            val selected = router?.selectedRoute
            if (selected != null && selected.volumeMax > 0) {
                selected.requestSetVolume((clamped * selected.volumeMax).toInt())
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to set device volume: ${e.message}")
        }
    }

    private fun cleanupSession() {
        stopTicker()
        try {
            currentCastSession?.remoteMediaClient?.removeProgressListener(progressListener)
            currentCastSession?.remoteMediaClient?.unregisterCallback(remoteMediaClientCallback)
        } catch (_: Exception) {}
        currentCastSession = null
        updateAvailableRoutes()
    }

    private fun startTicker() {
        stopTicker()
        tickerJob = scope.launch {
            while (isActive) {
                val rmc = currentCastSession?.remoteMediaClient
                if (rmc != null && rmc.isPlaying) {
                    val pos = rmc.approximateStreamPosition
                    val dur = rmc.streamDuration
                    if (pos >= 0) _castPositionMs.value = pos
                    if (dur > 0) _castDurationMs.value = dur
                }
                delay(PROGRESS_INTERVAL_MS)
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    val isConnected: Boolean
        get() = _castState.value is CastState.Connected

    val currentDeviceName: String?
        get() = (_castState.value as? CastState.Connected)?.deviceName

    fun getApproximateStreamPosition(): Long {
        val rmc = currentCastSession?.remoteMediaClient ?: return _castPositionMs.value
        val pos = rmc.approximateStreamPosition
        return if (pos >= 0) pos else _castPositionMs.value
    }

    fun getStreamDuration(): Long {
        val rmc = currentCastSession?.remoteMediaClient ?: return _castDurationMs.value
        val dur = rmc.streamDuration
        return if (dur > 0) dur else _castDurationMs.value
    }

    fun isPlaying(): Boolean {
        return currentCastSession?.remoteMediaClient?.isPlaying ?: false
    }

    fun loadMedia(
        title: String,
        artist: String,
        albumArtUrl: String?,
        mediaUrl: String,
        mimeType: String = "audio/mp4",
        startPositionMs: Long = 0L,
        autoPlay: Boolean = true,
        lyricsVtt: String? = null
    ) {
        val rmc = currentCastSession?.remoteMediaClient ?: run {
            Log.w(TAG, "Cannot load media: RemoteMediaClient is null (not connected to Cast)")
            return
        }

        // Update local lyrics server content
        localLyricsServer.updateLyrics(lyricsVtt)
        val lyricsUrl = if (!lyricsVtt.isNullOrBlank()) localLyricsServer.getLyricsUrl() else null

        val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK).apply {
            putString(MediaMetadata.KEY_TITLE, title)
            putString(MediaMetadata.KEY_ARTIST, artist)
            albumArtUrl?.let { url ->
                if (url.isNotEmpty()) {
                    addImage(WebImage(Uri.parse(url)))
                }
            }
        }

        val mediaInfoBuilder = MediaInfo.Builder(mediaUrl)
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType(mimeType)
            .setMetadata(metadata)

        val activeTrackIds = mutableListOf<Long>()
        if (lyricsUrl != null) {
            val lyricsTrack = MediaTrack.Builder(1L, MediaTrack.TYPE_TEXT)
                .setName("Lyrics")
                .setSubtype(MediaTrack.SUBTYPE_SUBTITLES)
                .setContentId(lyricsUrl)
                .setContentType("text/vtt")
                .setLanguage("ja")
                .build()
            mediaInfoBuilder.setMediaTracks(listOf(lyricsTrack))
            activeTrackIds.add(1L)
            Log.d(TAG, "Attached WebVTT lyrics track to Cast: $lyricsUrl")
        }

        val mediaInfo = mediaInfoBuilder.build()

        val requestBuilder = MediaLoadRequestData.Builder()
            .setMediaInfo(mediaInfo)
            .setAutoplay(autoPlay)
            .setCurrentTime(startPositionMs)

        if (activeTrackIds.isNotEmpty()) {
            requestBuilder.setActiveTrackIds(activeTrackIds.toLongArray())
        }

        val request = requestBuilder.build()

        Log.d(TAG, "Loading media on Cast: title='$title', artist='$artist', startPos=${startPositionMs}ms, hasLyrics=${lyricsUrl != null}")
        rmc.load(request)
    }

    /**
     * Dynamically updates lyrics for the currently playing track on Cast.
     */
    fun updateLyricsTrack(lyricsVtt: String?) {
        localLyricsServer.updateLyrics(lyricsVtt)
        val rmc = currentCastSession?.remoteMediaClient ?: return
        if (!lyricsVtt.isNullOrBlank()) {
            try {
                rmc.setActiveMediaTracks(longArrayOf(1L))
                Log.d(TAG, "Activated WebVTT lyrics track on Cast")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to activate media tracks: ${e.message}")
            }
        }
    }

    fun play() {
        currentCastSession?.remoteMediaClient?.play()
    }

    fun pause() {
        currentCastSession?.remoteMediaClient?.pause()
    }

    fun seekTo(positionMs: Long) {
        _castPositionMs.value = positionMs
        currentCastSession?.remoteMediaClient?.seek(positionMs)
    }

    fun setVolume(volume: Double) {
        try {
            currentCastSession?.setVolume(volume.coerceIn(0.0, 1.0))
        } catch (e: Exception) {
            Log.w(TAG, "Failed to set Cast volume: ${e.message}")
        }
    }

    fun getVolume(): Double {
        return try {
            currentCastSession?.volume ?: 1.0
        } catch (_: Exception) {
            1.0
        }
    }

    fun disconnect() {
        try {
            castContext?.sessionManager?.endCurrentSession(true)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to end Cast session: ${e.message}")
        }
    }
}

