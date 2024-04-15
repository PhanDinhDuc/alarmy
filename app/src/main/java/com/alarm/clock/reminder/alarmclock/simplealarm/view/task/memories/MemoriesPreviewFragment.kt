package com.alarm.clock.reminder.alarmclock.simplealarm.view.task.memories

import android.graphics.Color
import android.os.CountDownTimer
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.color
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewModel
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.FragmentMemoriesPreviewBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.SingleLiveEvent
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.gone
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.visible
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.BaseTaskFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskLevel
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskType
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.memories.MemoriesGameView.Companion.STATE_NONE
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.memories.MemoriesGameView.Companion.STATE_PREVIEW
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MemoriesPreviewFragment :
    BaseTaskFragment<FragmentMemoriesPreviewBinding, MemoriesPreviewViewModel>() {
    override val taskType: TaskType = TaskType.MEMORY
    override fun reload() {
    }

    override fun timeOver() {
        viewModel.isComplete.postValue(false)
    }

    override val viewModel by viewModels<MemoriesPreviewViewModel>()
    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMemoriesPreviewBinding {
        return FragmentMemoriesPreviewBinding.inflate(layoutInflater)
    }

    override fun setupView() {
        super.setupView()
        if (taskSettingModel?.isPreview == true) {
            binding.exitBtn.visible()
            binding.exitBtn.setOnSingleClickListener {
                taskActionCallbackListener?.exitPreview()
            }
        } else binding.exitBtn.gone()

        //get data
        viewModel.maxRound = taskSettingModel?.repeatTime ?: 1
        viewModel.mTaskLevel = taskSettingModel?.level ?: TaskLevel.EASY
        viewModel.nextRound()
        setupQuestion()
        setupObservers()

    }


    private fun setupQuestion() {
        binding.thumbView.setInteractionListener {
            it.toInt().apply {
                binding.thumbView.checkPositionView(this)
                viewModel.compare(this)
            }
        }
    }

    private fun setupObservers() {
        viewModel.isComplete.observe(this) {
            if (it) taskActionCallbackListener?.pass() else taskActionCallbackListener?.fail()
        }

        viewModel.title.observe(this) {
            setTitle(it)
        }

        viewModel.isComplete.observe(this) {
            if (it) taskActionCallbackListener?.pass() else taskActionCallbackListener?.fail()
        }

        viewModel.title.observe(this) {
            setTitle(it)
        }

        viewModel.isEnableView.observe(this) {
            if (it) binding.thumbView.isEnabledTrue() else binding.thumbView.isEnabledFalse()
        }

        viewModel.titleIncorrect.observe(this) {
            binding.tvQuestion.text = getString(it, viewModel.mTaskLevel.memories.second)
        }

        viewModel.currentQuestion.observe(this) {
            if (it) {
                binding.thumbView.apply {
                    setGridView(viewModel.mTaskLevel.memories.first)
                    getRandomSelectedSquare(
                        viewModel.mTaskLevel.memories.second,
                        STATE_PREVIEW
                    )
                    viewModel.currentPositionList = randomPositions.toMutableList()
                }

            } else {
                taskActionCallbackListener?.start()
                binding.thumbView.apply {
                    setBackgroundView(randomPositions, STATE_NONE)
                }

            }
        }

        viewModel.countdownLiveData.observe(this) {
            binding.tvQuestion.text = getString(R.string.keep_in_mind, it + 1)
        }

        viewModel.isNextRound.observe(this) {
            if (it) {
                binding.tvNextRound.visible()
                binding.tvQuestion.gone()
                binding.thumbView.apply {
                    setBackgroundView(
                        viewModel.currentPositionList.toMutableSet(),
                        STATE_NONE
                    )
                }
            } else {
                binding.tvNextRound.gone()
                binding.tvQuestion.visible()
            }
        }

        viewModel.showColorBoxCurrent.observe(this) {
            if (it) {
                binding.thumbView.apply {
                    setBackgroundView(
                        viewModel.currentPositionList.toMutableSet(),
                        STATE_PREVIEW
                    )
                }

            } else {
                viewModel.titleIncorrect.postValue(viewModel.isString)
                viewModel.enteredPositionsList.clear()
            }
        }
    }
}

@HiltViewModel
class MemoriesPreviewViewModel @Inject constructor() : BaseViewModel() {
    val currentQuestion = MutableLiveData<Boolean>()
    val isComplete = SingleLiveEvent<Boolean>()
    var mTaskLevel: TaskLevel = TaskLevel.EASY
    val isNextRound = SingleLiveEvent<Boolean>()
    val title = MutableLiveData<SpannableStringBuilder>()
    val isEnableView = SingleLiveEvent<Boolean>()
    val titleIncorrect = SingleLiveEvent<Int>()

    var currentPositionList = mutableListOf<Int>()
    val enteredPositionsList = mutableListOf<Int>()
    val showColorBoxCurrent = SingleLiveEvent<Boolean>()

    private var consecutiveWrongCount = 0
    private val maxConsecutiveWrong = 2
    var round = 0
    var maxRound = 1

    var isString = R.string.try_again
    val countdownLiveData = MutableLiveData<Int>()
    private var countdownTimer: CountDownTimer? = null
    fun startCountdown() {
        countdownTimer?.cancel()
        countdownTimer = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countdownLiveData.postValue((millisUntilFinished / 1000).toInt())
                isEnableView.postValue(false)
            }

            override fun onFinish() {
                isEnableView.postValue(true)
                currentQuestion.postValue(false)
                isNextRound.postValue(false)
                showColorBoxCurrent.postValue(false)
            }
        }.start()
    }


    fun compare(position: Int) {
        viewModelScope.launch {
            if (position !in currentPositionList) {
                titleIncorrect.postValue(R.string.incorrect)
                delay(500)
                titleIncorrect.postValue(R.string.detects_color_cells)
                consecutiveWrongCount++
                if (consecutiveWrongCount >= maxConsecutiveWrong) {
                    startCountdown()
                    isString = R.string.try_again
                    showColorBoxCurrent.postValue(true)
                    consecutiveWrongCount = 0
                }

            } else {
                enteredPositionsList.add(position)
                if (enteredPositionsList.toSet() == currentPositionList.toSet()) {
                    isEnableView.postValue(false)
                    nextRound()
                }
                consecutiveWrongCount = 0
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
                isNextRound.postValue(true)
                delay(500)
            }
            round++
            currentPositionList.clear()
            enteredPositionsList.clear()
            isNextRound.postValue(false)
            currentQuestion.postValue(true)
            title.postValue(
                SpannableStringBuilder()
                    .append("$round")
                    .color(Color.parseColor("#94949A")) { append("/$maxRound") })
            isString = R.string.detects_color_cells
            startCountdown()
        }
    }

    private fun isFinish() = round >= maxRound
    override fun onCleared() {
        super.onCleared()
        countdownTimer?.cancel()
    }
}
