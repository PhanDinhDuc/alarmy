package com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseDialogFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.DialogChangenameBarcodeBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.gone
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.visible

const val ARG_SCANNING_RESULT = "scanning_result"
const val ARG_SCANNING_ENABLE = "scanning_enable"

class DialogChangeNameBarcode : BaseDialogFragment<DialogChangenameBarcodeBinding>() {
    override fun makeBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogChangenameBarcodeBinding {
        return DialogChangenameBarcodeBinding.inflate(inflater)
    }

    interface Listener {
        fun dialogChangeNameBarcodeOnDismiss()

        fun dialogChangeNameBarcodeOnClickOk(value: String)
    }

    private val callback: Listener
        get() = (parentFragment as? Listener) ?: (activity as? Listener)
        ?: error("No callback for ConfirmDialog")


    override fun setupView() {
        super.setupView()
        val scannedResult = arguments?.getString(ARG_SCANNING_RESULT)
        val scannedEnablerEditText = arguments?.getBoolean(ARG_SCANNING_ENABLE)
        if (scannedEnablerEditText == false) {
            binding.edtBarcode.isEnabled = false
            binding.tvDelete.gone()
            binding.txtOk.isEnabled = true
        } else {
            binding.edtBarcode.apply {
                doOnTextChanged { text, start, before, count ->
                    if (text.isNullOrBlank()) {
                        binding.txtOk.isEnabled = false
                        binding.tvDelete.gone()
                    } else {
                        binding.txtOk.isEnabled = true
                        binding.tvDelete.visible()
                    }
                }
            }
        }
        binding.edtBarcode.setText(scannedResult)
        binding.tvDelete.setOnSingleClickListener {
            binding.edtBarcode.setText("")
        }
        binding.txtOk.setOnClickListener {
            callback.dialogChangeNameBarcodeOnClickOk(binding.edtBarcode.text.toString().trim())
            dismiss()
        }
        binding.txtCancel.setOnClickListener { callback.dialogChangeNameBarcodeOnDismiss(); dismiss() }
    }

    companion object {
        fun newInstance(
            scanningResult: String, enableEditText: Boolean
        ): DialogChangeNameBarcode = DialogChangeNameBarcode().apply {
            arguments = Bundle().apply {
                putString(ARG_SCANNING_RESULT, scanningResult)
                putBoolean(ARG_SCANNING_ENABLE, enableEditText)
            }
        }

    }

    override fun onCancel(dialog: DialogInterface) {
        callback.dialogChangeNameBarcodeOnDismiss()
        super.onCancel(dialog)
    }
}