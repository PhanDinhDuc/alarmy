package com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable

class CustomDrawable(
    context: Context,
    startColor: Int,
    endColor: Int,
    borderWidth: Int,
    borderRadius: Int
) : Drawable() {
    private val mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mPath: Path = Path()
    private val mRect: RectF = RectF()
    private var startColor: Int = 0
    private var endColor: Int = 0
    private var mBorderWidth: Int = 0
    private var mBorderRadius: Int = 0

    init {
        mPaint.style = Paint.Style.FILL
        mPath.fillType = Path.FillType.EVEN_ODD
        this.startColor = startColor
        this.endColor = endColor
        mBorderWidth = borderWidth
        val density = context.resources.displayMetrics.density
        mBorderRadius = (borderRadius * density).toInt()
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        mPath.reset()

        // outer rect
        mRect.set(
            bounds.left + mBorderWidth.toFloat(),
            bounds.top + mBorderWidth.toFloat(),
            bounds.right - mBorderWidth.toFloat(),
            bounds.bottom - mBorderWidth.toFloat()
        )
        mPath.addRoundRect(
            mRect,
            mBorderRadius.toFloat(),
            mBorderRadius.toFloat(),
            Path.Direction.CW
        )

        // inner rect
        mRect.set(
            bounds.left + 2f,
            bounds.top + 2f,
            bounds.right - 2f,
            bounds.bottom - 2f
        )
        mPath.addRoundRect(
            mRect,
            mBorderRadius.toFloat(),
            mBorderRadius.toFloat(),
            Path.Direction.CW
        )
    }

    override fun draw(canvas: Canvas) {
        // stroke
        mPaint.shader = LinearGradient(
            0f,
            0f,
            0f,
            100f,
            startColor,
            endColor,
            Shader.TileMode.MIRROR
        )
        canvas.drawPath(mPath, mPaint)
    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }
}
