package com.alarm.clock.reminder.alarmclock.simplealarm.view.task.action_task

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.MainApplication
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseVMActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewModel
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.PushType
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.pushTo
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.Settings
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ActivityActionTaskBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.setOnBackClick
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.SingleLiveEvent
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.PREVIEW
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.PREVIEW_SETTING
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.showActivityWhenLockScreen
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.gone
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.visible
import com.alarm.clock.reminder.alarmclock.simplealarm.service.AlarmService
import com.alarm.clock.reminder.alarmclock.simplealarm.view.main.MainActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.priview.AutoDismissAlarm
import com.alarm.clock.reminder.alarmclock.simplealarm.view.priview.PreviewAlarmActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.priview.PreviewAlarmActivity.Companion.listenerCallBack
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.CUR_TASK
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.ListTask
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskActionCallbackListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskActionFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskSettingModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject


@AndroidEntryPoint
class ActionTaskActivity : BaseVMActivity<ActivityActionTaskBinding, ActionTaskViewModel>(),
    PreviewAlarmActivity.CallBackDismissCountDown {

    override val viewModel: ActionTaskViewModel by viewModels()

    override fun makeBinding(layoutInflater: LayoutInflater): ActivityActionTaskBinding {
        return ActivityActionTaskBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showActivityWhenLockScreen()
    }

    fun resetTimer() {
        viewModel.timer = Settings.alarmSettings.taskTimeLimit.duration.toInt()
        binding.clock.start(
            from = viewModel.timer,
            onTick = { tick, progress ->
                binding.progressBar.progress = progress
                viewModel.timer = tick
            }, onFinish = {
                viewModel.isCompleteAllTask.postValue(false)
                visibleFragment()?.timeOver()
            })
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            WindowCompat.setDecorFitsSystemWindows(window, true)
            val controllerCompat = WindowInsetsControllerCompat(window, window.decorView)
            controllerCompat.hide(WindowInsetsCompat.Type.systemBars())
            controllerCompat.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun reset() {
        binding.sound.visible()
        binding.clock.gone()
        binding.progressBar.gone()
        viewModel.timer = Settings.alarmSettings.taskTimeLimit.duration.toInt()
    }

    private fun startClock() {
        if (Settings.alarmSettings.muteDuringMission) {
            AlarmService.setSoundEnable(this, false)
            binding.sound.setImageResource(R.drawable.ic_off_sound)
        }
        binding.progressBar.visible()
        binding.sound.isEnabled = true
        resetTimer()
    }

    val taskActionCallbackListener = object : TaskActionCallbackListener {
        override fun start() {
            startClock()
        }

        override fun pass() {
            viewModel.nextTask()
        }

        override fun fail() {
            viewModel.isCompleteAllTask.postValue(false)
        }

        override fun exitPreview() {
            finishPreview()
        }
    }

    private fun finishPreview() {
        if (intent.extras?.getString(Util.STATUS) == PREVIEW) {
            listener?.onShowToastFinish()
        }
        (application as? MainApplication)?.shareCallback?.invoke()
        finish()
    }

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        activityScope.launch {
            AutoDismissAlarm.onFinish.collectLatest {
                if (it) {
                    val serviceIntent =
                        Intent(this@ActionTaskActivity, AlarmService::class.java)
                    applicationContext.stopService(serviceIntent)
                    finish()
                }
            }
        }

        binding.sound.isEnabled = false
        listenerCallBack = this
        binding.sound.setOnSingleClickListener {
            AlarmService.setSoundEnable(this, false)
            binding.clock.visible()
            binding.sound.gone()
        }
        binding.header.setOnBackClick {
            if (viewModel.currentTaskSettingModel.value?.isPreview == true) {
                if (intent.extras?.getString(Util.STATUS) == PREVIEW_SETTING)
                    (application as? MainApplication)?.shareCallback?.invoke()
            }
            finish()
        }
        viewModel.currentTaskSettingModel.observe(this) {
            pushTo(R.id.action_emptyFragment, anim = PushType.NONE)
            val bundle = bundleOf(CUR_TASK to it)
            pushTo(it.type.actionFragmentId, bundle, PushType.NONE)
            reset()
        }
        viewModel.isCompleteAllTask.observe(this) {
            if (!it) {
                if (viewModel.currentTaskSettingModel.value?.isPreview == true) {
                    finishPreview()
                } else {
                    finish()
                }
            } else {
                val serviceIntent = Intent(this@ActionTaskActivity, AlarmService::class.java)
                applicationContext.stopService(serviceIntent)
                val intent = Intent(this@ActionTaskActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }


    private fun visibleFragment(): TaskActionFragment? {
        val navHostFragment: Fragment? =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        return navHostFragment?.childFragmentManager?.fragments?.firstOrNull() as? TaskActionFragment
    }

    override var navHostId: Int? = R.id.nav_host_fragment
    override fun onDismissCountDown(serviceIntent: Intent) {
        applicationContext.stopService(serviceIntent)
        (application as? MainApplication)?.shareCallback?.invoke()
        finish()
    }

    companion object {
        var listener: ShowToastFinishListener? = null
    }
}

interface ShowToastFinishListener {
    fun onShowToastFinish()
}

@HiltViewModel
class ActionTaskViewModel @Inject constructor(savedStateHandle: SavedStateHandle) :
    BaseViewModel() {

    val currentTaskSettingModel = SingleLiveEvent<TaskSettingModel>()
    val isCompleteAllTask = SingleLiveEvent<Boolean>()
    private var allTask: List<TaskSettingModel>? = null
    private var currentIdx = -1
    var timer = Settings.alarmSettings.taskTimeLimit.duration.toInt()

    companion object {
        const val LIST_TASK = "LIST_TASK"
    }

    init {
        val listTask: ListTask? = savedStateHandle[LIST_TASK]
        allTask = listTask?.list ?: emptyList()
//        allTask = listOf(TaskType.MATH.defaultData, TaskType.MATH.defaultData.copy(level = TaskLevel.EASY))
        nextTask()
    }

    fun nextTask() {
        val allTask = allTask
        if (allTask.isNullOrEmpty()) return
        if (currentIdx < allTask.size - 1) {
            currentIdx++
            loadTask(currentIdx)
        } else {
            isCompleteAllTask.postValue(true)
        }
    }

    private fun loadTask(idx: Int) {
        val task = allTask?.getOrNull(idx)
        if (task != null) {
            currentTaskSettingModel.postValue(task)
        }
    }

}