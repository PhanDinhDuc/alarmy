package com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.text.color
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.recyclerview.widget.GridLayoutManager
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseVMActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.Settings
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.Settings.Companion.alarmSettings
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Alarm
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Alarm.Companion.NO_ID
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.getDayTxt
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.repository.AlarmRepository
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ActivityAddAlarmBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.receiver.helper.AlarmHelper
import com.alarm.clock.reminder.alarmclock.simplealarm.receiver.helper.canScheduleExactAlarms
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.returnDaySelectString1
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.setDaySelect
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.setOnClickTextDay
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.parcelable
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.dialog.DialogExit
import com.alarm.clock.reminder.alarmclock.simplealarm.view.permission.PermissionActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.sound.AlarmSoundActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.sound.AlarmSoundActivity.Companion.KEY_IS_VIBRATE
import com.alarm.clock.reminder.alarmclock.simplealarm.view.sound.AlarmSoundActivity.Companion.KEY_SOUND_PATH
import com.alarm.clock.reminder.alarmclock.simplealarm.view.sound.AlarmSoundActivity.Companion.KEY_VOLUME
import com.alarm.clock.reminder.alarmclock.simplealarm.view.sound.getNameSound
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TASK_SELECTED
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskSettingModel
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.choseTask.ChooseTaskActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.viewmodels.AlarmViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class AddAlarmActivity : BaseVMActivity<ActivityAddAlarmBinding, AddAlarmViewModel>(),
    DialogExit.OnCallBackExitListener, SnoozeActivity.CallBackSnoozeListener {

    override val viewModel: AddAlarmViewModel by viewModels()
    private lateinit var selectedDays: MutableList<Int>
//    private var tasks: List<TaskSettingModel>? = null

    private var snooze: Long = Snooze.M5.duration
    private var isVibrate = false
    private var volume: Int = 0

    private lateinit var alarmSoundLauncher: ActivityResultLauncher<Intent>
    private lateinit var soundPath: String
    private lateinit var adapter: AdapterTask
    private var alarm: Alarm? = null
    private var currentDays: MutableList<Int> = mutableListOf()
    private var currentSettingModel: MutableList<TaskSettingModel> = mutableListOf()

    @SuppressLint("SetTextI18n")
    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        SnoozeActivity.listener = this
        selectedDays = mutableListOf()
        soundPath = alarmSettings.soundPath

        addKeyboardToggleListener { shown ->
            if (!shown) binding.txtDay.clearFocus()
        }
        alarm = intent.parcelable(Util.ALARM_ARG) ?: viewModel.getTemplate()
        alarm?.let {
            binding.timePicker.setTimePicker(it.time)
            SetDataTaskSetting.instance?.clearTaskSettings()
            it.tasks?.forEach { SetDataTaskSetting.instance?.addTaskSetting(it) }
            snooze = it.snooze.toLong()
            isVibrate = it.isVibrate
            volume = it.soundLevel
            soundPath = it.soundPath
            currentDays = it.days as MutableList<Int>
            currentSettingModel = it.tasks as MutableList<TaskSettingModel>
            it.days?.let { it1 -> selectedDays.addAll(it1) }
            it.days?.let { it1 -> binding.mWeekday.selectedDays.addAll(it1) }
            it.days?.let { it1 -> binding.mWeekday.setDaySelect() }
            binding.txtDay.setText(it.label)
            binding.txtDay.hint =
                it.getDayTxt(this).ifEmpty { getString(R.string.enter) }
            binding.txtDay.setHintTextColor(
                ContextCompat.getColor(
                    this,
                    if (it.getDayTxt(this).isEmpty()) R.color.newtral_3 else R.color.newtral_6
                )
            )
            viewModel.tasks.postValue(SetDataTaskSetting.instance?.getTaskSettings())
            enabledSaveButton()
        } ?: apply {
            listOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)).let { it1 ->
                selectedDays.addAll(it1)
                binding.mWeekday.selectedDays.addAll(it1)
                binding.mWeekday.setDaySelect()
                isVibrate = Settings.IS_VIBRATE.get(false)
                binding.txtDay.hint =
                    returnDaySelectString1(this, it1.toSet()).ifEmpty { getString(R.string.enter) }
                binding.txtDay.setHintTextColor(
                    ContextCompat.getColor(
                        this,
                        if (returnDaySelectString1(
                                this,
                                it1.toSet()
                            ).isEmpty()
                        ) R.color.newtral_3 else R.color.newtral_6
                    )
                )
            }

            enabledSaveButton()
        }

        binding.txtTime.text = if (snooze > 0) getString(
            R.string.not_space_min,
            (snooze / 1000) / 60
        ) else getString(R.string.off)
        binding.mTxtNameSound.text = soundPath.getNameSound(this)
        binding.timePicker.timeChangeListener = {
            enabledSaveButton()
        }

        binding.imgDone.setOnSingleClickListener {
            if (canScheduleExactAlarms()) {
                insertAlarm()
            } else {
                val intent = Intent(this, PermissionActivity::class.java)
                startActivity(intent)
            }
        }
        binding.imgBack.setOnSingleClickListener {
            if (selectedDays.toSet() != currentDays.toSet() || SetDataTaskSetting.instance?.getTaskSettings()
                    ?.toSet() != currentSettingModel.toSet()
            ) {
                DialogExit.show(supportFragmentManager)
            } else {
                SetDataTaskSetting.instance?.clearTaskSettings()
                finish()
            }
        }

        binding.mSnooze.setOnSingleClickListener {
            val intent = Intent(this, SnoozeActivity::class.java)
            intent.putExtra("keySnoozeAlarm", snooze)
            startActivity(intent)
        }

        binding.mWeekday.setOnClickTextDay { list, strg ->
            binding.txtDay.clearFocus()
            binding.txtDay.hint =
                if (binding.mWeekday.selectedDays.isEmpty()) getString(R.string.enter) else strg
            binding.txtDay.setHintTextColor(
                ContextCompat.getColor(
                    this,
                    if (binding.mWeekday.selectedDays.isEmpty()) R.color.newtral_3 else R.color.newtral_6
                )
            )
            selectedDays.clear()
            selectedDays.addAll(binding.mWeekday.selectedDays)
            enabledSaveButton()
        }

        binding.mAlarmSound.setOnSingleClickListener {
            val intent = Intent(this, AlarmSoundActivity::class.java)
            intent.putExtra("sounds", soundPath)
            intent.putExtra("isVibrate", isVibrate)
            alarmSoundLauncher.launch(intent)
        }

        viewModel.tasks.map { it.size }.observe(this) {
            val s = SpannableStringBuilder().append("(")
                .color(Color.parseColor(if (it == 0) "#C6C6C9" else "#1E6AB0")) { append("$it") }
                .append("/3)")
            binding.textView12.text = s
        }

        alarmSoundLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data: Intent? = result.data
                    if (data != null) {
                        soundPath = data.getStringExtra(KEY_SOUND_PATH).toString()
                        isVibrate = data.getBooleanExtra(KEY_IS_VIBRATE, false)
                        volume = data.getIntExtra(KEY_VOLUME, 0)
                        binding.mTxtNameSound.text = soundPath.getNameSound(this)
                        enabledSaveButton()
                    }

                } else if (result.resultCode == RESULT_FROM_TASK) {
                    viewModel.tasks.postValue(SetDataTaskSetting.instance?.getTaskSettings())
                    SetDataTaskSetting.instance?.getVTaskSettings()?.let { adapter.setupData(it) }
                    enabledSaveButton()
                }
            }

        viewModel.enableSaveButton.observe(this) {
            binding.imgDone.setImageResource(if (it) R.drawable.ic_done else R.drawable.ic_tick)
            binding.imgDone.isEnabled = it
        }

        setupListTask()
    }

    private fun enabledSaveButton() {
        viewModel.enableSaveButton.postValue(selectedDays.isNotEmpty())
    }

    private fun setupListTask() {
        binding.rc.layoutManager = GridLayoutManager(this, 3)
        adapter = AdapterTask(selected = {
            viewModel.tasks.postValue(SetDataTaskSetting.instance?.getTaskSettings())
        }) { model, position ->
            if (model is AddButton) {
                alarmSoundLauncher.launch(Intent(this, ChooseTaskActivity::class.java).apply {
                    putExtra(Util.ALARM_ARG, getAlarmData())
                })
            } else {
                val selectedTask = model as? TaskSettingModel ?: return@AdapterTask
                val i = Intent(this, ChooseTaskActivity::class.java).apply {
                    putExtra(TASK_SELECTED, selectedTask)
                    putExtra("POSITION_TASK", position)
                    putExtra(Util.ALARM_ARG, getAlarmData())
                }
                alarmSoundLauncher.launch(i)
            }
        }
        binding.rc.adapter = adapter

//        tasks = SetDataTaskSetting.instance?.getTaskSettings()
        SetDataTaskSetting.instance?.getVTaskSettings()?.let { adapter.setupData(it) }
    }


    private fun insertAlarm() {
        viewModel.insertAlarm(getAlarmData())
        listener?.onShowToast(Util.convertMillisToTimeFormat(this, getAlarmData()))
        SetDataTaskSetting.instance?.clearTaskSettings()
        finish()
    }

    private fun getAlarmData(): Alarm {
        return Alarm(
            if ((alarm?.id ?: -1) > 0) alarm?.id!! else NO_ID,
            binding.txtDay.text.toString().trim(),
            binding.timePicker.getTimePicker(),
            binding.mWeekday.returnDaySelectString(binding.mWeekday.selectedDays.toList()),
            true,
            snooze.toInt(),
            0,
            selectedDays.sorted(),
            SetDataTaskSetting.instance?.getTaskSettings() ?: emptyList(),
            soundPath,
            volume,
            isVibrate,
            false,
            isQuickAlarm = false,
            null
        )
    }

    override fun makeBinding(layoutInflater: LayoutInflater): ActivityAddAlarmBinding {
        return ActivityAddAlarmBinding.inflate(layoutInflater)
    }

    companion object {
        const val RESULT_FROM_TASK = 1999
        var listener: OnShowToastInsertNewAlarmListener? = null
    }

    override fun onBackDialogOK() {
        SetDataTaskSetting.instance?.clearTaskSettings()
        finish()
    }

    override fun onSnoozeListener(snoozeDuration: Long) {
        snooze = snoozeDuration
        binding.txtTime.text = if (snoozeDuration > 0) getString(
            R.string.not_space_min,
            (snoozeDuration / 1000) / 60
        ) else getString(R.string.off)

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

interface OnShowToastInsertNewAlarmListener {
    fun onShowToast(text: String)
}

@HiltViewModel
class AddAlarmViewModel @Inject constructor(
    alarmRepository: AlarmRepository, alarmHelper: AlarmHelper, gson: Gson
) : AlarmViewModel(alarmRepository, alarmHelper, gson) {
    val tasks = MutableLiveData<List<TaskSettingModel>>()
    val enableSaveButton = MutableLiveData(false)
}

fun Activity.addKeyboardToggleListener(onKeyboardToggleAction: (shown: Boolean) -> Unit): KeyboardToggleListener? {
    val root = findViewById<View>(android.R.id.content)
    val listener = KeyboardToggleListener(root, onKeyboardToggleAction)
    return root?.viewTreeObserver?.run {
        addOnGlobalLayoutListener(listener)
        listener
    }
}

fun Fragment.addKeyboardToggleListener(
    root: View,
    onKeyboardToggleAction: (shown: Boolean) -> Unit
): KeyboardToggleListener? {
    val listener = KeyboardToggleListener(root, onKeyboardToggleAction)
    return root.viewTreeObserver?.run {
        addOnGlobalLayoutListener(listener)
        listener
    }
}

open class KeyboardToggleListener(
    private val root: View?,
    private val onKeyboardToggleAction: (shown: Boolean) -> Unit
) : ViewTreeObserver.OnGlobalLayoutListener {
    private var shown = false
    override fun onGlobalLayout() {
        root?.run {
            val heightDiff = rootView.height - height
            val keyboardShown = heightDiff > dpToPx(200f)
            if (shown != keyboardShown) {
                onKeyboardToggleAction.invoke(keyboardShown)
                shown = keyboardShown
            }
        }
    }
}

fun View.dpToPx(dp: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
        .roundToInt()