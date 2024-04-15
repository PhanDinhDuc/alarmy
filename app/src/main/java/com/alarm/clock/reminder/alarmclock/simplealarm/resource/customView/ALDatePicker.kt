package com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView

import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.CustomDatePickerBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.InputFilterMinMax
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.ALDatePicker.Companion.MONTH
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.gone
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.visible
import com.alarm.clock.reminder.alarmclock.simplealarm.view.first_alarm.wheel_picker.OnScrollListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.first_alarm.wheel_picker.OnScrollListener.Companion.SCROLL_STATE_IDLE
import com.alarm.clock.reminder.alarmclock.simplealarm.view.first_alarm.wheel_picker.OnValueChangeListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.first_alarm.wheel_picker.OnValueTapListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.first_alarm.wheel_picker.WheelAdapter
import com.alarm.clock.reminder.alarmclock.simplealarm.view.first_alarm.wheel_picker.WheelPicker
import com.alarm.clock.reminder.alarmclock.simplealarm.view.myday.displayText
import java.time.DateTimeException
import java.time.LocalDate
import java.time.Month


class ALDatePicker @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), OnValueChangeListener, OnScrollListener {

    companion object {
        var MONTH = Month.values().map {
            it.displayText()
        }.toList()
    }

    var binding: CustomDatePickerBinding

    var dateChangeListener: ((LocalDate) -> Unit)? = null

    var date: LocalDate = LocalDate.now()
        get() {
            return LocalDate.of(
                year, month, dayOfMonth
            )
        }
        set(value) {
            field = value
            dateChangeListener?.invoke(value)
            binding.pickerYear.setValue(value.year.toString())
            binding.pickerMonth.setValue(MONTH.getOrNull(value.monthValue - 1) ?: MONTH.first())
            binding.pickerDay.setValue(value.dayOfMonth.toString())

            checkDateValid()

            binding.pickerYear.reload()
            binding.pickerMonth.reload()
            binding.pickerDay.reload()
        }

    val today = LocalDate.now()

    val month: Int
        get() {
            if (binding.edtMonth.isVisible) {
                val value = binding.edtMonth.text.toString()

                binding.pickerMonth.setValue(
                    MONTH.getOrNull((value.toIntOrNull() ?: 1) - 1) ?: MONTH.first()
                )
            }
            return MONTH.indexOf(binding.pickerMonth.getCurrentItem()) + 1
        }

    val year: Int
        get() {
            if (binding.edtYear.isVisible) {
                val value = binding.edtYear.text.toString()

                binding.pickerYear.setValue(value)
            }
            return binding.pickerYear.getCurrentItem().toInt()
        }

    val dayOfMonth: Int
        get() {
            if (binding.edtDay.isVisible) {
                val value = binding.edtDay.text.toString()

                binding.pickerDay.setValue(value)
            }
            return binding.pickerDay.getCurrentItem().toInt()
        }

    var minDate: LocalDate? = null

    var maxDate: LocalDate? = null

    init {
        MONTH = Month.values().map {
            it.displayText()
        }.toList()
        binding = CustomDatePickerBinding.inflate(LayoutInflater.from(context), this, true)
        listOf(binding.pickerYear, binding.pickerMonth, binding.pickerDay).forEach {
            it.setOnValueChangedListener(this@ALDatePicker)
            it.setOnScrollListener(this@ALDatePicker)
        }
        binding.pickerMonth.apply {
            setAdapter(WPMONTHPickerAdapter())
        }

        binding.root.setOnSingleClickListener {
            clearFocus()
        }

        handleTapPicker(binding.edtDay, binding.pickerDay, InputFilterMinMax(1, 31))
        handleTapPicker(binding.edtMonth, binding.pickerMonth, InputFilterMinMax(1, 12))
        handleTapPicker(binding.edtYear, binding.pickerYear, InputFilterMinMax("1", "3000"))
        date = today
    }

