package com.alarm.clock.reminder.alarmclock.simplealarm.view.task.squat

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewModel
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.FragmentSquatBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.NonRepeatingLiveData
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.SingleLiveEvent
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.gone
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.hidden
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnHoldInView
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.visible
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.BaseTaskFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskType
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SquatFragment : BaseTaskFragment<FragmentSquatBinding, SquatFragmentViewModel>() {

    @Inject
    lateinit var squatCounter: SquatCounter

    override val taskType: TaskType = TaskType.SQUAT

    override fun reload() {

    }

    override fun timeOver() {
        viewModel.isComplete.postValue(false)
    }

    override val viewModel by viewModels<SquatFragmentViewModel>()

    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSquatBinding = FragmentSquatBinding.inflate(inflater)

    override fun setupView() {
        super.setupView()
        Log.d("AAA", "setupView:$taskSettingModel")
        if (taskSettingModel?.isPreview == true) {
            binding.exitBtn.visible()
            binding.exitBtn.setOnSingleClickListener {
                taskActionCallbackListener?.exitPreview()
            }
        } else binding.exitBtn.gone()
        val target = taskSettingModel?.targetValue ?: viewModel.maxCounter
        viewModel.maxCounter = target
        observeViewStart()
        observeView()
        viewModel.isComplete.observe(this) {
            if (it) taskActionCallbackListener?.pass() else taskActionCallbackListener?.fail()
        }
    }

    private fun observeViewStart() {
        viewModel.isStart.observe(this) {
            if (it) {
                taskActionCallbackListener?.start()
                binding.ready.gone()
                binding.start.visible()
            } else {
                binding.start.gone()
                binding.ready.visible()
                binding.countDown.start(
                    from = viewModel.timer,
                    onTick = { tick, _ -> viewModel.timer = tick }) {
                    viewModel.isStart.postValue(true)
                }
                binding.startBtn.setOnSingleClickListener {
                    binding.countDown.stop()
                    viewModel.isStart.postValue(true)
                }
            }
        }
    }

    private fun observeView() {
        binding.leftFigure.setOnHoldInView { isHold ->
            viewModel.isHoldLeft.postValue(isHold)
        }
        binding.rightFigure.setOnHoldInView { isHold ->
            viewModel.isHoldRight.postValue(isHold)
        }

        viewModel.isHoldLeft.observe(this) {
            val isHold = it == true
            binding.leftFigure.setImageResource(if (isHold) R.drawable.hand_left_blue else R.drawable.hand_left)
            viewModel.isHoldBoth.postValue(isHold && viewModel.isHoldRight.value == true)
        }
        viewModel.isHoldRight.observe(this) {
            val isHold = it == true
            binding.rightFigure.setImageResource(if (isHold) R.drawable.hand_right_blue else R.drawable.hand_right)
            viewModel.isHoldBoth.postValue(isHold && viewModel.isHoldLeft.value == true)
        }
        viewModel.isHoldBoth.observe(this) {
            val isHold = it == true
            Log.d("AAA", "observeView:$isHold")
            binding.text.text =
                getString(if (isHold) R.string.start_squat else R.string.hold_with_both_hands)
            if (isHold) binding.counter.visible() else binding.counter.hidden()
            if (isHold) squatCounter.start { viewModel.insertCounter() }
            else squatCounter.cancel()
        }
        viewModel.counter.observe(this) {
            if (it == null) return@observe
            binding.counter.text = (viewModel.maxCounter - it).toString()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        squatCounter.cancel()
    }
}

@HiltViewModel
class SquatFragmentViewModel @Inject constructor() : BaseViewModel() {

    var maxCounter: Int = 5
    var timer = 3
    val isStart = NonRepeatingLiveData(false)
    val isHoldLeft = NonRepeatingLiveData(false)
    val isHoldRight = NonRepeatingLiveData(false)
    val isHoldBoth = NonRepeatingLiveData(false)
    val counter = NonRepeatingLiveData(0)
    val isComplete = SingleLiveEvent<Boolean>()

    fun insertCounter() {
        val current = counter.value?.plus(1) ?: 1
        if (current == maxCounter) isComplete.postValue(true)
        counter.postValue(current)
    }

}