package pl.droidsonroids.gif.sample.opengl

import android.opengl.GLES20.*
import android.util.Log
import android.widget.ImageView
import android.widget.ImageView.ScaleType.FIT_CENTER
import android.widget.ImageView.ScaleType.FIT_XY
import org.intellij.lang.annotations.Language
import pl.droidsonroids.gif.GifTexImage2D

class GifTexImage2DProgram2 {

    var gifTexImage2D: GifTexImage2D?=null

    fun getWidth() : Int{
        return gifTexImage2D?.width?:200
    }

    fun getHeight() : Int{
        return gifTexImage2D?.height?:200
    }


    private var program = 0
    private var positionLocation = -1
    private var textureLocation = -1
    private var coordinateLocation = -1

    @Language("GLSL")
    private val vertexShaderCode = """
        attribute vec4 position;
        attribute vec4 coordinate;
        varying vec2 textureCoordinate;
        void main() {
            gl_Position = position;
            textureCoordinate = vec2(coordinate.s, 1.0 - coordinate.t);
        }
        """

    @Language("GLSL")
    private val fragmentShaderCode = """
        varying mediump vec2 textureCoordinate;
        uniform sampler2D texture;
        void main() {
             gl_FragColor = texture2D(texture, textureCoordinate);
        }
        """

    private val textureBuffer = floatArrayOf(0f, 0f, 1f, 0f, 0f, 1f, 1f, 1f).toFloatBuffer()
    private val verticesBuffer = floatArrayOf(-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f).toFloatBuffer()

    fun initialize() {
        Log.i("GifTexImage2DProgram2","initialize")
        val texNames = intArrayOf(0)
        glGenTextures(1, texNames, 0)
        glBindTexture(GL_TEXTURE_2D, texNames[0])
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

        val vertexShader = loadShader(GL_VERTEX_SHADER, vertexShaderCode)
        val pixelShader = loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode)
        program = glCreateProgram()
        glAttachShader(program, vertexShader)
        glAttachShader(program, pixelShader)
        glLinkProgram(program)
        glDeleteShader(pixelShader)
        glDeleteShader(vertexShader)
        positionLocation = glGetAttribLocation(program, "position")
        textureLocation = glGetUniformLocation(program, "texture")
        coordinateLocation = glGetAttribLocation(program, "coordinate")

        glActiveTexture(GL_TEXTURE0)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, getWidth(), getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, null)
        glUseProgram(program)
        glUniform1i(textureLocation, 0)
        glEnableVertexAttribArray(coordinateLocation)
        glVertexAttribPointer(coordinateLocation, 2, GL_FLOAT, false, 0, textureBuffer)
        glEnableVertexAttribArray(positionLocation)
        glVertexAttribPointer(positionLocation, 2, GL_FLOAT, false, 0, verticesBuffer)
    }

    private var viewport = Viewport(0, 0, 0, 0)

    fun setDimensions(screenWidth: Int, screenHeight: Int, scaleType: ImageView.ScaleType = FIT_CENTER) {
        viewport = when (scaleType) {
            FIT_XY -> Viewport(0, 0, screenWidth, screenHeight)
            FIT_CENTER -> createFitCenterViewport(screenWidth, screenHeight)
            else -> TODO("$scaleType is not yet supported")
        }
    }

    private fun createFitCenterViewport(screenWidth: Int, screenHeight: Int): Viewport {
        val screenAspectRatio = screenWidth / screenHeight.toFloat()
        val mediaAspectRation = getWidth() / getHeight().toFloat()

        val scaleFactor = if (screenAspectRatio < mediaAspectRation) {
            screenWidth / getWidth().toFloat()
        } else {
            screenHeight / getHeight().toFloat()
        }

        val viewportWidth = (getWidth() * scaleFactor).toInt()
        val viewportHeight = (getHeight() * scaleFactor).toInt()

        return Viewport(
                (screenWidth - viewportWidth) / 2,
                (screenHeight - viewportHeight) / 2,
                viewportWidth, viewportHeight
        )
    }

    fun draw() {
        viewport.applyToGlWindow()
        gifTexImage2D?.let {
            it.glTexSubImage2D(GL_TEXTURE_2D, 0)
            glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
        }

    }

    fun destroy() = gifTexImage2D?.recycle()
}
