package com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView

import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.CustomTimePickerBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.InputFilterMinMax
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.gone
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.visible
import com.alarm.clock.reminder.alarmclock.simplealarm.view.first_alarm.wheel_picker.OnValueChangeListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.first_alarm.wheel_picker.OnValueTapListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.first_alarm.wheel_picker.WheelAdapter
import com.alarm.clock.reminder.alarmclock.simplealarm.view.first_alarm.wheel_picker.WheelPicker
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class ALTimePicker @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), OnValueChangeListener {

    companion object {
        const val FORMAT_TIME = "hh:mm:a"
    }

    var binding: CustomTimePickerBinding

    var timeChangeListener: ((LocalTime) -> Unit)? = null

    var time: LocalTime = LocalTime.now()
        get() {
            return LocalTime.parse(
                listOf(
                    binding.hourPicker.getCurrentItem(),
                    binding.minutesPicker.getCurrentItem(),
                    binding.amPmPicker2.getCurrentItem()
                ).joinToString(":"), DateTimeFormatter.ofPattern(FORMAT_TIME, Locale.US)
            )
        }
        set(value) {
            field = value
            val temps = value.format(DateTimeFormatter.ofPattern(FORMAT_TIME, Locale.US)).split(":")
            if (temps.size != 3) return
            binding.hourPicker.setValue(temps[0])
            binding.minutesPicker.setValue(temps[1])
            binding.amPmPicker2.setValue(temps[2])
            timeChangeListener?.invoke(value)
            setVisibleAMLine()
        }

    fun getTimePicker(): String {
        if (binding.edtHour.isFocused) {
            val value = binding.edtHour.text.toString()

            binding.hourPicker.setValue(if (value.length < 2) "0${value}" else value)
        }
        if (binding.edtMinus.isFocused) {
            val value = binding.edtMinus.text.toString()
            binding.minutesPicker.setValue(if (value.length < 2) "0${value}" else value)
        }
        val valueTime = listOf(
            binding.hourPicker.getCurrentItem(),
            binding.minutesPicker.getCurrentItem(),
            binding.amPmPicker2.getCurrentItem()

        )
        val formattedTime = valueTime.joinToString(":")
        return formattedTime.format(DateTimeFormatter.ofPattern(FORMAT_TIME, Locale.US))
    }

    fun setTimePicker(value: String) {
        val temps = value.format(DateTimeFormatter.ofPattern(FORMAT_TIME, Locale.US)).split(":")
        if (temps.size != 3) return
        binding.hourPicker.setValue(temps[0])
        binding.minutesPicker.setValue(temps[1])
        binding.amPmPicker2.setValue(temps[2])
        setVisibleAMLine()
    }


    init {
        binding = CustomTimePickerBinding.inflate(LayoutInflater.from(context), this, true)
        binding.amPmPicker2.apply {
            setAdapter(WPAMPMPickerAdapter())
            setOnValueChangedListener(this@ALTimePicker)
        }
        binding.hourPicker.apply {
            setAdapter(WPHoursPickerAdapter())
            setOnValueChangedListener(this@ALTimePicker)
        }
        binding.minutesPicker.apply {
            setAdapter(WPMinutesPickerAdapter())
            setOnValueChangedListener(this@ALTimePicker)
        }

        binding.root.setOnSingleClickListener {
            clearFocus()
        }

        handleTapPicker(binding.edtMinus, binding.minutesPicker, InputFilterMinMax("0", "59"))
        handleTapPicker(binding.edtHour, binding.hourPicker, InputFilterMinMax("0", "12"))
        time = LocalTime.now()
    }

    private fun handleTapPicker(editText: EditText, picker: WheelPicker, filter: InputFilter) {
        picker.setOnValueTapListener(object : OnValueTapListener {
            override fun onValueTap(picker: WheelPicker, value: String) {
                editText.setText(value)
                editText.visible()
                editText.showKeyboard()
                editText.setSelection(editText.length())
//                picker.isScrollEnable = false

                editText.filters = arrayOf(filter)

                editText.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        editText.gone()
                        setValueTapPicker(editText, picker)
//                        picker.isScrollEnable = true
                        true
                    }
                    false
                }

                editText.setOnFocusChangeListener { v, hasFocus ->
                    setValueTapPicker(editText, picker)
//                    picker.isScrollEnable = !hasFocus
                    editText.visibility = if (hasFocus) View.VISIBLE else View.GONE
                }
            }

        })
    }

    fun setValueTapPicker(editText: EditText, picker: WheelPicker) {
        val value = editText.text.toString()
        picker.setValue(if (value.length < 2) "0${value}" else value)
        timeEnable()
    }

    private fun setVisibleAMLine() {
        if (binding.amPmPicker2.getCurrentItem() == "PM") {
            binding.line1.visible()
            binding.line2.gone()
        } else {
            binding.line1.gone()
            binding.line2.visible()
        }
    }

    private fun timeEnable() {
        val hourPickerValue = binding.hourPicker.getCurrentItem()
        val minutesPickerValue = binding.minutesPicker.getCurrentItem()
        val isAm =
            (hourPickerValue == "00" || (hourPickerValue == "12" && minutesPickerValue == "00"))
        val isPM = (hourPickerValue == "12" && minutesPickerValue != "00")
        if (isAm) {
            binding.amPmPicker2.setValue("AM")
        } else if (isPM) {
            binding.amPmPicker2.setValue("PM")
        }
        binding.amPmPicker2.isScrollEnable = !(isAm || isPM)
        setVisibleAMLine()
    }

    override fun onValueChange(picker: WheelPicker, oldVal: String, newVal: String) {
        if (picker == binding.hourPicker) {
            binding.edtHour.setText(newVal)
        }

        if (picker == binding.minutesPicker) {
            binding.edtMinus.setText(newVal)
        }

        clearFocus()
        timeChangeListener?.invoke(time)
        if (picker == binding.hourPicker || picker == binding.minutesPicker || picker == binding.amPmPicker2) timeEnable()
    }

    override fun isFocused(): Boolean {
        return binding.edtMinus.isFocused || binding.edtHour.isFocused
    }

    override fun clearFocus() {
        if (isFocused) {
            val imm =
                context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.edtHour.windowToken, 0)
        }
        binding.edtMinus.clearFocusIfNeed()
        binding.edtHour.clearFocusIfNeed()
        super.clearFocus()
    }
}

