package com.yuraiz.ponycorn.Shader

import android.opengl.GLES20
import java.nio.IntBuffer

class Shader(vertexCode: String, fragmentCode: String) {
    private val program = linkProgram(compileVertex(vertexCode), compileFragment(fragmentCode))

    fun use() = GLES20.glUseProgram(program)

    fun attribLocation(attrib: String) = GLES20.glGetAttribLocation(program, attrib)

    fun uniformLocation(attrib: String) = GLES20.glGetUniformLocation(program, attrib)

    private fun checkCompileStatus(shader: Int, name: String) {
        val success = IntBuffer.allocate(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, success)
        if (success[0] == 0) {
            print("$name shader compile error")
            val infoLog = GLES20.glGetShaderInfoLog(shader)
            println("Log: $infoLog")
        }
    }

    private fun compileVertex(vertexCode: String): Int {
        val vertex = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)

        GLES20.glShaderSource(vertex, vertexCode)
        GLES20.glCompileShader(vertex)

        checkCompileStatus(vertex, "Vertex")

        return vertex
    }

    private fun compileFragment(fragmentCode: String): Int {

        val fragment = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)

        GLES20.glShaderSource(fragment, fragmentCode)
        GLES20.glCompileShader(fragment)

        checkCompileStatus(fragment, "Fragment")

        return fragment
    }

    private fun linkProgram(vararg shaders: Int): Int {
        val program = GLES20.glCreateProgram()
        for (shader in shaders) {
            GLES20.glAttachShader(program, shader)
        }
        GLES20.glLinkProgram(program)

        val success = IntBuffer.allocate(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, success)
        if (success[0] == 0) {
            print("Link program error")
            val infoLog = GLES20.glGetProgramInfoLog(program)
            println("Log: $infoLog")
        }

        for (shader in shaders) {
            GLES20.glDeleteShader(shader)
        }
        return program
    }
}