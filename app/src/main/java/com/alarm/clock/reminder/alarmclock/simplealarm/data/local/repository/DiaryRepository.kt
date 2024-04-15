package com.alarm.clock.reminder.alarmclock.simplealarm.data.local.repository

import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.dao.DiaryDao
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.MyDayDiaryModel
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

interface DiaryRepository {

    suspend fun get(date: LocalDate): MyDayDiaryModel?

    suspend fun insert(model: MyDayDiaryModel)
}

class DiaryRepositoryImpl @Inject constructor(private val dao: DiaryDao) : DiaryRepository {
    override suspend fun get(date: LocalDate): MyDayDiaryModel? =
        withContext(Dispatchers.IO) {
            return@withContext dao.get(date.toEpochDay()).firstOrNull()?.map()
        }

    override suspend fun insert(model: MyDayDiaryModel) = withContext(Dispatchers.IO) {
        dao.add(model.map())
    }

}