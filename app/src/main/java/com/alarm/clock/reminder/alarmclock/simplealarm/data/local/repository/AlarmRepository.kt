package com.alarm.clock.reminder.alarmclock.simplealarm.data.local.repository

import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.dao.DAO
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Alarm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface AlarmRepository {
    val allAlarmData: Flow<List<Alarm>>
    suspend fun insertAlarm(alarm: Alarm): Alarm?
    suspend fun deleteAlarm(alarm: Alarm)
    suspend fun updateAlarm(alarm: Alarm)
    suspend fun turnOffAlarm(id: Int)
    suspend fun getAlarmById(id: Int): Alarm
    suspend fun updateRepeat(id: Int, repeat: Int)

}

class AlarmRepositoryImpl @Inject constructor(private val dao: DAO) : AlarmRepository {

    override val allAlarmData: Flow<List<Alarm>> = dao.getAllAlarm()

    override suspend fun insertAlarm(alarm: Alarm): Alarm? = withContext(Dispatchers.IO) {
        dao.insertAlarm(alarm)
        return@withContext dao.getAllAlarmSuspend().lastOrNull()
    }

    override suspend fun deleteAlarm(alarm: Alarm) = withContext(Dispatchers.IO) {
        dao.deleteAlarm(alarm)
    }

    override suspend fun updateAlarm(alarm: Alarm) = withContext(Dispatchers.IO) {
        dao.updateAlarm(alarm)
    }

    override suspend fun turnOffAlarm(id: Int) = withContext(Dispatchers.IO) {
        dao.turnOff(id)
    }

    override suspend fun getAlarmById(id: Int): Alarm = withContext(Dispatchers.IO) {
        return@withContext dao.getAlarmById(id)
    }

    override suspend fun updateRepeat(id: Int, repeat: Int) = withContext(Dispatchers.IO) {
        dao.updateRepeatValue(id, repeat)
    }
}