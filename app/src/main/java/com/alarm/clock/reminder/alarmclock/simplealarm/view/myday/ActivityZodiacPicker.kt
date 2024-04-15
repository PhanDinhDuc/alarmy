package com.alarm.clock.reminder.alarmclock.simplealarm.view.myday

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.MainApplication
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.data.network.HoroscopeFetch
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ActivityZodiacPickerBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.setOnBackClick
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.setOnImageRightClick
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class ActivityZodiacPicker : BaseActivity<ActivityZodiacPickerBinding>() {
    override fun makeBinding(layoutInflater: LayoutInflater): ActivityZodiacPickerBinding {
        return ActivityZodiacPickerBinding.inflate(layoutInflater)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        binding.header.actionRightView?.isEnabled = false
        binding.datePicker.date = HoroscopeFetch.Zodiac.getDateOfBirth()
        binding.header.actionRightView?.isClickable = false
        binding.datePicker.maxDate = LocalDate.now()

        var gender = HoroscopeFetch.Zodiac.getGender()

        fun selectGender() {
            listOf(
                binding.imgRadio,
                binding.imgRadioFemale
            ).forEachIndexed { index, imageView ->
                imageView.isSelected = index == gender
            }
        }

        selectGender()

        fun changeSttSaveButton() {
            binding.header.imageRightDrawable =
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_done
                )
            binding.header.actionRightView?.isEnabled = true
            binding.header.actionRightView?.isClickable = true
        }

        listOf(binding.malec, binding.femalec).forEachIndexed { index, constraintLayout ->
            constraintLayout.setOnClickListener {
                if (index == gender) return@setOnClickListener
                gender = index
                selectGender()
                changeSttSaveButton()
            }
        }

        binding.header.setOnImageRightClick {
            HoroscopeFetch.Zodiac.saveDateOfBirth(binding.datePicker.date)
            HoroscopeFetch.Zodiac.saveGender(gender)
            (application as? MainApplication)?.shareCallback?.invoke()
            finish()
        }

        binding.header.setOnBackClick {
            finish()
        }

        binding.datePicker.dateChangeListener = {
            changeSttSaveButton()
        }
    }
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}