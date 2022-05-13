package com.yuraiz.ponycorn.render

import android.view.View
import com.yuraiz.ponycorn.fractal.VariableFractal
import com.yuraiz.ponycorn.fractal.IFractal
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * [FractalRenderer] that support switching between multiple fractals
 *
 * for switching use [prevFractal] and [nextFractal] methods
 */
class MultiFractalRenderer(vararg fractals: IFractal) : FractalRenderer(fractals.first()) {
    private val fractals = arrayListOf(*fractals)

    init {
        current %= fractals.count()
        fractal = fractals[current]
    }

    companion object {
        private var current = 0

        lateinit var slider_hide: () -> Unit
        lateinit var slider_show: () -> Unit

        lateinit var slider_visibiliry: (Int) -> Unit
    }

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        for (fractal in fractals) {
            fractal.onSurfaceCreated()
        }
    }

    fun change_slider() {
        if(fractal is VariableFractal)
            slider_visibiliry(View.VISIBLE)
        else
            slider_visibiliry(View.INVISIBLE)
    }

    fun prevFractal() {
        if (--current == -1)
            current = fractals.count() - 1
        fractal = fractals[current]
        change_slider()
        reset()
    }

    fun nextFractal() {
        current++
        current %= fractals.count()
        fractal = fractals[current]
        change_slider()
        reset()
    }
}