class WPAMPMPickerAdapter : WheelAdapter() {
    override fun getValue(position: Int): String {
        return when (position) {
            0 -> "AM"
            1 -> "PM"
            else -> ""
        }
    }

    override fun getPosition(vale: String): Int {
        return when (vale) {
            "AM" -> 0
            "PM" -> 1
            else -> 0
        }
    }

    override fun getTextWithMaximumLength(): String {
        return "AM"
    }
}

class WPHoursPickerAdapter : WheelAdapter() {
    override fun getValue(position: Int): String {
        return when (position) {
            0 -> "00"
            1 -> "01"
            2 -> "02"
            3 -> "03"
            4 -> "04"
            5 -> "05"
            6 -> "06"
            7 -> "07"
            8 -> "08"
            9 -> "09"
            10 -> "10"
            11 -> "11"
            12 -> "12"
            else -> position.toString()
        }
    }

    override fun getPosition(vale: String): Int {
        return when (vale) {
            "00" -> 0
            "01" -> 1
            "02" -> 2
            "03" -> 3
            "04" -> 4
            "05" -> 5
            "06" -> 6
            "07" -> 7
            "08" -> 8
            "09" -> 9
            "10" -> 10
            "11" -> 11
            "12" -> 12
            else -> vale.toInt()
        }
    }

    override fun getTextWithMaximumLength(): String {
        return "12"
    }
}

class WPMinutesPickerAdapter : WheelAdapter() {
    override fun getValue(position: Int): String {
        if (position < 10) return "0$position"

        return position.toString()
    }

    override fun getPosition(vale: String): Int {
        return when (vale) {
            "00" -> 0
            "01" -> 1
            "02" -> 2
            "03" -> 3
            "04" -> 4
            "05" -> 5
            "06" -> 6
            "07" -> 7
            "08" -> 8
            "09" -> 9
            else -> vale.toInt()
        }
    }

    override fun getTextWithMaximumLength(): String {
        return "00"
    }
}

fun EditText.showKeyboard() {
    requestFocus()
    val imm: InputMethodManager? =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun EditText.hideKeyboard() {
    clearFocus()
    val imm: InputMethodManager? =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.hideSoftInputFromWindow(this.windowToken, 0)
}
