package com.alarm.clock.reminder.alarmclock.simplealarm.view.step

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewModel
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.FragmentStepBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.NonRepeatingLiveData
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.SingleLiveEvent
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.gone
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.visible
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.BaseTaskFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskType
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
class StepFragment : BaseTaskFragment<FragmentStepBinding, StepFragmentViewModel>() {

    @Inject
    lateinit var stepCounter: StepCounter

    override val taskType: TaskType = TaskType.STEP

    override fun reload() {

    }

    override fun timeOver() {
        viewModel.isComplete.postValue(false)
    }

    override val viewModel by viewModels<StepFragmentViewModel>()

    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentStepBinding = FragmentStepBinding.inflate(inflater)

    override fun setupView() {
        super.setupView()
        if (taskSettingModel?.isPreview == true) {
            binding.exitBtn.visible()
            binding.exitBtn.setOnSingleClickListener {
                taskActionCallbackListener?.exitPreview()
            }
        } else binding.exitBtn.gone()
        val target = taskSettingModel?.targetValue ?: viewModel.maxCounter
        viewModel.maxCounter = target
        taskActionCallbackListener?.start()
        observeView()
        stepCounter.start { viewModel.insertCounter() }
    }

    override fun onDestroy() {
        super.onDestroy()
        stepCounter.cancel()
    }


    private fun observeView() {
        viewModel.isComplete.observe(this) {
            if (it) taskActionCallbackListener?.pass() else taskActionCallbackListener?.fail()
        }
        viewModel.counter.observe(this) {
            if (it == null) return@observe
            binding.counter.text = (viewModel.maxCounter - it).toString()
        }
    }


}

@HiltViewModel
class StepFragmentViewModel @Inject constructor() : BaseViewModel() {

    var maxCounter: Int = 20
    val counter = NonRepeatingLiveData(0)
    val isComplete = SingleLiveEvent<Boolean>()

    fun insertCounter() {
        val current = counter.value?.plus(1) ?: 1
        if (current == maxCounter) isComplete.postValue(true)
        counter.postValue(current)
    }

}