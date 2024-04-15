package com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView

import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.CustomNumberEquationBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.InputFilterMinMax
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.handleTapPicker
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.setValueTapPicker
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.gone
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.visible
import com.alarm.clock.reminder.alarmclock.simplealarm.view.first_alarm.wheel_picker.OnValueChangeListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.first_alarm.wheel_picker.WheelPicker

class NumberEquationsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    private var binding: CustomNumberEquationBinding
    var onValueChange: ((Int) -> Unit)? = null
    private var number: Int = 0
    var maxValue: Int = 0
        set(value) {
            binding.minutesPicker.setMaxValue(value)
            field = value
        }
    var minValue: Int = 0
        set(value) {
            binding.minutesPicker.setMinValue(value)
            field = value
        }

    var currentValue: String = "1"
    var mTitle: String = ""
        get() {
            return binding.tvTitle.text.toString()
        }
        set(value) {
            field = value
            binding.tvTitle.text = value
        }

    var mTitle2: String = ""
        get() {
            return binding.tvTitle2.text.toString()
        }
        set(value) {
            binding.tvTitle.gone()
            binding.tvTitle2.visible()
            binding.tvTitle2.text = value
            field = value
        }

    var inputFilterTapPicker: InputFilter = InputFilterMinMax("1", "20")
        set(value) {
            field = value
            handleTapPicker(binding.edtHour2, binding.minutesPicker, value)
        }

    init {
        binding = CustomNumberEquationBinding.inflate(LayoutInflater.from(context), this, true)
        val bottomCustomView =
            context.obtainStyledAttributes(attrs, R.styleable.NumberEquationsView, defStyleAttr, 0)

        try {
            maxValue = bottomCustomView.getInt(R.styleable.NumberEquationsView_maxValue, 0)
            minValue = bottomCustomView.getInt(R.styleable.NumberEquationsView_minValue, 0)
            mTitle = bottomCustomView.getString(R.styleable.NumberEquationsView_mTitle) ?: ""
            bottomCustomView.getString(R.styleable.NumberEquationsView_mTitle2)?.let {
                mTitle2 = it
            }
            currentValue =
                bottomCustomView.getString(R.styleable.NumberEquationsView_currentValue).toString()
            binding.minutesPicker.setValue(currentValue)
            binding.root.setOnSingleClickListener {
                clearFocus()
            }
            binding.minutesPicker.setOnValueChangedListener(object :
                OnValueChangeListener {
                override fun onValueChange(picker: WheelPicker, oldVal: String, newVal: String) {
                    clearFocus()
                    onValueChange?.invoke(getNumber())
                }

            })

            binding.edtHour2.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val imm =
                        context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.edtHour2.windowToken, 0)
                    false
                }
                false
            }
        } finally {
            bottomCustomView.recycle()
        }
    }

    fun getNumber(): Int {
        if (binding.edtHour2.isFocused || binding.edtHour2.isVisible) {
            setValueTapPicker(binding.edtHour2, binding.minutesPicker)
        }
        return binding.minutesPicker.getCurrentItem().toInt()
    }

    override fun clearFocus() {
        super.clearFocus()
        if (binding.edtHour2.isFocused) {
            val imm =
                context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.edtHour2.windowToken, 0)
        }
        if (binding.edtHour2.isFocused) {
            binding.edtHour2.clearFocus()
        }
    }

    fun setNumber(newValue: Int) {
        number = newValue
        binding.minutesPicker.setValue(newValue.toString())
    }
}