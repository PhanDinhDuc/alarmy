package com.alarm.clock.reminder.alarmclock.simplealarm.view.task

import androidx.viewbinding.ViewBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.parcelable
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.visible
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.settingTask.SettingTaskActivity

typealias TaskSettingFragment = BaseTaskSettingFragment<*>

abstract class BaseTaskSettingFragment<B : ViewBinding> : BaseFragment<B>() {

    abstract val taskType: TaskType
    var taskSettingModel: TaskSettingModel? = null
        private set

    val actionCallback: OnActionCallBack?
        get() = (activity as? SettingTaskActivity)?.actionCallback

    fun showHeaderSettingTask() {
        val activity = requireActivity() as? SettingTaskActivity
        activity?.binding?.header?.visible()
    }

    override fun setupView() {
        super.setupView()
        taskSettingModel = arguments?.parcelable(CUR_TASK)
    }

    interface OnActionCallBack {
        fun onPreview(task: TaskSettingModel)
        fun onSave(task: TaskSettingModel)
    }
}