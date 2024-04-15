package com.alarm.clock.reminder.alarmclock.simplealarm.view.myday

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseVMFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewModel
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.MyDayDiaryModel
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.MyDayFeelingModel
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.repository.DiaryRepository
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.repository.MyDayFeelingRepo
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.FragmentMydayBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ViewDiaryContainerBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ViewFeelingContainerBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.gone
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject


@AndroidEntryPoint
class MyDayFragment : BaseVMFragment<FragmentMydayBinding, MyDayViewModel>() {
    override val viewModel: MyDayViewModel by viewModels()

    override fun makeBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentMydayBinding {
        return FragmentMydayBinding.inflate(inflater, container, false)
    }

    override fun setupView() {
        super.setupView()
        binding.cardDiary.setOnSingleClickListener {
            if (viewModel.diaryToday.value == null) {
                startActivity(Intent(mActivity, AddDiaryActivity::class.java))
            } else {
                startActivity(Intent(mActivity, MyDayCalendarActivity::class.java).apply {
                    putExtra(MyDayCalendarActivity.ARG_TYPE, MyDayCalendarActivity.ARG_TYPE_DIARY)
                })
            }
        }

        binding.cardFeeling.setOnSingleClickListener {
            if (viewModel.morningFeeling.value == null) {
                startActivity(Intent(mActivity, MyDayFeelingInputActivity::class.java))
            } else {
                startActivity(Intent(mActivity, MyDayCalendarActivity::class.java).apply {
                    putExtra(MyDayCalendarActivity.ARG_TYPE, MyDayCalendarActivity.ARG_TYPE_FEELING)
                })
            }
        }

        binding.cardHoroscope.setOnSingleClickListener {
            startActivity(Intent(mActivity, HoroScopeActivity::class.java))
        }

        viewModel.morningFeeling.observe(this) {
            it?.let {
                binding.feelingContainer.visible()
                it.setupFeeling(binding.feeling)
            } ?: binding.feelingContainer.gone()
        }

        viewModel.diaryToday.observe(this) {
            it?.let {
                binding.diaryContainer.visible()
                binding.baseTextview14.visibility = View.INVISIBLE
                it.setupDiary(binding.diary)
            } ?: apply {
                binding.diaryContainer.gone() ; binding.baseTextview14.visibility = View.VISIBLE
            }
        }

        binding.feelingContainer.setOnSingleClickListener {
            startActivity(Intent(mActivity, MyDayFeelingInputActivity::class.java).apply {
                putExtra(MyDayFeelingInputActivity.ARG_DATE, LocalDate.now().toEpochDay())
            })
        }

        binding.diaryContainer.setOnSingleClickListener {
            startActivity(Intent(mActivity, AddDiaryActivity::class.java).apply {
                putExtra(MyDayFeelingInputActivity.ARG_DATE, LocalDate.now().toEpochDay())
            })
        }

        viewModel.fetchMorningFeeling()
        viewModel.fetchDiary()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchMorningFeeling()
        viewModel.fetchDiary()
    }
}

@HiltViewModel
class MyDayViewModel @Inject constructor(
    private val myDayFeelingRepo: MyDayFeelingRepo, private val diaryRepository: DiaryRepository
) : BaseViewModel() {
    val morningFeeling = MutableLiveData<MyDayFeelingModel?>()
    val diaryToday = MutableLiveData<MyDayDiaryModel?>()

    fun fetchDiary() {
        viewModelScope.launch {
            diaryToday.postValue(diaryRepository.get(LocalDate.now()))
        }
    }

    fun fetchMorningFeeling() {
        viewModelScope.launch {
            morningFeeling.postValue(myDayFeelingRepo.getFeeling(LocalDate.now()))
        }
    }
}

fun MyDayFeelingModel.setupFeeling(view: ViewFeelingContainerBinding) {
    this.feeling?.icon?.let { it1 -> view.imgFeeling.setImageResource(it1) }
    view.tvFeeling.text =
        this.feeling?.stringName?.let { it1 -> view.root.context.getString(it1) }
    view.tvFeelingDate.text =
        this.date.format(DateTimeFormatter.ofPattern("M/d/yyyy"))
}

fun MyDayDiaryModel.setupDiary(view: ViewDiaryContainerBinding) {
    this.weather?.iconSelect?.let { it1 -> view.imgDiaryWeather.setImageResource(it1) }
    view.tvDiaryDate.text = this.date.format(DateTimeFormatter.ofPattern("M/d/yyyy"))
    view.rcvPlan.removeAllViews()
    view.tvPlan.text = this.planDetail
    this.plan.take(3).forEach {
        val img = ImageView(view.root.context)
        img.setImageResource(it.iconDef)
        img.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        img.adjustViewBounds = true
        view.rcvPlan.addView(img)
    }
}