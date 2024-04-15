package com.alarm.clock.reminder.alarmclock.simplealarm.view.myday

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseSingleAdapter
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseVMActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewHolder
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewModel
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.MyDayDiaryModel
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Plan
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Weather
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.repository.DiaryRepository
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ActivityAddDiaryBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ItemDiarySelectBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.setOnBackClick
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.setOnImageRightClick
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.dialog.DialogExit
import com.alarm.clock.reminder.alarmclock.simplealarm.view.reminder.DatePickerDialog
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class AddDiaryActivity : BaseVMActivity<ActivityAddDiaryBinding, AddDiaryViewModel>(),
    DatePickerDialog.OnDateChangedListener, DialogExit.OnCallBackExitListener {
    override val viewModel: AddDiaryViewModel by viewModels()
    private lateinit var weatherAdapter: ItemDiarySelectAdapter
    private lateinit var planAdapter: ItemDiarySelectAdapter

    override fun makeBinding(layoutInflater: LayoutInflater): ActivityAddDiaryBinding {
        return ActivityAddDiaryBinding.inflate(layoutInflater)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)

        onBackPressedDispatcher.addCallback {
            if (viewModel.isEdited(
                    binding.edtWeather.text.toString().trim(),
                    binding.edtPlan.getCText()
                )
            ) {
                DialogExit.show(supportFragmentManager)
            } else {
                finish()
            }
        }

        viewModel.date.observe(this) {
            val dateString = it.format(
                DateTimeFormatter.ofPattern("M/d/yyy")
            )
            binding.tvDate.text =
                if (it == LocalDate.now()) "${getString(R.string.today_lbl)}, $dateString" else dateString

            viewModel.fetchDiary()
        }

        intent?.extras?.getLong(MyDayFeelingInputActivity.ARG_DATE)?.let {
            viewModel.date.value = LocalDate.ofEpochDay(it)
        }

        binding.rcvWeather.apply {
            weatherAdapter = ItemDiarySelectAdapter {
                Weather.get(it.id)?.let { viewModel.weatherSelected.value = it }
                checkSaveAllow()
            }
            adapter = weatherAdapter
            layoutManager =
                LinearLayoutManager(this@AddDiaryActivity, LinearLayoutManager.HORIZONTAL, false)
            weatherAdapter.setupData(
                Weather.values().toList().map { ItemDiarySelect(it.id, it.icon, it.iconSelect) })
        }

        binding.rcvPlan.apply {
            planAdapter = ItemDiarySelectAdapter {
                Plan.get(it.id)?.let { viewModel.selectPlan(it) }
                checkSaveAllow()
            }
            adapter = planAdapter
            layoutManager =
                LinearLayoutManager(this@AddDiaryActivity, LinearLayoutManager.HORIZONTAL, false)
            planAdapter.setupData(
                Plan.values().toList().map { ItemDiarySelect(it.id, it.icon, it.iconSelect) })
        }

        viewModel.weatherSelected.observe(this) {
            it?.let {
                weatherAdapter.setSelected(setOf(ItemDiarySelect(it.id, it.icon, it.iconSelect)))
            } ?: weatherAdapter.setSelected(emptySet())
        }

        viewModel.planSelected.observe(this) {
            it?.let {
                planAdapter.setSelected(it.map { ItemDiarySelect(it.id, it.icon, it.iconSelect) }
                    .toSet())
            } ?: planAdapter.setSelected(emptySet())
        }

        binding.header.setOnBackClick {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.header.setOnImageRightClick {
            viewModel.saveDiary(
                binding.edtWeather.text.toString().trim(), binding.edtPlan.getCText()
            )

            finish()
        }

        viewModel.isSaveAllow.observe(this) {
            binding.header.imageRightDrawable =
                ContextCompat.getDrawable(this, if (it) R.drawable.ic_done else R.drawable.ic_tick)
            binding.header.actionRightView?.isEnabled = it
            binding.header.actionRightView?.isClickable = it
        }

        viewModel.selectedDiary.observe(this) {
            binding.edtPlan.setText(it?.planDetail ?: "")
            binding.edtWeather.setText(it?.weatherDetail ?: "")
            checkSaveAllow()
        }

        binding.btnDatePicker.setOnSingleClickListener {
            DatePickerDialog.show(
                supportFragmentManager,
                viewModel.date.value ?: LocalDate.now(),
                LocalDate.now()
            )
        }
    }

    private fun checkSaveAllow() {
        var isAllow = false
        isAllow = viewModel.weatherSelected.value != null
        viewModel.isSaveAllow.postValue(isAllow)
    }

    override fun onDateChanged(updated: LocalDate) {
        if (viewModel.isEdited(
                binding.edtWeather.text.toString().trim(),
                binding.edtPlan.getCText()
            )
        ) {
            viewModel.tempDate = updated
            DialogExit.show(supportFragmentManager, R.string.txtok)
        } else {
            viewModel.date.value = updated
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

    override fun onBackDialogOK() {
        if (viewModel.tempDate != null) {
            viewModel.date.value = viewModel.tempDate
            viewModel.tempDate = null
        } else {
            finish()
        }
    }
}

@HiltViewModel
class AddDiaryViewModel @Inject constructor(private val diaryRepository: DiaryRepository) :
    BaseViewModel() {
    var tempDate: LocalDate? = null
    val weatherSelected = MutableLiveData<Weather?>()
    val planSelected = MutableLiveData<Set<Plan>?>()
    val selectedDiary = MutableLiveData<MyDayDiaryModel?>()
    val date = MutableLiveData(LocalDate.now())
    val isSaveAllow = MutableLiveData(false)
    fun selectPlan(plan: Plan) {
        val tmp = mutableSetOf<Plan>()
        tmp.addAll(planSelected.value ?: emptySet())
        if (tmp.contains(plan)) {
            tmp.remove(plan)
        } else {
            tmp.add(plan)
        }
        planSelected.postValue(tmp)
    }

    fun fetchDiary() {
        viewModelScope.launch {
            val diary = diaryRepository.get(date.value!!)
            weatherSelected.postValue(diary?.weather)
            planSelected.postValue(diary?.plan?.toSet())
            selectedDiary.postValue(diary)
        }
    }

    fun isEdited(weatherDetail: String, planDetail: String): Boolean {
        return selectedDiary.value != newDiary(
            weatherDetail,
            planDetail
        ) && weatherSelected.value != null
    }

    fun newDiary(weatherDetail: String, planDetail: String): MyDayDiaryModel {
        return MyDayDiaryModel(
            date.value!!,
            weatherSelected.value,
            weatherDetail,
            planSelected.value?.toList() ?: emptyList(),
            planDetail
        )
    }

    fun saveDiary(weatherDetail: String, planDetail: String) {
        CoroutineScope(Dispatchers.IO).launch {
            diaryRepository.insert(
                newDiary(weatherDetail, planDetail)
            )
        }
    }
}

data class ItemDiarySelect(val id: Int, val icon: Int, val iconSelect: Int)

class ItemDiarySelectAdapter(private val itemSelected: (ItemDiarySelect) -> Unit) :
    BaseSingleAdapter<ItemDiarySelect, ItemDiarySelectBinding>() {
    private var selected = setOf<ItemDiarySelect>()
    override fun createViewBinding(
        parent: ViewGroup, viewType: Int
    ): ItemDiarySelectBinding = ItemDiarySelectBinding.inflate(
        LayoutInflater.from(parent.context), parent, false
    )

    fun setSelected(select: Set<ItemDiarySelect>) {
        this.selected = select
        notifyDataSetChanged()
    }

    override fun bindingViewHolder(
        holder: BaseViewHolder<ItemDiarySelectBinding>, position: Int
    ) {
        holder.binding.apply {
            val item = getItemAt(position) ?: return
            imgView.setImageResource(if (selected.contains(item)) item.iconSelect else item.icon)
        }
    }

    override fun createViewHolder(binding: ItemDiarySelectBinding): BaseViewHolder<ItemDiarySelectBinding> {
        return BaseViewHolder(binding).apply {
            this.binding.imgView.setOnClickListener {
                getItemAt(this.adapterPosition)?.let { it1 -> itemSelected.invoke(it1) }
            }
        }
    }

}