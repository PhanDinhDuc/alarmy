package com.alarm.clock.reminder.alarmclock.simplealarm.view.task.rewrite

import android.view.LayoutInflater
import android.view.ViewGroup
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.FragmentSettingTaskRewriteBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.InputFilterMinMax
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.OnChangeLevelListener
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.getRandomString
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.BaseTaskSettingFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskType

class SettingTaskRewriteFragment : BaseTaskSettingFragment<FragmentSettingTaskRewriteBinding>(),
    OnChangeLevelListener {
    override val taskType: TaskType = TaskType.REWRITE

    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSettingTaskRewriteBinding {
        return FragmentSettingTaskRewriteBinding.inflate(inflater, container, false)
    }

    override fun setupView() {
        super.setupView()
        binding.numberEquationsView.inputFilterTapPicker = InputFilterMinMax("1", "20")
        binding.taskLevelView.mOnChangeListener = this
        setupExampleRewrite()
        binding.numberEquationsView.setNumber(
            taskSettingModel?.repeatTime ?: TaskType.REWRITE.defaultData.repeatTime
        )
        binding.taskLevelView.taskLevel =
            taskSettingModel?.level ?: TaskType.REWRITE.defaultData.level
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
        setupExampleRewrite()
    }

    fun setupExampleRewrite() {
        val textExample = getRandomString(binding.taskLevelView.taskLevel.rewrite)
        binding.tvExample.text = textExample
    }

    companion object {
        val ALLOWED_CHARACTERS = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        const val TIME_DELAY = 2000
    }

}