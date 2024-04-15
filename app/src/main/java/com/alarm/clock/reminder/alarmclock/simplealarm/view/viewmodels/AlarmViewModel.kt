package com.alarm.clock.reminder.alarmclock.simplealarm.view.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewModel
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.Settings
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Alarm
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.repository.AlarmRepository
import com.alarm.clock.reminder.alarmclock.simplealarm.receiver.helper.AlarmHelper
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class AlarmViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val alarmHelper: AlarmHelper,
    private val gson: Gson,
) :
    BaseViewModel() {

    val allAlarmData: LiveData<List<Alarm>> = alarmRepository.allAlarmData.asLiveData()


    fun insertAlarm(alarm: Alarm) = viewModelScope.launch {
        alarmRepository.insertAlarm(alarm)?.let {
            updateAlarmState(it)
        }
    }

    fun deleteAlarm(alarm: Alarm) = viewModelScope.launch {
        alarmHelper.cancelPendingIntent(alarm)
        alarmHelper.killService(alarm)
        alarmRepository.deleteAlarm(alarm)
    }

    fun updateAlarm(alarm: Alarm) = viewModelScope.launch {
        if (!alarm.isON) {
            alarmHelper.killService(alarm)
        }
        updateAlarmState(alarm)
        alarmRepository.updateAlarm(alarm)
    }

    fun updateAlarmDB(alarm: Alarm) = CoroutineScope(Dispatchers.IO).launch {
        if (!alarm.isON) {
            alarmHelper.killService(alarm)
        }
        alarmRepository.updateAlarm(alarm)
    }

    private fun updateAlarmState(alarm: Alarm) {
        alarmHelper.setAlarm(alarm)
    }

    suspend fun getAlarmById(id: Int): Alarm {
        return alarmRepository.getAlarmById(id)
    }

    fun updateRepeat(id: Int, repeat: Int) = CoroutineScope(Dispatchers.IO).launch {
        alarmRepository.updateRepeat(id, repeat)
    }

    fun snoozeAlarm(alarm: Alarm) = viewModelScope.launch {
        alarmRepository.updateAlarm(alarm)
    }

    fun skipNextAlarm(alarm: Alarm) {
        if (alarm.isSkipNext) {
            val daySkip = alarmHelper.skipNextAlarm(alarm)
            alarm.skipDay = daySkip
            updateAlarm(alarm)
        } else {
            alarm.skipDay = null
            updateAlarm(alarm)
        }
    }


    fun setTemplate(alarm: Alarm) {
        val alarmObjectToString = gson.toJson(alarm)
        Settings.ALARM_TEMPLATE.put(alarmObjectToString)
    }

    fun getTemplate(): Alarm? {
        if (Settings.ALARM_TEMPLATE.get("").isBlank() || Settings.ALARM_TEMPLATE.get("")
                .isEmpty()
        ) return null
        return gson.fromJson(Settings.ALARM_TEMPLATE.get(""), Alarm::class.java).copy(id = -1)
    }
}