package com.alarm.clock.reminder.alarmclock.simplealarm.view.bedtime

import android.view.LayoutInflater
import android.view.ViewGroup
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseDialogFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.TimeSoundModel
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.DialogChangeTimeBedtimeBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.TIME_SOUND
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener


@Suppress("DEPRECATION")
class DialogChangeTimeBed : BaseDialogFragment<DialogChangeTimeBedtimeBinding>() {
    lateinit var time: TimeSoundModel
    private var onTimeChangedListener: OnTimeChangedListener? = null
    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogChangeTimeBedtimeBinding {
        return DialogChangeTimeBedtimeBinding.inflate(inflater)
    }


    override fun setupView() {
        super.setupView()

        val timeData = arguments?.getParcelable<TimeSoundModel>(TIME_SOUND)

        timeData.let {
            it?.let { it1 -> binding.ALTimePicker.setTimePicker(it1.time) }
            binding.txtDay.text = it?.let { it1 -> Util.getDayString(it1.day, requireContext()) }
        }

        binding.txtCancel.setOnSingleClickListener {
            dismiss()
        }

        binding.txtConfirm.setOnSingleClickListener {
            timeData?.let { it1 -> onTimeChangedListener?.onTimeChanged(it1.copy(time = binding.ALTimePicker.getTimePicker())) }
            dismiss()
        }

    }

    fun setOnTimeChangedListener(listener: OnTimeChangedListener) {
        this.onTimeChangedListener = listener
    }

    interface OnTimeChangedListener {
        fun onTimeChanged(updatedTime: TimeSoundModel)
    }
}