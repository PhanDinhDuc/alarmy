package com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities

import android.content.Context
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.AlarmTypeConverters
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.Settings
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.returnDaySelectString1
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.Snooze
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.dialog.DialogItemSetting
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskSettingModel
import kotlinx.parcelize.Parcelize
import java.util.Calendar

@Entity(tableName = "alarm")
@Parcelize
@Keep
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "label")
    var label: String? = null,

    //24h time
    @ColumnInfo(name = "time")
    var time: String,

    @ColumnInfo(name = "listDay_txt")
    var listDays_txt: String,

    @ColumnInfo(name = "isON")
    var isON: Boolean,

    @ColumnInfo(name = "snooze")
    var snooze: Int = 1,

    @ColumnInfo(name = "repeat")
    var repeat: Int = 0,

    @TypeConverters(AlarmTypeConverters::class)
    @ColumnInfo(name = "day")
    var days: List<Int>?,

    @TypeConverters(AlarmTypeConverters::class)
    @ColumnInfo(name = "task")
    var tasks: List<TaskSettingModel>?,

    @ColumnInfo(name = "soundPath")
    var soundPath: String,

    @ColumnInfo(name = "sound_level")
    var soundLevel: Int,

    @ColumnInfo(name = "is_Vibrate")
    var isVibrate: Boolean,

    @ColumnInfo(name = "skip_next")
    var isSkipNext: Boolean,

    @ColumnInfo(name = "is_quick_alarm")
    var isQuickAlarm: Boolean,

    @ColumnInfo(name = "skip_day")
    var skipDay: Int? = null


) : Parcelable {
    companion object {
        const val NO_ID = 0
    }
}

fun Alarm.getSortDay(): List<Int>? {
    return days?.sortDate()
}

fun List<Int>.sortDate(): List<Int> {
    val tmp = this.toMutableSet()
    if (tmp.contains(1)) {
        tmp.remove(1)
        tmp.add(1)
    }
    return tmp.toList()
}

fun Alarm.getDayTxt(context: Context): String {
    return returnDaySelectString1(context, days?.toSet() ?: emptySet())
}

fun Alarm.hasSnooze(): Boolean {
    val alarmSnooze = snooze != Snooze.OFF.duration.toInt()
    val settingSnooze =
        Settings.alarmSettings.snoozeLimit == DialogItemSetting.Duration.UNLIMITED ||
                repeat < Settings.alarmSettings.snoozeLimit.duration
    return alarmSnooze && settingSnooze
}

fun List<Alarm>.sortedByTime(): List<Alarm> {
    return this.sortedWith(compareBy({ !it.isON }, { getNextAlarm(it) }))
}

private fun getNextAlarm(alarm: Alarm): Calendar {
    val now = Calendar.getInstance()
    return alarm.days.orEmpty().map { day ->
        getAlarmCalendar(alarm.time, day).also { alarmTime ->
            while (alarmTime.before(now)) {
                alarmTime.add(Calendar.DAY_OF_YEAR, 7)
            }
        }
    }.minByOrNull { it.timeInMillis } ?: now
}


private fun getAlarmCalendar(time: String, dayOfWeek: Int): Calendar {
    val timeParts = time.split(":")

    return Calendar.getInstance().apply {
        var hour = timeParts[0].toInt()
        if (hour >= 12) {
            hour -= 12
        }
        set(Calendar.DAY_OF_WEEK, dayOfWeek)
        set(Calendar.HOUR, hour)
        set(Calendar.MINUTE, timeParts[1].toInt())
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        val isAM = timeParts[2]
        if (isAM == "AM") {
            set(Calendar.AM_PM, Calendar.AM)
        } else {
            set(Calendar.AM_PM, Calendar.PM)
        }
    }
}





