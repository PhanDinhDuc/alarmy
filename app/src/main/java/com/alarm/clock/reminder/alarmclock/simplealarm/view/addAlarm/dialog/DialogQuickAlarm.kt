package com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.dialog

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseDialogFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.Settings
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.Settings.*
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Alarm
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.DialogQuickAlarmBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.receiver.helper.canScheduleExactAlarms
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.getToDayString
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.showCustomToast
import com.alarm.clock.reminder.alarmclock.simplealarm.view.permission.PermissionActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.viewmodels.AlarmViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class DialogQuickAlarm : BaseDialogFragment<DialogQuickAlarmBinding>() {
    private var isVibrant = false
    private var isSoundClicked = true
    private var currentText: String? = null
    private var time: String? = null
    private var soundVolume = 100
    private var mediaPlayer: MediaPlayer? = null
    private val viewModel: AlarmViewModel by viewModels()
    var vibrator: Vibrator? = null
    private var mAudioManager: AudioManager? = null
    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogQuickAlarmBinding {
        return DialogQuickAlarmBinding.inflate(inflater)
    }

    override fun setupView() {
        super.setupView()
        mAudioManager = context?.getSystemService(AppCompatActivity.AUDIO_SERVICE) as AudioManager
        soundVolume = mAudioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        binding.seekBar.max = soundVolume
        isVibrant = IS_VIBRATE.get(false)
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                activity?.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            activity?.getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator
        }
        playSound()
        val listKeys = listOf(
            binding.txt0,
            binding.txt1,
            binding.txt2,
            binding.txt3,
            binding.txt4,
            binding.txt5,
            binding.txt6,
            binding.txt7,
            binding.txt8,
            binding.txt9,
        )

        listKeys.forEachIndexed { index, textView ->
            textView.setOnSingleClickListener {
                getText(index, binding.textView9)
            }
        }

        binding.imgClose.setOnSingleClickListener {
            dismiss()
        }
        VOLUME.put(mAudioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC))

        val currVolume = VOLUME.get(100)
        changeImageSound(mAudioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
        setupIconVolume(currVolume)
        binding.seekBar.progress = currVolume
        binding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                VOLUME.put(p1)
                setupIconVolume(p1)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }

        })

        setupIconVibrate()
        if (isVibrant) {
            activeVibrator()
        } else {
            vibrator?.cancel()
        }

        binding.txtSave.setOnSingleClickListener {
            if (binding.textView9.length() != 5) {
                return@setOnSingleClickListener
            }
            val alarm = Alarm(
                Alarm.NO_ID,
                "",
                convertTo12HFormat(binding.textView9.text.toString()),
                requireContext().getToDayString(),
                true,
                DialogItemSetting.Duration.D5M.duration.toInt(),
                0,
                //defaul get today
                listOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)),
                null,
                Settings.alarmSettings.soundPath,
                soundLevel = soundVolume,
                isVibrate = isVibrant,
                false,
                isQuickAlarm = true,
                null
            )

            if (activity?.canScheduleExactAlarms() == true) {
                viewModel.insertAlarm(alarm)
                activity?.let { it1 ->
                    Toast(requireContext()).showCustomToast(
                        Util.convertMillisToTimeFormat(requireContext(), alarm),
                        it1
                    )
                }

                dismiss()
            } else {
                val intent = Intent(activity, PermissionActivity::class.java)
                startActivity(intent)
            }
        }

        binding.imgReturn.setOnSingleClickListener {
            if (binding.textView9.text.isNotEmpty()) {
                binding.textView9.apply {
                    text = ""
                    hint = "00:00"
                }
            } else {
                return@setOnSingleClickListener
            }
        }

        binding.imgDelete.setOnSingleClickListener {
            val currentText = binding.textView9.text.toString()
            when {
                currentText.matches(Regex(".*:\\d$")) -> binding.textView9.text =
                    currentText.substring(0, currentText.length - 2)

                currentText.isNotEmpty() -> binding.textView9.text = currentText.dropLast(1)
            }
        }
    }


    private fun playSound() {
        try {
            mediaPlayer =
                MediaPlayer.create(requireContext(), Uri.parse(Settings.alarmSettings.soundPath))
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener {
                it.start()
            }
        } catch (e: Exception) {
            Log.e("DialogQuickAlarm", e.message.toString())
        }

    }

    private fun changeImageSound(volume: Int) {
        binding.imageView5.setOnSingleClickListener {
            if (binding.seekBar.progress != 0) {
                setupIconVolume(0)
                binding.seekBar.progress = 0
            } else {
                setupIconVolume(volume)
                binding.seekBar.progress = volume
            }
        }
    }


    private fun setupIconVolume(volume: Int) {
        if (volume == 0) {
            mediaPlayer?.pause()
        } else {
            mediaPlayer?.start()
        }
        mAudioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
        binding.imageView5.isSelected = volume != 0
        soundVolume = volume
    }

    private fun setupIconVibrate() {
        binding.imageView6.isSelected = isVibrant
        binding.imageView6.setOnSingleClickListener {
            isVibrant = !isVibrant
            it?.isSelected = isVibrant
            getVibrate(isVibrant)
        }
    }

    private fun getVibrate(isVibrate: Boolean) {
        if (isVibrate) {
            activeVibrator()
        } else {
            vibrator?.cancel()
        }
    }

    private fun activeVibrator() {
        if (vibrator!!.hasVibrator()) {
            val timings = longArrayOf(0, 100, 200, 300)
            vibrator!!.vibrate(VibrationEffect.createWaveform(timings, 0))
        }
    }

    private fun getText(index: Int, targetTextView: TextView) {
        currentText = (targetTextView.text ?: "").toString()

        when {
            currentText!!.isEmpty() -> {
                //number hour first must be 0-2
                if (index <= 2) {
                    targetTextView.text = "$currentText$index"
                }
            }
            //
            currentText!!.length == 1 -> {
                val hour = "$currentText$index".toInt()
                if (hour in 0..23) {
                    targetTextView.text = "$currentText$index:"
                }
            }

            currentText!!.length == 3 -> {
                //number munit first must be 0-5
                if (index <= 5) {
                    targetTextView.text = "$currentText$index"
                }
            }

            currentText!!.length == 4 -> {
                targetTextView.text = "$currentText$index"
            }
        }
    }

    private fun convertTo12HFormat(time: String): String {
        val splitTime = time.split(":")
        var hour = splitTime[0].toInt()
        val minute = splitTime[1].toInt()

        var amOrPm = "AM"

        if (hour >= 12) {
            amOrPm = "PM"
            if (hour > 12) hour -= 12
        }

        if (hour == 0) hour = 12

        return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}:$amOrPm"
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.stop()
        vibrator?.cancel()
        mediaPlayer = null
    }


    companion object {
        fun showDialog(fmg: FragmentManager) {
            val newDialog = DialogQuickAlarm()
            newDialog.show(fmg, "QuickAlarmDialog")
        }
    }
}
