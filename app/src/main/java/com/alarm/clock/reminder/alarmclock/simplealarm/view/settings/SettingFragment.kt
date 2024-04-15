package com.alarm.clock.reminder.alarmclock.simplealarm.view.settings

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alarm.clock.reminder.alarmclock.simplealarm.BuildConfig
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.Settings
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.FragmentSettingBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener

class SettingFragment : BaseFragment<FragmentSettingBinding>(), OnRateClickListener {
    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSettingBinding {
        return FragmentSettingBinding.inflate(inflater, container, false)
    }

    override fun setupView() {
        super.setupView()
        binding.btnRateUs.visibility = if (Settings.RATE.get(0) != 0) View.GONE else View.VISIBLE

        binding.btnAlarmSetting.setOnSingleClickListener {
            startActivity(Intent(context, AlarmSettingActivity::class.java))
        }
        binding.btnLanguage.setOnSingleClickListener {
            startActivity(Intent(context, LanguageActivity::class.java))
        }

        binding.btnRateUs.setOnSingleClickListener {
            DialogRatingApp.show(childFragmentManager)
        }

        binding.btnShare.setOnSingleClickListener {
            shareThisApp()
        }

        binding.btnPrivacyPolicy.setOnSingleClickListener {
            getUrlView(BuildConfig.URL_PRIVACY)
        }

    }

    private fun getUrlView(url: String) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(url)
        )
        startActivity(intent)
    }

    private fun shareThisApp() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Note")
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
        )
        startActivity(Intent.createChooser(shareIntent, "Share"))
    }

    override fun onRateClick() {
        binding.btnRateUs.visibility = View.GONE
    }
}