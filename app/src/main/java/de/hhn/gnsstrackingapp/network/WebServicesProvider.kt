package de.hhn.gnsstrackingapp.network

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import okio.ByteString.Companion.toByteString

class WebServicesProvider(private val url: String) {
    private val client = HttpClient(OkHttp) {
        install(WebSockets)
    }

    var socketEventChannel: Channel<SocketUpdate> = Channel(10)
    var connected = mutableStateOf(false)

    suspend fun startSocket() {
        var retryCount = 0

        while (!connected.value && retryCount < MAX_RETRIES) {
            try {
                client.webSocket(urlString = url) {
                    Log.d("WebSocket", "Connected to WebSocket: $url")

                    connected.value = true
                    retryCount = 0

                    while (isActive) {
                        try {
                            when (val frame = incoming.receive()) {
                                is Frame.Text -> {
                                    val message = frame.readText()
                                    socketEventChannel.send(SocketUpdate(text = message))
                                }

                                is Frame.Binary -> {
                                    val data = frame.data
                                    socketEventChannel.send(SocketUpdate(byteString = data.toByteString()))
                                }

                                else -> {
                                    Log.d("WebSocket", "Received Unsupported Frame: $frame")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("WebSocket", "Error receiving frame: ${e.message}", e)
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("WebSocket", "WebSocket connection failed: ${e.message}", e)
                socketEventChannel.send(SocketUpdate(exception = e))
                retryCount++
                if (retryCount < MAX_RETRIES) {
                    delay(RETRY_DELAY)
                    Log.d("WebSocket", "Retrying connection... Attempt #$retryCount")
                }
            }
        }

        if (retryCount == MAX_RETRIES) {
            Log.e(
                "WebSocket", "Max retry attempts reached. Could not establish WebSocket connection."
            )
        }
    }

    fun stopSocket() {
        runBlocking {
            client.close()
            socketEventChannel.close()
            Log.d("WebSocket", "WebSocket closed.")
        }
    }

    companion object {
        private const val MAX_RETRIES = 5
        private const val RETRY_DELAY = 5000L // Delay between retries in milliseconds
    }
}
