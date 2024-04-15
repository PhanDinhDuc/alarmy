package com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.LayoutWeekdayBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.view.myday.displayTextSelect
import java.time.DayOfWeek
import java.util.Calendar

interface OnDaySelectedListener {
    val actionTv: List<TextView>
}

fun WeekdayView.setOnClickTextDay(onclick: (selectedDays: List<Int>, stringDay: String) -> Unit) {

    actionTv.forEachIndexed { index, textView ->
        textView.setOnClickListener {

            val day = listDays[index]
            if (selectedDays.contains(day)) {
                selectedDays.remove(day)
                textView.setBackgroundResource(R.drawable.bg_choose_day_none)
            } else {
                selectedDays.add(day)
                textView.setBackgroundResource(R.drawable.bg_choose_day)
            }

            onclick.invoke(selectedDays.toList(), returnDaySelectString(selectedDays.toList()))
        }
    }
}

fun WeekdayView.setDaySelect() {
    selectedDays.forEachIndexed { index1, it ->
        actionTv.forEachIndexed { index, textView ->
            val day = listDays[index]
            if (it == day) {
                textView.setBackgroundResource(R.drawable.bg_choose_day)
            }

        }
    }
}

class WeekdayView : FrameLayout, OnDaySelectedListener {

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

    private lateinit var binding: LayoutWeekdayBinding
    val selectedDays = mutableSetOf<Int>()
    val listDays = listOf(
        Calendar.MONDAY,
        Calendar.TUESDAY,
        Calendar.WEDNESDAY,
        Calendar.THURSDAY,
        Calendar.FRIDAY,
        Calendar.SATURDAY,
        Calendar.SUNDAY,
    )
    val dayLabels = listOf(
        DayOfWeek.MONDAY.displayTextSelect(),
        DayOfWeek.TUESDAY.displayTextSelect(),
        DayOfWeek.WEDNESDAY.displayTextSelect(),
        DayOfWeek.THURSDAY.displayTextSelect(),
        DayOfWeek.FRIDAY.displayTextSelect(),
        DayOfWeek.SATURDAY.displayTextSelect(),
        DayOfWeek.SUNDAY.displayTextSelect()
    )

    fun getToday(): String {
        val calendar = Calendar.getInstance()
        return dayLabels[calendar.get(Calendar.DAY_OF_WEEK)]
    }

    private fun initView(attrs: AttributeSet?, @AttrRes defStyleAttr: Int) {
        binding = LayoutWeekdayBinding.inflate(LayoutInflater.from(context), this, true)
        actionTv.forEachIndexed { index, textView ->
            textView.text = dayLabels.getOrNull(index)
        }
    }


    override val actionTv: List<TextView> by lazy {
        val dayTextViews = listOf(
            binding.textView3,
            binding.textView4,
            binding.textView5,
            binding.textView6,
            binding.textView7,
            binding.textView8,
            binding.textView10,
        )
        return@lazy dayTextViews
    }

    fun returnDaySelectString(selectedDays: List<Int>): String {

        val workingDays = listDays.subList(0, 5)
        val weekend = listDays.subList(5, 6 + 1)

        return when {
            selectedDays.containsAll(listDays) ->
                context.getString(R.string.everyDay)

            selectedDays.containsAll(workingDays) && selectedDays.size == 5 -> context.getString(R.string.workingDays)

            selectedDays.containsAll(weekend) && selectedDays.size == 2 -> context.getString(R.string.weekend)

            else -> {
                val day =
                    selectedDays.joinToString(" ") { day -> dayLabels[listDays.indexOf(day)] }
                val sortDay = sortTextViewDay(day)
                sortDay
            }
        }
    }

    private fun sortTextViewDay(input: String): String {
        val sortedDays = input.split(" ")
            .sortedWith(compareBy { dayLabels.indexOf(it) })
        return sortedDays.joinToString(" ")
    }
}


fun returnDaySelectString1(
    context: Context,
    selectedDays: Set<Int>
): String {

    val listDays = listOf(
        Calendar.MONDAY,
        Calendar.TUESDAY,
        Calendar.WEDNESDAY,
        Calendar.THURSDAY,
        Calendar.FRIDAY,
        Calendar.SATURDAY,
        Calendar.SUNDAY,
    )

    fun dayLabels(context: Context) = listOf(
        DayOfWeek.MONDAY.displayTextSelect(),
        DayOfWeek.TUESDAY.displayTextSelect(),
        DayOfWeek.WEDNESDAY.displayTextSelect(),
        DayOfWeek.THURSDAY.displayTextSelect(),
        DayOfWeek.FRIDAY.displayTextSelect(),
        DayOfWeek.SATURDAY.displayTextSelect(),
        DayOfWeek.SUNDAY.displayTextSelect()
    )

    val workingDays = listDays.subList(0, 5)
    val weekend = listDays.subList(5, 6 + 1)

    fun sortTextViewDay(input: String): String {
        val sortedDays = input.split(" ")
            .sortedWith(compareBy { dayLabels(context).indexOf(it) })
        return sortedDays.joinToString(" ")
    }

    return when {
        selectedDays.containsAll(listDays) ->
            context.getString(R.string.everyDay)

        selectedDays.containsAll(workingDays) && selectedDays.size == 5 -> context.getString(R.string.workingDays)

        selectedDays.containsAll(weekend) && selectedDays.size == 2 -> context.getString(R.string.weekend)

        else -> {
            val day =
                selectedDays.joinToString(" ") { day -> dayLabels(context)[listDays.indexOf(day)] }
            val sortDay = sortTextViewDay(day)
            sortDay
        }
    }
}