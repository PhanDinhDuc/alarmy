package com.alarm.clock.reminder.alarmclock.simplealarm.receiver.helper

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.dao.ReminderDao
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Reminder
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.RepeatMode
import com.alarm.clock.reminder.alarmclock.simplealarm.receiver.ReminderReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ReminderHelper @Inject constructor(
    @ApplicationContext val context: Context,
    private val reminderDao: ReminderDao
) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun cancelReminderFromId(id: Int) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun updateReminder(reminder: Reminder) {
        cancelReminderFromId(reminder.id)
        createReminder(reminder.copy(nextEvent = null))
    }

    fun createReminder(
        reminder: Reminder
    ) {
        val nextEvent = if (reminder.nextEvent == null) {

            var reminderDateTime = LocalDateTime.of(reminder.date, reminder.time)
            val now = LocalDateTime.now()

            if (reminderDateTime.isBefore(now)) {
                when (reminder.repeatMode) {
                    RepeatMode.Annually -> {
                        while (reminderDateTime.isBefore(now)) {
                            reminderDateTime = reminderDateTime.plusYears(1)
                        }
                        reminderDateTime
                    }

                    RepeatMode.Daily -> {
                        while (reminderDateTime.isBefore(now)) {
                            reminderDateTime = reminderDateTime.plusDays(1)
                        }
                        reminderDateTime
                    }

                    is RepeatMode.EveryHour -> {
                        while (reminderDateTime.isBefore(now)) {
                            reminderDateTime =
                                reminderDateTime.plusHours(reminder.repeatMode.hour.toLong())
                        }
                        reminderDateTime
                    }

                    is RepeatMode.Monthly -> {
                        val monthEvent = LocalDateTime.of(
                            now.year,
                            now.month,
                            reminder.date.dayOfMonth,
                            reminder.time.hour,
                            reminder.time.minute,
                            reminder.time.second
                        )
                        if (monthEvent.isBefore(now)) {
                            val currentMonth = now.month.value
                            val minDiff = reminder.repeatMode.months.minOf {
                                if (it > currentMonth) it - currentMonth
                                else if (it < currentMonth) 12 - currentMonth + it
                                else 12
                            }
                            monthEvent.plusMonths(minDiff.toLong())
                        } else monthEvent
                    }

                    is RepeatMode.Weekly -> {
                        val dayEvent = LocalDateTime.of(
                            now.toLocalDate(),
                            reminder.time
                        )
                        if (dayEvent.isBefore(now)) {
                            val currentDayOfWeek = now.dayOfWeek.value
                            val minDiff = reminder.repeatMode.days.minOf {
                                if (it > currentDayOfWeek) it - currentDayOfWeek
                                else if (it < currentDayOfWeek) 7 - currentDayOfWeek + it
                                else 7
                            }
                            dayEvent.plusDays(minDiff.toLong())
                        } else dayEvent
                    }

                    RepeatMode.None -> null
                    is RepeatMode.SeveralTimeInDay -> {
                        val minDiff =
                            reminder.repeatMode.times
                                .minOf {
                                    val time =
                                        LocalDateTime.of(now.toLocalDate(), it)
                                    val second = now.until(time, ChronoUnit.SECONDS)
                                    if (second < 10) {
                                        now.until(
                                            time.plusDays(1),
                                            ChronoUnit.SECONDS
                                        )
                                    } else second
                                }
                        now.plusSeconds(minDiff)
                    }
                }
            } else reminderDateTime

        } else when (reminder.repeatMode) {
            RepeatMode.Annually -> {
                reminder.nextEvent.plusYears(1)
            }

            RepeatMode.Daily -> {
                reminder.nextEvent.plusDays(1)
            }

            is RepeatMode.EveryHour -> {
                reminder.nextEvent.plusHours(reminder.repeatMode.hour.toLong())
            }

            is RepeatMode.Monthly -> {
                val currentMonth = reminder.nextEvent.month.value
                val minDiff = reminder.repeatMode.months.minOf {
                    if (it > currentMonth) it - currentMonth
                    else if (it < currentMonth) 12 - currentMonth + it
                    else 12
                }
                reminder.nextEvent.plusMonths(minDiff.toLong())
            }

            is RepeatMode.Weekly -> {
                val currentDayOfWeek = reminder.nextEvent.dayOfWeek.value
                val minDiff = reminder.repeatMode.days.minOf {
                    if (it > currentDayOfWeek) it - currentDayOfWeek
                    else if (it < currentDayOfWeek) 7 - currentDayOfWeek + it
                    else 7
                }
                reminder.nextEvent.plusDays(minDiff.toLong())
            }

            RepeatMode.None -> null
            is RepeatMode.SeveralTimeInDay -> {
                val minDiff =
                    reminder.repeatMode.times
                        .minOf {
                            val time = LocalDateTime.of(reminder.nextEvent.toLocalDate(), it)
                            val second = reminder.nextEvent.until(time, ChronoUnit.SECONDS)
                            if (second < 10) {
                                reminder.nextEvent.until(time.plusDays(1), ChronoUnit.SECONDS)
                            } else second
                        }
                reminder.nextEvent.plusSeconds(minDiff)
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            if (nextEvent != null) {
                val currentReminder = reminder.copy(nextEvent = nextEvent)
                reminderDao.updateReminder(currentReminder)
                val intent = Intent(context, ReminderReceiver::class.java)
                intent.putExtra(REMINDER_DATA, currentReminder)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    currentReminder.id,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextEvent.getTime(),
                    pendingIntent
                )
            }
        }
    }

    private fun rebootReminder(
        reminder: Reminder
    ) {
        val intent = Intent(context, ReminderReceiver::class.java)
        intent.putExtra(REMINDER_DATA, reminder)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (reminder.nextEvent != null) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminder.nextEvent.getTime(),
                pendingIntent
            )
        }
    }

    suspend fun reboot() {
        reminderDao.getAll().forEach {
            rebootReminder(it)
        }
    }

    companion object {
        const val REMINDER_DATA = "REMINDER_DATA"
        const val REBOOT_DATA = "REBOOT_DATA"
        const val ACTION_CLOSE = "ACTION_CLOSE"
        fun reminderChannel(reminder: Reminder) =
            "${reminder.alertTonePath}-${reminder.isVibrate}"
    }
}

fun LocalDateTime.getTime() = atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()