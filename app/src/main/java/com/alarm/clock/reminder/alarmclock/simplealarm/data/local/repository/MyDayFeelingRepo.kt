package com.alarm.clock.reminder.alarmclock.simplealarm.data.local.repository

import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.dao.FeelingDao
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.MyDayFeelingModel
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

interface MyDayFeelingRepo {

    suspend fun getFeeling(date: LocalDate): MyDayFeelingModel?

    suspend fun insertFeeling(feelingModel: MyDayFeelingModel)
}

class MyDayFeelingRepoImpl @Inject constructor(private val dao: FeelingDao) : MyDayFeelingRepo {
    override suspend fun getFeeling(date: LocalDate): MyDayFeelingModel? =
        withContext(Dispatchers.IO) {
            return@withContext dao.getFeeling(date.toEpochDay()).firstOrNull()?.map()
        }

    override suspend fun insertFeeling(feelingModel: MyDayFeelingModel) =
        withContext(Dispatchers.IO) {
            feelingModel.feeling ?: return@withContext
            dao.addFeeling(feelingModel.map())
        }

}