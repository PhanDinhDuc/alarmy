package com.alarm.clock.reminder.alarmclock.simplealarm.view.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.StringRes
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.MainApplication
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseVMActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.Settings
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.Settings.Companion.alarmSettings
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.FragmentAlarmSettingBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.setOnBackClick
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.dialog.DialogItemSetting
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.dialog.DialogSensitivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.sound.AlarmDefault
import com.alarm.clock.reminder.alarmclock.simplealarm.view.sound.AlarmSoundActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.sound.getNameSound
import com.alarm.clock.reminder.alarmclock.simplealarm.view.viewmodels.AlarmViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AlarmSettingActivity : BaseVMActivity<FragmentAlarmSettingBinding, AlarmViewModel>(),
    DialogItemSetting.CallbackListener,
    DialogSensitivity.CallbackListeners,
    DialogSortAlarm.OnClickSortAlarm {
    override val viewModel: AlarmViewModel by viewModels()

    override fun makeBinding(layoutInflater: LayoutInflater): FragmentAlarmSettingBinding {
        return FragmentAlarmSettingBinding.inflate(layoutInflater)
    }


    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)

        binding.headerSetting.setOnBackClick {
            finish()
        }
        with(binding) {
            alarmSettings.let {
                tvCurrentVolume.apply {
                    text = getString(it.increaseVolume.title)
                    setTextColors(it.increaseVolume.title)
                }
                tvCurrentTimeDismiss.apply {
                    text = getString(it.autoDismiss.title)
                    setTextColors(it.autoDismiss.title)
                }

                tvCurrentSnooze.text = getString(it.snoozeLimit.title)
                tvCurrentTimeLimit.apply {
                    text = getString(it.taskTimeLimit.title)
                    setTextColors(it.taskTimeLimit.title)
                }
                tvCurrentShakeSensitivity.text = getString(it.shakeSensitivity.title)
                switch4.isChecked = it.muteDuringMission
                switchShowNextAlarm.isChecked = it.showNextAlarm
                txtType.text = getString(it.sortAlarmBy.title)
//                switch3.isChecked = it.sortByAlarmFirst
            }

        }
        binding.btnSound.setOnSingleClickListener {
            val intent = Intent(this, AlarmSoundActivity::class.java)
            intent.putExtra("setting_sound", true)
            intent.putExtra("sounds", alarmSettings.soundPath)
            startActivity(intent)
        }
        binding.btnIncreaseVolume.setOnSingleClickListener {
            DialogItemSetting.show(
                supportFragmentManager,
                R.string.increase_volume,
                4,
                DialogItemSetting.Duration.getAllVolume(),
                DialogItemSetting.Duration.getAllVolume().indexOf(alarmSettings.increaseVolume)
            )
        }
        binding.btnAutoDismiss.setOnSingleClickListener {
            DialogItemSetting.show(
                supportFragmentManager,
                R.string.auto_dismiss,
                4,
                DialogItemSetting.Duration.getAllAutoDismiss(),
                DialogItemSetting.Duration.getAllAutoDismiss().indexOf(alarmSettings.autoDismiss)
            )
        }
        binding.btnShakeSensitivity.setOnSingleClickListener {
            DialogSensitivity.show(
                supportFragmentManager,
                R.string.shake_sensitivity,
                Sensitivity.getAll().indexOf(alarmSettings.shakeSensitivity)
            )
        }
        binding.btnSnooze.setOnSingleClickListener {
            DialogItemSetting.show(
                supportFragmentManager,
                R.string.snooze_limit,
                3,
                DialogItemSetting.Duration.getAllSnoozeSetting(),
                DialogItemSetting.Duration.getAllSnoozeSetting().indexOf(alarmSettings.snoozeLimit)
            )
        }
        binding.btnTimeLimit.setOnSingleClickListener {
            DialogItemSetting.show(
                supportFragmentManager,
                R.string.task_time_limit,
                4,
                DialogItemSetting.Duration.getAllTimeLimit(),
                DialogItemSetting.Duration.getAllTimeLimit().indexOf(alarmSettings.taskTimeLimit)
            )
        }

        binding.switch4.setOnCheckedChangeListener { _, b ->
            alarmSettings = alarmSettings.copy(muteDuringMission = b)

        }
        viewModel.allAlarmData.observe(this) { list ->
            binding.switchShowNextAlarm.setOnCheckedChangeListener { _, b ->
                Settings.alarmSettings = alarmSettings.copy(showNextAlarm = b)
                Util.checkShowNoti(this, list)
            }
        }
