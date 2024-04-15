@file:Suppress("DEPRECATION")

package com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.text.InputFilter
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.FLAG_ONGOING_EVENT
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.Settings
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Alarm
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.showKeyboard
import com.alarm.clock.reminder.alarmclock.simplealarm.view.first_alarm.wheel_picker.OnValueTapListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.first_alarm.wheel_picker.WheelPicker
import com.alarm.clock.reminder.alarmclock.simplealarm.view.main.MainActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.myday.displayFullText
import com.alarm.clock.reminder.alarmclock.simplealarm.view.myday.displayTextSelect
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.rewrite.SettingTaskRewriteFragment
import java.time.DayOfWeek
import java.util.Calendar


object Util {

    const val ALARM_ARG = "Alarm"
    const val STATUS = "Status"
    const val PREVIEW = "Preview"
    const val PREVIEW_SETTING = "PreviewSetting"
    const val SHOW_DETAIL = "Detail"
    const val RING = "Ring"
    const val NOTI_ID = 1000
    const val NOTI_ID_BEDTIME = 1001
    const val BEDTIME_DATA = "Bedtime"
    const val TIME_SOUND = "Time_Sound"
    const val ALARM_START_TIME = "alarmStartTime"

    const val TIME_DEFAULT = "10:00:PM"
    const val NOTI_SETTING_ID = 10011
    const val BEDTIME_ID = "Bedtime_Id"
    const val TIME_UPDATE = 60000L

    fun Context.getCurrentFormattedDate(): String {
        val calendar = Calendar.getInstance()

        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)

        val dayOfWeekStr = when (dayOfWeek) {
            Calendar.SUNDAY -> getString(R.string.SU)
            Calendar.MONDAY -> getString(R.string.MO)
            Calendar.TUESDAY -> getString(R.string.TU)
            Calendar.WEDNESDAY -> getString(R.string.WE)
            Calendar.THURSDAY -> getString(R.string.TH)
            Calendar.FRIDAY -> getString(R.string.FR)
            else -> getString(R.string.SA)
        }

        val monthStr = "Th" + (month + 1)

