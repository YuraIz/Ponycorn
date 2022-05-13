package com.yuraiz.ponycorn.render

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.yuraiz.ponycorn.R
import com.yuraiz.ponycorn.fractal.VariableFractal
import com.yuraiz.ponycorn.fractal.SimpleFractal
import com.yuraiz.ponycorn.fractal.TimedFractal

/**
 * View to render fractals
 *
 * bind [prev] and [next] methods for switching fractals
 */
class FractalView(context: Context, attributeSet: AttributeSet) :
    GLSurfaceView(context, attributeSet) {

    private val renderer: MultiFractalRenderer

    class FractalScaleListener(private val renderer: FractalRenderer) :
        ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            if (detector != null) FractalRenderer.scale /= detector.scaleFactor
            return true
        }
    }

    private var fractalScaleDetector: ScaleGestureDetector

    fun prev() = renderer.prevFractal()

    fun next() = renderer.nextFractal()

    private var mActivePointerId = MotionEvent.INVALID_POINTER_ID
    private var mLastTouchX = 0f
    private var mLastTouchY = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return true

        fractalScaleDetector.onTouchEvent(event)

        if (event.pointerCount == 2) {
            mActivePointerId = MotionEvent.INVALID_POINTER_ID
            return true
        }

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

                    val scale = FractalRenderer.scale * 1.5f

                    FractalRenderer.center[0] -= dx * scale
                    FractalRenderer.center[1] += dy * scale

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
        return true
    }

    init {
        fun readRes(resourceId: Int) = context
                .resources
                .openRawResource(resourceId)
                .bufferedReader()
                .use { it.readText() }


        fun fractal(resourceId: Int) = SimpleFractal(
            readRes(resourceId)
        )

        fun varFractal(resourceId: Int) = VariableFractal(
            readRes(resourceId)
        )

        fun timedFractal(resourceId: Int) = TimedFractal(
            readRes(resourceId)
        )

        setEGLContextClientVersion(3)

        renderer = MultiFractalRenderer(
            fractal(R.raw.mandelbrot),
            fractal(R.raw.julia1),
            fractal(R.raw.julia2),
            fractal(R.raw.julia_sin),
            fractal(R.raw.koch),
            varFractal(R.raw.julia_var1),
            varFractal(R.raw.julia_var2),
            varFractal(R.raw.julia_var3),
            timedFractal(R.raw.julia_var2),
            timedFractal(R.raw.julia_timed)
        )

        fractalScaleDetector = ScaleGestureDetector(context, FractalScaleListener(renderer))
        setRenderer(renderer)
    }
}
