package com.codetrio.overdrive.data.diagnostics

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class LogLevel {
    INFO,
    SUCCESS,
    WARN,
    ERROR
}

data class PlaybackLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel = LogLevel.INFO,
    val tag: String,
    val message: String,
    val details: String? = null
) {
    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
}

data class PlaybackSession(
    val id: String = UUID.randomUUID().toString(),
    val startTime: Long = System.currentTimeMillis(),
    var songId: String? = null,
    var videoId: String? = null,
    var title: String = "Unknown Title",
    var artist: String = "Unknown Artist",
    var source: String = "Unknown",
    var streamUrl: String? = null,
    var mimeType: String? = null,
    var codec: String? = null,
    var bitrate: Int? = null,
    var sampleRate: Int? = null,
    var channelCount: Int? = null,
    var extractor: String? = null,
    var extractionDurationMs: Long? = null,
    var audioSessionId: Int? = null,
    var decoderName: String? = null,
    var isOffload: Boolean? = null,
    var playbackState: String = "IDLE",
    var bufferHealthMs: Long = 0L,
    var bufferedPositionMs: Long = 0L,
    var currentPositionMs: Long = 0L,
    var durationMs: Long = 0L,
    var loudnessDb: Float? = null,
    var isError: Boolean = false,
    var errorCodeName: String? = null,
    var errorMessage: String? = null,
    var errorStackTrace: String? = null,
    var httpStatusCode: Int? = null,
    var isAutoRecovered: Boolean = false,
    var recoveryAction: String? = null,
    private val _entries: MutableList<PlaybackLogEntry> = mutableListOf()
) {
    val entries: List<PlaybackLogEntry>
        get() = synchronized(_entries) { _entries.toList() }

    fun addEntry(entry: PlaybackLogEntry) {
        synchronized(_entries) {
            _entries.add(entry)
        }
    }

    val formattedStartTime: String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(startTime))
        }

    fun toFormattedString(): String {
        val sb = StringBuilder()
        sb.appendLine("=== OVERDRIVE PLAYBACK DIAGNOSTICS REPORT ===")
        sb.appendLine("Session ID: $id")
        sb.appendLine("Time: $formattedStartTime")
        sb.appendLine("Song: $title - $artist (ID: $songId, VideoID: $videoId)")
        sb.appendLine("Source: $source")
        sb.appendLine("----------------------------------------")
        sb.appendLine("Stream URL: ${streamUrl ?: "None"}")
        sb.appendLine("Format: MIME=${mimeType ?: "N/A"}, Codec=${codec ?: "N/A"}, Bitrate=${bitrate?.let { "${it / 1000}kbps" } ?: "N/A"}, SampleRate=${sampleRate?.let { "${it}Hz" } ?: "N/A"}")
        sb.appendLine("Extractor: ${extractor ?: "N/A"} (${extractionDurationMs?.let { "${it}ms" } ?: "N/A"})")
        sb.appendLine("----------------------------------------")
        sb.appendLine("Player State: $playbackState")
        sb.appendLine("Buffer: Health=${bufferHealthMs}ms, Buffered=${bufferedPositionMs}ms, Pos=${currentPositionMs}ms, Duration=${durationMs}ms")
        sb.appendLine("Decoder: ${decoderName ?: "N/A"}, AudioSessionId: ${audioSessionId ?: "N/A"}, Offload: ${isOffload ?: "N/A"}")
        sb.appendLine("Loudness: ${loudnessDb?.let { "${it}dB" } ?: "N/A"}")
        if (isError) {
            sb.appendLine("----------------------------------------")
            sb.appendLine("ERROR STATUS: TRUE")
            sb.appendLine("Error Code: $errorCodeName")
            sb.appendLine("HTTP Status: ${httpStatusCode ?: "N/A"}")
            sb.appendLine("Error Message: $errorMessage")
            if (!errorStackTrace.isNullOrEmpty()) {
                sb.appendLine("Stacktrace:\n$errorStackTrace")
            }
            if (isAutoRecovered) {
                sb.appendLine("Auto-Recovery: SUCCESS ($recoveryAction)")
            }
        }
        sb.appendLine("----------------------------------------")
        sb.appendLine("EVENT TIMELINE (${entries.size} events):")
        entries.forEach { entry ->
            sb.appendLine("[${entry.formattedTime}] [${entry.level}] [${entry.tag}] ${entry.message}")
            if (!entry.details.isNullOrEmpty()) {
                sb.appendLine("    └─ Details: ${entry.details}")
            }
        }
        sb.appendLine("========================================")
        return sb.toString()
    }
}

