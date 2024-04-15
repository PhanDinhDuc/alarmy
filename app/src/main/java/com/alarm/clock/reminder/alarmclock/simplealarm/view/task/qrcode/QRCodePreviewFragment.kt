package com.alarm.clock.reminder.alarmclock.simplealarm.view.task.qrcode

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewModel
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.PushType
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.pushTo
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.FragmentQrCodePreviewBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.SingleLiveEvent
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.gone
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.visible
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.BarCodes
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.BaseTaskFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskType
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
class QRCodePreviewFragment :
    BaseTaskFragment<FragmentQrCodePreviewBinding, QRCodePreviewViewModel>() {
    override val taskType: TaskType = TaskType.SCANQR
    override val viewModel by viewModels<QRCodePreviewViewModel>()
    override fun reload() {

    }

    override fun timeOver() {
        viewModel.isComplete.postValue(false)

    }

    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentQrCodePreviewBinding {
        return FragmentQrCodePreviewBinding.inflate(inflater)
    }

    override fun setupView() {
        super.setupView()
        viewModel.idQrCode = taskSettingModel?.barcodes?.get(0)!!
        if (taskSettingModel?.isPreview == true) {
            binding.exitBtn.visible()
            binding.exitBtn.setOnSingleClickListener {
                taskActionCallbackListener?.exitPreview()
            }
        } else binding.exitBtn.gone()

        binding.tvNameQr.text = taskSettingModel?.barcodes?.get(0)?.nameQr
        viewModel.isComplete.observe(this) {
            if (it) taskActionCallbackListener?.pass() else taskActionCallbackListener?.fail()
        }

        setUpExampleMath()

        parentFragmentManager.setFragmentResultListener(
            "KEY_RESULT", this
        ) { requestKey, result ->
            val resultReceived = result.getBoolean("bundleKey")
            if (resultReceived) {
                viewModel.isComplete.postValue(true)
            }
        }
    }

    private fun setUpExampleMath() {
        binding.btnSave.setOnSingleClickListener {
            taskActionCallbackListener?.start()
            startScanning()
        }
    }

    private fun startScanning() {
        val bundle = bundleOf(ARG_SCANNING_SDK to (taskSettingModel?.barcodes?.get(0)?.idQr))
        pushTo(R.id.action_QRCodePreviewFragment_to_barcodeScanningFragment, bundle, PushType.NONE)
    }

    override fun onResume() {
        super.onResume()
        showHeaderActionTask()
    }
}

@HiltViewModel
class QRCodePreviewViewModel @Inject constructor() : BaseViewModel() {
    val isComplete = SingleLiveEvent<Boolean>()
    val title = MutableLiveData<String>()
    var idQrCode = BarCodes("test", "1234567798765432")
}