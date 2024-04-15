package com.alarm.clock.reminder.alarmclock.simplealarm.view.myday

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.view.children
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseVMActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewModel
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.MyDayDiaryModel
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.MyDayFeelingModel
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.repository.DiaryRepository
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.repository.MyDayFeelingRepo
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ActivityMydayCalendarBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.Example1CalendarDayBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.Language
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.locale
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.setOnBackClick
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.gone
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.visible
import com.alarm.clock.reminder.alarmclock.simplealarm.view.myday.MyDayCalendarActivity.Companion.ARG_TYPE_FEELING
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.WeekDayPosition
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class MyDayCalendarActivity :
    BaseVMActivity<ActivityMydayCalendarBinding, MyDayCalendarViewModel>() {

    companion object {
        const val ARG_TYPE = "ARG_TYPE"
        const val ARG_TYPE_DIARY = "ARG_TYPE_DIARY"
        const val ARG_TYPE_FEELING = "ARG_TYPE_FEELING"
    }

    override val viewModel: MyDayCalendarViewModel by viewModels()

    override fun makeBinding(layoutInflater: LayoutInflater): ActivityMydayCalendarBinding {
        return ActivityMydayCalendarBinding.inflate(layoutInflater)
    }

    private val today = LocalDate.now()
    private fun setupWeekCalendar(
        startMonth: YearMonth,
        endMonth: YearMonth,
        daysOfWeek: List<DayOfWeek>,
    ) {
        class WeekDayViewContainer(view: View) : ViewContainer(view) {
            // Will be set when this container is bound. See the dayBinder.
            lateinit var day: WeekDay
            val textView = Example1CalendarDayBinding.bind(view).exOneDayText

            init {
                view.setOnClickListener {
                    if (day.position == WeekDayPosition.RangeDate) {
                        dateClicked(date = day.date)
                    }
                }
            }
        }

        binding.exOneWeekCalendar.dayBinder = object : WeekDayBinder<WeekDayViewContainer> {
            override fun create(view: View): WeekDayViewContainer = WeekDayViewContainer(view)
            override fun bind(container: WeekDayViewContainer, data: WeekDay) {
                container.day = data
                bindDate(data.date, container.textView, data.position == WeekDayPosition.RangeDate)
            }
        }
        binding.exOneWeekCalendar.weekScrollListener = {
            checkNeedShowData()
            updateTitle()
        }
        binding.exOneWeekCalendar.setup(
            startMonth.atStartOfMonth(),
            endMonth.atEndOfMonth(),
            daysOfWeek.first(),
        )
        binding.exOneWeekCalendar.scrollToDate(LocalDate.now())

    }

    private fun bindDate(date: LocalDate, textView: TextView, isSelectable: Boolean) {
        textView.text = date.dayOfMonth.toString()
        if (date.toEpochDay() > today.toEpochDay()) {
            textView.setTextColor(Color.parseColor("#C6C6C9"))
            textView.background = null
        } else {
            if (isSelectable) {
                when {
                    viewModel.selectedDay.value == date -> {
                        textView.setTextColor(Color.WHITE)
                        textView.setBackgroundResource(R.drawable.bg_calendar_day_select)
                    }

                    today == date -> {
                        textView.setTextColor(Color.parseColor("#1A1C28"))
                        textView.setBackgroundResource(R.drawable.bg_calendar_day_today)
                    }

                    else -> {
                        textView.setTextColor(Color.parseColor("#1A1C28"))
                        textView.background = null
                    }
                }
            } else {
                textView.setTextColor(Color.parseColor("#1A1C28"))
                textView.background = null
            }
        }
    }

    private fun dateClicked(date: LocalDate) {
        if (date > today) return
        val lastSelected = viewModel.selectedDay.value!!
        viewModel.selectedDay.value = date
        binding.exOneWeekCalendar.notifyDateChanged(lastSelected)
        binding.exOneWeekCalendar.notifyDateChanged(date)
        viewModel.fetchData()
        checkNeedShowData()
    }

    private fun checkNeedShowData() {
        val week = binding.exOneWeekCalendar.findFirstVisibleWeek() ?: return

        if (today.weakOfYear() < week.days.first().date.weakOfYear()) return

        viewModel.visibleData.postValue(week.days.map { it.date }
            .contains(viewModel.selectedDay.value))
    }

    private fun updateTitle() {
        val week = binding.exOneWeekCalendar.findFirstVisibleWeek() ?: return

        val firstDate = week.days.first().date
        val lastDate = week.days.last().date

        if (today.weakOfYear() < week.days.first().date.weakOfYear()) {
            binding.exOneWeekCalendar.smoothScrollToDate(LocalDate.now())
        } else {
            (binding.headerdate.actionRightView as? ImageView)?.apply {
                setColorFilter(Color.parseColor(if (today < lastDate) "#C6C6C9" else "#1A1C28"))
                isActivated = today > lastDate
                isClickable = today > lastDate
            }

            week.days.forEachIndexed { index, weekDay ->
                if (weekDay.date > today) {
                    listWeakDayView.getOrNull(index)?.setTextColor(Color.parseColor("#C6C6C9"))
                } else {
                    listWeakDayView.getOrNull(index)?.setTextColor(Color.parseColor("#1A1C28"))
                }
            }

            binding.headerdate.titleString =
                "${firstDate.month.displayText(short = false)} ${firstDate.year}"

            showIndicator()
        }
    }

    private var listWeakDayView = emptyList<TextView>()
    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        intent?.extras?.getString(ARG_TYPE)?.let {
            viewModel.type.value = it
        }

        binding.header.setOnBackClick {
            finish()
        }
        setupCalendar()
        setupObser()

        viewModel.fetchData()
    }

    private fun setupCalendar() {
        val daysOfWeek = daysOfWeek(DayOfWeek.MONDAY)
        listWeakDayView = binding.legendLayout.root.children.map { it as TextView }.toList()

        listWeakDayView.forEachIndexed { index, textView ->
            textView.text = daysOfWeek[index].displayText(uppercase = true)
            textView.setTextColor(Color.BLACK)
        }

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)
        val endMonth = currentMonth.plusMonths(1)
        setupWeekCalendar(startMonth, endMonth, daysOfWeek)

        binding.headerdate.actionLeftView?.setOnClickListener {
            binding.exOneWeekCalendar.findFirstVisibleDay()?.date?.minusWeeks(1)
                ?.let { it1 -> binding.exOneWeekCalendar.scrollToWeek(it1) }
        }

        binding.headerdate.actionRightView?.setOnClickListener {
            binding.exOneWeekCalendar.findFirstVisibleDay()?.date?.plusWeeks(1)
                ?.let { it1 -> binding.exOneWeekCalendar.scrollToWeek(it1) }
        }
    }

    private fun showIndicator() {
        val week = binding.exOneWeekCalendar.findFirstVisibleWeek() ?: return
        week.days.forEachIndexed { index, weekDay ->
            if (weekDay.date == viewModel.selectedDay.value) {
                binding.indicator.root.findViewWithTag<ImageView>(index.toString())?.visible()
            } else {
                binding.indicator.root.findViewWithTag<ImageView>(index.toString())?.visibility =
                    View.INVISIBLE
            }
        }
    }

    private fun setupObser() {
        viewModel.type.observe(this) {
            binding.header.title =
                if (it == ARG_TYPE_FEELING) R.string.morning_feeling_title else R.string.diary_of_the_day_title

            binding.viewEmpty.tvTitle.text =
                getString(if (it == ARG_TYPE_FEELING) R.string.empty_feeling_title else R.string.empty_diary)
            binding.viewEmpty.grBtnCheck.visibility =
                if (it == ARG_TYPE_FEELING) View.VISIBLE else View.GONE
        }

        binding.viewEmpty.btnCheck.setOnSingleClickListener {
            startActivity(Intent(this, MyDayFeelingInputActivity::class.java).apply {
                putExtra(
                    MyDayFeelingInputActivity.ARG_DATE,
                    viewModel.selectedDay.value?.toEpochDay()
                )
            })
        }

        viewModel.visibleData.observe(this) {
            binding.groupData.visibility = if (it) View.VISIBLE else View.INVISIBLE
        }

        if (viewModel.type.value == ARG_TYPE_FEELING) {
            viewModel.morningFeeling.observe(this) {
                if (it != null) {
                    binding.viewEmpty.root.gone()
                    binding.feeling.root.visible()
                    binding.diary.root.gone()
                    it.setupFeeling(binding.feeling)
                } else {
                    binding.viewEmpty.root.visible()
                    binding.feeling.root.gone()
                    binding.diary.root.gone()
                }

                showIndicator()
            }
        } else {
            viewModel.diaryToday.observe(this) {
                if (it != null) {
                    binding.viewEmpty.root.gone()
                    binding.feeling.root.gone()
                    binding.diary.root.visible()
                    it.setupDiary(binding.diary)
                } else {
                    binding.viewEmpty.root.visible()
                    binding.feeling.root.gone()
                    binding.diary.root.gone()
                }
                showIndicator()
            }
        }

        binding.feeling.root.setOnSingleClickListener {
            startActivity(Intent(this, MyDayFeelingInputActivity::class.java).apply {
                putExtra(
                    MyDayFeelingInputActivity.ARG_DATE,
                    viewModel.selectedDay.value?.toEpochDay()
                )
            })
        }

        binding.diary.root.setOnSingleClickListener {
            startActivity(Intent(this, AddDiaryActivity::class.java).apply {
                putExtra(
                    MyDayFeelingInputActivity.ARG_DATE,
                    viewModel.selectedDay.value?.toEpochDay()
                )
            })
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchData()
    }
}

