package com.alarm.clock.reminder.alarmclock.simplealarm.view.myday

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.MainApplication
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseVMActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewModel
import com.alarm.clock.reminder.alarmclock.simplealarm.data.network.HoroscopeData
import com.alarm.clock.reminder.alarmclock.simplealarm.data.network.HoroscopeFetch
import com.alarm.clock.reminder.alarmclock.simplealarm.data.network.LuckyColor
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ActivityHoroscopeBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.Language
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.setOnBackClick
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.showSingleActionAlert
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class HoroScopeActivity : BaseVMActivity<ActivityHoroscopeBinding, HoroScopeViewModel>() {
    override val viewModel: HoroScopeViewModel by viewModels()

    override fun makeBinding(layoutInflater: LayoutInflater): ActivityHoroscopeBinding {
        return ActivityHoroscopeBinding.inflate(layoutInflater)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        binding.header.setOnBackClick {
            finish()
        }

        binding.skeToday.showSkeleton()
        binding.skeTomorrow.showSkeleton()

        viewModel.todayHoroscope.observe(this) {
            if (it != null) {
                binding.tvZodiac1Detail.text =
                    it.describe.firstOrNull { it.lang == Language.current.localizeCode }?.content
                binding.skeToday.showOriginal()
                binding.luckyNumber1.tvLuckyNumber.text = it.luckyNumber.toString()
                binding.luckyColor1.tvColor.text = LuckyColor.get(it.luckyColor).getName(this)
                binding.luckyColor1.colorView.setCardBackgroundColor(LuckyColor.get(it.luckyColor).color)
            } else showSingleActionAlert(
                getString(R.string.an_error_has_been_occurred),
                getString(R.string.please_try_again_later)
            ) {
                finish()
            }
        }

        viewModel.tomorrowHoroscope.observe(this) {
            it?.let {
                binding.tvZodiac2Detail.text =
                    it.describe.firstOrNull { it.lang == Language.current.localizeCode }?.content
                binding.skeTomorrow.showOriginal()
                binding.luckyNumber2.tvLuckyNumber.text = it.luckyNumber.toString()
                binding.luckyColor2.tvColor.text = LuckyColor.get(it.luckyColor).getName(this)
                binding.luckyColor2.colorView.setCardBackgroundColor(LuckyColor.get(it.luckyColor).color)
            }
        }

        viewModel.currentDateOfBirth.observe(this) {
            viewModel.currentZodiac = HoroscopeFetch.Zodiac.getZodiacLocal()
            binding.tvZodiac.text = viewModel.currentZodiac.getTitle(this)
            viewModel.fetchHoroscope()
            binding.skeToday.showSkeleton()
            binding.skeTomorrow.showSkeleton()
            binding.tvDate.text = "(${it.format(DateTimeFormatter.ofPattern("M.d.yyyy"))})"
        }

        binding.btnPicker.setOnSingleClickListener {
            startActivity(Intent(this, ActivityZodiacPicker::class.java))
            (application as? MainApplication)?.shareCallback = {
                viewModel.fetchCurrentDateOfBirth()
                (application as? MainApplication)?.shareCallback = null
            }
        }

        binding.btnShareToday.setOnClickListener {
            viewModel.todayHoroscope.value?.describe?.firstOrNull { it.lang == Language.current.localizeCode }?.content?.let {
                shareThisApp(it)
            }
        }

        binding.btnShareTomo.setOnClickListener {
            viewModel.tomorrowHoroscope.value?.describe?.firstOrNull { it.lang == Language.current.localizeCode }?.content?.let {
                shareThisApp(it)
            }
        }
    }

    private fun shareThisApp(text: String) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Horoscope")
        shareIntent.putExtra(
            Intent.EXTRA_TEXT, text
        )
        startActivity(Intent.createChooser(shareIntent, "Share"))
    }
}

@HiltViewModel
class HoroScopeViewModel @Inject constructor(private val horoscopeFetch: HoroscopeFetch) :
    BaseViewModel() {

    val todayHoroscope = MutableLiveData<HoroscopeData?>()
    val tomorrowHoroscope = MutableLiveData<HoroscopeData?>()

    val currentDateOfBirth = MutableLiveData(HoroscopeFetch.Zodiac.getDateOfBirth())

    var currentZodiac = HoroscopeFetch.Zodiac.getZodiacLocal()
    fun fetchCurrentDateOfBirth() {
        currentDateOfBirth.postValue(HoroscopeFetch.Zodiac.getDateOfBirth())
    }

    fun fetchHoroscope() {
        viewModelScope.launch {
            todayHoroscope.postValue(
                horoscopeFetch.getHoroscope(
                    currentZodiac, HoroscopeFetch.HoroScopeDay.TODAY
                )
            )

            fetchTomorrow()
        }
    }

    private fun fetchTomorrow() {
        viewModelScope.launch {
            tomorrowHoroscope.postValue(
                horoscopeFetch.getHoroscope(
                    currentZodiac, HoroscopeFetch.HoroScopeDay.TOMORROW
                )
            )
        }
    }
}
