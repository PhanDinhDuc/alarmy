package com.alarm.clock.reminder.alarmclock.simplealarm.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.MyDayFeeling

@Dao
interface FeelingDao {
    @Query("SELECT * FROM feeling WHERE date = :date")
    suspend fun getFeeling(date: Long): List<MyDayFeeling>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFeeling(feeling: MyDayFeeling)
}