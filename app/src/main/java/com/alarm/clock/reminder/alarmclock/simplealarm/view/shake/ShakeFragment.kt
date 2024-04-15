package com.alarm.clock.reminder.alarmclock.simplealarm.view.shake

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewModel
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.Settings
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.FragmentShakeBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.NonRepeatingLiveData
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.SingleLiveEvent
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.gone
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.visible
import com.alarm.clock.reminder.alarmclock.simplealarm.view.settings.Sensitivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.BaseTaskFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskType
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
class ShakeFragment : BaseTaskFragment<FragmentShakeBinding, ShakeFragmentViewModel>() {

    @Inject
    lateinit var shakeCounter: ShakeCounter

    override val taskType: TaskType = TaskType.SHAKE

    override fun reload() {

    }

    override fun timeOver() {
        viewModel.isComplete.postValue(false)
    }

    override val viewModel by viewModels<ShakeFragmentViewModel>()

    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentShakeBinding = FragmentShakeBinding.inflate(inflater)

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
        shakeCounter.start(
            threshold = when (Settings.alarmSettings.shakeSensitivity) {
                Sensitivity.HIGH -> 10
                Sensitivity.NORMAL -> 11
                Sensitivity.LOW -> 12
            }
        ) { viewModel.insertCounter() }
    }

    override fun onDestroy() {
        super.onDestroy()
        shakeCounter.cancel()
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
class ShakeFragmentViewModel @Inject constructor() : BaseViewModel() {

    var maxCounter: Int = 20
    val counter = NonRepeatingLiveData(0)
    val isComplete = SingleLiveEvent<Boolean>()

    fun insertCounter() {
        val current = counter.value?.plus(1) ?: 1
        if (current == maxCounter) isComplete.postValue(true)
        counter.postValue(current)
    }

}