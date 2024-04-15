package com.alarm.clock.reminder.alarmclock.simplealarm.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Reminder
import com.alarm.clock.reminder.alarmclock.simplealarm.receiver.helper.ReminderHelper
import com.alarm.clock.reminder.alarmclock.simplealarm.receiver.helper.ReminderHelper.Companion.REMINDER_DATA
import com.alarm.clock.reminder.alarmclock.simplealarm.receiver.helper.ReminderHelper.Companion.reminderChannel
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.parcelable
import com.alarm.clock.reminder.alarmclock.simplealarm.view.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import kotlin.math.roundToInt


@AndroidEntryPoint
class ReminderService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private var originalVolume: Int? = null

    @Inject
    lateinit var reminderHelper: ReminderHelper

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val reboot = intent?.getBooleanExtra(ReminderHelper.REBOOT_DATA, false) ?: false
        val close = intent?.getBooleanExtra(ReminderHelper.ACTION_CLOSE, false) ?: false
        val reminder = intent?.parcelable<Reminder>(ReminderHelper.REMINDER_DATA)
        when {
            close -> stopSelf()
            reboot -> {
                scope.launch {
                    reminderHelper.reboot()
                    stopSelf()
                }
            }

            reminder != null -> {
                val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
                val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
                val data = (max * (reminder.soundLevel.toFloat() / audioManager.getStreamMaxVolume(
                    AudioManager.STREAM_MUSIC
                ))).roundToInt()

                audioManager.setStreamVolume(
                    AudioManager.STREAM_NOTIFICATION,
                    data,
                    0
                )
                reminderHelper.createReminder(reminder)
                notificationManager.notify(Date().time.toInt(), createNotification(reminder))
                stopSelf()
            }

            else -> {
                stopSelf()
            }
        }
        return START_STICKY
    }

    private val notificationManager by lazy {
        getSystemService(
            NotificationManager::class.java
        )
    }

    private fun createNotification(reminder: Reminder): Notification {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
        val reminderChannel = NotificationChannel(
            reminderChannel(reminder),
            reminderChannel(reminder),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableLights(true)
            enableVibration(reminder.isVibrate)
            setSound(
                Uri.parse(reminder.alertTonePath),
                audioAttributes
            )
        }
        notificationManager?.createNotificationChannel(reminderChannel)
        val pendingIntent = getPendingIntent(reminder)

        val builder =
            NotificationCompat.Builder(this, reminderChannel(reminder))
                .setContentTitle(reminder.name)
                .setSmallIcon(
                    reminder.type.getIcon()
                )
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setSound(Uri.parse(reminder.alertTonePath))
                .setContentIntent(pendingIntent)

        if (reminder.isVibrate) builder.setVibrate(
            longArrayOf(
                0,
                600,
                200,
                600,
                200,
                800,
                200,
                1000
            )
        )
        return builder.build()
    }

    private fun getPendingIntent(reminder: Reminder): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(REMINDER_DATA, reminder)
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }



}