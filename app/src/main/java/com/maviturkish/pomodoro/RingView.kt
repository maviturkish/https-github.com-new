package com.maviturkish.pomodoro

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/** Circular progress ring. [progress] goes from 1 (full) down to 0 (done). */
class RingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val density = resources.displayMetrics.density
    private val stroke = 14f * density

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke
        color = 0x22FFFFFF
    }
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke
        strokeCap = Paint.Cap.ROUND
        color = Mode.FOCUS.color
    }
    private val oval = RectF()

    var progress: Float = 1f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    var ringColor: Int
        get() = arcPaint.color
        set(value) {
            arcPaint.color = value
            invalidate()
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Keep it square
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        val size = if (h == 0) w else minOf(w, h)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val half = stroke / 2f
        oval.set(half, half, width - half, height - half)
        canvas.drawArc(oval, 0f, 360f, false, trackPaint)
        if (progress > 0f) {
            canvas.drawArc(oval, -90f, 360f * progress, false, arcPaint)
        }
    }
}
