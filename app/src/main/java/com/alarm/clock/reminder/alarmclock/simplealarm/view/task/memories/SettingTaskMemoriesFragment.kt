package com.alarm.clock.reminder.alarmclock.simplealarm.view.task.memories

import android.view.LayoutInflater
import android.view.ViewGroup
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.FragmentSettingTaskMemoriesBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.InputFilterMinMax
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.OnChangeLevelListener
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.BaseTaskSettingFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskType


class SettingTaskMemoriesFragment : BaseTaskSettingFragment<FragmentSettingTaskMemoriesBinding>(),
    OnChangeLevelListener {
    override val taskType: TaskType = TaskType.MEMORY

    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSettingTaskMemoriesBinding {
        return FragmentSettingTaskMemoriesBinding.inflate(inflater, container, false)
    }

    override fun setupView() {
        super.setupView()
        binding.numberEquationsView.inputFilterTapPicker = InputFilterMinMax("1", "20")
        binding.taskLevelView.mOnChangeListener = this
        setUpExampleMemories()
        binding.numberEquationsView.setNumber(
            taskSettingModel?.repeatTime ?: TaskType.MEMORY.defaultData.repeatTime
        )
        binding.taskLevelView.taskLevel =
            taskSettingModel?.level ?: TaskType.MEMORY.defaultData.level
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
        setUpExampleMemories()
    }

    fun setUpExampleMemories() {
        mOnChangeSelectedSquare?.onChangeSelectedSquare(
            binding.taskLevelView.taskLevel.memories.second,
            binding.taskLevelView.taskLevel.memories.first,
            MemoriesGameView.STATE_SELECTED
        )
    }

    companion object {
        var mOnChangeSelectedSquare: OnChangeSelectedSquareListener? = null

    }
}