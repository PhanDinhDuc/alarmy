package com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import java.util.Locale

class BaseTextview @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {
    init {
//        val array = context.obtainStyledAttributes(attrs, R.styleable.BaseTextView)
        try {

        } finally {
//            array.recycle()
        }
    }


}

enum class Language(
    val id: Int,
    @StringRes val nameFlag: Int,
    @DrawableRes val flag: Int,
    val localizeCode: String
) {

    ENGLISH(0, R.string.lg_english, R.drawable.flag_kingdom, "en"),
    HINDI(3, R.string.lg_hindi, R.drawable.flag_of_hindi, "hi"),
    CHINA(5, R.string.lg_china, R.drawable.ic_china, "zh"),
    SPANISH(1, R.string.lg_spanish, R.drawable.flag_of_bandera, "es"),
    FRENCH(2, R.string.lg_french, R.drawable.flag_of_france, "fr"),
    PORTUGUESE(4, R.string.lg_portuguese, R.drawable.flag_of_portugal, "pt");

    companion object {
        fun getAll(): List<Language> = Language.values().toList()
        fun get(id: Int) = getAll().firstOrNull { it.id == id }

        var current: Language = ENGLISH

        fun setCurrent(id: Int) {
            current = get(id) ?: ENGLISH
        }
    }
}

fun Language.locale(): Locale = try {
    Locale(localizeCode)
} catch (e: Exception) {
    Locale.ENGLISH
}