    private fun handleTapPicker(editText: EditText, picker: WheelPicker, filter: InputFilter) {
        picker.setOnValueTapListener(object : OnValueTapListener {
            override fun onValueTap(picker: WheelPicker, value: String) {
                var value = value
                if (picker == binding.pickerMonth) {
                    value = (MONTH.indexOf(value) + 1).toString()
                }
                editText.setText(value)
                editText.visible()
                editText.showKeyboard()
                editText.requestFocus()
                editText.setSelection(editText.length())
//                picker.isScrollEnable = false

                editText.filters = arrayOf(filter)

                editText.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        editText.gone()
                        setValueTapPicker(editText, picker)
                        editText.hideKeyboard()
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

    private fun setValueTapPicker(editText: EditText, picker: WheelPicker) {
        val value = editText.text.toString()
        if (picker == binding.pickerMonth) {
            binding.pickerMonth.setValue(
                MONTH.getOrNull((value.toIntOrNull() ?: 1) - 1) ?: MONTH.first()
            )
            Log.d(


                "aaaa", value.toIntOrNull().toString()
            )
        } else {
            picker.setValue(value)
        }
        setMaxDayOfMonth(picker, picker.getCurrentItem())
        dateChangeListener?.invoke(date)
        binding.pickerYear.reload()
        binding.pickerMonth.reload()
        binding.pickerDay.reload()
        handleMaxDate()
    }


    private fun checkValidDate(year: Int, month: Int, dayOfMonth: Int): Boolean {
        return try {
            Log.d("aa", LocalDate.of(year, month, dayOfMonth).toString())
            true
        } catch (e: DateTimeException) {
            false
        }
    }

    private fun checkDateValid() {
        if (month == 2) {
            if (checkValidDate(year, 2, 29)) {
                binding.pickerDay.setMaxValue(29)
            } else {
                binding.pickerDay.setMaxValue(28)
            }
        } else {
            if (checkValidDate(year, month, 31)) {
                binding.pickerDay.setMaxValue(31)
            } else {
                binding.pickerDay.setMaxValue(30)
            }
        }
    }

    private fun setMaxDayOfMonth(picker: WheelPicker, value: String) {
        if (picker == binding.pickerMonth) {
            if (value == MONTH[1]) {
                if (checkValidDate(year, 2, 29)) {
                    binding.pickerDay.setMaxValue(29)
                } else {
                    binding.pickerDay.setMaxValue(28)
                }
            } else {
                if (checkValidDate(year, month, 31)) {
                    binding.pickerDay.setMaxValue(31)
                } else {
                    binding.pickerDay.setMaxValue(30)
                }
            }
        }

        if (picker == binding.pickerYear && month == 2) {
            if (checkValidDate(year, 2, 29)) {
                binding.pickerDay.setMaxValue(29)
            } else {
                binding.pickerDay.setMaxValue(28)
            }
        }
    }

    private fun handleMaxDate() {
        val maxDate = maxDate ?: return
        when {
            year > maxDate.year -> binding.pickerYear.setValue(maxDate.year.toString())
            year == maxDate.year -> {
                when {
                    month > maxDate.monthValue -> binding.pickerMonth.setValue(
                        MONTH.getOrNull(
                            maxDate.monthValue - 1
                        ) ?: MONTH.first()
                    )

                    month == maxDate.monthValue -> {
                        if (dayOfMonth > maxDate.dayOfMonth) {
                            binding.pickerDay.setValue(maxDate.dayOfMonth.toString())
                        }
                    }
                }
            }
        }
    }

    override fun onValueChange(picker: WheelPicker, oldVal: String, newVal: String) {
        setMaxDayOfMonth(picker, newVal)
        dateChangeListener?.invoke(date)
        clearFocus()
    }

    override fun onScrollStateChange(view: WheelPicker, scrollState: Int) {
        if (scrollState == SCROLL_STATE_IDLE) {
            binding.pickerYear.reload()
            binding.pickerMonth.reload()
            binding.pickerDay.reload()
            handleMaxDate()
        }
    }

    override fun isFocused(): Boolean {
        return binding.edtDay.isVisible || binding.edtMonth.isVisible || binding.edtYear.isVisible
    }

    override fun clearFocus() {
        if (isFocused) {
            val imm =
                context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.edtDay.windowToken, 0)
        }
        binding.edtMonth.clearFocusIfNeed()
        binding.edtDay.clearFocusIfNeed()
        binding.edtYear.clearFocusIfNeed()
        super.clearFocus()
    }
}

fun EditText.clearFocusIfNeed() {
    if (isVisible || isFocused) {
        clearFocus()
    }
}

class WPMONTHPickerAdapter : WheelAdapter() {
    override fun getValue(position: Int): String {
        return MONTH.getOrNull(position) ?: MONTH.first()
    }

    override fun getPosition(vale: String): Int {
        return if (MONTH.indexOf(vale) < 0) 0 else MONTH.indexOf(vale)
    }

    override fun getTextWithMaximumLength(): String {
        return MONTH.last()
    }
}