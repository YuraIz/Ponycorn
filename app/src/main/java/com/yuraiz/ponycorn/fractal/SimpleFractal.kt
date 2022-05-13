package com.yuraiz.ponycorn.fractal

import android.opengl.GLES20.*
import com.yuraiz.ponycorn.shader.Shader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Class that can show fractal from fragment shader
 *
 * Your shader need to have that uniform values
 * ```glsl
 * uniform float uScale;
 * uniform vec2 uCenter;
 * ```
 */
open class SimpleFractal(private val fragmentCode: String) : IFractal {

    protected companion object {
        // number of coordinates per vertex in this array
        const val COORDS_PER_VERTEX = 2
        val triangleCoords = floatArrayOf(
            -1.0f, 1.0f,   // top left
            -1.0f, -1.0f,  // bottom left
            1.0f, -1.0f,   // bottom right

            -1.0f, 1.0f,   // top left
            1.0f, -1.0f,   // bottom right
            1.0f, 1.0f,  // top right
        )

        val vertexCount: Int = triangleCoords.size / COORDS_PER_VERTEX
        const val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

        val vertexCode =
            """#version 300 es
                uniform mat4 uMVPMatrix;
                in vec2 vPosition;
                out vec2 coords;
                void main() {
                    gl_Position = vec4(vPosition.xy, 0.0, 1.0) * uMVPMatrix;
                    coords = vPosition.xy;
                }""".trimIndent()

        var vertexBuffer: FloatBuffer =
            // (number of coordinate values * 4 bytes per float)
            ByteBuffer.allocateDirect(triangleCoords.size * 4).run {
                // use the device hardware's native byte order
                order(ByteOrder.nativeOrder())

                // create a floating point buffer from the ByteBuffer
                asFloatBuffer().apply {
                    // add the coordinates to the FloatBuffer
                    put(triangleCoords)
                    // set the buffer to read the first coordinate
                    position(0)
                }
            }
    }

    protected lateinit var shader: Shader

    final override fun onSurfaceCreated() {
        shader = Shader(vertexCode, fragmentCode)
    }

   protected open fun setValues(mvpMatrix: FloatArray, scale: Float, center: FloatArray) {
        // Set model, view and projection matrix
        shader.uniformLocation("uMVPMatrix").also { mvpMatrixLocation ->
            glUniformMatrix4fv(mvpMatrixLocation, 1, false, mvpMatrix, 0)
        }

        // Set scale
        shader.uniformLocation("uScale").also { scaleLocation ->
            glUniform1f(scaleLocation, scale)
        }

        // Set center position
        shader.uniformLocation("uCenter").also { centerLocation ->
            glUniform2fv(centerLocation, 1, center, 0)
        }
    }

    final override fun draw(mvpMatrix: FloatArray, scale: Float, center: FloatArray) {
        // Add program to OpenGL ES environment
        shader.use()

        // get handle to vertex shader's vPosition member
        shader.attribLocation("vPosition").also {

            // Enable a handle to the triangle vertices
            glEnableVertexAttribArray(it)

            // Prepare the triangle coordinate data
            glVertexAttribPointer(
                it,
                COORDS_PER_VERTEX,
                GL_FLOAT,
                false,
                vertexStride,
                vertexBuffer
            )

            // Set values to uniform variables
            setValues(mvpMatrix, scale, center)

            // Draw the triangle
            glDrawArrays(GL_TRIANGLES, 0, vertexCount)

            // Disable vertex array
            glDisableVertexAttribArray(it)
        }
        glUseProgram(0)
    }
}