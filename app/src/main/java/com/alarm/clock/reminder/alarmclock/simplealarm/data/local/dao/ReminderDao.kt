package com.alarm.clock.reminder.alarmclock.simplealarm.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Reminder

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminder")
    fun getAllReminder(): LiveData<List<Reminder>>

    @Query("SELECT * FROM reminder")
    suspend fun getAll(): List<Reminder>

    @Query("SELECT * FROM reminder WHERE id = :id")
    suspend fun getReminderById(id: Int): Reminder

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Query("DELETE FROM reminder WHERE id = :reminderId")
    suspend fun deleteReminder(reminderId: Int)

    @Update
    suspend fun updateReminder(reminder: Reminder)


}