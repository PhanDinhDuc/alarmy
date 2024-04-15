package com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm

import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ActivitySnoozeBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.setOnBackClick
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.setOnImageRightClick
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.dialog.DialogItemSetting


class SnoozeActivity : BaseActivity<ActivitySnoozeBinding>() {
    private lateinit var adapterSnooze: AdapterSnooze
    private var snoozeDuration: Long = DialogItemSetting.Duration.D5M.duration
    private lateinit var listSnooze: List<Snooze>
    override fun makeBinding(layoutInflater: LayoutInflater): ActivitySnoozeBinding {
        return ActivitySnoozeBinding.inflate(layoutInflater)
    }


    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        if (intent.hasExtra("keySnoozeAlarm")) {
            snoozeDuration = intent.getLongExtra("keySnoozeAlarm", 0)
            binding.mHeader.title = R.string.snooze
            listSnooze = Snooze.getListSnoozeAlarm()
        } else {
            snoozeDuration = intent.getLongExtra("keySnoozeBedTime", Snooze.M10.duration)
            binding.mHeader.title = R.string.sound_timer
            listSnooze = Snooze.getListSnoozeBedTime()
        }

        adapterSnooze = AdapterSnooze(this) {
            adapterSnooze.setSelected(it.duration)
            snoozeDuration = it.duration
        }
        adapterSnooze.setSelected(snoozeDuration)
        binding.mViewpager.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        adapterSnooze.setupData(listSnooze)
        binding.mViewpager.adapter = adapterSnooze

        binding.mHeader.setOnBackClick { finish() }
        binding.mHeader.setOnImageRightClick {
            finish()
            listener?.onSnoozeListener(snoozeDuration)
        }
    }


    companion object {
        var listener: CallBackSnoozeListener? = null
    }

    interface CallBackSnoozeListener {
        fun onSnoozeListener(snoozeDuration: Long)
    }

}

enum class Snooze {
    OFF, M1, M5, M10, M15, M20, M25, M30, M40, M50, M60;

    val duration: Long
        get() {
            return when (this) {
                OFF -> 0
                M1 -> 60000L
                M5 -> 5 * 60000L
                M10 -> 10 * 60000L
                M15 -> 15 * 60000L
                M20 -> 20 * 60000L
                M25 -> 25 * 60000L
                M30 -> 30 * 60000L
                M40 -> 40 * 60000L
                M50 -> 50 * 60000L
                M60 -> 60 * 60000L
            }
        }


    val title: Int
        get() {
            return when (this) {
                OFF -> 0
                M1 -> 1
                M5 -> 5
                M10 -> 10
                M15 -> 15
                M20 -> 20
                M25 -> 25
                M30 -> 30
                M40 -> 40
                M50 -> 50
                M60 -> 60
            }
        }

    companion object {
        fun getListSnoozeAlarm() = listOf(OFF, M1, M5, M10, M15, M20, M25, M30)
        fun getListSnoozeBedTime() = listOf(M10, M20, M30, M40, M50, M60)
    }
}


