package com.alarm.clock.reminder.alarmclock.simplealarm.view.reminder

import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseDialogFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.DialogChangeTimeBedtimeBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.parcelable
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.serializable
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import kotlinx.parcelize.Parcelize
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class TimePicker : BaseDialogFragment<DialogChangeTimeBedtimeBinding>() {


    private val onTimeChangedListener: OnTimeChangedListener?
        get() {
            return (activity as? OnTimeChangedListener)
        }

    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogChangeTimeBedtimeBinding {
        return DialogChangeTimeBedtimeBinding.inflate(inflater)
    }

    companion object {
        const val LocalTimeValue = "LocalTimeValue"
        const val PickTimeValue = "PickTimeValue"
        fun show(fMng: FragmentManager, time: LocalTime, type: PickType = PickType.ChangeTime) {
            val dialog = TimePicker()
            dialog.arguments = bundleOf(
                LocalTimeValue to time,
                PickTimeValue to type
            )
            dialog.show(fMng, "Dialog")
        }
    }


    override fun setupView() {
        super.setupView()
        val timeData = arguments?.serializable<LocalTime>(LocalTimeValue)
        timeData?.let {
            binding.ALTimePicker.setTimePicker(
                it.format(
                    DateTimeFormatter.ofPattern("hh:mm:a").withLocale(
                        Locale.US
                    )
                )
            )
        }

        binding.txtCancel.setOnSingleClickListener {
            dismiss()
        }

        binding.txtConfirm.setOnSingleClickListener {
            try {
                onTimeChangedListener?.onTimeChanged(
                    LocalTime.parse(
                        binding.ALTimePicker.getTimePicker(),
                        DateTimeFormatter.ofPattern("hh:mm:a").withLocale(
                            Locale.US
                        )
                    ),
                    arguments?.parcelable(PickTimeValue) ?: PickType.ChangeTime
                )
            } catch (e: Exception) {
                Log.e("TimePicker", "TimePicker", e)
            }
            dismiss()
        }

    }

    interface OnTimeChangedListener {
        fun onTimeChanged(updatedTime: LocalTime, type: PickType)
    }
}

@Parcelize
sealed class PickType : Parcelable {
    object ChangeTime : PickType()
    data class ChangeSeveralTime(val idx: Int) : PickType()
}