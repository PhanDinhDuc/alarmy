package com.alarm.clock.reminder.alarmclock.simplealarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Reminder
import com.alarm.clock.reminder.alarmclock.simplealarm.receiver.helper.ReminderHelper
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.parcelable
import com.alarm.clock.reminder.alarmclock.simplealarm.service.ReminderService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        when {
            intent?.action.equals(Intent.ACTION_BOOT_COMPLETED) -> {
                context.startService(Intent(context, ReminderService::class.java).apply {
                    putExtra(
                        ReminderHelper.REMINDER_DATA, true
                    )
                })
            }

            intent?.parcelable<Reminder>(ReminderHelper.REMINDER_DATA) != null -> {
                context.startService(Intent(context, ReminderService::class.java).apply {
                    putExtra(
                        ReminderHelper.REMINDER_DATA,
                        intent.parcelable<Reminder>(ReminderHelper.REMINDER_DATA)
                    )
                })
            }

            intent?.getBooleanExtra(ReminderHelper.ACTION_CLOSE, false) == true -> {
                context.startService(Intent(context, ReminderService::class.java).apply {
                    putExtra(
                        ReminderHelper.ACTION_CLOSE, true
                    )
                })
            }
        }
    }
}
