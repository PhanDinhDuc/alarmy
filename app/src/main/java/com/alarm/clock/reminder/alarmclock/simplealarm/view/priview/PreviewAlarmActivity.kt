package com.alarm.clock.reminder.alarmclock.simplealarm.view.priview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.viewModels
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.MainApplication
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseVMActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.application.playSound
import com.alarm.clock.reminder.alarmclock.simplealarm.application.stopSound
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.Settings
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Alarm
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.hasSnooze
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ActivityPreviewAlarmBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.receiver.helper.AlarmHelper
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.ALARM_ARG
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.PREVIEW
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.PREVIEW_SETTING
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.RING
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.STATUS
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.getCurrentFormattedDate
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.isAppInForeground
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.showActivityWhenLockScreen
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.parcelable
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.showCustomToast
import com.alarm.clock.reminder.alarmclock.simplealarm.service.AlarmService
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.OnShowToastInsertNewAlarmListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.dialog.DialogItemSetting
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.ListTask
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.action_task.ActionTaskActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.action_task.ActionTaskViewModel.Companion.LIST_TASK
import com.alarm.clock.reminder.alarmclock.simplealarm.view.viewmodels.AlarmViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("DEPRECATION")
@AndroidEntryPoint
class PreviewAlarmActivity : BaseVMActivity<ActivityPreviewAlarmBinding, AlarmViewModel>() {

    @Inject
    lateinit var alarmHelper: AlarmHelper
    private var isSnooze = false

    override fun makeBinding(layoutInflater: LayoutInflater): ActivityPreviewAlarmBinding {
        return ActivityPreviewAlarmBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        turnScreenOnAndKeyguardOff()
        showActivityWhenLockScreen()
    }

    override val viewModel: AlarmViewModel by viewModels()
    private var job: Job? = null
    private var timeLeft: Int = 0
    private var serviceIntent: Intent? = null
    private lateinit var listTask: ListTask


