package com.alarm.clock.reminder.alarmclock.simplealarm.view.task.math

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.text.color
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewModel
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.FragmentMathPreviewBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.SingleLiveEvent
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.gone
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.visible
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.BaseTaskFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskLevel
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskType
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MathPreviewFragment :
    BaseTaskFragment<FragmentMathPreviewBinding, MathPreviewViewModel>() {
    override val taskType: TaskType = TaskType.MATH
    override val viewModel by viewModels<MathPreviewViewModel>()

    override fun reload() {

    }

    override fun timeOver() {
        viewModel.isComplete.postValue(false)

    }

    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMathPreviewBinding {
        return FragmentMathPreviewBinding.inflate(inflater)
    }

    override fun setupView() {
        super.setupView()
        viewModel.maxRound = taskSettingModel?.repeatTime ?: 1
        viewModel.mTaskLevel = taskSettingModel?.level ?: TaskLevel.EASY
        viewModel.nextRound()
        if (taskSettingModel?.isPreview == true) {
            binding.exitBtn.visible()
            binding.exitBtn.setOnSingleClickListener {
                taskActionCallbackListener?.exitPreview()
            }
        } else binding.exitBtn.gone()

        viewModel.currentEquation.observe(this) {
            binding.tvCalculation.text = it.string()
            resetTimer()
        }

        viewModel.isComplete.observe(this) {
            if (it) taskActionCallbackListener?.pass() else taskActionCallbackListener?.fail()
        }

        setUpExampleMath()
    }

    private fun setUpExampleMath() {
        taskActionCallbackListener?.start()

        viewModel.isNextRound.observe(this) {
            if (it) {
                binding.tvNextRound.visible()
                binding.round.gone()
                clearText()
                binding.numPadView.isEnabledFalse()
            } else {
                binding.tvNextRound.gone()
                binding.round.visible()
                binding.numPadView.isEnabledTrue()
            }
        }

        viewModel.title.observe(this) {
            setTitle(it)
        }

        viewModel.resultImage.observe(this) {
            binding.tvResultImage.setImageDrawable(
                AppCompatResources.getDrawable(
                    requireContext(),
                    if (it) R.drawable.ic_correct else R.drawable.ic_wrong
                )
            )

            if (!it) {
                clearText()
            }
        }

        viewModel.visibleResultImage.observe(this) {
            binding.tvResult.visibility = if (!it) View.VISIBLE else View.GONE
            binding.tvResultImage.visibility = if (it) View.VISIBLE else View.GONE
        }

        binding.numPadView.setOnInteractionListener(onLeftIconClick = {
            val currentText = binding.tvResult.text
            binding.tvResult.text = currentText.dropLast(1)
        },
            onRightIconClick = {
                viewModel.compare(binding.tvResult.text.toString())
            }, onNewValue = {
                binding.tvResult.text = "${binding.tvResult.text}$it"
            })
    }

    fun clearText() {
        binding.tvResult.text = ""
    }

}

@HiltViewModel
class MathPreviewViewModel @Inject constructor() : BaseViewModel() {
    val currentEquation = MutableLiveData<MathEquation>()
    val isComplete = SingleLiveEvent<Boolean>()
    var mTaskLevel: TaskLevel = TaskLevel.EASY
    val isNextRound = SingleLiveEvent<Boolean>()
    val resultImage = MutableLiveData<Boolean>()
    val visibleResultImage = MutableLiveData<Boolean>()

    val title = MutableLiveData<SpannableStringBuilder>()

    var round = 0
    var maxRound = 1
    fun compare(text: String?) {
        if (text.isNullOrEmpty()) {
            return
        }
        val isResultCorrect = text.toString().toLong() == currentEquation.value?.result()
        resultImage.postValue(isResultCorrect)
        visibleResultImage.postValue(true)
        if (isResultCorrect) {
            nextRound()
        } else {
            viewModelScope.launch {
                delay(500)
                visibleResultImage.postValue(false)
            }
        }
    }

    fun nextRound() {
        viewModelScope.launch {
            if (isFinish()) {
                delay(500)
                isComplete.postValue(true)
                return@launch
            }

            if (round != 0) {
                delay(500)
                visibleResultImage.postValue(false)
                isNextRound.postValue(true)
                delay(500)
            }
            round++
            currentEquation.postValue(mTaskLevel.math)
            isNextRound.postValue(false)
            title.postValue(SpannableStringBuilder()
                .append("$round")
                .color(Color.parseColor("#94949A")) { append("/$maxRound") })
        }
    }

    private fun isFinish() = round >= maxRound

}