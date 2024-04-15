package com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils

import com.alarm.clock.reminder.alarmclock.simplealarm.BuildConfig
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

class FBConfig private constructor() {

    private val remoteConfig = Firebase.remoteConfig
    fun config() {
        remoteConfig.setConfigSettingsAsync(remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        })

        remoteConfig.setDefaultsAsync(
            mapOf(
//                key_display_language to false,
//                key_display_intro to true,
                app_version to BuildConfig.VERSION_NAME
            )
        )

        remoteConfig.fetchAndActivate()
    }

//    fun getDisplayLanguage(): Boolean {
//        return remoteConfig.getBoolean(key_display_language)
//    }
//
//    fun getDisplayIntro(): Boolean {
//        return remoteConfig.getBoolean(key_display_intro)
//    }

    fun getAppVersion(): String {
        return remoteConfig.getString(app_version)
    }

    private fun isNeedUpdateAppVersions(version1: String, version2: String): Boolean {
        try {
            val parts1 = version1.split(".")
            val parts2 = version2.split(".")

            val minSize = minOf(parts1.size, parts2.size)

            for (i in 0 until minSize) {
                val part1 = parts1[i].toInt()
                val part2 = parts2[i].toInt()

                if (part1 < part2) {
                    return true
                } else if (part1 > part2) {
                    return false
                }
            }

            if (parts1.size < parts2.size) {
                return true
            } else if (parts1.size > parts2.size) {
                return false
            }

            return false
        } catch (e: Exception) {
            return false
        }
    }

    companion object {
        val shared = FBConfig()
        fun isForceUpdate(): Boolean {
            return shared.isNeedUpdateAppVersions(BuildConfig.VERSION_NAME, shared.getAppVersion())
        }

        const val app_version = "app_version"
//        const val key_display_language = "display_language"
//        const val key_display_intro = "display_intro"
    }
}

