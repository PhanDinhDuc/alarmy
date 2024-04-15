package com.alarm.clock.reminder.alarmclock.simplealarm.view.task.qrcode

import android.Manifest
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.PushType
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.pushTo
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.FragmentTaskqrcodeBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.BaseTaskSettingFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.CUR_TASK
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskSettingModel
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskType

class QRCodeTaskFragment : BaseTaskSettingFragment<FragmentTaskqrcodeBinding>() {
    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTaskqrcodeBinding {
        return FragmentTaskqrcodeBinding.inflate(inflater)
    }

    override val taskType: TaskType = TaskType.SCANQR
    lateinit var task: TaskSettingModel
    override fun setupView() {
        super.setupView()
        task = taskSettingModel ?: return
        if ((task.barcodes?.size ?: 0) > 0) {
            pushTo(
                R.id.action_QRCodeFragment_to_fragmentListBarcode,
                bundleOf(CUR_TASK to task),
                PushType.NONE
            )
        }
        binding.addQR.setOnSingleClickListener {
            permissionsResult.launch(arrayOf(Manifest.permission.CAMERA))
        }
    }

    override fun onPermissionGranted(permissions: Map<String, Boolean>) {
        super.onPermissionGranted(permissions)
        if (permissions.containsKey(Manifest.permission.CAMERA)) {
            pushTo(
                R.id.action_QRCodeFragment_to_barcodeScanningFragment2,
                bundleOf(CUR_TASK to task),
                PushType.NONE
            )
        }
    }

    override fun onResume() {
        super.onResume()
        showHeaderSettingTask()
    }
}