fun DayOfWeek.displayText(uppercase: Boolean = false): String {
    return getDisplayName(TextStyle.SHORT, Language.current.locale()).let { value ->
        if (uppercase) value.uppercase(Language.current.locale()) else value
    }.let {
        return@let if (Language.current == Language.FRENCH
            || Language.current == Language.PORTUGUESE
        ) it.dropLast(1)
        else it
    }
}

fun DayOfWeek.displayFullText(uppercase: Boolean = false): String {
    return getDisplayName(TextStyle.FULL, Language.current.locale()).let { value ->
        if (uppercase) value.uppercase(Language.current.locale()) else value.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Language.current.locale()
            ) else it.toString()
        }
    }
}

fun DayOfWeek.displayTextSelect(): String {
    return getDisplayName(TextStyle.SHORT, Language.current.locale())
        .uppercase(Language.current.locale())
        .let {
            return@let when (Language.current) {
                Language.ENGLISH -> it.dropLast(1)
                Language.HINDI -> it
                Language.CHINA -> it
                Language.SPANISH -> it.dropLast(1)
                Language.FRENCH -> it.dropLast(2)
                Language.PORTUGUESE -> it.dropLast(2)
            }
        }
}

fun Month.displayText(short: Boolean = true): String {
    val style = if (short) TextStyle.SHORT else TextStyle.FULL
    return getDisplayName(style, Language.current.locale())
}

@HiltViewModel
class MyDayCalendarViewModel @Inject constructor(
    private val myDayFeelingRepo: MyDayFeelingRepo,
    private val diaryRepository: DiaryRepository
) : BaseViewModel() {
    val selectedDay = MutableLiveData(LocalDate.now())
    val morningFeeling = MutableLiveData<MyDayFeelingModel?>()
    val diaryToday = MutableLiveData<MyDayDiaryModel?>()
    val type = MutableLiveData(ARG_TYPE_FEELING)
    val visibleData = MutableLiveData<Boolean>(false)

    fun fetchData() {
        if (type.value == ARG_TYPE_FEELING) fetchMorningFeeling()
        else fetchDiary()
    }

    private fun fetchDiary() {
        viewModelScope.launch {
            selectedDay.value?.let { diaryToday.postValue(diaryRepository.get(it)) }
        }
    }

    private fun fetchMorningFeeling() {
        viewModelScope.launch {
            selectedDay.value?.let { morningFeeling.postValue(myDayFeelingRepo.getFeeling(it)) }
        }
    }
}

fun LocalDate.weakOfYear(): Int {
    val weekFields = WeekFields.of(Locale.KOREA)
    return this.get(weekFields.weekOfWeekBasedYear())
}
