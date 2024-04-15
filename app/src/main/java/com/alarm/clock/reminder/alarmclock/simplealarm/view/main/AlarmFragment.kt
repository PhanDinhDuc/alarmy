package com.alarm.clock.reminder.alarmclock.simplealarm.view.main

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseVMFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Alarm
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.FragmentAlarmHomeBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.showCustomToast
import com.alarm.clock.reminder.alarmclock.simplealarm.view.adapter.OnClickItemAlarm
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.AddAlarmActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.OnShowToastInsertNewAlarmListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.home.HomeAlarmManage
import com.alarm.clock.reminder.alarmclock.simplealarm.view.priview.PreviewAlarmActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.viewmodels.AlarmViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job

@AndroidEntryPoint
class AlarmFragment : BaseVMFragment<FragmentAlarmHomeBinding, AlarmViewModel>(), OnClickItemAlarm,
    OnShowToastInsertNewAlarmListener, PopupWindowHandle {

    override val viewModel: AlarmViewModel by viewModels()
    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAlarmHomeBinding {
        return FragmentAlarmHomeBinding.inflate(inflater, container, false)
    }

    private var job: Job? = null

    private val homeAlarmManage: HomeAlarmManage by lazy {
        HomeAlarmManage(binding, this, viewModel, job)
    }

    override fun setupView() {
        super.setupView()
        homeAlarmManage.intView()
        AddAlarmActivity.listener = this
        PreviewAlarmActivity.listener = this

    }

    override fun onDelete(alarm: Alarm) {
        viewModel.deleteAlarm(alarm)
        Toast(requireContext()).showCustomToast(getString(R.string.delete_alarm), requireActivity())
    }

    override fun onPreview(alarm: Alarm) {
        val intent = Intent(mActivity, PreviewAlarmActivity::class.java).apply {
            this.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(Util.ALARM_ARG, alarm)
            putExtra(Util.STATUS, Util.PREVIEW)
        }
        startActivity(intent)
    }

    override fun onSetTemPlate(alarm: Alarm) {
        viewModel.setTemplate(alarm)
    }

    override fun onUndoSkip(alarm: Alarm) {
        viewModel.skipNextAlarm(alarm)
    }

    override fun onCopy(alarm: Alarm) {
        viewModel.insertAlarm(alarm.copy(id = 0))
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
        AddAlarmActivity.listener = null
    }

    override fun onShowToast(text: String) {
        homeAlarmManage.showToast(text)
    }

    override fun isPopupWindowShow(): Boolean = homeAlarmManage.isPopupMenuShow()

    override fun dismissPopupWindow() {
        homeAlarmManage.dismissPopupWindow()
    }
}