package com.alarm.clock.reminder.alarmclock.simplealarm.data.local

import android.content.Context
import android.content.SharedPreferences
import com.alarm.clock.reminder.alarmclock.simplealarm.BuildConfig
import com.alarm.clock.reminder.alarmclock.simplealarm.application.MainApplication
import com.alarm.clock.reminder.alarmclock.simplealarm.view.settings.AlarmSetting
import com.google.gson.Gson

enum class PrefMigrate(val newVersion: Int) {
    M_0_1(1), M_1_2(2);

    fun migrate() {
        when (this) {
            M_0_1 -> {
                if (Settings.SP_VERSION.get(0) == 0) {
                    Settings.HOROSCOPE_DATA.put("")
                    Settings.SP_VERSION.put(1)
                }
            }

            M_1_2 -> {
                if (Settings.SP_VERSION.get(0) == 1) {
                    Settings.HOROSCOPE_DATA.put("")
                    Settings.SP_VERSION.put(2)
                }
            }
        }
    }
}


enum class Settings {

    SP_VERSION, PASS_TUTORIAL, APP_LANGUAGE, RATE, VOLUME, ALARM_TEMPLATE,
    ALARM_SETTING, IS_VIBRATE, HOROSCOPE_DATA, DATEOFBIRTH,

    //1 -> male, 2 -> female
    GENDER;


    inline fun <reified T> get(defaultValue: T): T {
        return sharedPref.get(this.name, defaultValue)
    }

    inline fun <reified T> put(value: T) {
        return sharedPref.put(name, value)
    }

    companion object {
        const val VERSION = 2
        val gson = Gson()
        var alarmSettings: AlarmSetting = AlarmSetting()
            get() {
                return gson.fromJson(
                    ALARM_SETTING.get(gson.toJson(field)), AlarmSetting::class.java
                )
            }
            set(value) {
                field = value
                val alarmObjectToString = gson.toJson(value)
                ALARM_SETTING.put(alarmObjectToString)
            }

        val sharedPref: SharedPreferences = MainApplication.CONTEXT.getSharedPreferences(
            BuildConfig.APPLICATION_ID + "_" + SharedPreferences::class.java.simpleName + "_2023",
            Context.MODE_PRIVATE
        )

        fun clear() {
            sharedPref.edit().clear().apply()
        }

        fun migrate() {
            PrefMigrate.values().filter { it.newVersion <= VERSION }.sortedBy { it.newVersion }
                .forEach { it.migrate() }
        }
    }
}

inline fun <reified T> SharedPreferences.get(key: String, defaultValue: T): T {
    when (T::class) {
        Boolean::class -> return this.getBoolean(key, defaultValue as Boolean) as T
        Float::class -> return this.getFloat(key, defaultValue as Float) as T
        Int::class -> return this.getInt(key, defaultValue as Int) as T
        Long::class -> return this.getLong(key, defaultValue as Long) as T
        String::class -> return this.getString(key, defaultValue as String) as T
        else -> {
            if (defaultValue is Set<*>) {
                return this.getStringSet(key, defaultValue as Set<String>) as T
            }
        }
    }

    return defaultValue
}

inline fun <reified T> SharedPreferences.put(key: String, value: T) {
    val editor = this.edit()
    when (T::class) {
        Boolean::class -> editor.putBoolean(key, value as Boolean)
        Float::class -> editor.putFloat(key, value as Float)
        Int::class -> editor.putInt(key, value as Int)
        Long::class -> editor.putLong(key, value as Long)
        String::class -> editor.putString(key, value as String)
        else -> {
            if (value is Set<*>) {
                editor.putStringSet(key, value as Set<String>)
            }
        }
    }

    editor.commit()
}