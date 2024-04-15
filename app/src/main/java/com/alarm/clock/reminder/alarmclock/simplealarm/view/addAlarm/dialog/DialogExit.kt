package com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.dialog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseDialogFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.DialogExitBinding

class DialogExit :
    BaseDialogFragment<DialogExitBinding>() {

    override fun makeBinding(inflater: LayoutInflater, container: ViewGroup?): DialogExitBinding {
        return DialogExitBinding.inflate(inflater)
    }

    override fun setupView() {
        super.setupView()
        binding.txtCancel.setOnClickListener {
            dismiss()
        }

        arguments?.getInt(TXT_BACK)?.let {
            binding.txtBack.setText(it)
        }

        binding.txtBack.setOnClickListener {
            callback.onBackDialogOK()
            dismiss()
        }
    }

    private val callback: OnCallBackExitListener
        get() = (parentFragment as? OnCallBackExitListener)
            ?: (activity as? OnCallBackExitListener)
            ?: error("No callback for ConfirmDialog")

    interface OnCallBackExitListener {
        fun onBackDialogOK()
    }

    companion object {

        const val TXT_BACK = "TXT_BACK"
        fun show(fragmentManager: FragmentManager, @StringRes txtBack: Int = R.string.txt_back) {
            val dialog = DialogExit()
            dialog.arguments = bundleOf(TXT_BACK to txtBack)
            dialog.show(fragmentManager, "Exit Dialog")
        }
    }
}