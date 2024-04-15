package com.alarm.clock.reminder.alarmclock.simplealarm.receiver.helper

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Alarm
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.getSortDay
import com.alarm.clock.reminder.alarmclock.simplealarm.receiver.AlarmReceiver
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.ALARM_ARG
import com.alarm.clock.reminder.alarmclock.simplealarm.service.AlarmService
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.Calendar.DAY_OF_WEEK
import java.util.Calendar.WEEK_OF_YEAR
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmHelper @Inject constructor(@ApplicationContext val context: Context) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun setAlarm(alarm: Alarm) {
        if (alarm.isON) {
            setupAlarm(alarm)
        } else {
            cancelPendingIntent(alarm)
        }
    }

    fun cancelPendingIntent(alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            pendingId(alarm),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun Alarm.getCalender(weakOfYear: Int? = null, day: Int? = null): Calendar {
        val calendar = Calendar.getInstance().apply {
            val timeParts = this@getCalender.time.split(":")
            var hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()
            val amPm = timeParts[2]
            if (hour >= 12) {
                hour -= 12
            }
            set(Calendar.HOUR, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (amPm == "AM") {
                set(Calendar.AM_PM, Calendar.AM)
            } else {
                set(Calendar.AM_PM, Calendar.PM)
            }

            weakOfYear?.let {
                this.set(WEEK_OF_YEAR, it)
            }

            day?.let {
                this.set(DAY_OF_WEEK, it)
            }
        }
        return calendar.clone() as Calendar
    }

    // 1. get time now
    // 2. get time alarm day
    // 3. set day of week = now
    // 4. compare 1 vs 2, if 1 > 2 continue,
    // if 1 < 2 set alarm = 2, break
    // khong cos 1 < 2 thi tang 3 len 1, chayj lai 4
    @SuppressLint("ScheduleExactAlarm")
    private fun setupAlarm(
        alarm: Alarm,
        calendar: Calendar? = null,
        skipDays: List<Int>? = null
    ) {
        cancelPendingIntent(alarm)
        fun createAlarmPending(calendar: Calendar) {
            val intent = Intent(context, AlarmReceiver::class.java)
            intent.putExtra(ALARM_ARG, alarm)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                pendingId(alarm),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            alarmManager.setExactAndAllowWhileIdleIfNeed(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }

        if (calendar != null) {
            createAlarmPending(calendar)
            return
        }

        val weakOfYear = Calendar.getInstance().get(WEEK_OF_YEAR)
        val now = Calendar.getInstance()
        var hasAlarmCreated = false

        fun compareAndAddAlarm(days: List<Int>?, weakOfYear: Int) {
            for (day in days.orEmpty()) {
                val calender = alarm.getCalender(weakOfYear, day)
                if (now.timeInMillis < calender.timeInMillis) {
                    //save alarm
                    createAlarmPending(calender)
                    hasAlarmCreated = true
                    break
                } else {
                    continue
                }
            }
        }
        compareAndAddAlarm(skipDays ?: alarm.getSortDay(), weakOfYear)
        if (!hasAlarmCreated) {
            compareAndAddAlarm(alarm.getSortDay(), weakOfYear + 1)
        }
    }

    fun pendingId(alarm: Alarm): Int {
        return alarm.id + 1212
    }

    fun snoozeAlarm(alarm: Alarm) {
        val now = Calendar.getInstance()
//        now.add(Calendar.MINUTE, 1)
        now.add(Calendar.MINUTE, alarm.snooze / 60000)
        setupAlarm(alarm, now)
    }

    //get next alarm
    private fun getNextAlarmDay(alarm: Alarm): Int? {
        val days = alarm.getSortDay() ?: return null

        if (days.size < 2) return null
        val now = Calendar.getInstance()
        var skipDay: Int? = null

        for (day in days) {
            val calender = alarm.getCalender(Calendar.getInstance().get(WEEK_OF_YEAR), day)
            if (now.timeInMillis < calender.timeInMillis) {
                //save alarm
                skipDay = day
                break
            } else {
                continue
            }
        }
        return skipDay
    }

    fun skipNextAlarm(alarm: Alarm): Int? = getNextAlarmDay(alarm)?.apply {
        val days = alarm.days?.toMutableSet()
        days?.remove(this)
        setupAlarm(alarm, skipDays = days?.toList())
    }

    fun cancelNoti() {
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(Util.NOTI_ID)
    }

    fun killService(alarm: Alarm) {
        if (AlarmService.alarmId == alarm.id && AlarmService.isRunning) {
            val intent = Intent(context, AlarmService::class.java)
            context.stopService(intent)
        }
    }
}