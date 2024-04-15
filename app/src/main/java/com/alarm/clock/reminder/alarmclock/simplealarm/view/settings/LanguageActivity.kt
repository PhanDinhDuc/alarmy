package com.alarm.clock.reminder.alarmclock.simplealarm.view.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.Settings
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.FragmentLanguageBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.ActionType
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.Language
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.TitleGravity
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.setOnBackClick
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.setOnImageRightClick
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.LocaleHelper
import com.alarm.clock.reminder.alarmclock.simplealarm.view.main.MainActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.settings.adapter.LanguageAdapter
import com.alarm.clock.reminder.alarmclock.simplealarm.view.tutorial.TutorialActivity

class LanguageActivity : BaseActivity<FragmentLanguageBinding>() {

    companion object {
        const val IS_TUTORIAL = "IS_TUTORIAL"
    }

    lateinit var adapter: LanguageAdapter
    override fun makeBinding(layoutInflater: LayoutInflater): FragmentLanguageBinding {
        return FragmentLanguageBinding.inflate(layoutInflater)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)

        val isIntroActivity = intent?.extras?.getBoolean(IS_TUTORIAL) == true
        if (!isIntroActivity) {
            binding.headerLanguage.actionLeftType = ActionType.IMAGE
        } else {
            binding.headerLanguage.actionLeftType = ActionType.NONE
            binding.headerLanguage.titleGravity = TitleGravity.LEFT
            binding.headerLanguage.titleTextSize = 24f
        }
        binding.recLanguages.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        adapter = LanguageAdapter(context = this, Language.getAll()) {
            adapter.setSelected(it.id)
        }

        binding.recLanguages.adapter = adapter

        adapter.setSelected(Settings.APP_LANGUAGE.get(0))


        binding.headerLanguage.setOnBackClick {
            finish()
        }

        binding.headerLanguage.setOnImageRightClick {
            if (isIntroActivity) {
                Settings.APP_LANGUAGE.put(adapter.selectedID)
                Language.setCurrent(adapter.selectedID)
                this.let {
                    LocaleHelper.setLocale(it, Language.current.localizeCode, false)
                    val intent = Intent(it, TutorialActivity::class.java)
                    startActivity(intent)
                    finish()
//                    overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
                }
            } else {
                Settings.APP_LANGUAGE.put(adapter.selectedID)
                Language.setCurrent(adapter.selectedID)
                LocaleHelper.setLocale(this, Language.current.localizeCode, false)
                this.let {
                    val intent = Intent(it, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                    overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
                }
            }
        }
    }
}