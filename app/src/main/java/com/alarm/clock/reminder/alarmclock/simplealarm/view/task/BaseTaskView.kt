package com.alarm.clock.reminder.alarmclock.simplealarm.view.task

import android.text.SpannableStringBuilder
import androidx.viewbinding.ViewBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseVMFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewModel
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.parcelable
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.visible
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.action_task.ActionTaskActivity

typealias TaskActionFragment = BaseTaskFragment<*, *>

const val CUR_TASK = "CUR_TASK"

const val TASK_SELECTED = "TASK_SELECTED"

abstract class BaseTaskFragment<B : ViewBinding, VM : BaseViewModel> : BaseVMFragment<B, VM>() {

    abstract val taskType: TaskType
    var taskSettingModel: TaskSettingModel? = null
        private set
    val taskActionCallbackListener: TaskActionCallbackListener?
        get() = (activity as? ActionTaskActivity)?.taskActionCallbackListener

    override fun setupView() {
        super.setupView()
        taskSettingModel = arguments?.parcelable(CUR_TASK)
    }

    fun showHeaderActionTask() {
        val activity = mActivity
        if (activity is ActionTaskActivity) {
            activity.binding.apply {
                header.visible()
                progressBar.visible()
            }
        }
    }

    abstract fun reload()

    abstract fun timeOver()

    override val mActivity: ActionTaskActivity?
        get() = super.mActivity as? ActionTaskActivity

    fun resetTimer() {
        mActivity?.resetTimer()
    }

    fun setTitle(title: Int) {
        mActivity?.binding?.header?.title = title
    }

    fun setTitle(title: SpannableStringBuilder) {
        mActivity?.binding?.header?.titleSpannable = title
    }

}

interface TaskActionCallbackListener {
    fun start()
    fun pass()
    fun fail()

    fun exitPreview()
}
