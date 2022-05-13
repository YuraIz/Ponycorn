package com.yuraiz.ponycorn.fractal

import android.opengl.GLES20

class ConfigurableFractal(fragmentCode: String): MyFractal(fragmentCode) {
    companion object {
        var value = 0.5f
            set(value) {
                if(value in 0f..1f)
                    field = value
            }
    }

    override fun draw(mvpMatrix: FloatArray, scale: Float, center: FloatArray) {
        // Add program to OpenGL ES environment
        shader.use()

        // get handle to vertex shader's vPosition member
        shader.attribLocation("vPosition").also {

            // Enable a handle to the triangle vertices
            GLES20.glEnableVertexAttribArray(it)

            // Prepare the triangle coordinate data
            GLES20.glVertexAttribPointer(
                it,
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                vertexBuffer
            )

            // Set model, view and projection matrix
            shader.uniformLocation("uMVPMatrix").also { mvpMatrixLocation ->
                GLES20.glUniformMatrix4fv(mvpMatrixLocation, 1, false, mvpMatrix, 0)
            }

            // Set scale
            shader.uniformLocation("uScale").also { scaleLocation ->
                GLES20.glUniform1f(scaleLocation, scale)
            }

            // Set center position
            shader.uniformLocation("uCenter").also { centerLocation ->
                GLES20.glUniform2fv(centerLocation, 1, center, 0)
            }

            // Set center position
            shader.uniformLocation("uVar").also { varLocation ->
                GLES20.glUniform1f(varLocation, value)
            }

            // Draw the triangle
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

            // Disable vertex array
            GLES20.glDisableVertexAttribArray(it)
        }
        GLES20.glUseProgram(0)
    }
}