object PlaybackDiagnosticsLogger {
    private const val MAX_HISTORY_SESSIONS = 50
    private val sessionsLock = Any()
    private val _sessions = mutableListOf<PlaybackSession>()

    private val _currentSessionState = MutableStateFlow<PlaybackSession?>(null)
    val currentSessionState: StateFlow<PlaybackSession?> = _currentSessionState.asStateFlow()

    private val _sessionsHistoryState = MutableStateFlow<List<PlaybackSession>>(emptyList())
    val sessionsHistoryState: StateFlow<List<PlaybackSession>> = _sessionsHistoryState.asStateFlow()

    fun startSession(
        title: String,
        artist: String,
        songId: String? = null,
        videoId: String? = null,
        source: String = "Unknown"
    ): PlaybackSession {
        val session = PlaybackSession(
            title = title,
            artist = artist,
            songId = songId,
            videoId = videoId,
            source = source
        )
        session.addEntry(
            PlaybackLogEntry(
                level = LogLevel.INFO,
                tag = "Session",
                message = "Playback session started: \"$title\" by \"$artist\"",
                details = "SongId=$songId, VideoId=$videoId, Source=$source"
            )
        )

        synchronized(sessionsLock) {
            _sessions.add(0, session)
            if (_sessions.size > MAX_HISTORY_SESSIONS) {
                _sessions.removeAt(_sessions.lastIndex)
            }
            _sessionsHistoryState.value = _sessions.toList()
        }
        _currentSessionState.value = session
        return session
    }

    fun getActiveSession(): PlaybackSession? = _currentSessionState.value

    fun log(
        level: LogLevel = LogLevel.INFO,
        tag: String,
        message: String,
        details: String? = null,
        session: PlaybackSession? = null
    ) {
        val targetSession = session ?: _currentSessionState.value ?: return
        val entry = PlaybackLogEntry(
            level = level,
            tag = tag,
            message = message,
            details = details
        )
        targetSession.addEntry(entry)
        
        // Notify state update for Compose observers
        if (targetSession.id == _currentSessionState.value?.id) {
            _currentSessionState.value = targetSession.copy()
        }
        synchronized(sessionsLock) {
            _sessionsHistoryState.value = _sessions.map { if (it.id == targetSession.id) targetSession.copy() else it }
        }
    }

    fun updateStreamInfo(
        streamUrl: String?,
        mimeType: String? = null,
        codec: String? = null,
        bitrate: Int? = null,
        sampleRate: Int? = null,
        channelCount: Int? = null,
        extractor: String? = null,
        extractionDurationMs: Long? = null,
        session: PlaybackSession? = null
    ) {
        val targetSession = session ?: _currentSessionState.value ?: return
        targetSession.streamUrl = streamUrl
        if (mimeType != null) targetSession.mimeType = mimeType
        if (codec != null) targetSession.codec = codec
        if (bitrate != null) targetSession.bitrate = bitrate
        if (sampleRate != null) targetSession.sampleRate = sampleRate
        if (channelCount != null) targetSession.channelCount = channelCount
        if (extractor != null) targetSession.extractor = extractor
        if (extractionDurationMs != null) targetSession.extractionDurationMs = extractionDurationMs

        log(
            level = LogLevel.SUCCESS,
            tag = "StreamExtractor",
            message = "Stream resolved via ${extractor ?: "Unknown"} in ${extractionDurationMs ?: 0}ms",
            details = "URL=$streamUrl, MIME=$mimeType, Codec=$codec, Bitrate=$bitrate, SampleRate=$sampleRate",
            session = targetSession
        )
    }

