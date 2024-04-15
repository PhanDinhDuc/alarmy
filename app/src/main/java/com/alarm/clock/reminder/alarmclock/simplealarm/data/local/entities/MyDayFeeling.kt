package com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.IntRange
import androidx.annotation.StringRes
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

enum class Feeling(@IntRange(0, 5) val id: Int) {
    CHEERY(0), CALM_DOWN(1), UNFEELING(2), SLEEPY(3), TIRED(4), ANGRY(5);


    @get:StringRes
    val stringName: Int
        get() = when (this) {
            CHEERY -> R.string.cheery
            CALM_DOWN -> R.string.calm_down
            UNFEELING -> R.string.unfeeling
            SLEEPY -> R.string.sleepy
            TIRED -> R.string.tired
            ANGRY -> R.string.angry
        }

    @get:DrawableRes
    val icon: Int
        get() = when (this) {
            CHEERY -> R.drawable.ic_cheery
            CALM_DOWN -> R.drawable.ic_calm_down
            UNFEELING -> R.drawable.ic_unfeeling
            SLEEPY -> R.drawable.ic_sleepy
            TIRED -> R.drawable.ic_tired
            ANGRY -> R.drawable.ic_uncomfortable
        }

    companion object {
        fun get(id: Int): Feeling? = values().firstOrNull { it.id == id }
    }
}

@Entity(tableName = "feeling")
@Parcelize
data class MyDayFeeling(
    @ColumnInfo(name = "feeling") @IntRange(0, 5) val feeling: Int,

    @ColumnInfo(name = "date") @PrimaryKey(autoGenerate = false) val date: Long
) : Parcelable

data class MyDayFeelingModel(val feeling: Feeling?, val date: LocalDate)

fun MyDayFeelingModel.map(): MyDayFeeling {
    return MyDayFeeling(feeling?.id ?: 0, date.toEpochDay())
}

fun MyDayFeeling.map(): MyDayFeelingModel {
    return MyDayFeelingModel(getFeeling(), LocalDate.ofEpochDay(date))
}

fun MyDayFeeling.getFeeling(): Feeling? = Feeling.get(feeling)