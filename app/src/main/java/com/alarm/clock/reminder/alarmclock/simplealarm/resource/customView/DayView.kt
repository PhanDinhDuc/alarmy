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


fun DayView.setOnClickDay(onclick: (List<Int>, Boolean) -> Unit) {
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
            onclick.invoke(selectedDays.map { it.value }, listDays.size == selectedDays.size)
        }
    }
}

class DayView : FrameLayout, OnDaySelectedListener {

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
    var selectedDays = mutableListOf<DayOfWeek>()
        set(value) {
            listDays.forEachIndexed { index, i ->
                if (i in value) actionTv[index].setBackgroundResource(R.drawable.bg_choose_day)
                else actionTv[index].setBackgroundResource(R.drawable.bg_choose_day_none)
            }
            field = value
        }

    val listDays = DayOfWeek.values().toList()


    private fun initView(attrs: AttributeSet?, @AttrRes defStyleAttr: Int) {
        binding = LayoutWeekdayBinding.inflate(LayoutInflater.from(context), this, true)
        actionTv.forEachIndexed { index, textView ->
            textView.text = listDays[index].displayTextSelect()
        }
    }


    override val actionTv: List<TextView> by lazy {
        return@lazy listOf<TextView>(
            binding.textView3,
            binding.textView4,
            binding.textView5,
            binding.textView6,
            binding.textView7,
            binding.textView8,
            binding.textView10,
        )
    }
}


