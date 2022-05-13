package com.yuraiz.ponycorn.fractal

import android.opengl.GLES20

class VariableFractal(fragmentCode: String): SimpleFractal(fragmentCode) {
    companion object {
        var value = 0.5f
            set(value) {
                if(value in 0f..1f)
                    field = value
            }
    }

    override fun setValues(mvpMatrix: FloatArray, scale: Float, center: FloatArray) {
        super.setValues(mvpMatrix, scale, center)
        // Set var position
        shader.uniformLocation("uVar").also { varLocation ->
            GLES20.glUniform1f(varLocation, value)
        }
    }
}

