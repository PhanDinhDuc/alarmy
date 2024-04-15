package com.alarm.clock.reminder.alarmclock.simplealarm.data.local.repository

import androidx.lifecycle.LiveData
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.dao.DAO
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.BedTimeData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface BedtimeRepository {
    val bedtimeData: LiveData<List<BedTimeData>>
    suspend fun insertBedTime(bedTimeData: BedTimeData)
    suspend fun deleteBebTime(bedTimeData: BedTimeData)
    suspend fun updateBebTime(bedTimeData: BedTimeData)
    suspend fun getBedTimeById(id: Int): BedTimeData
}

class BedtimeRepositoryImpl @Inject constructor(private val dao: DAO) : BedtimeRepository {
    override val bedtimeData: LiveData<List<BedTimeData>> = dao.getBedTime()
    override suspend fun insertBedTime(bedTimeData: BedTimeData) = withContext(Dispatchers.IO) {
        dao.insertBedTime(bedTimeData)
    }

    override suspend fun deleteBebTime(bedTimeData: BedTimeData) = withContext(Dispatchers.IO) {
        dao.deleteBedtime(bedTimeData)
    }

    override suspend fun updateBebTime(bedTimeData: BedTimeData) = withContext(Dispatchers.IO) {
        dao.upDateBedtime(bedTimeData)
    }

    override suspend fun getBedTimeById(id: Int): BedTimeData = withContext(Dispatchers.IO) {
        return@withContext dao.getBedTimeId(id)
    }

}