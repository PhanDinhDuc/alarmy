package com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.IntRange
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

enum class Weather(@IntRange(0, 5) val id: Int) {
    SUNNY(0), WINDY(1), CLOUDY(2), HEAVY_RAIN(3), SLIGHTLY_RAIN(4), THUNDER_STORM(5);

    @get:DrawableRes
    val icon: Int
        get() = when (this) {
            SUNNY -> R.drawable.ic_weather_0
            WINDY -> R.drawable.ic_weather_1
            CLOUDY -> R.drawable.ic_weather_2
            HEAVY_RAIN -> R.drawable.ic_weather_3
            SLIGHTLY_RAIN -> R.drawable.ic_weather_4
            THUNDER_STORM -> R.drawable.ic_weather_5
        }

    @get:DrawableRes
    val iconSelect: Int
        get() = when (this) {
            SUNNY -> R.drawable.ic_weather_sel0
            WINDY -> R.drawable.ic_weather_sel1
            CLOUDY -> R.drawable.ic_weather_sel2
            HEAVY_RAIN -> R.drawable.ic_weather_sel3
            SLIGHTLY_RAIN -> R.drawable.ic_weather_sel4
            THUNDER_STORM -> R.drawable.ic_weather_sel5
        }

    companion object {
        fun get(id: Int): Weather? = Weather.values().firstOrNull { it.id == id }
    }
}

enum class Plan(@IntRange(0, 5) val id: Int) {
    WORKING(0), FRIEND(1), FAMILY(2), HOLIDAY(3),
    PARTY(4), EXERCISE(5), PLAY_WITH_PET(6), READING(7),
    SLEEP(8), PLAY_GAME(9), YOGA(10), STUDY(11), CHAT(12),
    AIRPLANE(14), DRINK(15);

    @get:DrawableRes
    val icon: Int
        get() = when (this) {
            WORKING -> R.drawable.ic_plan_0
            FRIEND -> R.drawable.ic_plan_1
            FAMILY -> R.drawable.ic_plan_2
            HOLIDAY -> R.drawable.ic_plan_3
            PARTY -> R.drawable.ic_plan_4
            EXERCISE -> R.drawable.ic_plan_5
            PLAY_WITH_PET -> R.drawable.ic_plan_6
            READING -> R.drawable.ic_plan_7
            SLEEP -> R.drawable.ic_plan_8
            PLAY_GAME -> R.drawable.ic_plan_9
            YOGA -> R.drawable.ic_plan_10
            STUDY -> R.drawable.ic_plan_11
            CHAT -> R.drawable.ic_plan_12
            AIRPLANE -> R.drawable.ic_plan_13
            DRINK -> R.drawable.ic_plan_14
        }

    @get:DrawableRes
    val iconSelect: Int
        get() = when (this) {
            WORKING -> R.drawable.ic_plan_sel0
            FRIEND -> R.drawable.ic_plan_sel1
            FAMILY -> R.drawable.ic_plan_sel2
            HOLIDAY -> R.drawable.ic_plan_sel3
            PARTY -> R.drawable.ic_plan_sel4
            EXERCISE -> R.drawable.ic_plan_sel5
            PLAY_WITH_PET -> R.drawable.ic_plan_sel6
            READING -> R.drawable.ic_plan_sel7
            SLEEP -> R.drawable.ic_plan_sel8
            PLAY_GAME -> R.drawable.ic_plan_sel9
            YOGA -> R.drawable.ic_plan_sel10
            STUDY -> R.drawable.ic_plan_sel11
            CHAT -> R.drawable.ic_plan_sel12
            AIRPLANE -> R.drawable.ic_plan_sel13
            DRINK -> R.drawable.ic_plan_sel14
        }

    @get:DrawableRes
    val iconDef: Int
        get() = when (this) {
            WORKING -> R.drawable.ic_plan_def_14
            FRIEND -> R.drawable.ic_plan_def_6
            FAMILY -> R.drawable.ic_plan_def_5
            HOLIDAY -> R.drawable.ic_plan_def_3
            PARTY -> R.drawable.ic_plan_def_9
            EXERCISE -> R.drawable.ic_plan_def_13
            PLAY_WITH_PET -> R.drawable.ic_plan_def_10
            READING -> R.drawable.ic_plan_def_11
            SLEEP -> R.drawable.ic_plan_def_12
            PLAY_GAME -> R.drawable.ic_plan_def_7
            YOGA -> R.drawable.ic_plan_def_8
            STUDY -> R.drawable.ic_plan_def_4
            CHAT -> R.drawable.ic_plan_def_1
            AIRPLANE -> R.drawable.ic_plan_def_0
            DRINK -> R.drawable.ic_plan_def_2
        }

    companion object {
        fun get(id: Int): Plan? = Plan.values().firstOrNull { it.id == id }
    }
}

@Entity(tableName = "diary")
@Parcelize
data class MyDayDiary(
    @ColumnInfo(name = "date") @PrimaryKey(autoGenerate = false) val date: Long,
    @ColumnInfo(name = "weather") @IntRange(0, 5) val weather: Int?,
    @ColumnInfo(name = "weather_detail") val weatherDetail: String,
    @ColumnInfo(name = "plan") val plan: String,
    @ColumnInfo(name = "plan_detail") val planDetail: String,
) : Parcelable

data class MyDayDiaryModel(
    val date: LocalDate = LocalDate.now(),
    val weather: Weather? = null,
    val weatherDetail: String = "",
    val plan: List<Plan> = emptyList(),
    val planDetail: String = ""
)

fun MyDayDiaryModel.map(): MyDayDiary {
    return MyDayDiary(
        date.toEpochDay(),
        weather?.id,
        weatherDetail,
        plan.map { it.id }.joinToString("||"),
        planDetail
    )
}

fun MyDayDiary.map(): MyDayDiaryModel {
    return MyDayDiaryModel(
        LocalDate.ofEpochDay(date),
        Weather.get(weather ?: -1),
        weatherDetail,
        if (plan.isNotEmpty()) plan.split("||").mapNotNull { Plan.get(it.toInt()) } else emptyList(),
        planDetail
    )
}