package com.alarm.clock.reminder.alarmclock.simplealarm.data.local

import androidx.annotation.Keep
import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.TimeSoundModel
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskSettingModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject


@ProvidedTypeConverter
@Keep
class AlarmTypeConverters @Inject constructor(private val gson: Gson) {

    @TypeConverter
    fun fromTaskList(tasks: List<TaskSettingModel>?): String {
        val taskList = tasks ?: emptyList()
        val type = object : TypeToken<List<TaskSettingModel>>() {}.type
        return gson.toJson(taskList, type)
    }

    @TypeConverter
    fun toTaskList(taskString: String?): List<TaskSettingModel> {
        if (taskString == null) return emptyList()
        val type = object : TypeToken<List<TaskSettingModel>>() {}.type
        return gson.fromJson(taskString, type)
    }

    @TypeConverter
    fun fromTimeSoundList(tasks: List<TimeSoundModel>?): String {
        val taskList = tasks ?: emptyList()
        val type = object : TypeToken<MutableList<TimeSoundModel>>() {}.type
        return gson.toJson(taskList, type)
    }

    @TypeConverter
    fun toTimeSoundList(timeSoundModelString: String?): List<TimeSoundModel> {
        val type = object : TypeToken<List<TimeSoundModel>>() {}.type
        return gson.fromJson(timeSoundModelString, type)
    }

    @TypeConverter
    fun fromIntegerList(list: List<Int>?): String? {
        if (list == null) return null
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.toJson(list, type)
    }

    @TypeConverter
    fun toIntegerList(data: String?): List<Int>? {
        if (data == null) return null
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(data, type)
    }


}