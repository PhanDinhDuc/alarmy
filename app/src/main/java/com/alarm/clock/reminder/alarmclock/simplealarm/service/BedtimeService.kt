package com.alarm.clock.reminder.alarmclock.simplealarm.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.repository.BedtimeRepository
import com.alarm.clock.reminder.alarmclock.simplealarm.receiver.BedtimeReceiver
import com.alarm.clock.reminder.alarmclock.simplealarm.receiver.BedtimeReceiver.Companion.ACTION_STOP_SERVICE
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.BEDTIME_ID
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.NOTI_ID_BEDTIME
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class BedtimeService : Service() {

    @Inject
    lateinit var bedtimeRepository: BedtimeRepository
    private var mediaPlayer: MediaPlayer? = null
    private var job: Job? = null

    companion object {
        var isRunning = false
    }


    @SuppressLint("ObsoleteSdkInt")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val bedtimeId = intent?.extras?.getInt(BEDTIME_ID, 1) ?: 0

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTI_ID_BEDTIME, createNotification())
        }

        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                bedtimeRepository.getBedTimeById(bedtimeId).let {
                    withContext(Dispatchers.Main) {
                        playSound(it.sound)
                        delay(timeMillis = it.timeSound)
                        stopSelf()
                    }
                }
            } catch (e: Exception) {
                Log.e("BedtimeService", e.message.toString())
                stopSelf()
            }
        }

        isRunning = true

        return START_STICKY
    }

    private fun playSound(sound: String?) {
        mediaPlayer = MediaPlayer.create(this, Uri.parse(sound))
        mediaPlayer?.start()
        mediaPlayer?.isLooping = true
    }


    private fun createNotification(): Notification {
        val notificationChannelId = "Chanel Bedtime"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Bedtime Notification Channel"
            val descriptionText = "Channel for Bedtime Notification"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(notificationChannelId, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

        }

        val cancelIntent = Intent(this, BedtimeReceiver::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val cancelPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            cancelIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )

        val builder = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.ic_alarm_noti)
            .setContentTitle(getString(R.string.bedtime_running))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .addAction(R.drawable.ic_exit, getString(R.string.txt_cancel), cancelPendingIntent)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(false)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)

        return builder.build()

    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        job?.cancel()
        stopSelf()
    }
}