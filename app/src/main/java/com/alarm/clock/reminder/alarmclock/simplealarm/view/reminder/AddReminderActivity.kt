package com.alarm.clock.reminder.alarmclock.simplealarm.view.reminder

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseVMActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewModel
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Reminder
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.ReminderType
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Repeat
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.RepeatMode
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.getString
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.repository.ReminderRepository
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ActivityAddReminderBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.LayoutRepeatBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.setOnClickDay
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.SingleLiveEvent
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.gone
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.visible
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.addKeyboardToggleListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.dialog.DialogDeleteBarcode
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.dialog.DialogExit
import com.alarm.clock.reminder.alarmclock.simplealarm.view.sound.AlarmDefault
import com.alarm.clock.reminder.alarmclock.simplealarm.view.sound.AlarmSoundActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.sound.getNameSound
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class AddReminderActivity : BaseVMActivity<ActivityAddReminderBinding, AddReminderViewModel>(),
    TimePicker.OnTimeChangedListener, NumberPickerDialog.OnNumberChangedListener,
    DatePickerDialog.OnDateChangedListener, DialogDeleteBarcode.OnOkDeleteListener,
    DialogExit.OnCallBackExitListener {

    override val viewModel: AddReminderViewModel by viewModels()
    override fun makeBinding(layoutInflater: LayoutInflater): ActivityAddReminderBinding {
        return ActivityAddReminderBinding.inflate(layoutInflater)
    }

    private var popupWindow: PopupWindow? = null
    private val alarmSoundLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val soundPath =
                        data.getStringExtra(AlarmSoundActivity.KEY_SOUND_PATH).toString()
                    viewModel.soundPath.postValue(soundPath)
                    val isVibrate = data.getBooleanExtra(AlarmSoundActivity.KEY_IS_VIBRATE, false)
                    viewModel.isVibrate = (isVibrate)
                    val volume = (data.getIntExtra(AlarmSoundActivity.KEY_VOLUME, 0))
                    viewModel.volume = volume
                    viewModel.isEdit =
                        (soundPath != viewModel.reminder?.alertTonePath || isVibrate != viewModel.reminder?.isVibrate || volume != viewModel.reminder?.soundLevel)
                }
            }
        }

    private val typeAdapter by lazy {
        ReminderTypeAdapter { type ->
            viewModel.isEdit = type != viewModel.reminder?.type
            viewModel.reminderType.postValue(type)
        }
    }

    private val repeatSeveralAdapter by lazy {
        RepeatSeveralAdapter(this) { time, idx ->
            TimePicker.show(
                supportFragmentManager,
                time,
                PickType.ChangeSeveralTime(idx)
            )
        }
    }

    @SuppressLint("SetTextI18n")
    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        addKeyboardToggleListener { shown ->
            if (!shown) binding.edtName.clearFocus()
        }
        binding.weekday.setOnClickDay { values, isAll ->
            val new = if (isAll) RepeatMode.Daily
            else if (values.isEmpty()) RepeatMode.None
            else RepeatMode.Weekly(values)
            viewModel.isEdit = new != viewModel.reminder?.repeatMode
            viewModel.currentRepeatMode.postValue(new)
        }
        binding.month.setOnChange { values, _ ->
            val new = if (values.isEmpty()) RepeatMode.None
            else RepeatMode.Monthly(values)
            viewModel.isEdit = new != viewModel.reminder?.repeatMode
            viewModel.currentRepeatMode.postValue(new)
        }
        binding.repeatText.setOnSingleClickListener {
            viewModel.showPopup.postValue(it)
        }
        binding.imageView.setOnSingleClickListener {
            viewModel.showPopup.postValue(binding.repeatText)
        }
        binding.time.setOnSingleClickListener {
            TimePicker.show(
                supportFragmentManager,
                viewModel.time.value ?: LocalTime.now()
            )
        }
        binding.date.setOnSingleClickListener {
            DatePickerDialog.show(
                supportFragmentManager,
                viewModel.date.value ?: LocalDate.now(),
            )
        }
        binding.repeatSeveral.adapter = repeatSeveralAdapter
        binding.typePicker.adapter = typeAdapter
        typeAdapter.setupData(ReminderType.values().toList())
        binding.repeatType.setOnSingleClickListener {
            when (viewModel.currentRepeatMode.value) {
                is RepeatMode.EveryHour -> {
                    NumberPickerDialog.show(
                        supportFragmentManager,
                        viewModel.repeatEveryHour.value ?: 8,
                        NumberPickType.RepeatTime
                    )
                }

                is RepeatMode.SeveralTimeInDay -> {
                    NumberPickerDialog.show(
                        supportFragmentManager,
                        viewModel.repeatSeveralTime.value ?: 6,
                        NumberPickType.SeveralTime
                    )
                }

                else -> {
                    return@setOnSingleClickListener
                }
            }
        }
        binding.imgDone.setOnSingleClickListener {
            viewModel.insertOrUpdate(binding.edtName.text.toString())
        }
        binding.imgBack.setOnSingleClickListener {
            if (viewModel.isEdit) {
                DialogExit.show(supportFragmentManager)
            } else finish()
        }
        binding.imgDelete.setOnSingleClickListener {
            DialogDeleteBarcode.show(
                supportFragmentManager,
                getString(R.string.do_you_want_to_delete_reminder)
            )
        }
        binding.edtName.doOnTextChanged { text, _, _, _ ->
            viewModel.isEdit = text.toString() != viewModel.reminder?.name
            binding.edtName.typeface = Typeface.create(
                binding.edtName.typeface,
                if (text.isNullOrEmpty()) Typeface.NORMAL else Typeface.BOLD
            )
            viewModel.isEnableAdd.postValue(!text.isNullOrEmpty())
        }
        binding.maskPopup.setOnSingleClickListener {
            viewModel.showPopup.postValue(null)
        }
        binding.alertTone.setOnSingleClickListener {
            val intent = Intent(this, AlarmSoundActivity::class.java)
            intent.putExtra("sounds", viewModel.soundPath.value ?: AlarmDefault(this))
            intent.putExtra("isVibrate", viewModel.isVibrate)
            intent.putExtra("isReminder", true)
            alarmSoundLauncher.launch(intent)
        }
        observeData()
    }

    private fun observeData() {
        viewModel.currentRepeatMode.observe(this) {
            viewModel.isEdit = it != viewModel.tmpRepeatMode
            binding.repeatText.setText(Repeat.getRepeat(it.id).getString())
            when (it) {
                RepeatMode.None -> {
                    binding.repeatSeveral.gone()
                    binding.weekday.gone()
                    binding.month.gone()
                    binding.repeatType.gone()
                }

                RepeatMode.Annually -> {
                    binding.repeatSeveral.gone()
                    binding.weekday.gone()
                    binding.month.gone()
                    binding.repeatType.gone()
                    binding.month.selectedMonth = Month.values().toMutableList()
                }

                RepeatMode.Daily -> {
                    binding.weekday.visible()
                    binding.weekday.selectedDays = DayOfWeek.values().toMutableList()
                    binding.repeatSeveral.gone()
                    binding.month.gone()
                    binding.repeatType.gone()
                }

                is RepeatMode.EveryHour -> {
                    binding.repeatSeveral.gone()
                    binding.weekday.gone()
                    binding.month.gone()
                    binding.repeatType.visible()
                    viewModel.repeatEveryHour.postValue(it.hour)
                }

                is RepeatMode.Monthly -> {
                    binding.repeatSeveral.gone()
                    binding.weekday.gone()
                    binding.repeatType.gone()
                    binding.month.visible()
                    binding.month.selectedMonth =
                        it.months.map { id -> Month.of(id) }.toMutableList()
                }

                is RepeatMode.SeveralTimeInDay -> {
                    binding.repeatType.visible()
                    binding.repeatSeveral.visible()
                    binding.weekday.gone()
                    binding.month.gone()
                    viewModel.repeatSeveralValues.postValue(it.times)
                }

                is RepeatMode.Weekly -> {
                    binding.weekday.visible()
                    binding.repeatSeveral.gone()
                    binding.month.gone()
                    binding.repeatType.gone()
                    binding.weekday.selectedDays =
                        it.days.map { id -> DayOfWeek.of(id) }.toMutableList()
                }
            }
        }
        viewModel.date.observe(this) {
            val dateFormatters = DateTimeFormatter.ofPattern("MM/dd/yyyy")
            binding.dateText.text = it.format(dateFormatters)
        }
        viewModel.time.observe(this) {
            val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a").withLocale(Locale.US)
            binding.timeText.text = it.format(timeFormatter)
        }
        viewModel.repeatEveryHour.observe(this) {
            binding.repeatType.text = getString(R.string.every_hours, it)
            val currentRepeatMode = viewModel.currentRepeatMode.value
            val newRepeatMode = RepeatMode.EveryHour(it)
            if (currentRepeatMode != newRepeatMode) viewModel.currentRepeatMode.postValue(
                newRepeatMode
            )
        }
        viewModel.repeatSeveralTime.observe(this) {
            binding.repeatType.text = getString(R.string.times, it)
        }
        viewModel.repeatSeveralValues.observe(this) {
            viewModel.repeatSeveralTime.postValue(it.size)
            repeatSeveralAdapter.setupData(it)
            val currentRepeatMode = viewModel.currentRepeatMode.value
            val newRepeatMode = RepeatMode.SeveralTimeInDay(it)
            if (currentRepeatMode != newRepeatMode) viewModel.currentRepeatMode.postValue(
                newRepeatMode
            )
        }
        viewModel.reminderType.observe(this) {
            typeAdapter.update(it)
        }
        viewModel.name.observe(this) {
            binding.edtName.setText(it)
            binding.imgDelete.visible()
            binding.label.text = getString(R.string.edit_reminders)
        }
        viewModel.backEvent.observe(this) {
            if (it) {
                val resultIntent = Intent()
                resultIntent.putExtra(KEY_IS_DELETE, true)
                setResult(RESULT_OK, resultIntent)
            }
            finish()
        }
        viewModel.isEnableAdd.observe(this) {
            binding.imgDone.isEnabled = it
            binding.imgDone.setImageResource(if (it == true) R.drawable.archive_tick_enable else R.drawable.archive_tick)
        }
        viewModel.soundPath.observe(this) {
            binding.mTxtNameSound.text = it.getNameSound(this)
        }
        viewModel.showPopup.observe(this) {
            if (it != null) {
                showPopUp(it)
                binding.maskPopup.visible()
            } else {
                popupWindow?.dismiss()
                binding.maskPopup.gone()
            }
        }
    }


    private fun showPopUp(
        it: View?,
    ) {
        popupWindow?.dismiss()
        val layout = LayoutRepeatBinding.inflate(
            LayoutInflater.from(this),
            null,
            false
        )

        val width: Int = LinearLayout.LayoutParams.WRAP_CONTENT
        val height: Int = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = false

        popupWindow = PopupWindow(
            layout.root, width, height, focusable
        )

        layout.recyclerview.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val adapter = RepeatAdapter {
            viewModel.currentRepeatMode.postValue(
                when (it) {
                    Repeat.None -> RepeatMode.None
                    Repeat.Daily -> RepeatMode.Daily
                    Repeat.Weekly -> RepeatMode.Weekly(
                        listOf(
                            viewModel.date.value?.dayOfWeek ?: DayOfWeek.MONDAY
                        ).map { dayOfWeek -> dayOfWeek.value })

                    Repeat.Monthly -> RepeatMode.Monthly(
                        listOf(
                            viewModel.date.value?.month ?: Month.JANUARY
                        ).map { month -> month.value })

                    Repeat.Annually -> RepeatMode.Annually
                    Repeat.EveryHour -> RepeatMode.EveryHour(viewModel.repeatEveryHour.value ?: 8)
                    Repeat.SeveralTimeInDay -> {
                        val value = viewModel.repeatSeveralValues.value ?: 6.makeSeveralTimes()
                        RepeatMode.SeveralTimeInDay(value)
                    }
                }
            )
            viewModel.showPopup.postValue(null)
        }
        layout.recyclerview.adapter = adapter
        adapter.setupData(Repeat.values().toMutableList())
        popupWindow?.elevation = 10f
        popupWindow?.setOnDismissListener {
            viewModel.showPopup.postValue(null)
        }
        popupWindow?.setBackgroundDrawable(null)
        layout.root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val measuredWidth = layout.root.measuredWidth
        popupWindow?.showAsDropDown(
            it, -measuredWidth, 0, Gravity.END
        )
    }

    private fun Int.makeSeveralTimes(): List<LocalTime> {
        val items = mutableListOf<LocalTime>()
        val current = viewModel.time.value ?: LocalTime.now()
        repeat(this) { count ->
            items.add(current.plusHours(1L + count))
        }
        return items
    }

    override fun onTimeChanged(updatedTime: LocalTime, type: PickType) {
        when (type) {
            is PickType.ChangeSeveralTime -> {
                val repeatSeveralValues =
                    viewModel.repeatSeveralValues.value?.toMutableList() ?: mutableListOf()
                if (repeatSeveralValues.size - 1 >= type.idx) {
                    repeatSeveralValues[type.idx] = updatedTime
                    viewModel.repeatSeveralValues.postValue(repeatSeveralValues.distinct())
                }
            }

            PickType.ChangeTime -> {
                viewModel.isEdit = updatedTime != viewModel.reminder?.time
                viewModel.time.postValue(updatedTime)
            }
        }
    }

    override fun onNumberChanged(number: Int, type: NumberPickType) {
        when (type) {
            NumberPickType.RepeatTime -> viewModel.repeatEveryHour.postValue(number)
            NumberPickType.SeveralTime -> {
                val current = viewModel.repeatSeveralValues.value
                    ?: number.makeSeveralTimes()
                if (current.size != number) {
                    if (current.size > number) {
                        viewModel.repeatSeveralValues.postValue(current.subList(0, number))
                    } else {
                        val last = current.last()
                        val mutableList = current.toMutableList()
                        repeat(number - current.size) { count ->
                            mutableList.add(last.plusHours(count + 1L))
                        }
                        viewModel.repeatSeveralValues.postValue(mutableList)
                    }
                }
            }
        }
    }

    override fun onPause() {
        viewModel.showPopup.postValue(null)
        super.onPause()
    }

    override fun onStop() {
        viewModel.showPopup.postValue(null)
        super.onStop()
    }

    override fun onDestroy() {
        viewModel.showPopup.postValue(null)
        super.onDestroy()
    }

    override fun onDateChanged(updated: LocalDate) {
        viewModel.isEdit = updated != viewModel.reminder?.date
        viewModel.date.postValue(updated)
    }

    override fun onOkDeleteListener() {
        viewModel.delete()
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

    companion object {
        const val KEY_IS_DELETE = "KEY_IS_DELETE"
    }

    override fun onBackDialogOK() {
        finish()
    }
}