//        binding.switch3.setOnCheckedChangeListener { _, b ->
//            listenerCallBack?.onUpdateList(b)
//        }

        binding.btnSortByEnable.setOnSingleClickListener {
            DialogSortAlarm.showDialogSort(
                supportFragmentManager,
                SortAlarmBy.lists.indexOf(alarmSettings.sortAlarmBy)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        binding.tvCurrentLanguage.text =
            alarmSettings.soundPath.getNameSound(this)
    }

    override fun onSelectItemDialog(timeSnooze: DialogItemSetting.Duration, stringResId: Int) {
        when (stringResId) {
            R.string.increase_volume -> {
                binding.tvCurrentVolume.apply {
                    text = getString(timeSnooze.title)
                    setTextColors(timeSnooze.title)
                }
                alarmSettings = alarmSettings.copy(increaseVolume = timeSnooze)
            }

            R.string.auto_dismiss -> {
                binding.tvCurrentTimeDismiss.apply {
                    text = getString(timeSnooze.title)
                    setTextColors(timeSnooze.title)
                }
                alarmSettings = alarmSettings.copy(autoDismiss = timeSnooze)
            }

            R.string.snooze_limit -> {
                binding.tvCurrentSnooze.text = getString(timeSnooze.title)
                alarmSettings = alarmSettings.copy(snoozeLimit = timeSnooze)
            }

            R.string.task_time_limit -> {
                binding.tvCurrentTimeLimit.apply {
                    text = getString(timeSnooze.title)
                    setTextColors(timeSnooze.title)
                }
                alarmSettings = alarmSettings.copy(taskTimeLimit = timeSnooze)
            }
        }


    }

    override fun onSelectItemDialog(timeSnooze: Sensitivity) {
        binding.tvCurrentShakeSensitivity.text = getString(timeSnooze.title)
        alarmSettings = alarmSettings.copy(shakeSensitivity = timeSnooze)
    }

    private fun TextView.setTextColors(value: Int) {
        this.setTextColor(
            if (getString(value) == getString(R.string.off)) getColor(
                R.color.color_04
            ) else getColor(R.color.color_1E6AB0)
        )
    }

    companion object {
        var listenerCallBack: CallBackOnChangeListener? = null
    }

    interface CallBackOnChangeListener {
        fun onUpdateList(type: SortAlarmBy)
    }

    override fun sortAlarm(type: SortAlarmBy) {
        binding.txtType.text = getString(type.title)
        Settings.alarmSettings = alarmSettings.copy(sortAlarmBy = type)
        listenerCallBack?.onUpdateList(type)
    }


}


data class AlarmSetting(
    val soundPath: String = AlarmDefault(MainApplication.CONTEXT),
    var increaseVolume: DialogItemSetting.Duration = DialogItemSetting.Duration.D60S,
    var autoDismiss: DialogItemSetting.Duration = DialogItemSetting.Duration.D1M,
    var snoozeLimit: DialogItemSetting.Duration = DialogItemSetting.Duration.D2T,
    val taskTimeLimit: DialogItemSetting.Duration = DialogItemSetting.Duration.D60S,
    val muteDuringMission: Boolean = true,
    val shakeSensitivity: Sensitivity = Sensitivity.NORMAL,
    val sortAlarmBy: SortAlarmBy = SortAlarmBy.NORMAL,
    val showNextAlarm: Boolean = false,
    val sortByAlarmFirst: Boolean = true,
)

enum class Sensitivity(val id: Int) {
    HIGH(0), NORMAL(1), LOW(2);

    @get:StringRes
    val title: Int
        get() {
            return when (this) {
                HIGH -> R.string.high
                NORMAL -> R.string.normal
                LOW -> R.string.low
            }
        }

    companion object {
        fun getAll() = Sensitivity.values().toList()
    }
}