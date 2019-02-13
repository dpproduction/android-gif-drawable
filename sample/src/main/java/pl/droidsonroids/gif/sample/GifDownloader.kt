package pl.droidsonroids.gif.sample

import kotlinx.coroutines.*
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.URL
import java.nio.ByteBuffer
import java.nio.channels.Channels

const val GIF_URL =
        "https://raw.githubusercontent.com/koral--/android-gif-drawable-sample/cb2d1f42b3045b2790a886d1574d3e74281de743/sample/src/main/assets/Animated-Flag-Hungary.gif"

class GifDownloader(httpFragment: GifDownloaderCallback) {

    interface GifDownloaderCallback {
        fun onGifDownloaded(buffer: ByteBuffer)
        fun onDownloadFailed(e: Exception)
    }

    private val fragmentReference = WeakReference(httpFragment)
    private var loadJob: Job? = null

    fun load(url: String) {
        loadJob = GlobalScope.launch {
            try {
                val buffer = downloadGif(url)
                runOnUiThread {
                    onGifDownloaded(buffer)
                }
            } catch (e: IOException) {
                runOnUiThread {
                    onDownloadFailed(e)
                }
            }
        }
    }

    private suspend fun runOnUiThread(action: GifDownloaderCallback.() -> Unit) {
        withContext(Dispatchers.Main) {
            fragmentReference.get()?.apply {
                action()
            }
        }
    }

    fun destroy() {
        loadJob?.cancel()
    }

    private fun downloadGif(url: String): ByteBuffer {
        val urlConnection = URL(url).openConnection()
        urlConnection.connect()
        val contentLength = urlConnection.contentLength
        if (contentLength < 0) {
            throw IOException("Content-Length header not present")
        }
        urlConnection.getInputStream().use {
            val buffer = ByteBuffer.allocateDirect(contentLength)
            Channels.newChannel(it).use { channel ->
                while (buffer.remaining() > 0) {
                    channel.read(buffer)
                }
                return buffer
            }
        }
    }
}
