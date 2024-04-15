package com.alarm.clock.reminder.alarmclock.simplealarm.view.sound.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.AudioModel
import com.alarm.clock.reminder.alarmclock.simplealarm.view.sound.fragment.FragmentListSound


class AdapterSoundViewPager(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

    private val fragments: MutableList<FragmentListSound> = ArrayList()
    private val titles = ArrayList<String>()

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    fun addFragment(fragment: Fragment, title: String?) {
        if (title != null) {
            fragments.add(fragment as FragmentListSound)
            titles.add(title)
        }
    }

    fun updateDataForTab(tabPosition: Int, newList: List<AudioModel>) {
        if (tabPosition >= 0 && tabPosition < fragments.size) {
            fragments[tabPosition].updateListData(tabPosition, newList)
        }
    }

    fun getPageTitle(position: Int): CharSequence {
        return titles[position]
    }
}
