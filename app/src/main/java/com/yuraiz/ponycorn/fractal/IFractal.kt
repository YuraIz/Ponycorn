package com.yuraiz.ponycorn.Fractal

interface IFractal {
    fun onSurfaceCreated()
    fun draw(mvpMatrix: FloatArray, scale: Float, center: FloatArray)
}