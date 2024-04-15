package com.alarm.clock.reminder.alarmclock.simplealarm.view.first_alarm

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ActivityFirstTimeAlarmBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import java.time.format.DateTimeFormatter

class FirstAlarmActivity : BaseActivity<ActivityFirstTimeAlarmBinding>(), OnChangeListener {

    override fun makeBinding(layoutInflater: LayoutInflater): ActivityFirstTimeAlarmBinding {
        return ActivityFirstTimeAlarmBinding.inflate(layoutInflater)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)

        binding.txtGetStarted.setOnSingleClickListener {

            val intent = Intent(this, CreateFirstAlarmActivity::class.java)
            intent.putExtra(LOCALTIME, binding.timePicker.getTimePicker())
            startActivity(intent)
        }
    }

    fun setTimePicker(localTime: String) {
        val value = localTime.format(DateTimeFormatter.ofPattern(FORMAT_TIME)).split(":")
        binding.timePicker.binding.hourPicker.setValue(value[0])
        binding.timePicker.binding.amPmPicker2.setValue(value[2])
        binding.timePicker.binding.minutesPicker.setValue(value[1])
    }

    companion object {
        const val FORMAT_TIME = "hh:mm:a"
        const val LOCALTIME = "localTime"
    }

    override fun setOnChange(localTime: String) {
        setTimePicker(localTime)
    }

}

interface OnChangeListener {

    fun setOnChange(localTime: String)


}

