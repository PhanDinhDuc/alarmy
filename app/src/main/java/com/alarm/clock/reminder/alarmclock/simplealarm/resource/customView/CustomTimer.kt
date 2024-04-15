package com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView

import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.Settings
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.CustomTimerBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.getAppCompactDrawable


class CustomTimer : FrameLayout {

    constructor(context: Context) : super(context) {
        initView(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(attrs, 0)
    }

    constructor(
        context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        initView(attrs, defStyleAttr)
    }

    lateinit var binding: CustomTimerBinding
    private var countDown = Settings.alarmSettings.taskTimeLimit.duration.toInt()

    private fun initView(attrs: AttributeSet?, @AttrRes defStyleAttr: Int) {
        binding = CustomTimerBinding.inflate(LayoutInflater.from(context), this, true)

        val customHeader =
            context.obtainStyledAttributes(attrs, R.styleable.CustomTimer, defStyleAttr, 0)

        try {
            val textSize = customHeader.getInteger(R.styleable.CustomTimer_text_size, 14)
            binding.textCountdown.textSize = textSize.toFloat()

            val isBackground =
                customHeader.getBoolean(R.styleable.CustomTimer_view_background, false)
            if (isBackground) {
                binding.textCountdown.background =
                    context.getAppCompactDrawable(R.drawable.bg_circle)
            }

            val countDownTimer =
                customHeader.getInteger(R.styleable.CustomTimer_count_down, countDown)
            countDown = countDownTimer
            binding.textCountdown.text = countDown.toString()
            binding.progressBar.progress = 100

        } finally {
            customHeader.recycle()
        }
    }

    private var countDownTimer: CountDownTimer? = null

    fun start(from: Int = countDown, onTick: (Int, Int) -> Unit, onFinish: () -> Unit) {
        stop()
        countDownTimer = object : CountDownTimer(from * 1000L, 100) {
            override fun onTick(leftTimeInMilliseconds: Long) {
                val seconds = leftTimeInMilliseconds.toInt() / 1000 + 1
                val progress = ((leftTimeInMilliseconds / countDown) * 0.3).toInt()
                onTick.invoke(seconds, progress)
                binding.textCountdown.text = seconds.toString()
                binding.progressBar.progress = progress
            }

            override fun onFinish() {
                onFinish.invoke()
            }
        }.start()
    }

    fun stop() {
        countDownTimer?.cancel()
        countDownTimer = null
    }
}
