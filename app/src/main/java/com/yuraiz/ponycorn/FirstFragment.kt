package com.yuraiz.ponycorn

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.INVALID_POINTER_ID
import android.view.ScaleGestureDetector
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Shader(vertexCode: String, fragmentCode: String) {
    private val program = linkProgram(compileVertex(vertexCode), compileFragment(fragmentCode))

    fun use() = glUseProgram(program)

    fun attribLocation(attrib: String) = glGetAttribLocation(program, attrib)

    fun uniformLocation(attrib: String) = glGetUniformLocation(program, attrib)

    private fun checkCompileStatus(shader: Int, name: String) {
        val success = IntBuffer.allocate(1)
        glGetShaderiv(shader, GL_COMPILE_STATUS, success)
        if (success[0] == 0) {
            print("$name shader compile error")
            val infoLog = glGetShaderInfoLog(shader)
            println("Log: $infoLog")
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
            print("Link program error")
            val infoLog = glGetProgramInfoLog(program)
            println("Log: $infoLog")
        }

        for (shader in shaders) {
            glDeleteShader(shader)
        }
        return program
    }
}

interface IFractal {
    fun onSurfaceCreated()
    fun draw(mvpMatrix: FloatArray, scale: Float, center: FloatArray)
}

open class MyFractal(private val fragmentCode: String) : IFractal {

    private var vertexBuffer: FloatBuffer =
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

    protected lateinit var shader: Shader

    override fun onSurfaceCreated() {
        shader = Shader(vertexCode, fragmentCode)
    }

    override fun draw(mvpMatrix: FloatArray, scale: Float, center: FloatArray) {
        // Add program to OpenGL ES environment
        shader.use()

        // get handle to vertex shader's vPosition member
        shader.attribLocation("vPosition").also {

            // Enable a handle to the triangle vertices
            glEnableVertexAttribArray(it)

            // Prepare the triangle coordinate data
            glVertexAttribPointer(
                it,
                Companion.COORDS_PER_VERTEX,
                GL_FLOAT,
                false,
                vertexStride,
                vertexBuffer
            )

            shader.uniformLocation("uMVPMatrix").also { mvpMatrixLocation ->
                glUniformMatrix4fv(mvpMatrixLocation, 1, false, mvpMatrix, 0)
            }

            shader.uniformLocation("uScale").also { scaleLocation ->
                glUniform1f(scaleLocation, scale)
            }

            shader.uniformLocation("uCenter").also { centerLocation ->
                glUniform2fv(centerLocation, 1, center, 0)
            }

            // Draw the triangle
            glDrawArrays(GL_TRIANGLES, 0, vertexCount)

            // Disable vertex array
            glDisableVertexAttribArray(it)
        }
        glUseProgram(0)
    }

    companion object {
        // number of coordinates per vertex in this array
        private val COORDS_PER_VERTEX = 2
        private val triangleCoords = floatArrayOf(
            -1.0f, 1.0f,   // top left
            -1.0f, -1.0f,  // bottom left
            1.0f, -1.0f,   // bottom right

            -1.0f, 1.0f,   // top left
            1.0f, -1.0f,   // bottom right
            1.0f, 1.0f,  // top right
        )

        private val vertexCount: Int = triangleCoords.size / Companion.COORDS_PER_VERTEX
        private val vertexStride: Int = Companion.COORDS_PER_VERTEX * 4 // 4 bytes per vertex

        internal val vertexCode =
            """#version 300 es
                uniform mat4 uMVPMatrix;
                in vec2 vPosition;
                out vec2 coords;
                void main() {
                    gl_Position = vec4(vPosition.xy, 0.0, 1.0) * uMVPMatrix;
                    coords = vPosition.xy;
                }""".trimIndent()
    }

}

class MultiShaderFractal(vararg fragmentCodes: String) :
    MyFractal(fragmentCode = fragmentCodes.first()), IFractal {
    val codes = listOf(*fragmentCodes)
    val shaders = arrayListOf(shader)

    override fun onSurfaceCreated() {
        super.onSurfaceCreated()
        for (code in codes) {
            shaders.add(Shader(vertexCode, code))
        }
        shader = shaders.first()
    }
}

open class MyGLRendererD(fractal: IFractal) : GLSurfaceView.Renderer {

    @Volatile
    var fractal = fractal

    var scale = 2.5f
    var center = floatArrayOf(-0.0f, 0.0f)

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        glClearColor(0.5f, 0.0f, 0.0f, 1.0f)
        fractal.onSurfaceCreated()
    }

    override fun onDrawFrame(unused: GL10) {
        // Redraw background color
        glClear(GL_COLOR_BUFFER_BIT)
        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // Draw shape
        fractal.draw(vPMatrix, scale, center)
    }

    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }
}


class MultiFractalRenerer(vararg fractals: IFractal) : MyGLRendererD(fractals.first()) {
    private val fractals = arrayListOf(*fractals)
    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        glClearColor(0.5f, 0.0f, 0.0f, 1.0f)
        for (fractal in fractals) {
            fractal.onSurfaceCreated()
        }
    }

    private var current = 0;

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

class FractalView(context: Context, attributeSet: AttributeSet) :
    GLSurfaceView(context, attributeSet) {

    private val renderer: MultiFractalRenerer

    class FractalScaleListener(private val renderer: MyGLRendererD) :
        ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            if (detector != null) {
                renderer.scale /= detector.scaleFactor
            }
            return true
        }
    }

    private var fractalScaleDetector: ScaleGestureDetector

    fun prev() {
        renderer.prevFractal()
        renderer.scale = 3.0f
        renderer.center = floatArrayOf(0.0f, 0.0f)
    }

    fun next() {
        renderer.nextFractal()
        renderer.scale = 3.0f
        renderer.center = floatArrayOf(0.0f, 0.0f)
    }

    private var mActivePointerId = INVALID_POINTER_ID
    private var mLastTouchX = 0f
    private var mLastTouchY = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (event == null) return true
        fractalScaleDetector.onTouchEvent(event)

        if (event.pointerCount == 2) {
            mActivePointerId = INVALID_POINTER_ID
        }

        if (event.pointerCount == 1) {
            val x = event.x / 1080
            val y = event.y / 1920

            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    mActivePointerId = event.getPointerId(0)
                    mLastTouchX = x
                    mLastTouchY = y
                }

                MotionEvent.ACTION_MOVE -> {
                    if (mActivePointerId == event.getPointerId(0)) {
                        val dx = x - mLastTouchX
                        val dy = y - mLastTouchY

                        val scale = renderer.scale * 1.5f

                        renderer.center[0] -= dx * scale
                        renderer.center[1] += dy * scale

                        invalidate()

                        mLastTouchX = x
                        mLastTouchY = y
                    }
                }
                MotionEvent.ACTION_UP -> {
                    mLastTouchX = x
                    mLastTouchY = y
                }
                else -> {}
            }
        }
        return true
    }

    init {
        fun getCode(resourceId: Int) =
            context.resources.openRawResource(resourceId).bufferedReader().use { it.readText() }

        setEGLContextClientVersion(3)

        renderer = MultiFractalRenerer(
            MyFractal(getCode(R.raw.mandelbrot)),
            MyFractal(getCode(R.raw.julia1)),
            MyFractal(getCode(R.raw.julia2)),
            MyFractal(getCode(R.raw.julia_sin)),
            MyFractal(getCode(R.raw.koch))
        )

        fractalScaleDetector = ScaleGestureDetector(context, FractalScaleListener(renderer))
        setRenderer(renderer)
    }
}
