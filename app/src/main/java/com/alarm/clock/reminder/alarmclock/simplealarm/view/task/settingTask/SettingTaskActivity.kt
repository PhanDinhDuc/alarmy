package com.alarm.clock.reminder.alarmclock.simplealarm.view.task.settingTask

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.PushType
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.pushTo
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Alarm
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ActivitySettingTaskBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.setOnBackClick
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.PREVIEW_SETTING
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.STATUS
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.delay
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.gone
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.parcelable
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.visible
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.SetDataTaskSetting
import com.alarm.clock.reminder.alarmclock.simplealarm.view.priview.PreviewAlarmActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.BaseTaskSettingFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.CUR_TASK
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.ListTask
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskSettingFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskSettingModel
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.action_task.ActionTaskActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.action_task.ActionTaskViewModel.Companion.LIST_TASK
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.action_task.ShowToastFinishListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingTaskActivity : BaseActivity<ActivitySettingTaskBinding>(), ShowToastFinishListener {

    val currentTask: TaskSettingModel?
        get() = visibleFragment()?.taskSettingModel
    var positionTask = -1
    override fun makeBinding(layoutInflater: LayoutInflater): ActivitySettingTaskBinding {
        return ActivitySettingTaskBinding.inflate(layoutInflater)
    }

    val actionCallback = object : BaseTaskSettingFragment.OnActionCallBack {
        override fun onPreview(task: TaskSettingModel) {
            startActivity(Intent(this@SettingTaskActivity, PreviewAlarmActivity::class.java).apply {
                this.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(
                    STATUS,
                    PREVIEW_SETTING
                )
                val alarm: Alarm? = intent.parcelable(Util.ALARM_ARG)
                alarm?.let { putExtra(Util.ALARM_ARG, it) }

                putExtra(
                    LIST_TASK,
                    ListTask(listOf(task.copy(isPreview = true)))
                )
            })
        }

        override fun onSave(task: TaskSettingModel) {
            if (positionTask != -1) {
                SetDataTaskSetting.instance?.updateTaskSetting(positionTask, task)
            } else {
                SetDataTaskSetting.instance?.addTaskSetting(task)
            }
            setResult(RESULT_OK, Intent())
            finish()
        }

    }

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        binding.header.setOnBackClick {
            finish()
        }
        ActionTaskActivity.listener = this
        positionTask = intent.getIntExtra("POSITION_TASK", -1)
        val taskSettingModel: TaskSettingModel? = intent.parcelable(CUR_TASK)
        taskSettingModel?.apply {
            val bundle = bundleOf(CUR_TASK to this)
            pushTo(this.type.settingFragmentId, bundle, PushType.NONE)
            binding.header.title = this.type.title
        } ?: finish()
    }

    private fun visibleFragment(): TaskSettingFragment? {
        val navHostFragment: Fragment? =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        return navHostFragment?.childFragmentManager?.fragments?.firstOrNull() as? TaskSettingFragment
    }

    override var navHostId: Int? = R.id.nav_host_fragment
    override fun onShowToastFinish() {
        binding.tvToast.visible()
        delay(TOAST_DELAY) {
            binding.tvToast.gone()
        }
    }

    companion object {
        const val TOAST_DELAY = 3000L
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
}