    fun updatePlaybackState(
        stateName: String,
        bufferHealthMs: Long = 0L,
        bufferedPositionMs: Long = 0L,
        currentPositionMs: Long = 0L,
        durationMs: Long = 0L,
        session: PlaybackSession? = null
    ) {
        val targetSession = session ?: _currentSessionState.value ?: return
        val previousState = targetSession.playbackState
        targetSession.playbackState = stateName
        targetSession.bufferHealthMs = bufferHealthMs
        targetSession.bufferedPositionMs = bufferedPositionMs
        targetSession.currentPositionMs = currentPositionMs
        targetSession.durationMs = durationMs

        if (previousState != stateName) {
            val level = when (stateName) {
                "READY", "PLAYING" -> LogLevel.SUCCESS
                "BUFFERING" -> LogLevel.INFO
                "ENDED" -> LogLevel.INFO
                else -> LogLevel.INFO
            }
            log(
                level = level,
                tag = "ExoPlayer",
                message = "State: $previousState ➔ $stateName",
                details = "Pos: ${currentPositionMs}ms / ${durationMs}ms (Buffer Health: ${bufferHealthMs}ms)",
                session = targetSession
            )
        }
    }

    fun updateAudioSink(
        audioSessionId: Int? = null,
        decoderName: String? = null,
        isOffload: Boolean? = null,
        loudnessDb: Float? = null,
        session: PlaybackSession? = null
    ) {
        val targetSession = session ?: _currentSessionState.value ?: return
        if (audioSessionId != null) targetSession.audioSessionId = audioSessionId
        if (decoderName != null) targetSession.decoderName = decoderName
        if (isOffload != null) targetSession.isOffload = isOffload
        if (loudnessDb != null) targetSession.loudnessDb = loudnessDb

        log(
            level = LogLevel.INFO,
            tag = "AudioOutput",
            message = "AudioSink configured: Decoder=${decoderName ?: "N/A"}, SessionId=${audioSessionId ?: "N/A"}",
            details = "Offload=$isOffload, LoudnessDb=$loudnessDb",
            session = targetSession
        )
    }

    fun logError(
        errorCodeName: String,
        errorMessage: String?,
        throwable: Throwable? = null,
        httpStatusCode: Int? = null,
        session: PlaybackSession? = null
    ) {
        val targetSession = session ?: _currentSessionState.value ?: return
        targetSession.isError = true
        targetSession.errorCodeName = errorCodeName
        targetSession.errorMessage = errorMessage
        targetSession.httpStatusCode = httpStatusCode
        targetSession.errorStackTrace = throwable?.stackTraceToString()

        log(
            level = LogLevel.ERROR,
            tag = "PlaybackError",
            message = "Playback Exception ($errorCodeName): $errorMessage",
            details = buildString {
                if (httpStatusCode != null) append("HTTP Status: $httpStatusCode\n")
                if (throwable != null) append("Exception: ${throwable.javaClass.simpleName}: ${throwable.message}")
            },
            session = targetSession
        )
    }

    fun logRecovery(
        action: String,
        details: String? = null,
        session: PlaybackSession? = null
    ) {
        val targetSession = session ?: _currentSessionState.value ?: return
        targetSession.isAutoRecovered = true
        targetSession.recoveryAction = action

        log(
            level = LogLevel.WARN,
            tag = "AutoRecovery",
            message = "Recovery triggered: $action",
            details = details,
            session = targetSession
        )
    }

    fun getAllLogsFormatted(): String {
        val sb = StringBuilder()
        sb.appendLine("=== OVERDRIVE ALL PLAYBACK SESSIONS DUMP ===")
        sb.appendLine("Total recorded sessions: ${_sessions.size}")
        sb.appendLine("==============================================")
        sb.appendLine()
        synchronized(sessionsLock) {
            _sessions.forEach { session ->
                sb.appendLine(session.toFormattedString())
                sb.appendLine()
            }
        }
        return sb.toString()
    }

    fun clearAllLogs() {
        synchronized(sessionsLock) {
            _sessions.clear()
            _sessionsHistoryState.value = emptyList()
        }
        _currentSessionState.value = null
    }
}