        return "$dayOfWeekStr , $dayOfMonth $monthStr"
    }


    //calculator next alarm
    private fun getNextAlarmTime(alarms: List<Alarm>): Calendar? {
        val now = Calendar.getInstance()
        var closestTime: Calendar? = null
        val activeAlarms = alarms.filter { it.isON }

        for (alarm in activeAlarms) {
            for (day in alarm.days ?: listOf()) {
                val alarmTime = getAlarmCalendarTime(alarm.time, day)
                if (alarmTime.before(now)) {
                    alarmTime.add(Calendar.DAY_OF_YEAR, 7)
                }
                if (closestTime == null || alarmTime.before(closestTime)) {
                    closestTime = alarmTime
                }
            }
        }
        return closestTime
    }


    private fun getAlarmCalendarTime(time: String, dayOfWeek: Int): Calendar {
        val cal = Calendar.getInstance()
        val parts = time.split(":")
        var hour = parts[0].toInt()
        val minute = parts[1].toInt()
        val amPm = parts[2]
        if (hour >= 12) {
            hour -= 12
        }
        cal.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        cal.set(Calendar.HOUR, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        if (amPm == "PM") {
            cal.set(Calendar.AM_PM, Calendar.PM)
        } else {
            cal.set(Calendar.AM_PM, Calendar.AM)
        }
        return cal
    }


    fun getTimeDifferenceToNextAlarm(alarms: List<Alarm>): Pair<Int, Int> {
        val now = Calendar.getInstance()
        val closestTime = getNextAlarmTime(alarms)

        if (closestTime != null) {
            var differenceInMillis = closestTime.timeInMillis - now.timeInMillis

            if (differenceInMillis < 0) {
                differenceInMillis += 7 * 24 * 60 * 60 * 1000
            }

            val hours = (differenceInMillis / (1000 * 60 * 60)).toInt()
            val minutes = ((differenceInMillis % (1000 * 60 * 60)) / (1000 * 60)).toInt()

            return Pair(hours, minutes)
        }

        return Pair(0, 0)
    }


    fun getDayToNextAlarm(hours: Int): Pair<Int, Int> {
        val days = hours / 24
        val remainingHours = hours % 24
        return Pair(days, remainingHours)
    }

    private fun calculatorTimeInsertNewAlarm(alarm: Alarm): Long {
        val now = Calendar.getInstance()

        val parts = alarm.time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()
        val amPm = if (parts[2].toUpperCase() == "AM") Calendar.AM else Calendar.PM

        val todayAlarmTime = Calendar.getInstance().apply {
            if (amPm == Calendar.PM && hour != 12) {
                set(Calendar.HOUR_OF_DAY, hour + 12)
            } else {
                set(Calendar.HOUR_OF_DAY, hour)
            }
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (now.after(todayAlarmTime)) {
            todayAlarmTime.add(Calendar.DAY_OF_YEAR, 1)
        }

        if ((alarm.days ?: emptyList()).contains(now.get(Calendar.DAY_OF_WEEK))) {
            return todayAlarmTime.timeInMillis - now.timeInMillis
        }

        var minDaysUntilNext = 7
        for (day in (alarm.days ?: emptyList())) {
            val daysUntil = if (day >= now.get(Calendar.DAY_OF_WEEK)) {
                day - now.get(Calendar.DAY_OF_WEEK)
            } else {
                7 + day - now.get(Calendar.DAY_OF_WEEK)
            }

            minDaysUntilNext = kotlin.math.min(minDaysUntilNext, daysUntil)
        }

        val nextAlarmDateTime = todayAlarmTime.apply {
            add(Calendar.DAY_OF_YEAR, minDaysUntilNext)
        }

        return nextAlarmDateTime.timeInMillis - now.timeInMillis
    }

    @SuppressLint("StringFormatInvalid")
    fun convertMillisToTimeFormat(context: Context, alarm: Alarm): String {
        val millisRemaining = calculatorTimeInsertNewAlarm(alarm)

        val totalSeconds = millisRemaining / 1000
        val totalMinutes = totalSeconds / 60
        val hours = (totalMinutes / 60).toInt()
        val minutes = (totalMinutes % 60).toInt()
        val days = hours / 24

        return when {
            days > 0 -> context.getString(R.string.alarm_is_set_day, days, hours % 24)
            else -> context.getString(R.string.alarm_is_set_hour, hours, minutes)
        }
    }

    fun getRandomString(sizeOfRandomString: Int): String {
        return (1..sizeOfRandomString)
            .map { SettingTaskRewriteFragment.ALLOWED_CHARACTERS.random() }
            .joinToString("")
    }

    fun Context.getToDayString(): String {
        when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            1 -> {
                return getString(R.string.SU)
            }

            2 -> {
                return getString(R.string.MO)
            }

            3 -> {
                return getString(R.string.TU)
            }

            4 -> {
                return getString(R.string.WE)
            }

            5 -> {
                return getString(R.string.TH)
            }

            6 -> {
                return getString(R.string.FR)
            }

            7 -> {
                return getString(R.string.SA)
            }

            else -> {
                return ""
            }
        }
    }


    @SuppressLint("InvalidWakeLockTag")
    fun Activity.showActivityWhenLockScreen() {
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "WakelockTag"
        )
        wakeLock.acquire()
    }


    fun Context.isAppInForeground(): Boolean {
        val activityManager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = this.packageName
        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == packageName) {
                return true
            }
        }
        return false
    }

    fun Activity.hideNavBar() {
        window.decorView.apply {
            systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    fun getDayString(day: Int, context: Context): String {
        return when (day) {
            1 -> DayOfWeek.SUNDAY.displayFullText()
            2 -> DayOfWeek.MONDAY.displayFullText()
            3 -> DayOfWeek.TUESDAY.displayFullText()
            4 -> DayOfWeek.WEDNESDAY.displayFullText()
            5 -> DayOfWeek.THURSDAY.displayFullText()
            6 -> DayOfWeek.FRIDAY.displayFullText()
            7 -> DayOfWeek.SATURDAY.displayFullText()
            else -> ""
        }
    }

    fun String.getTimeData(): Triple<String, String, String> {
        val parts = this.split(":")
        val hour = parts[0]
        val minute = parts[1]
        val isAM = parts[2]

        return Triple(hour, minute, isAM)
    }

    private fun upDateTimeEarliestAlarm(context: Context, alarms: List<Alarm>): String {
        val (hours, minutes) = getTimeDifferenceToNextAlarm(alarms)
        val a = if (hours >= 24) {
            val (days, hours) = getDayToNextAlarm(hours)
            context.getString(R.string.time_will_ring_day, days, hours)
        } else {
            if (alarms.isEmpty()) {
                context.getString(R.string.create_new)
            } else if (hours == 0 && alarms.isNotEmpty() && minutes != 0) {
                context.getString(R.string.time_will_ring_min, minutes)
            } else if (alarms.isNotEmpty() && alarms.all { !it.isON }) {
                context.getString(R.string.no_alarm_turn_on)
            } else if (hours != 0) {
                context.getString(R.string.time_will_ring, hours, minutes)
            } else {
                context.getString(R.string.alarm_comming_soon)
            }
        }
        return a
    }

    private fun createNotification(
        context: Context,
        alarm: List<Alarm>
    ): Notification {
        val notificationChannelId = "Chanel alarm setting"

        val name = "Alarm Notification Channel"
        val descriptionText = "Channel for Alarm Notification"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(notificationChannelId, name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val pendingIntent = PendingIntent.getActivity(
            context, 1001, intent, PendingIntent.FLAG_IMMUTABLE
        )
        val time = upDateTimeEarliestAlarm(context, alarm)
        val builder = NotificationCompat.Builder(context, notificationChannelId)
            .setSmallIcon(R.drawable.ic_alarm_noti)
            .setContentTitle(context.getString(R.string.alarm))
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentText(time)
        builder.priority = NotificationCompat.PRIORITY_MIN
        builder.setSilent(true)

        return builder.build().apply {
            flags = FLAG_ONGOING_EVENT
        }
    }

    @SuppressLint("MissingPermission")
    fun checkShowNoti(context: Context, list: List<Alarm>) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Settings.alarmSettings.showNextAlarm) {
//            notificationManager.cancel(NOTI_SETTING_ID)
            notificationManager.notify(
                NOTI_SETTING_ID,
                createNotification(context, list)
            )
        } else {
            notificationManager.cancel(NOTI_SETTING_ID)
        }
    }

    fun handleTapPicker(editText: EditText, picker: WheelPicker, filter: InputFilter) {

        picker.setOnValueTapListener(object : OnValueTapListener {
            override fun onValueTap(picker: WheelPicker, value: String) {
                editText.setText(value)
                editText.visible()
                editText.showKeyboard()
                editText.setSelection(editText.length())
                editText.filters = arrayOf(filter)
                editText.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        editText.gone()
                        setValueTapPicker(editText, picker)
                        true
                    }
                    false
                }

                editText.setOnFocusChangeListener { v, hasFocus ->
                    setValueTapPicker(editText, picker)
                    editText.visibility = if (hasFocus) View.VISIBLE else View.GONE
                }
            }

        })
    }

    fun setValueTapPicker(editText: EditText, picker: WheelPicker) {
        val value = editText.text.toString()
        picker.setValue(if (value.length < 2) "0${value}" else value)
    }

}




