package com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities

import android.os.Parcelable
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.data.network.fromJson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kotlinx.parcelize.Parcelize
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity(tableName = "reminder")
@Parcelize
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(defaultValue = "0")
    val type: ReminderType = ReminderType.STAR,
    val name: String,
    @ColumnInfo(defaultValue = "")
    val date: LocalDate,
    @ColumnInfo(defaultValue = "")
    val time: LocalTime,
    @ColumnInfo(defaultValue = "")
    val repeatMode: RepeatMode = RepeatMode.None,
    val alertTonePath: String,
    var soundLevel: Int,
    var isVibrate: Boolean,
    val nextEvent: LocalDateTime? = null

) : Parcelable

enum class ReminderType {
    NOTIFICATION,
    HEART,
    BIRTHDAY,
    STAR,
    FLOWER,
    SMS,
    SPORT,
    GIFT,
    CALL,
    WATER,
    MEDICINE,
    EAT,
    SMILE;

    @DrawableRes
    fun getIcon() = when (this) {
        BIRTHDAY -> R.drawable.ic_birthday_cake
        CALL -> R.drawable.ic_call
        HEART -> R.drawable.ic_heart
        MEDICINE -> R.drawable.ic_medicine
        NOTIFICATION -> R.drawable.ic_notification
        SMILE -> R.drawable.ic_smiley
        SPORT -> R.drawable.ic_sport
        STAR -> R.drawable.ic_star
        WATER -> R.drawable.ic_water
        EAT -> R.drawable.ic_cutlery
        FLOWER -> R.drawable.ic_flower
        GIFT -> R.drawable.ic_gift_box
        SMS -> R.drawable.ic_sms
    }

}

object LocalDateTimeConverter {


    @TypeConverter
    fun toDateTime(dateString: String): LocalDateTime {
        return LocalDateTime.parse(dateString)
    }

    @TypeConverter
    fun toDateTimeString(dateTime: LocalDateTime): String {
        return dateTime.toString()
    }

    @TypeConverter
    fun toDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString)
    }

    @TypeConverter
    fun toDateString(date: LocalDate): String {
        return date.toString()
    }

    @TypeConverter
    fun toTime(timeString: String): LocalTime {
        return LocalTime.parse(timeString)
    }

    @TypeConverter
    fun toTimeString(time: LocalTime): String {
        return time.toString()
    }
}

object RepeatModeConverter {

    private val gson = GsonBuilder()
        .registerTypeAdapter(RepeatMode::class.java, SealedClassTypeAdapter())
        .registerTypeAdapter(LocalTime::class.java, LocalTimeTypeAdapter())
        .create()

    @TypeConverter
    fun toRepeatMode(string: String): RepeatMode {
        return gson.fromJson(string)
    }

    @TypeConverter
    fun toJson(repeatMode: RepeatMode): String {
        return gson.toJson(repeatMode)
    }

    @TypeConverter
    fun toType(value: Int) = enumValues<ReminderType>()[value]

    @TypeConverter
    fun fromType(value: ReminderType) = value.ordinal

}

class SealedClassTypeAdapter : JsonSerializer<RepeatMode>, JsonDeserializer<RepeatMode> {

    override fun serialize(
        src: RepeatMode,
        typeOfSrc: Type,
        context: JsonSerializationContext,
    ): JsonElement {
        return context.serialize(src, typeOfSrc)
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type,
        context: JsonDeserializationContext?,
    ): RepeatMode {
        try {
            val jsonObject = json?.asJsonObject
            return context?.deserialize(
                json, when (Repeat.getRepeat(jsonObject?.get("id")?.asInt)) {
                    Repeat.None -> RepeatMode.None::class.java
                    Repeat.Daily -> RepeatMode.Daily::class.java
                    Repeat.Weekly -> RepeatMode.Weekly::class.java
                    Repeat.Monthly -> RepeatMode.Monthly::class.java
                    Repeat.Annually -> RepeatMode.Annually::class.java
                    Repeat.EveryHour -> RepeatMode.EveryHour::class.java
                    Repeat.SeveralTimeInDay -> RepeatMode.SeveralTimeInDay::class.java
                }
            ) ?: RepeatMode.None
        } catch (e: Exception) {
            return RepeatMode.None
        }
    }
}


class LocalTimeTypeAdapter : TypeAdapter<LocalTime>() {
    override fun write(out: JsonWriter, value: LocalTime) {
        out.value(value.toString())
    }

    override fun read(`in`: JsonReader): LocalTime {
        return LocalTime.parse(`in`.nextString())
    }
}


@Parcelize
sealed class RepeatMode(val id: Int) : Parcelable {
    object None : RepeatMode(Repeat.None.ordinal)
    object Daily : RepeatMode(Repeat.Daily.ordinal)
    data class Weekly(val days: List<Int>) : RepeatMode(Repeat.Weekly.ordinal)
    data class Monthly(val months: List<Int>) : RepeatMode(Repeat.Monthly.ordinal)
    object Annually : RepeatMode(Repeat.Annually.ordinal)
    data class EveryHour(val hour: Int) : RepeatMode(Repeat.EveryHour.ordinal)
    data class SeveralTimeInDay(val times: List<LocalTime>) :
        RepeatMode(Repeat.SeveralTimeInDay.ordinal)
}

enum class Repeat {
    None,
    Daily,
    Weekly,
    Monthly,
    Annually,
    EveryHour,
    SeveralTimeInDay;

    companion object {
        fun getRepeat(idx: Int?) = idx?.let { values().getOrNull(it) } ?: None
    }
}

fun Repeat.getString() = when (this) {
    Repeat.Annually -> R.string.repeat_annually
    Repeat.Daily -> R.string.repeat_daily
    Repeat.EveryHour -> R.string.repeat_hour
    Repeat.Monthly -> R.string.repeat_monthly
    Repeat.None -> R.string.repeat_none
    Repeat.SeveralTimeInDay -> R.string.repeat_several
    Repeat.Weekly -> R.string.repeat_weekly
}



