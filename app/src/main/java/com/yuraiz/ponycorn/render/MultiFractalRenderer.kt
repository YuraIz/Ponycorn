package com.yuraiz.ponycorn.render

import android.opengl.GLES20
import com.yuraiz.ponycorn.fractal.IFractal
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MultiFractalRenerer(vararg fractals: IFractal) : MyGLRenderer(fractals.first()) {
    private val fractals = arrayListOf(*fractals)
    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        GLES20.glClearColor(0.5f, 0.0f, 0.0f, 1.0f)
        for (fractal in fractals) {
            fractal.onSurfaceCreated()
        }
    }

    private var current = 0

    fun prevFractal() {
        if (--current == -1)
            current = fractals.count() - 1
        fractal = fractals[current]
    }

    fun nextFractal() {
        if (++current == fractals.count())
            current = 0
        fractal = fractals[current]
    }
}