    @SuppressLint("SetTextI18n", "StringFormatInvalid")
    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)

        val alarmStartTime = intent.extras?.getLong(Util.ALARM_START_TIME) ?: return
        Log.d("AAA", alarmStartTime.toString())

        val status = intent.extras?.getString(STATUS)
        status?.let { Log.d("AAA", it) }

        if (status == RING) {
            onBackPressedDispatcher.addCallback {
            }
        }

        val alarm: Alarm = intent?.parcelable(ALARM_ARG) ?: return
        Log.d("AAA", alarm.toString())

        val previewTask: ListTask = intent?.parcelable(LIST_TASK) ?: ListTask(emptyList())

        listTask =
            if (status == PREVIEW) ListTask(alarm.tasks?.onEach { it.isPreview = true }
                ?: emptyList()) else previewTask

        binding.txtTime.visibility = if (status == PREVIEW) View.VISIBLE else View.GONE
        binding.baseTextview7.visibility = if (status == PREVIEW) View.VISIBLE else View.GONE
        binding.btnExitPriview.visibility =
            if (status == PREVIEW || status == PREVIEW_SETTING) View.VISIBLE else View.GONE
        binding.txtStartOrDismiss.text =
            if (alarm.tasks.isNullOrEmpty() && status == RING) getString(R.string.dismiss) else getString(
                R.string.start_the_task
            )
        binding.txtStartOrDismiss.visibility =
            if (alarm.tasks.isNullOrEmpty() && status == PREVIEW) View.INVISIBLE else View.VISIBLE

        binding.txtSnooze.visibility = if (alarm.snooze != 0) View.VISIBLE else View.INVISIBLE

        val txtTime = alarm.time.split(":")
        val hour = txtTime[0].toInt()
        val minute = txtTime[1].toInt()
        binding.txtTime.text =
            "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
        binding.baseTextview7.text = getCurrentFormattedDate()

        if (status == RING) {

            activityScope.launch {
                AutoDismissAlarm.onFinish.collect {
                    if (it) {
                        val serviceIntent =
                            Intent(this@PreviewAlarmActivity, AlarmService::class.java)
                        applicationContext.stopService(serviceIntent)
                        finish()
                    }
                }

            }

            alarmHelper.setAlarm(alarm)
            serviceIntent = Intent(this@PreviewAlarmActivity, AlarmService::class.java)
            if (!AlarmService.isRunning) {
                serviceIntent?.putExtra(ALARM_ARG, alarm)
                applicationContext.startService(serviceIntent)
            }

            fun checkSnooze() {

                if (alarm.hasSnooze()) {
                    binding.txtSnooze.visibility = View.VISIBLE
                } else {
                    binding.txtSnooze.visibility = View.GONE
                }
            }

            checkSnooze()

            binding.txtSnooze.setOnSingleClickListener {

                applicationContext.stopService(serviceIntent)


                if (alarm.hasSnooze()) {
                    binding.txtSnoozeTime.apply {
                        text = getString(R.string.snoozed_time, alarm.repeat + 1)
                        background = null
                        visibility = View.VISIBLE
                    }
                    alarm.repeat++
                    alarmHelper.snoozeAlarm(alarm)
                    viewModel.updateAlarmDB(alarm.copy())
                    setCountDownTimer(alarm.snooze, 0) {
                        if (this.isAppInForeground()) {
                            binding.txtSnooze.visibility = View.VISIBLE
                            binding.txtStartOrDismiss.visibility = View.VISIBLE
                            binding.txtCountDownSnoozeTime.visibility = View.GONE
                            checkSnooze()
                            applicationContext.startService(serviceIntent)
                            alarmHelper.cancelNoti()
                            alarmHelper.setAlarm(alarm)
                        }
                    }

                    binding.txtSnooze.visibility = View.INVISIBLE
                    binding.txtStartOrDismiss.visibility = View.INVISIBLE
                    binding.txtCountDownSnoozeTime.visibility = View.VISIBLE
                } else {
                    alarm.repeat = 0
                    viewModel.updateAlarmDB(alarm.copy(repeat = 0))
                    binding.txtSnooze.visibility = View.INVISIBLE
                    binding.txtStartOrDismiss.visibility = View.INVISIBLE
                    binding.txtCountDownSnoozeTime.visibility = View.VISIBLE
                }

            }

            if (alarm.tasks.isNullOrEmpty()) {
                binding.txtStartOrDismiss.setOnSingleClickListener {
                    if (alarm.isQuickAlarm) {
                        viewModel.deleteAlarm(alarm)
                    }
                    applicationContext.stopService(serviceIntent)
                    finish()
                }
                //start task if alarm have task to dismiss alarm
            } else {
                binding.txtStartOrDismiss.setOnSingleClickListener {
                    alarmHelper.setAlarm(alarm)
                    startActivity(Intent(
                        this, ActionTaskActivity::class.java
                    ).apply {
                        this.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra(
                            LIST_TASK,
                            ListTask(alarm.tasks ?: emptyList())
                        )
                    })
                }
            }
        } else if (status == PREVIEW || status == PREVIEW_SETTING) {

            (application as? MainApplication)?.playSound(alarm.soundPath)

            binding.btnExitPriview.setOnSingleClickListener {
//                applicationContext.stopService(serviceIntent)
//                listener?.onShowToast(getString(R.string.alarm_priview_over))
                if (status == PREVIEW) {
                    Toast(this).showCustomToast(getString(R.string.alarm_priview_over), this)
                }
                finish()
            }

            binding.txtSnooze.setOnSingleClickListener {
//                applicationContext.stopService(serviceIntent)
                job = CoroutineScope(Dispatchers.Main).launch {
                    binding.txtSnoozeTime.apply {
                        text = getString(R.string.cant_enable_snooze)
                        setBackgroundResource(R.drawable.bg_toast)
                        visibility = View.VISIBLE
                    }
                    delay(2000)
                    binding.txtSnoozeTime.apply {
                        text = ""
                        background = null
                        visibility = View.INVISIBLE
                    }
                }
            }

            binding.txtStartOrDismiss.text =
                if (listTask.list.isEmpty()) getString(R.string.dismiss) else getString(R.string.start_the_task)
            if (listTask.list.isEmpty()) {
                binding.txtStartOrDismiss.setOnSingleClickListener {
//                    applicationContext.stopService(serviceIntent)
                    finish()
                }
                //start task if alarm have task to dismiss alarm
            } else {
                binding.txtStartOrDismiss.setOnSingleClickListener {
                    (application as? MainApplication)?.shareCallback = {
                        finish()
                        (application as? MainApplication)?.shareCallback = null
                        Toast(this).showCustomToast(getString(R.string.alarm_priview_over), this)
                    }
                    startActivity(Intent(
                        this, ActionTaskActivity::class.java
                    ).apply {
                        this.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra(
                            LIST_TASK,
                            listTask
                        )
                        putExtra(STATUS, intent.extras?.getString(STATUS))
                    })
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isSnooze) {
            AlarmService.setSoundEnable(this, true)
        }
    }

    private fun setCountDownTimer(
        snoozeMilliseconds: Int, startClickSnooze: Long,
        completion: (() -> Unit)? = null
    ) {
        isSnooze = true

        job?.cancel()
        job?.cancelChildren()
        job = null
        job = CoroutineScope(Dispatchers.Main).launch {

            timeLeft =
                (snoozeMilliseconds - (kotlin.math.ceil(startClickSnooze / 1000.0) * 1000).toInt())
            while (timeLeft >= 0) {
                binding.txtCountDownSnoozeTime.text = formatTime(timeLeft)
                delay(1000)
                timeLeft -= 1000
            }
            completion?.invoke()
        }
    }

    private fun formatTime(timeLeft: Int): String {
        val totalSeconds = timeLeft / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
//        AlarmService.setSoundEnable(this, false)
        (application as? MainApplication)?.stopSound()
        AutoDismissAlarm.stop()
    }

    companion object {
        var listener: OnShowToastInsertNewAlarmListener? = null
        var listenerCallBack: CallBackDismissCountDown? = null
    }

    interface CallBackDismissCountDown {
        fun onDismissCountDown(serviceIntent: Intent)
    }
}

fun Activity.turnScreenOnAndKeyguardOff() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        setShowWhenLocked(true)
        setTurnScreenOn(true)
    } else {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        )
    }
}

@Singleton
object AutoDismissAlarm {

    private var countdownTimer: CountDownTimer? = null

    var onFinish = MutableStateFlow(false)
    fun startCountdownDismiss() {
        if (Settings.alarmSettings.autoDismiss == DialogItemSetting.Duration.OFF) return
        countdownTimer?.cancel()
        countdownTimer =
            object : CountDownTimer(Settings.alarmSettings.autoDismiss.duration, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    Log.d("TAG", "onTick: $millisUntilFinished")
                }

                override fun onFinish() {
                    CoroutineScope(Dispatchers.Main).launch {
                        onFinish.emit(true)
                    }
                }
            }.start()
    }

    fun stop() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(300)
            onFinish.emit(false)
        }
        countdownTimer?.cancel()
        countdownTimer = null
    }
}