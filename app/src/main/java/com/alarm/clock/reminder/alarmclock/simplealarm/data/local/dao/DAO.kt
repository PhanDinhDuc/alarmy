package com.alarm.clock.reminder.alarmclock.simplealarm.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Alarm
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.BedTimeData
import kotlinx.coroutines.flow.Flow

@Dao
interface DAO {

    //query alarm data
    @Query("SELECT * FROM alarm ORDER BY id DESC")
    fun getAllAlarm(): Flow<List<Alarm>>

    @Query("SELECT * FROM alarm")
    suspend fun getAllAlarmSuspend(): List<Alarm>

    @Query("SELECT * FROM alarm WHERE id = :id")
    suspend fun getAlarmById(id: Int): Alarm

    @Query("UPDATE alarm SET repeat = :repeat WHERE id = :id")
    suspend fun updateRepeatValue(id: Int, repeat: Int)

    @Insert(entity = Alarm::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: Alarm)

    @Query("UPDATE alarm SET isON = 1 WHERE id = :id")
    suspend fun turnOff(id: Int)

    @Delete(entity = Alarm::class)
    suspend fun deleteAlarm(alarm: Alarm)

    @Update(entity = Alarm::class)
    suspend fun updateAlarm(alarm: Alarm)

    //query bedtime data
    @Query("SELECT * FROM bedtime")
    fun getBedTime(): LiveData<List<BedTimeData>>

    @Query("SELECT COUNT(*) FROM bedtime")
    fun getCountBedtime(): Flow<Int>

    @Query("SELECT * FROM bedtime WHERE id = :id")
    suspend fun getBedTimeId(id: Int): BedTimeData

    @Insert(entity = BedTimeData::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBedTime(bedTimeData: BedTimeData)

    @Update(entity = BedTimeData::class)
    suspend fun upDateBedtime(bedTimeData: BedTimeData)

    @Delete(entity = BedTimeData::class)
    suspend fun deleteBedtime(bedTimeData: BedTimeData)


}