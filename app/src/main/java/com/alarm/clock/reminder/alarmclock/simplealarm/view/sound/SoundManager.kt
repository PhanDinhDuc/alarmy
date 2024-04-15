package com.alarm.clock.reminder.alarmclock.simplealarm.view.sound

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.appcompat.app.AppCompatActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.AudioModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SoundManager @Inject constructor(@ApplicationContext val activity: Context) {
    private val mediaPlayer: MediaPlayer by lazy {
        MediaPlayer().apply {
            setAudioStreamType(
                AudioManager.STREAM_MUSIC
            )
            isLooping = true
        }
    }
    var vibrator: Vibrator? = null

    init {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                activity.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            activity.getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator
        }


    }

    fun activeVibrator() {
        if (vibrator!!.hasVibrator()) {
            val timings = longArrayOf(0, 100, 200, 300)
            vibrator!!.vibrate(VibrationEffect.createWaveform(timings, 0))
        }
    }

    fun playSound(audioModel: AudioModel) {
        mediaPlayer.play(activity, audioModel) {
        }
        mediaPlayer.setOnCompletionListener {
            cancelVibrator()
        }
    }

    fun pauseSound() {
        mediaPlayer.pause()
    }

    fun startSound() {
        mediaPlayer.start()
    }

    fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }

    fun onDestroy() {
        mediaPlayer.stop()
        vibrator?.cancel()
    }

    fun cancelVibrator() {
        vibrator?.cancel()
    }

}

fun MediaPlayer.play(
    context: Context,
    audioModel: AudioModel, onStart: () -> Unit
) {
    reset()
    if (audioModel.raw != null) {
        val mediaPath = Uri.parse("android.resource://${context.packageName}/${audioModel.raw}")
        setDataSource(context, mediaPath)

    } else {
        setDataSource(audioModel.aPath)
    }
    prepareAsync()
    setOnPreparedListener {
        isLooping = true
        onStart.invoke()
        start()
    }

}
