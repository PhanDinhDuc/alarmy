package com.alarm.clock.reminder.alarmclock.simplealarm.view.task.math

import android.view.LayoutInflater
import android.view.ViewGroup
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.FragmentSettingTaskMathBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.InputFilterMinMax
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.OnChangeLevelListener
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.BaseTaskSettingFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskType

class SettingTaskMathFragment : BaseTaskSettingFragment<FragmentSettingTaskMathBinding>(),
    OnChangeLevelListener {

    override val taskType: TaskType = TaskType.MATH

    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSettingTaskMathBinding {
        return FragmentSettingTaskMathBinding.inflate(inflater, container, false)
    }

    override fun setupView() {
        super.setupView()
        binding.numberEquationsView.inputFilterTapPicker = InputFilterMinMax("1", "10")
        binding.taskLevelView.mOnChangeListener = this
        setUpExampleMath()
        binding.numberEquationsView.setNumber(
            taskSettingModel?.repeatTime ?: TaskType.MATH.defaultData.repeatTime
        )
        binding.taskLevelView.taskLevel = taskSettingModel?.level ?: TaskType.MATH.defaultData.level
        binding.btnPreview.setOnSingleClickListener {
            val task = taskSettingModel ?: return@setOnSingleClickListener
            actionCallback?.onPreview(
                task.copy(
                    level = binding.taskLevelView.taskLevel,
                    repeatTime = binding.numberEquationsView.getNumber()
                )
            )
        }

        binding.btnSave.setOnSingleClickListener {
            val task = taskSettingModel ?: return@setOnSingleClickListener
            actionCallback?.onSave(
                task.copy(
                    level = binding.taskLevelView.taskLevel,
                    repeatTime = binding.numberEquationsView.getNumber()
                )
            )
        }
    }

    override fun onChangeLevel() {
        setUpExampleMath()
    }

    private fun setUpExampleMath() {
        binding.tvExample.text = binding.taskLevelView.taskLevel.math.string()
    }
}