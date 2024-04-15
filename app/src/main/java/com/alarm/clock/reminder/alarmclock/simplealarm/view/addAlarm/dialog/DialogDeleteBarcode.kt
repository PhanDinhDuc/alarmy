package com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.dialog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseDialogFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.DialogDeleteBarcodeBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.view.reminder.TimePicker
import java.time.LocalDate


class DialogDeleteBarcode :
    BaseDialogFragment<DialogDeleteBarcodeBinding>() {

    interface OnOkDeleteListener {
        fun onOkDeleteListener()
    }

    companion object {

        private const val TAG = "Dialog"
        const val EXTRA_TITLE = "EXTRA_TITLE"
        fun show(fragmentManager: FragmentManager, text: String?) {
            val dialog = DialogDeleteBarcode()
            dialog.arguments = bundleOf(
                EXTRA_TITLE to text
            )
            dialog.show(fragmentManager, TAG)
        }
    }

    private val onOkDeleteListener: OnOkDeleteListener?
        get() {
            return (parentFragment as? OnOkDeleteListener) ?: (activity as? OnOkDeleteListener)
        }

    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogDeleteBarcodeBinding {
        return DialogDeleteBarcodeBinding.inflate(inflater)
    }

    override fun setupView() {
        super.setupView()
        arguments?.getString(EXTRA_TITLE)?.let {
            binding.txtxDecrip.text = it
        }
        binding.txtOk.setOnClickListener {
            onOkDeleteListener?.onOkDeleteListener()
            dismiss()
        }
        binding.txtCancel.setOnClickListener { dismiss() }
    }

}