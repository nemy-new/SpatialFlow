package com.codetrio.overdrive.cast

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ultra-lightweight local HTTP server running inside OverDrive.
 * Serves dynamic WebVTT subtitle files to Chromecast devices on the same local Wi-Fi network.
 */
@Singleton
class LocalLyricsServer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "LocalLyricsServer"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null

    @Volatile
    private var currentVttContent: String = "WEBVTT\n\n"

    @Volatile
    private var boundPort: Int = 0

    init {
        start()
    }

    @Synchronized
    fun start() {
        if (serverJob != null && serverJob?.isActive == true) return

        try {
            val server = ServerSocket(0) // Bind to any available ephemeral port
            serverSocket = server
            boundPort = server.localPort
            Log.d(TAG, "LocalLyricsServer started on port $boundPort")

            serverJob = scope.launch {
                while (isActive && !server.isClosed) {
                    try {
                        val client = server.accept()
                        scope.launch {
                            handleClient(client)
                        }
                    } catch (e: Exception) {
                        if (!server.isClosed) {
                            Log.w(TAG, "Socket accept error: ${e.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start LocalLyricsServer: ${e.message}", e)
        }
    }

    private fun handleClient(socket: Socket) {
        try {
            socket.use { s ->
                val reader = BufferedReader(InputStreamReader(s.getInputStream()))
                val line = reader.readLine() ?: return
                
                // We serve /lyrics.vtt
                val vttBytes = currentVttContent.toByteArray(Charsets.UTF_8)
                val out: OutputStream = s.getOutputStream()

                val headers = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/vtt; charset=utf-8\r\n" +
                        "Content-Length: ${vttBytes.size}\r\n" +
                        "Access-Control-Allow-Origin: *\r\n" +
                        "Access-Control-Allow-Methods: GET, OPTIONS\r\n" +
                        "Access-Control-Allow-Headers: *\r\n" +
                        "Connection: close\r\n\r\n"

                out.write(headers.toByteArray(Charsets.UTF_8))
                out.write(vttBytes)
                out.flush()
            }
        } catch (e: Exception) {
            Log.d(TAG, "Handled client request (${e.message})")
        }
    }

    /**
     * Updates the current WebVTT lyrics served to Cast devices.
     */
    fun updateLyrics(vttContent: String?) {
        this.currentVttContent = if (!vttContent.isNullOrBlank()) {
            vttContent
        } else {
            "WEBVTT\n\n"
        }
    }

    /**
     * Gets the full HTTP URL reachable by Chromecast on the local Wi-Fi.
     * E.g. `http://192.168.1.10:54321/lyrics.vtt`
     */
    fun getLyricsUrl(): String? {
        val ip = getLocalIpAddress() ?: return null
        if (boundPort <= 0) return null
        return "http://$ip:$boundPort/lyrics.vtt"
    }

    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return null
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isLoopback || !networkInterface.isUp) continue

                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error getting local IP address: ${e.message}")
        }
        return null
    }

    @Synchronized
    fun stop() {
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverJob?.cancel()
        serverSocket = null
        serverJob = null
    }
}
