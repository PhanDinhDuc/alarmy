package com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.MainApplication
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.AlarmTypeConverters
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.TIME_DEFAULT
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.Snooze
import com.alarm.clock.reminder.alarmclock.simplealarm.view.bedtime.DayInWeek
import com.alarm.clock.reminder.alarmclock.simplealarm.view.bedtime.data.SoundBedtime
import kotlinx.parcelize.Parcelize

@Entity(tableName = "bedtime")
@Parcelize
@Keep
data class BedTimeData(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "isOn")
    var isOn: Boolean,

    @ColumnInfo(name = "sound")
    var sound: String? = null,

    @ColumnInfo(name = "sound_name")
    var soundName: String? = null,

    @ColumnInfo(name = "duration")
    var timeSound: Long,

    @ColumnInfo(name = "sound_level")
    var soundLevel: Int,

    @TypeConverters(AlarmTypeConverters::class)
    @ColumnInfo(name = "timeSetSound")
    var listTimes: List<TimeSoundModel>,

    @ColumnInfo(name = "txt_day_string")
    var txt_Day: String,


    ) : Parcelable {

    companion object {
        private val listTimeSoundModels = mutableListOf(
            TimeSoundModel(DayInWeek.MONDAY.day, TimeSoundModel.timeDefault),
            TimeSoundModel(DayInWeek.TUESDAY.day, TimeSoundModel.timeDefault),
            TimeSoundModel(DayInWeek.WEDNESDAY.day, TimeSoundModel.timeDefault),
            TimeSoundModel(DayInWeek.THURSDAY.day, TimeSoundModel.timeDefault),
            TimeSoundModel(DayInWeek.FRIDAY.day, TimeSoundModel.timeDefault),
            TimeSoundModel(DayInWeek.SATURDAY.day, TimeSoundModel.timeDefault),
            TimeSoundModel(DayInWeek.SUNDAY.day, TimeSoundModel.timeDefault)
        )

        val Default = BedTimeData(
            1,
            true,
            "android.resource://com.alarm.clock.reminder.alarmclock.simplealarm/${
                SoundBedtime.listSound().firstOrNull()?.soundRaw
            }",
            (SoundBedtime.listSound().firstOrNull()?.id ?: 0).toString(),
            Snooze.M10.duration,
            50,
            listTimeSoundModels,
            MainApplication.CONTEXT.getString(R.string.everyDay)
        )
    }
}


@Parcelize
@Keep
data class TimeSoundModel(
    var day: Int,
    var time: String
) : Parcelable {
    companion object {
        const val timeDefault = TIME_DEFAULT
    }
}