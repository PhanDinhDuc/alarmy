package com.alarm.clock.reminder.alarmclock.simplealarm.receiver.helper

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.BedTimeData
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.TimeSoundModel
import com.alarm.clock.reminder.alarmclock.simplealarm.receiver.BedtimeReceiver
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.BEDTIME_ID
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.getTimeData
import com.alarm.clock.reminder.alarmclock.simplealarm.service.BedtimeService
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BedtimeHelper @Inject constructor(@ApplicationContext private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    fun setBedtimeSleep(bedTimeData: BedTimeData) {
        if (!bedTimeData.isOn) return

        for (timeSoundModel in bedTimeData.listTimes) {
            setupBedtimeSleep(timeSoundModel, bedTimeData)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun setupBedtimeSleep(timeSoundModel: TimeSoundModel, bedtime: BedTimeData) {
        val time = timeSoundModel.time
        val (hour, minute, isAM) = time.getTimeData() ?: return
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, timeSoundModel.day)
            set(Calendar.HOUR_OF_DAY, hour.toInt())
            set(Calendar.MINUTE, minute.toInt())
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (isAM == "AM") {
                set(Calendar.AM_PM, Calendar.AM)
            } else {
                set(Calendar.AM_PM, Calendar.PM)
            }
        }

        if (Calendar.getInstance().after(cal)) {
            cal.add(Calendar.WEEK_OF_YEAR, 1)
        }

        val intent = Intent(context, BedtimeReceiver::class.java)
        intent.putExtra(BEDTIME_ID, bedtime.id)

        val pendingIntent = PendingIntent.getBroadcast(
            context, timeSoundModel.day, intent, PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdleIfNeed(
            AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent
        )

    }

    fun cancelForBedtime(bedTimeData: BedTimeData) {
        for (timeSoundModel in bedTimeData.listTimes) {
            cancelBedtimeSleep(timeSoundModel)
        }
    }

    private fun cancelBedtimeSleep(timeModel: TimeSoundModel) {
        val alarmIntent = Intent(context, BedtimeReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(
                context,
                timeModel.day,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
            )
        }

        alarmManager.cancel(alarmIntent)

    }

    fun killService() {
        if (BedtimeService.isRunning) {
            val intent = Intent(context, BedtimeService::class.java)
            context.stopService(intent)
        }
    }
}

fun Context.canScheduleExactAlarms(): Boolean {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return false
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }
}

fun AlarmManager.setExactAndAllowWhileIdleIfNeed(
    type: Int, triggerAtMillis: Long, operation: PendingIntent
) {

    val isHavePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        canScheduleExactAlarms()
    } else {
        true
    }

    if (isHavePermission) {
        setExactAndAllowWhileIdle(
            type, triggerAtMillis, operation
        )
    }
}