package com.alarm.clock.reminder.alarmclock.simplealarm.data.local.repository

import androidx.lifecycle.LiveData
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.dao.ReminderDao
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Reminder
import com.alarm.clock.reminder.alarmclock.simplealarm.receiver.helper.ReminderHelper
import javax.inject.Inject

interface ReminderRepository {
    val reminders: LiveData<List<Reminder>>
    suspend fun insert(reminder: Reminder)
    suspend fun delete(reminderId: Int)
    suspend fun update(reminder: Reminder)
}

class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao,
    private val reminderHelper: ReminderHelper
) : ReminderRepository {

    override val reminders: LiveData<List<Reminder>>
        get() = reminderDao.getAllReminder()

    override suspend fun insert(reminder: Reminder) {
        val id = reminderDao.insertReminder(reminder)
        reminderHelper.createReminder(reminder.copy(id = id.toInt()))
    }

    override suspend fun delete(reminderId: Int) {
        reminderDao.deleteReminder(reminderId)
        reminderHelper.cancelReminderFromId(reminderId)
    }

    override suspend fun update(reminder: Reminder) {
        reminderDao.updateReminder(reminder)
        reminderHelper.updateReminder(reminder)
    }


}