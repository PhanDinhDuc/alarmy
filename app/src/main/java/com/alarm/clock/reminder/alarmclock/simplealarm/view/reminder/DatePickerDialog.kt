package com.alarm.clock.reminder.alarmclock.simplealarm.view.reminder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.MainApplication
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseDialogFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.DialogPickDateBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.getAppCompactDrawable
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.serializable
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import java.time.LocalDate


class DatePickerDialog : BaseDialogFragment<DialogPickDateBinding>() {


    private val onDateChangedListener: OnDateChangedListener?
        get() {
            return (activity as? OnDateChangedListener)
        }

    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogPickDateBinding {
        return DialogPickDateBinding.inflate(inflater)
    }

    companion object {
        const val LocalDateValue = "LocalDateValue"
        const val LocalDateValueMax = "LocalDateValueMax"
        fun show(fMng: FragmentManager, date: LocalDate, maxDate: LocalDate? = null) {
            val dialog = DatePickerDialog()
            dialog.arguments = bundleOf(
                LocalDateValue to date,
                LocalDateValueMax to maxDate
            )
            dialog.show(fMng, "Dialog")
        }
    }


    override fun setupView() {
        super.setupView()
        val dateData = arguments?.serializable<LocalDate>(LocalDateValue)
        val maxDateData = arguments?.serializable<LocalDate>(LocalDateValueMax)

        dateData?.let {
            binding.datePicker.date = it
        }

        maxDateData?.let {
            binding.datePicker.maxDate = it
        }

        binding.txtCancel.setOnSingleClickListener {
            dismiss()
        }

        binding.datePicker.dateChangeListener = {
            if (!isEnableBtn) {
                binding.txtConfirm.background =
                    MainApplication.CONTEXT.getAppCompactDrawable(R.drawable.bg_view_add_alarm)
                binding.txtConfirm.setOnSingleClickListener {
                    onDateChangedListener?.onDateChanged(
                        binding.datePicker.date
                    )
                    dismiss()
                }
            }
        }
    }

    private var isEnableBtn: Boolean = false

    interface OnDateChangedListener {
        fun onDateChanged(updated: LocalDate)
    }
}

