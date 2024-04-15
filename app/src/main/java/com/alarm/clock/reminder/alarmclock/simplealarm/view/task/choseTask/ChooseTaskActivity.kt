package com.alarm.clock.reminder.alarmclock.simplealarm.view.task.choseTask

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Alarm
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ActivityChooseTaskBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.setOnBackClick
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.parcelable
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.AddAlarmActivity.Companion.RESULT_FROM_TASK
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.CUR_TASK
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.GroupTaskType
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TASK_SELECTED
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskSettingModel
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.adapter.AdapterChooseTask
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.settingTask.SettingTaskActivity

class ChooseTaskActivity : BaseActivity<ActivityChooseTaskBinding>() {

    override fun makeBinding(layoutInflater: LayoutInflater): ActivityChooseTaskBinding {
        return ActivityChooseTaskBinding.inflate(layoutInflater)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        val alarm: Alarm? = intent.parcelable(Util.ALARM_ARG)
        val selectedTask: TaskSettingModel? = intent.parcelable(TASK_SELECTED)
        val selectedTasks = alarm?.tasks?.map { it.type } ?: emptyList()
        val adapterChooseTask =
            AdapterChooseTask(this, selectedTasks) { type ->
                val i = Intent(this, SettingTaskActivity::class.java).apply {
                    alarm?.let { putExtra(Util.ALARM_ARG, it) }
                    putExtra(
                        CUR_TASK,
                        if (selectedTask?.type == type) selectedTask else type.defaultData
                    )
                    putExtra("POSITION_TASK", intent.getIntExtra("POSITION_TASK", -1))
                }
                chooseTaskResultLauncher.launch(i)
            }
        binding.rcTask.adapter = adapterChooseTask
        val layoutManager = GridLayoutManager(this, 3)
        binding.rcTask.layoutManager = layoutManager
        adapterChooseTask.setLayoutManager(layoutManager)
        adapterChooseTask.notifyDataSetChanged()
        adapterChooseTask.setData(GroupTaskType.values().toList())
        binding.header.setOnBackClick {
            finish()
        }
    }

    private var chooseTaskResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                setResult(RESULT_FROM_TASK, Intent())
                finish()
            }
        }
}