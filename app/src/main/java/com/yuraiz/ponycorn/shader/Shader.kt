package com.yuraiz.ponycorn.shader

import android.opengl.GLES20.*
import android.util.Log
import java.nio.IntBuffer

/**
 * Wrapper over OpenGL shader
 *
 * **WARNING**: Construct only in OpenGL context
 */
class Shader(vertexCode: String, fragmentCode: String) {
    private val program = linkProgram(compileVertex(vertexCode), compileFragment(fragmentCode))

    fun use() = glUseProgram(program)

    fun attribLocation(attrib: String) = glGetAttribLocation(program, attrib)

    fun uniformLocation(attrib: String) = glGetUniformLocation(program, attrib)

    private fun checkCompileStatus(shader: Int, name: String) {
        val success = IntBuffer.allocate(1)
        glGetShaderiv(shader, GL_COMPILE_STATUS, success)
        if (success[0] == 0) {
            Log.e(
                "SHADER COMPILE ERROR",
                glGetShaderInfoLog(shader)
            )
        }
    }

    private fun compileVertex(vertexCode: String): Int {
        val vertex = glCreateShader(GL_VERTEX_SHADER)

        glShaderSource(vertex, vertexCode)
        glCompileShader(vertex)

        checkCompileStatus(vertex, "Vertex")

        return vertex
    }

    private fun compileFragment(fragmentCode: String): Int {
        val fragment = glCreateShader(GL_FRAGMENT_SHADER)

        glShaderSource(fragment, fragmentCode)
        glCompileShader(fragment)

        checkCompileStatus(fragment, "Fragment")

        return fragment
    }

    private fun linkProgram(vararg shaders: Int): Int {
        val program = glCreateProgram()
        for (shader in shaders) {
            glAttachShader(program, shader)
        }
        glLinkProgram(program)

        val success = IntBuffer.allocate(1)
        glGetProgramiv(program, GL_LINK_STATUS, success)
        if (success[0] == 0) {
            Log.e(
                "PROGRAM LINK ERROR",
                glGetProgramInfoLog(program)
            )
        }

        for (shader in shaders) {
            glDeleteShader(shader)
        }
        return program
    }
}