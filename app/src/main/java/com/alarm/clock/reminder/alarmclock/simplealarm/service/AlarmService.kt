package com.alarm.clock.reminder.alarmclock.simplealarm.service

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.MainApplication
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.CoroutineLauncher
import com.alarm.clock.reminder.alarmclock.simplealarm.application.handleSound
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.Settings
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Alarm
import com.alarm.clock.reminder.alarmclock.simplealarm.receiver.helper.AlarmHelper
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.ALARM_ARG
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.ALARM_START_TIME
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.RING
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.STATUS
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.isAppInForeground
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.parcelable
import com.alarm.clock.reminder.alarmclock.simplealarm.view.priview.AutoDismissAlarm
import com.alarm.clock.reminder.alarmclock.simplealarm.view.priview.PreviewAlarmActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

@Suppress("DEPRECATION")
@AndroidEntryPoint
class AlarmService : Service() {

    @Inject
    lateinit var alarmHelper: AlarmHelper
    private var mediaPlayer: MediaPlayer? = null
    private var volumeIncreaseTimer: Timer? = null
    private lateinit var audioManager: AudioManager
    private var currentVolume: Int? = null

    private val serviceScope: CoroutineLauncher by lazy {
        return@lazy CoroutineLauncher()
    }

    companion object {
        var alarmId = -1
        var isRunning = false
        val notificationChannelId = "Chanel alarm1"
        val MUSIC_HANDLE_INTENT = "MUSIC_HANDLE_INTENT"

        fun setSoundEnable(context: Activity, enable: Boolean) {
            (context.application as? MainApplication)?.handleSound(enable)
            if (isRunning) {
                val serviceIntent = Intent(context, AlarmService::class.java)
                serviceIntent.putExtra(MUSIC_HANDLE_INTENT, enable)
                context.applicationContext.startService(serviceIntent)
            }
        }
    }

    private var vibrator: Vibrator? = null
    private var isVibrator = false

    override fun onCreate() {
        super.onCreate()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val isPlaySound = intent?.getBooleanExtra(MUSIC_HANDLE_INTENT, true)
        handleSound(isPlaySound ?: true)
        if (isPlaySound != false) {
            if (isVibrator) vibratorPhone()
        } else {
            vibrator?.cancel()
        }
        val alarm: Alarm = intent?.parcelable(ALARM_ARG) ?: return START_NOT_STICKY

        val isInForeground = this.isAppInForeground()
        if (!intent.hasExtra(MUSIC_HANDLE_INTENT)) {
            if (!(isInForeground && MainApplication.ACTIVITY_CONTEXT is PreviewAlarmActivity)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForeground(
                        Util.NOTI_ID,
                        createNotificationRingAlarm(alarm)
                    )
                }
            }
        }

        serviceScope.launch {
            alarmHelper.setAlarm(alarm)
            AutoDismissAlarm.onFinish.collect {
                if (it) {
                    stopSelf()
                    AutoDismissAlarm.stop()
                }
            }

        }

        AutoDismissAlarm.startCountdownDismiss()

        if (!isRunning) {
            audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

            if (Settings.alarmSettings.increaseVolume.duration > 0) {
                increaseVolumeGradually()
            }
        }

        if (alarm.isVibrate) {
            vibratorPhone()
        }
        isVibrator = alarm.isVibrate

        playSound(alarm.soundPath)

        isRunning = true
        alarmId = alarm.id

        return START_STICKY
    }

    private fun playSound(soundPath: String) {
        mediaPlayer = MediaPlayer.create(this, Uri.parse(soundPath))
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    private fun handleSound(isPlaying: Boolean) {
        if (isPlaying && isRunning) {
            mediaPlayer?.start()
        } else {
            mediaPlayer?.pause()
            vibrator?.cancel()
        }
    }


    private fun createNotificationRingAlarm(
        alarm: Alarm
    ): Notification {
        val name = "Alarm Notification Channel"
        val descriptionText = "Channel for Alarm Notification"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(notificationChannelId, name, importance).apply {
            description = descriptionText
            setSound(null, null)
            enableVibration(false)
        }

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(this, PreviewAlarmActivity::class.java).apply {
            putExtra(ALARM_ARG, alarm)
            putExtra(STATUS, RING)
            putExtra(ALARM_START_TIME, System.currentTimeMillis())
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }


        val pendingIntent = PendingIntent.getActivity(
            this,
            alarmHelper.pendingId(alarm),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.ic_alarm_noti)
            .setContentTitle(getString(R.string.alarm_is_running))
            .setContentText(getString(R.string.tap_to_dismiss))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(false)
            .setFullScreenIntent(pendingIntent, true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        return builder.build().apply {
            defaults = 0
        }
    }


    private fun increaseVolumeGradually() {

        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        var minVolume = 1

        val durationMillis = Settings.alarmSettings.increaseVolume.duration
        val intervalMillis = 1000
        val steps = durationMillis * intervalMillis / intervalMillis
        val volumeStep = steps / maxVolume

        volumeIncreaseTimer = Timer()
        volumeIncreaseTimer?.scheduleAtFixedRate(object : TimerTask() {
            private var currentStep = 0

            override fun run() {
                if (currentStep < steps && minVolume < maxVolume) {
                    if (currentStep % volumeStep.toInt() == 0) {
                        minVolume++
                        setAudioStreamVolume(minVolume)
                    }
                    currentStep++
                } else {
                    setAudioStreamVolume(minVolume)
                }
            }
        }, 0, intervalMillis.toLong())
    }

    fun setAudioStreamVolume(value: Int) {
        audioManager.setStreamVolume(
            AudioManager.STREAM_ALARM, value, AudioManager.FLAG_PLAY_SOUND
        )
    }

    private fun vibratorPhone() {
        val vibrationPattern = longArrayOf(0, 600, 200, 600, 200, 800, 200, 1000)
        val vibrationAmplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255)

        if (vibrator?.hasVibrator() == true) {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(
                    vibrationPattern, vibrationAmplitudes, 0
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        vibrator = null
        volumeIncreaseTimer?.cancel()
        currentVolume?.let { setAudioStreamVolume(it) }
        serviceScope.cancelCoroutines()
        serviceScope.cancel()
        AutoDismissAlarm.stop()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}