package com.alarm.clock.reminder.alarmclock.simplealarm.view.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseVMActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.Settings
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ActivitySplashBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.FBConfig
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.delay
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.goToStore
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.showSingleActionAlert
import com.alarm.clock.reminder.alarmclock.simplealarm.view.bedtime.BedTimeSettingViewModel
import com.alarm.clock.reminder.alarmclock.simplealarm.view.main.MainActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.settings.LanguageActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.settings.LanguageActivity.Companion.IS_TUTORIAL
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    override fun makeBinding(layoutInflater: LayoutInflater): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        if (FBConfig.isForceUpdate()) {
            showSingleActionAlert(
                getString(R.string.new_version),
                getString(R.string.new_version_msg),
                getString(R.string.update)
            ) {
                goToStore()
                finish()
            }
        } else nextScreen()
    }

    private fun nextScreen() {
        if (Settings.PASS_TUTORIAL.get(false)) {
            delay(timeMillis) {
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        } else {
            delay(timeMillis) {
                val intent =
                    Intent(this@SplashActivity, LanguageActivity::class.java).apply {
                        putExtra(IS_TUTORIAL, true)
                    }
                startActivity(intent)
                finish()
            }
        }
    }

    companion object {
        const val timeMillis = 2000L
    }
}