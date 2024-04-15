package com.alarm.clock.reminder.alarmclock.simplealarm.application

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.alarm.clock.reminder.alarmclock.simplealarm.BuildConfig
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.Settings
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.Language
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.FBConfig
import com.alarm.clock.reminder.alarmclock.simplealarm.view.main.MainViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Singleton

@HiltAndroidApp
@Singleton
class MainApplication : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var CONTEXT: Context

        @SuppressLint("StaticFieldLeak")
        var ACTIVITY_CONTEXT: Context? = null
    }

    var shareCallback: (() -> Unit)? = null
    val currentTabSelected = MutableLiveData(MainViewModel.TabBarItem.ALARM.index)
    var media: MediaPlayer? = null


    override fun onCreate() {
        super.onCreate()
        CONTEXT = this
        Settings.migrate()
        FBConfig.shared.config()
        Language.setCurrent(Settings.APP_LANGUAGE.get(0))
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(!BuildConfig.DEBUG)
    }
}


fun MainApplication.playSound(soundPath: String) {
    media = MediaPlayer.create(this, Uri.parse(soundPath))
    media?.start()
    media?.isLooping = true
}

fun MainApplication.handleSound(isPlaying: Boolean) {
    if (isPlaying) {
        media?.start()
    } else {
        media?.pause()
    }
}

fun MainApplication.stopSound() {
    media?.stop()
    media?.release()
    media = null
}

