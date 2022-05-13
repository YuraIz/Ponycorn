package com.yuraiz.ponycorn.render

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.yuraiz.ponycorn.fractal.IFractal
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * GL Renderer that can render fractal
 */
open class FractalRenderer(var fractal: IFractal) : GLSurfaceView.Renderer {
    companion object {
        var scale = 2.5f
            set(value) {
                if(value < 10)
                    field = value
            }

        var center = floatArrayOf(-0.0f, 0.0f)
        fun reset() {
            scale = 2.5f
            center = floatArrayOf(-0.0f, 0.0f)
        }
    }

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) = fractal.onSurfaceCreated()

    override fun onDrawFrame(unused: GL10) =
        fractal.draw(vPMatrix, scale, center)

    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    init {
        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 4f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        if (height > width) {
            val ratio = width.toFloat() / height.toFloat()
            Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 4f, 5f)
        } else {
            val ratio = height.toFloat() / width.toFloat()
            Matrix.frustumM(projectionMatrix, 0, -1f, 1f, -ratio, ratio, 4f, 5f)
        }

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }
}