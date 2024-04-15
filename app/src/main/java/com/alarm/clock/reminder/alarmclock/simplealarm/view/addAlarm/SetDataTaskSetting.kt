package com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm

import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskSettingModel

class SetDataTaskSetting {
    private val MAX_LIST_SIZE = 3
    private val data: MutableList<NewAlarmTakModel> = mutableListOf()

    init {
        addDataDefault()
    }

    fun addTaskSetting(taskSetting: NewAlarmTakModel) {
        if (getTaskSettings().size <= MAX_LIST_SIZE) {
            val index = data.indexOfFirst { it is AddButton }
            if (index >= 0) {
                data.add(index, taskSetting)
            }
        }
    }

    fun getTaskSettings(): List<TaskSettingModel> {
        return data.toList().filterIsInstance(TaskSettingModel::class.java)
    }

    fun getVTaskSettings(): List<NewAlarmTakModel> {
        return if (data.count { it is TaskSettingModel } > 2) getTaskSettings() else data.toList()
    }

    companion object {
        var instance: SetDataTaskSetting? = null
            get() {
                if (field == null) {
                    field = SetDataTaskSetting()
                }
                return field
            }


    }

    fun removeTaskSetting(taskSetting: NewAlarmTakModel): List<TaskSettingModel> {
        val index = data.indexOf(taskSetting)
        if (index >= 0) {
            data.removeAt(index)
        }
        return getTaskSettings()
    }

    fun clearTaskSettings() {
        data.clear()
        addDataDefault()
    }

    private fun addDataDefault() {
        data.add(AddButton())
    }

    fun updateTaskSetting(index: Int, updatedTaskSetting: TaskSettingModel) {
        if (data.size < index) return
        data[index] = updatedTaskSetting
    }
}