package com.alarm.clock.reminder.alarmclock.simplealarm.view.task.squat

import android.view.LayoutInflater
import android.view.ViewGroup
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.FragmentTaskSquatBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.InputFilterMinMax
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.BaseTaskSettingFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskType

class SquatTaskFragment : BaseTaskSettingFragment<FragmentTaskSquatBinding>() {

    override val taskType: TaskType = TaskType.SQUAT
    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTaskSquatBinding = FragmentTaskSquatBinding.inflate(inflater)

    override fun setupView() {
        super.setupView()
        binding.numberLayout.inputFilterTapPicker = InputFilterMinMax("1", "100")
        binding.numberLayout.setNumber(
            taskSettingModel?.targetValue ?: binding.numberLayout.minValue
        )
        binding.btnPreview.setOnSingleClickListener {
            val task = taskSettingModel ?: return@setOnSingleClickListener
            actionCallback?.onPreview(task.copy(targetValue = binding.numberLayout.getNumber()))
        }

        binding.btnSave.setOnSingleClickListener {
            val task = taskSettingModel ?: return@setOnSingleClickListener
            actionCallback?.onSave(task.copy(targetValue = binding.numberLayout.getNumber()))
        }
    }
}