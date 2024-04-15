package com.alarm.clock.reminder.alarmclock.simplealarm.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.MyDayDiary

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary WHERE date = :date")
    suspend fun get(date: Long): List<MyDayDiary>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(model: MyDayDiary)
}