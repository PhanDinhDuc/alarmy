package com.alarm.clock.reminder.alarmclock.simplealarm.view.step

import android.view.LayoutInflater
import android.view.ViewGroup
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.FragmentTaskStepBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.InputFilterMinMax
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.BaseTaskSettingFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskType

class StepTaskFragment : BaseTaskSettingFragment<FragmentTaskStepBinding>() {

    override val taskType: TaskType = TaskType.STEP
    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTaskStepBinding = FragmentTaskStepBinding.inflate(inflater)

    override fun setupView() {
        super.setupView()
        binding.numberPicker.inputFilterTapPicker = InputFilterMinMax("1", "100")
        binding.numberPicker.setNumber(
            taskSettingModel?.targetValue ?: binding.numberPicker.minValue
        )
        binding.btnPreview.setOnSingleClickListener {
            val task = taskSettingModel ?: return@setOnSingleClickListener
            actionCallback?.onPreview(task.copy(targetValue = binding.numberPicker.getNumber()))
        }

        binding.btnSave.setOnSingleClickListener {
            val task = taskSettingModel ?: return@setOnSingleClickListener
            actionCallback?.onSave(task.copy(targetValue = binding.numberPicker.getNumber()))
        }
    }
}