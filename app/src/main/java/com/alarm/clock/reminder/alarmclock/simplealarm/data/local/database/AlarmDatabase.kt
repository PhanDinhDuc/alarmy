package com.alarm.clock.reminder.alarmclock.simplealarm.data.local.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.AlarmTypeConverters
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.dao.DAO
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.dao.DiaryDao
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.dao.FeelingDao
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.dao.ReminderDao
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Alarm
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.BedTimeData
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.LocalDateTimeConverter
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.MyDayDiary
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.MyDayFeeling
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Reminder
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.RepeatModeConverter

@Database(
    entities = [Alarm::class, BedTimeData::class, Reminder::class, MyDayFeeling::class, MyDayDiary::class],
    version = AlarmDatabase.LATEST_VERSION,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 3, to = 5),
        AutoMigration(from = 4, to = 5)
    ]
)

@TypeConverters(
    value = [
        (AlarmTypeConverters::class),
        (LocalDateTimeConverter::class),
        (RepeatModeConverter::class)
    ]
)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun dao(): DAO
    abstract fun reminderDao(): ReminderDao

    abstract fun mydayDao(): FeelingDao
    abstract fun mydayDiaryDao(): DiaryDao

    companion object {
        const val LATEST_VERSION = 5
    }

}