package com.yuraiz.ponycorn.fractal

import android.opengl.GLES20
import kotlin.math.sin

class TimedFractal(fragmentCode: String): SimpleFractal(fragmentCode) {
    private var last = 0f

    override fun setValues(mvpMatrix: FloatArray, scale: Float, center: FloatArray) {
        super.setValues(mvpMatrix, scale, center)
        last += 0.01f
        // Set var position
        shader.uniformLocation("uVar").also { varLocation ->
            GLES20.glUniform1f(varLocation, sin(last))
        }
    }
}