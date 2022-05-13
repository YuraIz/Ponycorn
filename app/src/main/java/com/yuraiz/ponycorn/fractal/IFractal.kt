package com.yuraiz.ponycorn.fractal

interface IFractal {
    /**
     * Run to init values that request OpenGl context
     */
    fun onSurfaceCreated()

    /**
     * Draw image
     */
    fun draw(mvpMatrix: FloatArray, scale: Float, center: FloatArray)
}