@HiltViewModel
class AddReminderViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    companion object {
        const val REMINDER_KEY = "reminder"
    }

    val showPopup = MutableLiveData<View?>()
    var isEdit = false
    var tmpRepeatMode: RepeatMode = RepeatMode.None
    val date = MutableLiveData(LocalDate.now())
    val time = MutableLiveData(LocalTime.now())
    val currentRepeatMode = MutableLiveData(tmpRepeatMode)
    val repeatEveryHour = MutableLiveData<Int>()
    val repeatSeveralTime = MutableLiveData<Int>()
    val repeatSeveralValues = MutableLiveData<List<LocalTime>>()
    val reminderType = MutableLiveData(ReminderType.NOTIFICATION)
    val name = MutableLiveData<String>()
    val isEnableAdd = MutableLiveData(false)
    var id: Int = 0
    val backEvent = SingleLiveEvent<Boolean>()
    var volume = 100
    var isVibrate = true
    val soundPath = MutableLiveData(AlarmDefault())
    var reminder: Reminder? = null

    init {
        savedStateHandle.get<Reminder>(REMINDER_KEY)?.let {
            this.reminder = it
            id = it.id
            date.postValue(it.date)
            time.postValue(it.time)
            reminderType.postValue(it.type)
            name.postValue(it.name)
            soundPath.postValue(it.alertTonePath)
            isVibrate = it.isVibrate
            volume = it.soundLevel
            tmpRepeatMode = it.repeatMode
            currentRepeatMode.postValue(it.repeatMode)
        }
    }

    fun delete() {
        viewModelScope.launch(Dispatchers.IO) {
            reminderRepository.delete(id)
            backEvent.postValue(true)
        }
    }

    fun insertOrUpdate(name: String) {
        viewModelScope.launch {
            val reminder = Reminder(
                id = id,
                type = reminderType.value ?: ReminderType.STAR,
                name = name,
                date = date.value ?: LocalDate.now(),
                time = time.value ?: LocalTime.now(),
                repeatMode = currentRepeatMode.value ?: RepeatMode.None,
                alertTonePath = soundPath.value ?: "",
                soundLevel = volume,
                isVibrate = isVibrate
            )
            if (id == 0) {
                reminderRepository.insert(reminder)
            } else {
                reminderRepository.update(reminder)
            }
            backEvent.postValue(false)
        }
    }


}
