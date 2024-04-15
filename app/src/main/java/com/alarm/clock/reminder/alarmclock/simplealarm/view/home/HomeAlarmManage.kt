package com.alarm.clock.reminder.alarmclock.simplealarm.view.home

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Rect
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.Settings
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Alarm
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.sortedByTime
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.FragmentAlarmHomeBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Converter
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.TIME_UPDATE
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.showCustomToast
import com.alarm.clock.reminder.alarmclock.simplealarm.view.adapter.AlarmAdapter
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.AddAlarmActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.main.AlarmFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.view.settings.AlarmSettingActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.settings.SortAlarmBy
import com.alarm.clock.reminder.alarmclock.simplealarm.view.viewmodels.AlarmViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class HomeAlarmManage(
    private val binding: FragmentAlarmHomeBinding,
    private val fragment: AlarmFragment,
    private val viewModel: AlarmViewModel,
    private var job: Job?
) : AlarmSettingActivity.CallBackOnChangeListener {

    private lateinit var alarmAdapter: AlarmAdapter
    private var list: List<Alarm>? = null
    private var jobToast: Job? = null

    fun dismissPopupWindow() {
        alarmAdapter.dismissPopupWindow()
    }

    fun isPopupMenuShow(): Boolean {
        return alarmAdapter.isPopupMenuShow()
    }

    fun intView() {
        AlarmSettingActivity.listenerCallBack = this
        fragment.activity?.let { activity ->
            alarmAdapter = AlarmAdapter(activity, onSwitch = { isOn, id ->
                activity.lifecycleScope.launch {

                    val alarm = viewModel.getAlarmById(id) ?: return@launch
                    alarm.isON = isOn
                    //delete alarm if alarm is quick
                    if (alarm.isQuickAlarm && !isOn) {
                        try {
                            viewModel.deleteAlarm(alarm)
                            Toast(activity).showCustomToast(
                                activity.getString(R.string.delete_alarm),
                                activity
                            )
                        } catch (e: Exception) {
                            Log.e("Delete", e.message.toString())
                        }

                    } else {
                        viewModel.updateAlarm(alarm)
                    }

                }
            }, onItemAlarmClick = { alarm ->
                if (!alarm.isQuickAlarm) {
                    val intent = Intent(activity, AddAlarmActivity::class.java).apply {
                        putExtra(Util.ALARM_ARG, alarm)
                        putExtra(Util.STATUS, Util.SHOW_DETAIL)
                    }
                    activity.startActivity(intent)
                }
            }, fragment) { visiblePopup ->
                binding.maskPopup.visibility = if (visiblePopup) View.VISIBLE else View.GONE
            }

            binding.maskPopup.setOnSingleClickListener {
                alarmAdapter.dismissPopupWindow()
            }

            //observer data alarm change

            viewModel.allAlarmData.observe(activity) {
                Util.checkShowNoti(activity, it)
                list = it as MutableList<Alarm>
                setUpList(Settings.alarmSettings.sortAlarmBy)

                job?.cancel()
                job = CoroutineScope(Dispatchers.Main).launch {
                    if (it.isNullOrEmpty()) {
                        binding.layoutHome.txtNextAlarm.text =
                            fragment.activity?.getString(R.string.create_new)
                    } else {
                        while (isActive && it.isNotEmpty()) {
                            upDateTimeEarliestAlarm(it)
                            delay(TIME_UPDATE)
                        }
                    }
                }

                initVisibilityView(it)
            }
        }

        binding.layoutHome.recAlarm.apply {
            layoutManager =
                LinearLayoutManager(fragment.activity, LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
            adapter = alarmAdapter
            addItemDecoration(BottomDecoration(Converter.asPixels(100)))
        }

    }

    private fun initVisibilityView(alarms: List<Alarm>) {
        binding.layoutHome.recAlarm.visibility =
            if (alarms.isEmpty()) View.INVISIBLE else View.VISIBLE
        binding.layoutHome.lineNoAlarm.visibility =
            if (alarms.isEmpty()) View.VISIBLE else View.INVISIBLE
    }


    //calculator earliest alarm from now
    private fun upDateTimeEarliestAlarm(alarms: MutableList<Alarm>) {
        val (hours, minutes) = Util.getTimeDifferenceToNextAlarm(alarms)
        if (hours >= 24) {
            val (days, hours) = Util.getDayToNextAlarm(hours)
            binding.layoutHome.txtNextAlarm.text =
                fragment.activity?.getString(R.string.time_will_ring_day, days, hours)
        } else {
            if (alarms.isNullOrEmpty()) {
                binding.layoutHome.txtNextAlarm.text =
                    fragment.activity?.getString(R.string.create_new)
            } else if (hours == 0 && !alarms.isNullOrEmpty() && minutes != 0) {
                binding.layoutHome.txtNextAlarm.text =
                    fragment.activity?.getString(R.string.time_will_ring_min, minutes)
            } else if (!alarms.isNullOrEmpty() && alarms.all { !it.isON }) {
                binding.layoutHome.txtNextAlarm.text =
                    fragment.activity?.getString(R.string.no_alarm_turn_on)
            } else if (hours != 0) {
                binding.layoutHome.txtNextAlarm.text =
                    fragment.activity?.getString(R.string.time_will_ring, hours, minutes)
            } else {
                binding.layoutHome.txtNextAlarm.text =
                    fragment.activity?.getString(R.string.alarm_comming_soon)
            }
        }
    }


    private fun setUpList(
        type: SortAlarmBy = Settings.alarmSettings.sortAlarmBy
    ) {
        val sortedList = when (type) {
            SortAlarmBy.NORMAL -> list
            SortAlarmBy.ENABLE -> list?.sortedByDescending { alarm -> alarm.isON }
            SortAlarmBy.TIME -> list?.sortedByTime()
        }

        sortedList?.let { alarmAdapter.setUpList(it) }
    }


    fun showToast(textValue: String) {
        jobToast = CoroutineScope(Dispatchers.Main).launch {
            //show toast in 2s
            binding.txtToast.apply {
                visibility = View.VISIBLE
                text = textValue
            }
            delay(2000)

            binding.txtToast.visibility = View.GONE
        }
    }

    override fun onUpdateList(type: SortAlarmBy) {
        setUpList(type)
    }

}

private class BottomDecoration(private val space: Int) : ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val isLast = position == state.itemCount - 1
        if (isLast) {
            outRect.bottom = space
        }
    }
}

fun View.rotationImageAdd(value: Float) {
    val rotation =
        ObjectAnimator.ofFloat(this, "rotation", this.rotation, this.rotation + value)
    rotation.duration = 200
    rotation.start()
}