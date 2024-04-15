package com.alarm.clock.reminder.alarmclock.simplealarm.view.first_alarm

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ActivityFirstTimeAlarmBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener

class FirstTimeAlarmActivity : BaseActivity<ActivityFirstTimeAlarmBinding>() {

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

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    companion object {
        const val LOCALTIME = "localTime"
    }


}



