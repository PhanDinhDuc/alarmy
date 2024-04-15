package com.alarm.clock.reminder.alarmclock.simplealarm.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.BEDTIME_ID
import com.alarm.clock.reminder.alarmclock.simplealarm.service.BedtimeService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BedtimeReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_STOP_SERVICE = "stop_service"
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onReceive(context: Context?, intent: Intent?) {
        val bedtimeId = intent?.extras?.getInt(BEDTIME_ID, 1) ?: 0

        Log.d("AAA", bedtimeId.toString())

        val intentService = Intent(context, BedtimeService::class.java).apply {
            putExtra(BEDTIME_ID, bedtimeId)
        }

        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                context?.stopService(intentService)
            }

            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context?.startForegroundService(intentService)
                } else {
                    context?.startService(intentService)
                }
            }
        }
    }
}
