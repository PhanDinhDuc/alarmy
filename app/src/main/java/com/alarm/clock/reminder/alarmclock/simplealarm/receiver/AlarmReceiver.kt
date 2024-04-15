package com.alarm.clock.reminder.alarmclock.simplealarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Alarm
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.ALARM_ARG
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.RING
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.STATUS
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.parcelable
import com.alarm.clock.reminder.alarmclock.simplealarm.service.AlarmService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val alarm: Alarm = intent?.parcelable(ALARM_ARG) ?: return

        if (context == null || alarm == null) {
            Log.e("AlarmReceiver", "ERROR")
            return
        }

        initReciver(context, alarm)
    }

    private fun initReciver(context: Context, alarm: Alarm) {
        Log.d("AAA", alarm.toString())

//        val isInForeground = context.isAppInForeground() || !context.isDeviceLocked()

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(ALARM_ARG, alarm)
            putExtra(STATUS, RING)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
