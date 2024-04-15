package com.alarm.clock.reminder.alarmclock.simplealarm.view.main

import android.app.NotificationManager
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.MainApplication
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseVMActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewModel
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Reminder
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ActivityMainTabbarBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.receiver.helper.ReminderHelper.Companion.REMINDER_DATA
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.parcelable
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.AddAlarmActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.OnShowToastInsertNewAlarmListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.dialog.DialogQuickAlarm
import com.alarm.clock.reminder.alarmclock.simplealarm.view.bedtime.BedTimeFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.view.home.rotationImageAdd
import com.alarm.clock.reminder.alarmclock.simplealarm.view.myday.MyDayFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.view.reminder.AddReminderActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.reminder.AddReminderViewModel
import com.alarm.clock.reminder.alarmclock.simplealarm.view.reminder.ReminderFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.view.settings.SettingFragment
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : BaseVMActivity<ActivityMainTabbarBinding, MainViewModel>(),
    OnShowToastInsertNewAlarmListener {
    override val viewModel: MainViewModel by viewModels()
    override fun makeBinding(layoutInflater: LayoutInflater): ActivityMainTabbarBinding {
        return ActivityMainTabbarBinding.inflate(layoutInflater)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.parcelable<Reminder>(REMINDER_DATA)?.let {
            startActivity(Intent(this, AddReminderActivity::class.java).apply {
                putExtra(AddReminderViewModel.REMINDER_KEY, it)
            })
        }
    }

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        intent.parcelable<Reminder>(REMINDER_DATA)?.let {
            startActivity(Intent(this, AddReminderActivity::class.java).apply {
                putExtra(AddReminderViewModel.REMINDER_KEY, it)
            })
        }
        initViewPager()
        setupAddAlarm()
    }

    private fun setupAddAlarm() {
        viewModel.showAddView.observe(this) {
            binding.groupAdd.visibility = if (it) View.VISIBLE else View.GONE
            binding.backgroundMask.isClickable = it
        }

        binding.imageAddAlarm.setOnSingleClickListener {
            when (binding.viewPager.findCurrentFragment(supportFragmentManager)) {
                is AlarmFragment -> {
                    it?.rotationImageAdd(45f)
                    viewModel.showAddView.postValue(viewModel.showAddView.value?.not())
                }

                is ReminderFragment -> {
                    startActivity(Intent(this, AddReminderActivity::class.java))
                }

                else -> {}
            }

        }

        binding.backgroundMask.setOnSingleClickListener {
            binding.imageAddAlarm.rotationImageAdd(-45f)
            viewModel.showAddView.postValue(false)
        }

        binding.layoutAdd.linQuickAlarm.setOnSingleClickListener {
            binding.imageAddAlarm.rotationImageAdd(-45f)
            DialogQuickAlarm.showDialog(supportFragmentManager)
            viewModel.showAddView.postValue(false)
        }

        binding.layoutAdd.linAddAlarm.setOnSingleClickListener {
            binding.imageAddAlarm.rotationImageAdd(-45f)
            startActivity(
                Intent(
                    this,
                    AddAlarmActivity::class.java
                )
            )
            viewModel.showAddView.postValue(false)
        }
    }

    private fun initViewPager() {
        binding.viewPager.offscreenPageLimit = MainViewModel.TabBarItem.values().size
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                return when (MainViewModel.TabBarItem.get(position)) {
                    MainViewModel.TabBarItem.BEDTIME -> BedTimeFragment()
                    MainViewModel.TabBarItem.REMINDER -> ReminderFragment()
                    MainViewModel.TabBarItem.ALARM -> AlarmFragment()
                    MainViewModel.TabBarItem.MYDAY -> MyDayFragment()
                    MainViewModel.TabBarItem.SETTING -> SettingFragment()
                }
            }

            override fun getItemCount(): Int {
                return MainViewModel.TabBarItem.values().size
            }
        }
        binding.tabBarView.setOnTabChangeListener {
            val currentFrg = binding.viewPager.findCurrentFragment(supportFragmentManager)
            if ((currentFrg as? PopupWindowHandle)?.isPopupWindowShow() == true) {
                (currentFrg as? PopupWindowHandle)?.dismissPopupWindow()
                return@setOnTabChangeListener false
            } else {
                (application as? MainApplication)?.currentTabSelected?.postValue(it)
                return@setOnTabChangeListener true
            }

        }

        binding.btnAlarm.setOnSingleClickListener {
            binding.tabBarView.currentSelectedIndex = MainViewModel.TabBarItem.ALARM.index
            (application as? MainApplication)?.currentTabSelected?.postValue(MainViewModel.TabBarItem.ALARM.index)
        }

        (application as? MainApplication)?.currentTabSelected?.observe(this) {
            binding.tabBarView.currentSelectedIndex = it
            binding.viewPager.setCurrentItem(it, false)
            val isAlarm = MainViewModel.TabBarItem.get(it) == MainViewModel.TabBarItem.ALARM
            val isReminder = MainViewModel.TabBarItem.get(it) == MainViewModel.TabBarItem.REMINDER
            binding.imageAddAlarm.visibility =
                if (isAlarm || isReminder) View.VISIBLE else View.GONE
            binding.btnAlarm.setImageResource(
                if (isAlarm)
                    R.drawable.ic_alarm_tab_selected else R.drawable.ic_alarm_tab
            )
            binding.btnAlarmShadown.visibility = if (isAlarm) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun onShowToast(text: String) {
        (binding.viewPager.findCurrentFragment(supportFragmentManager) as? AlarmFragment)?.onShowToast(
            text
        )
    }

}

@HiltViewModel
class MainViewModel @Inject constructor() : BaseViewModel() {

    val showAddView = MutableLiveData(false)

    enum class TabBarItem(val index: Int) {
        BEDTIME(0),
        REMINDER(1),
        ALARM(2),
        MYDAY(3),
        SETTING(4);

        companion object {
            fun get(index: Int): TabBarItem =
                TabBarItem.values().find { it.index == index } ?: ALARM
        }
    }
}

fun ViewPager2.findCurrentFragment(fragmentManager: FragmentManager): Fragment? {
    return fragmentManager.findFragmentByTag("f$currentItem")
}

interface PopupWindowHandle {
    fun isPopupWindowShow(): Boolean

    fun dismissPopupWindow()
}



