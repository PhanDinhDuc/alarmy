package com.alarm.clock.reminder.alarmclock.simplealarm.view.task.rewrite

import android.app.Activity
import android.graphics.Color
import android.graphics.Rect
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
import androidx.core.text.color
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewModel
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.FragmentRewritePreviewBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.SingleLiveEvent
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.gone
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.visible
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.addKeyboardToggleListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.BaseTaskFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskLevel
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskType
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RewritePreviewFragment :
    BaseTaskFragment<FragmentRewritePreviewBinding, RewritePreviewViewModel>() {
    override val taskType: TaskType = TaskType.REWRITE
    private var keyboardTriggerBehavior: KeyboardTriggerBehavior? = null
    override fun reload() {
    }

    override fun timeOver() {
//        viewModel.isComplete.postValue(false)
    }

    override val viewModel by viewModels<RewritePreviewViewModel>()
    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRewritePreviewBinding {
        return FragmentRewritePreviewBinding.inflate(layoutInflater)
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
        viewModel.mTaskLevel = taskSettingModel?.level ?: TaskLevel.EASY
        viewModel.maxRound = taskSettingModel?.repeatTime ?: 1

        mActivity?.addKeyboardToggleListener { shown ->
            if (!shown) binding.tvResult.clearFocus()
        }
        mActivity?.let { activity ->
            keyboardTriggerBehavior = KeyboardTriggerBehavior(activity).apply {
                observe(viewLifecycleOwner) {
                    when (it) {
                        KeyboardTriggerBehavior.Status.OPEN -> {}
                        KeyboardTriggerBehavior.Status.CLOSED -> binding.tvResult.clearFocus()
                    }
                }
            }
        }

        viewModel.resultCorrect.observe(this) {
            binding.tvError.visibility = if (it) View.INVISIBLE else View.VISIBLE
            binding.tvResult.background = ContextCompat.getDrawable(
                binding.tvResult.context,
                if (it) R.drawable.bg_edit_text else R.drawable.bg_edit_text_fail
            )
        }

        viewModel.isComplete.observe(this) {
            if (it) taskActionCallbackListener?.pass() else taskActionCallbackListener?.fail()
        }

        viewModel.currentString.observe(this) {
            binding.tvQuestion.text = it
            resetTimer()
//            binding.tvResult.requestFocus()
        }

        viewModel.isNextRound.observe(this) {
            if (it) {
                binding.tvQuestion.text = getString(R.string.next_round)
                binding.tvResult.setText("")
            }
        }

        binding.tvResult.doOnTextChanged { text, start, before, count ->
            Log.d("TAG", "setupQuestion: $text")
            viewModel.compare(text.toString())
        }

        viewModel.title.observe(this) {
            setTitle(it)
        }

        viewModel.nextRound()
        taskActionCallbackListener?.start()
    }
}

@HiltViewModel
class RewritePreviewViewModel @Inject constructor() : BaseViewModel() {
    val currentString = SingleLiveEvent<String>()
    val isComplete = SingleLiveEvent<Boolean>()
    val isNextRound = SingleLiveEvent<Boolean>()
    val resultCorrect = MutableLiveData(true)

    var mTaskLevel: TaskLevel = TaskLevel.EASY
    var round: Int = 0
    var maxRound: Int = 1

    val title = MutableLiveData<SpannableStringBuilder>()

    fun compare(text: String?) {
        if (text.isNullOrEmpty()) {
            return
        }
        if (text.length > currentString.value.toString().length) {
            resultCorrect.postValue(false)
            return
        }
        for (i in text.indices) {
            if (text[i] != currentString.value.toString()[i]) {
                resultCorrect.postValue(false)
            } else {
                resultCorrect.postValue(true)

            }
        }
        val isResultCorrect = text == currentString.value
        if (isResultCorrect) {
            nextRound()
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
            isNextRound.postValue(false)
            currentString.postValue(Util.getRandomString(mTaskLevel.rewrite))
            title.postValue(
                SpannableStringBuilder()
                    .append("$round")
                    .color(Color.parseColor("#94949A")) { append("/$maxRound") })
        }
    }

    private fun isFinish() = round >= maxRound

}

open class KeyboardTriggerBehavior(activity: Activity, val minKeyboardHeight: Int = 0) :
    LiveData<KeyboardTriggerBehavior.Status>() {
    enum class Status {
        OPEN, CLOSED
    }

    val contentView = activity.findViewById<View>(android.R.id.content)

    val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        val displayRect = Rect().apply { contentView.getWindowVisibleDisplayFrame(this) }
        val keypadHeight = contentView.rootView.height - displayRect.bottom
        if (keypadHeight > minKeyboardHeight) {
            setDistinctValue(Status.OPEN)
        } else {
            setDistinctValue(Status.CLOSED)
        }
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in Status>) {
        super.observe(owner, observer)
        observersUpdated()
    }

    override fun observeForever(observer: Observer<in Status>) {
        super.observeForever(observer)
        observersUpdated()
    }

    override fun removeObservers(owner: LifecycleOwner) {
        super.removeObservers(owner)
        observersUpdated()
    }

    override fun removeObserver(observer: Observer<in Status>) {
        super.removeObserver(observer)
        observersUpdated()
    }

    private fun setDistinctValue(newValue: Status) {
        if (value != newValue) {
            value = newValue
        }
    }

    private fun observersUpdated() {
        if (hasObservers()) {
            contentView.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
        } else {
            contentView.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
        }
    }
}