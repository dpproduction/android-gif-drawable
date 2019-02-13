package pl.droidsonroids.gif.sample

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import pl.droidsonroids.gif.GifOptions
import pl.droidsonroids.gif.GifTexImage2D
import pl.droidsonroids.gif.InputSource
import pl.droidsonroids.gif.sample.opengl.GifTexImage2DProgram2
import pl.droidsonroids.gif.sample.opengl.GifTexImage2DRenderer
import pl.droidsonroids.gif.sample.opengl.isOpenGLES2Supported
import java.nio.ByteBuffer

class GifTexImage2DFragment : BaseFragment(), GifDownloader.GifDownloaderCallback {

    val gifDownloader = GifDownloader(this)

    override fun onGifDownloaded(buffer: ByteBuffer) {
        val options = GifOptions()
        options.setInIsOpaque(true)
        val gifTexImage2D = GifTexImage2D(InputSource.DirectByteBufferSource(buffer), options)
        gifTexImage2D.startDecoderThread()
        gifTexImage2DProgram.gifTexImage2D = gifTexImage2D
    }

    override fun onDownloadFailed(e: Exception) {
        e.printStackTrace()
    }

    private lateinit var gifTexImage2DProgram: GifTexImage2DProgram2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gifTexImage2DProgram = GifTexImage2DProgram2()
        gifDownloader.load("https://media2.giphy.com/media/PKpXx1MiUYRIzWryzX/200w.gif")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (!context.isOpenGLES2Supported) {
            Snackbar.make(container!!, R.string.gles2_not_supported, Snackbar.LENGTH_LONG).show()
            return null
        }

        val view = inflater.inflate(R.layout.opengl, container, false) as GLSurfaceView
        view.setEGLContextClientVersion(2)
        view.setRenderer(GifTexImage2DRenderer(gifTexImage2DProgram))
        view.holder.setFixedSize(gifTexImage2DProgram.getWidth(), gifTexImage2DProgram.getHeight())
        return view
    }

    override fun onDetach() {
        super.onDetach()
        gifTexImage2DProgram.destroy()
    }
}