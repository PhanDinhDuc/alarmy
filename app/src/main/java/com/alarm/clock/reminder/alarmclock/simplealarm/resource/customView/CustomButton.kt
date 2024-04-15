package com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView


import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatButton
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Converter
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.getAppCompactColor
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.getAppCompactDrawable


@SuppressLint("CustomViewStyleable")
class CustomButton(
    context: Context,
    attrs: AttributeSet
) : AppCompatButton(context, attrs) {

    var type: ButtonType = ButtonType.NORMAL
        set(value) {
            setBtnType(value)
            field = value
        }

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.my_button_style)
        type = ButtonType.getValue(array.getInt(R.styleable.my_button_style_btn_type, 1))
            ?: ButtonType.NORMAL

        setPadding(
            Converter.asPixels(4),
            Converter.asPixels(4),
            Converter.asPixels(4),
            Converter.asPixels(4)
        )
        includeFontPadding = false
        isAllCaps = array.getBoolean(R.styleable.my_button_style_isAllCaps, false)
        stateListAnimator = null
        gravity = Gravity.CENTER
        setLines(1)
        ellipsize = TextUtils.TruncateAt.END
        array.recycle()
    }

    private fun setBtnType(type: ButtonType) {
        when (type) {
            ButtonType.NORMAL -> {
                background = context.getAppCompactDrawable(R.drawable.bg_custom_btn)
                setTextColor(context.getAppCompactColor(R.color.white))
            }

            ButtonType.DISABLE -> {
                setTextColor(context.getAppCompactColor(R.color.color_primary_01))
                background = context.getAppCompactDrawable(R.drawable.bg_custom_btn_2)
            }
        }
    }

}

enum class ButtonType(val value: Int) {
    NORMAL(value = 1),
    DISABLE(value = 2);

    companion object {
        fun getValue(int: Int): ButtonType? {
            return values().firstOrNull { it.value == int }
        }
    }
}