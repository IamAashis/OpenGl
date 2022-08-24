package com.opengl.opengl

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView.Renderer
import android.opengl.GLUtils
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


/**
 * Created by Aashis on 24/08/2022.
 */
class GLRenderer : Renderer {
    private val vertices = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        -1f, 1f,
        1f, 1f
    )
    private val textureVertices = floatArrayOf(
        0f, 1f,
        1f, 1f,
        0f, 0f,
        1f, 0f
    )
    private val vertexShaderCode = "attribute vec4 aPosition;" +
            "attribute vec2 aTexPosition;" +
            "varying vec2 vTexPosition;" +
            "void main() {" +
            "  gl_Position = aPosition;" +
            "  vTexPosition = aTexPosition;" +
            "}"
    private val fragmentShaderCode =
        """
        precision mediump float;uniform sampler2D uTexture;varying vec2 vTexPosition;void main() {
        vec4 color = texture2D(uTexture, vTexPosition);
        if(color.r == 0.0 && color.g == 0.0 && color.b == 0.0)
        color = vec4(1.0,0.5,0.5,1.0);  gl_FragColor = color;}
        """.trimIndent()
    private var verticesBuffer: FloatBuffer? = null
    private var textureBuffer: FloatBuffer? = null
    private var vertexShader = 0
    private var fragmentShader = 0
    private var program = 0
    private var bmp: Bitmap
    private val textures = IntArray(2)

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private val mMVPMatrix = FloatArray(16)
    private val mProjectionMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mRotationMatrix = FloatArray(16)
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        checkGlError("glClearColor")
        setup()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1F, 1F, 3F, 7F)
    }

    override fun onDrawFrame(gl: GL10?) {
        Log.d("Drawing_Frame", "Working")
        val scratch = FloatArray(16)

        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0F, 0F, -3F, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)

        // Draw Bitmap
        drawBinaryImage(bmp, textures[0])
        Matrix.setRotateM(mRotationMatrix, 0, 0F, 0F, 0F, 1.0f)
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0)
    }

    private fun setup() {
        GLES20.glGenTextures(2, textures, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0)
        //GLES20.glBindTexture(GL);
        initializeBuffers()
        initializeProgram()
    }

    private fun initializeBuffers() {
        var buff = ByteBuffer.allocateDirect(vertices.size * 4)
        buff.order(ByteOrder.nativeOrder())
        verticesBuffer = buff.asFloatBuffer()
        verticesBuffer?.put(vertices)
        verticesBuffer?.position(0)
        buff = ByteBuffer.allocateDirect(textureVertices.size * 4)
        buff.order(ByteOrder.nativeOrder())
        textureBuffer = buff.asFloatBuffer()
        textureBuffer?.put(textureVertices)
        textureBuffer?.position(0)
    }

    private fun initializeProgram() {
        vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        GLES20.glGetShaderInfoLog(vertexShader)
        checkGlError("glCreateShader")
        GLES20.glShaderSource(vertexShader, vertexShaderCode)
        GLES20.glCompileShader(vertexShader)
        fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(fragmentShader, fragmentShaderCode)
        GLES20.glCompileShader(fragmentShader)
        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        checkGlError("glLinkProgram")
    }

    fun updateTexture(bmp: Bitmap?) {
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0)
    }

    private fun drawBinaryImage(bmp: Bitmap, texture: Int) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        GLES20.glUseProgram(program)
        //Changes Here original Line GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glDisable(GLES20.GL_CULL_FACE)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE)
        val positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        val textureHandle = GLES20.glGetUniformLocation(program, "uTexture")
        val texturePositionHandle = GLES20.glGetAttribLocation(program, "aTexPosition")
        //Log.d("GL_SETUP",positionHandle+" , "+textureHandle);
        GLES20.glVertexAttribPointer(
            texturePositionHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            textureBuffer
        )
        GLES20.glEnableVertexAttribArray(texturePositionHandle)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)
        Log.d("FILTER_APPLY", "Applying")
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glUniform1i(textureHandle, 0)
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, verticesBuffer)
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        checkGlError("glDrawArrays")
    }

    fun setBitmap(bitmap: Bitmap) {
        updateTexture(bitmap)
        bmp = bitmap
    }

    companion object {
        private const val TAG = "MyGLRenderer"
        fun checkGlError(glOperation: String) {
            var error: Int
            while (GLES20.glGetError().also { error = it } != GLES20.GL_NO_ERROR) {
                Log.e(TAG, "$glOperation: glError $error")
                throw RuntimeException("$glOperation: glError $error")
            }
        }
    }

    init {
        bmp = Bitmap.createBitmap(513, 912, Bitmap.Config.ARGB_8888)
    }
}