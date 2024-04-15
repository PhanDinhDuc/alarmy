package com.alarm.clock.reminder.alarmclock.simplealarm.view.reminder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseDialogFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.DialogPickNumberBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.InputFilterMinMax
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener


class NumberPickerDialog : BaseDialogFragment<DialogPickNumberBinding>() {

    private val onNumberChangedListener: OnNumberChangedListener?
        get() {
            return (activity as? OnNumberChangedListener)
        }

    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogPickNumberBinding {
        return DialogPickNumberBinding.inflate(inflater)
    }

    companion object {
        const val NumberValue = "NumberValue"
        const val PickNumberValue = "PickNumberValue"
        fun show(
            fMng: FragmentManager,
            number: Int,
            type: NumberPickType
        ) {
            val dialog = NumberPickerDialog()
            dialog.arguments = bundleOf(
                NumberValue to number,
                PickNumberValue to type.ordinal
            )
            dialog.show(fMng, "Dialog")
        }
    }

    private var type: NumberPickType = NumberPickType.RepeatTime

    override fun setupView() {
        super.setupView()
        val timeData = arguments?.getInt(NumberValue)
        arguments?.getInt(PickNumberValue)?.let { idx ->
            NumberPickType.values().toList().getOrNull(idx)?.let {
                type = it
            }
        }
        timeData?.let {
            binding.numberPicker.setNumber(it)
        }

        binding.numberPicker.mTitle2 = when (type) {
            NumberPickType.RepeatTime -> getString(R.string.repeat_cycle)
            NumberPickType.SeveralTime -> getString(R.string.reminder_count)
        }

        binding.numberPicker.maxValue = when (type) {
            NumberPickType.RepeatTime -> 23
            NumberPickType.SeveralTime -> 6
        }

        binding.numberPicker.minValue = when (type) {
            NumberPickType.RepeatTime -> 1
            NumberPickType.SeveralTime -> 2
        }

        binding.numberPicker.inputFilterTapPicker =
            InputFilterMinMax(
                binding.numberPicker.minValue.toString(),
                binding.numberPicker.maxValue.toString()
            )

        binding.txtCancel.setOnSingleClickListener {
            dismiss()
        }

        binding.txtConfirm.setOnSingleClickListener {
            onNumberChangedListener?.onNumberChanged(
                binding.numberPicker.getNumber(),
                type
            )
            dismiss()
        }

    }

    interface OnNumberChangedListener {
        fun onNumberChanged(number: Int, type: NumberPickType)
    }
}


enum class NumberPickType {
    RepeatTime,
    SeveralTime;
}