package com.alarm.clock.reminder.alarmclock.simplealarm.view.first_alarm

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.GridLayoutManager
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseVMActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.Settings
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Alarm
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ActivityCreateFirstAlarmBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Constant
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Converter
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.getToDayString
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.visible
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.dialog.DialogItemSetting
import com.alarm.clock.reminder.alarmclock.simplealarm.view.first_alarm.TaskSound.Companion.getFirstSound
import com.alarm.clock.reminder.alarmclock.simplealarm.view.main.MainActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.sound.convertRawToPath
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskType
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskType.Companion.getFirstTaskSetting
import com.alarm.clock.reminder.alarmclock.simplealarm.view.viewmodels.AlarmViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import kotlin.math.floor


@AndroidEntryPoint
class CreateFirstAlarmActivity :
    BaseVMActivity<ActivityCreateFirstAlarmBinding, AlarmViewModel>() {

    override fun makeBinding(layoutInflater: LayoutInflater): ActivityCreateFirstAlarmBinding {
        return ActivityCreateFirstAlarmBinding.inflate(layoutInflater)
    }

    private lateinit var adapters: FirstTaskAlarmAdapter
    private var localTime: String? = null
    private var pathSound: String? = null
    override val viewModel: AlarmViewModel by viewModels()

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        localTime = intent.extras?.getString(FirstTimeAlarmActivity.LOCALTIME)
        adapters = FirstTaskAlarmAdapter(this) { it1, position ->
            adapters.setSelectedId(position = position)
            binding.btnNext.visible()
            adapters.stop()
            if (it1 is FirstSoundTaskModel) {
                binding.btnNext.setOnClickListener {
                    adapters.stop()
                    val intent = Intent(this, CreateFirstAlarmActivity::class.java)
                    intent.putExtra("data", it1.sound)
                    intent.putExtra(FirstTimeAlarmActivity.LOCALTIME, localTime)
                    startActivity(intent)
                }
            } else if (it1 is FirstTaskModel) {
                binding.btnNext.setOnClickListener {
                    Settings.PASS_TUTORIAL.put(true)
                    it1.taskType?.let { it2 ->
                        localTime?.let { it3 ->
                            insertAlarm(
                                it3,
                                it2,
                                pathSound!!
                            )
                        }
                    } ?: localTime?.let { it3 ->
                        insertAlarm(
                            it3,
                            null,
                            pathSound!!
                        )
                    }
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }

        localTime = intent.getStringExtra(FirstTimeAlarmActivity.LOCALTIME)

        if (intent.hasExtra("data")) {
            val soundId = intent.getIntExtra("data", 0)
            pathSound = soundId.convertRawToPath(this)
            binding.baseTextview5.text =
                getText(R.string.alarm_task)
            adapters.setupData(getFirstTaskSetting())
        } else {
            adapters.setupData(getFirstSound())
        }

        binding.rclChooseSound.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(
                this@CreateFirstAlarmActivity,
                floor(((Converter.dpFromPx(Constant.screenWidth) - 16) / 156).toDouble()).toInt()
            )
            adapter = adapters
        }


    }

    private fun insertAlarm(localTime: String, taskType: TaskType?, pathSound: String) {
        val tasks = if (taskType != null) listOf(taskType.defaultData) else listOf()

        val alarm = Alarm(
            1,
            "",
            localTime,
            getToDayString(),
            true,
            DialogItemSetting.Duration.D5M.duration.toInt(),
            0,
            //get current day
            listOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)),
            tasks,
            pathSound,
            100,
            true,
            isSkipNext = false,
            false,
            null,
        )

        viewModel.insertAlarm(alarm)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapters.stop()
    }

    override fun onPause() {
        super.onPause()
        adapters.stop()
    }
}

enum class TaskSound(
    val id: Int
) {
    ALARM_BELL(0),
    PEACEFUL_SOUND(1),
    HAPPY_SOUND(2),
    LOUD_SOUND(3);

    @get:DrawableRes
    val title: Int
        get() {
            return when (this) {
                ALARM_BELL -> R.string.alarm_bell
                PEACEFUL_SOUND -> R.string.peaceful
                HAPPY_SOUND -> R.string.happy_sound
                LOUD_SOUND -> R.string.loud_sound
            }
        }

    @get:DrawableRes
    val image: Int
        get() {
            return when (this) {
                ALARM_BELL -> R.drawable.ic_alarm_bell
                PEACEFUL_SOUND -> R.drawable.ic_peaceful_sound
                HAPPY_SOUND -> R.drawable.happy_sound
                LOUD_SOUND -> R.drawable.ic_loud_sound
            }
        }

    @get:DrawableRes
    val sound: Int
        get() {
            return when (this) {
                ALARM_BELL -> R.raw.alarm
                PEACEFUL_SOUND -> R.raw.peaceful
                HAPPY_SOUND -> R.raw.happy_sound
                LOUD_SOUND -> R.raw.loud_sound
            }
        }

    companion object {
        fun getFirstSound(): List<FirstSoundTaskModel> {
            return listOf(
                ALARM_BELL,
                PEACEFUL_SOUND,
                HAPPY_SOUND,
                LOUD_SOUND
            ).map { FirstSoundTaskModel(it.title, it.image, it.sound) }
        }
    }
}
