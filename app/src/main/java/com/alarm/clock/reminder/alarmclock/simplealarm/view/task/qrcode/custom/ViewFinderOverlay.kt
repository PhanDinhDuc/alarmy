package com.alarm.clock.reminder.alarmclock.simplealarm.view.task.qrcode.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ViewFinderOverlay(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val boxPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.color_background)
        style = Paint.Style.STROKE
        strokeWidth =
            context.resources.getDimensionPixelOffset(R.dimen.effect_border_width).toFloat()
    }
    private val linePaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.primary_1)
        style = Paint.Style.STROKE
        strokeWidth = context.resources.getDimensionPixelOffset(R.dimen.bar_width2).toFloat()
    }
    private val paint: Paint = Paint(Paint.DITHER_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.primary_1)
        style = Paint.Style.STROKE
        strokeWidth = context.resources.getDimensionPixelOffset(R.dimen.fab_medium_stroke).toFloat()
        strokeJoin = Paint.Join.MITER
    }
    private var mHeight = 0
    private var runAnimation = true
    private var showLine = true
    private var mPosY = 0
    private var isGoingDown = true
    private val scrimPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.color_camera)
    }

    private val eraserPaint: Paint = Paint().apply {
        strokeWidth = boxPaint.strokeWidth
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val boxCornerRadius: Float =
        context.resources.getDimensionPixelOffset(R.dimen.effect_border_width).toFloat()

    private var boxRect: RectF? = null

    fun setViewFinder() {
        val overlayWidth = width.toFloat()
        val overlayHeight = height.toFloat()
        val boxWidth = overlayWidth * 80 / 100
        val boxHeight = overlayHeight * 36 / 100
        val cx = overlayWidth / 2
        val cy = overlayHeight / 2
        boxRect =
            RectF(cx - boxWidth / 2, cy - boxHeight / 2, cx + boxWidth / 2, cy + boxHeight / 2)
        startAnimation()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        mHeight = height

        boxRect?.let {
            // Draws the dark background scrim and leaves the box area clear.
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), scrimPaint)
            // As the stroke is always centered, so erase twice with FILL and STROKE respectively to clear
            // all area that the box rect would occupy.
            eraserPaint.style = Paint.Style.FILL
            canvas.drawRoundRect(it, boxCornerRadius, boxCornerRadius, eraserPaint)
            eraserPaint.style = Paint.Style.STROKE
            canvas.drawRoundRect(it, boxCornerRadius, boxCornerRadius, eraserPaint)
            // Draws the box.
            canvas.drawRoundRect(it, boxCornerRadius, boxCornerRadius, boxPaint)
        }
        if (showLine) {
            canvas.drawLine(
                boxRect?.left ?: 0F,
                mPosY.toFloat(),
                boxRect?.right ?: 0F,
                mPosY.toFloat(),
                linePaint
            )
        }
        canvas.drawPath(
            createCornersPath(
                boxRect?.left ?: 0F,
                boxRect?.top ?: 0F,
                boxRect?.right ?: 0F,
                boxRect?.bottom ?: 0F,
                35F
            ), paint
        )
    }

    private fun createCornersPath(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        cornerWidth: Float
    ): Path {
        val path = Path()
        path.apply {
            moveTo(left, top + cornerWidth)
            moveTo(left, top + cornerWidth)
            lineTo(left, top)
            lineTo(left + cornerWidth, top)
            moveTo(right - cornerWidth, top)
            lineTo(right, top)
            lineTo(right, top + cornerWidth)
            moveTo(left, bottom - cornerWidth)
            lineTo(left, bottom)
            lineTo(left + cornerWidth, bottom)
            moveTo(right - cornerWidth, bottom)
            lineTo(right, bottom)
            lineTo(right, bottom - cornerWidth)
        }
        return path
    }

    fun startAnimation() {
        runAnimation = true
        showLine = true
        animateLine()
    }

    fun stopAnimation() {
        runAnimation = false
        showLine = false
    }

    private fun animateLine() {
        GlobalScope.launch(Dispatchers.Main) {
            val minY = boxRect?.top?.toInt() ?: 0
            val maxY = boxRect?.bottom?.toInt() ?: mHeight
            mPosY = minY
            while (runAnimation) {
                if (isGoingDown) {
                    mPosY += 5
                    if (mPosY > maxY) {
                        mPosY = maxY
                        isGoingDown = false
                    }
                } else {
                    mPosY -= 5
                    if (mPosY < minY) {
                        mPosY = minY
                        isGoingDown = true
                    }
                }
                invalidate()
                delay(DELAY)
            }
        }
    }


    companion object {
        private const val DELAY = 20L
    }
}

