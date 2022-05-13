package com.yuraiz.ponycorn.Render

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.INVALID_POINTER_ID
import android.view.ScaleGestureDetector
import com.yuraiz.ponycorn.Fractal.MyFractal
import com.yuraiz.ponycorn.R
import com.yuraiz.ponycorn.Render.MultiFractalRenerer
import com.yuraiz.ponycorn.Render.MyGLRendererD


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
