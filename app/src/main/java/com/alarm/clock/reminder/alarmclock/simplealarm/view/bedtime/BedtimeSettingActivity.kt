package com.alarm.clock.reminder.alarmclock.simplealarm.view.bedtime

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseVMActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewModel
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.BedTimeData
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.TimeSoundModel
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.repository.BedtimeRepository
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ActivityBedtimeSettingBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.receiver.helper.BedtimeHelper
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.returnDaySelectString1
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.setDaySelect
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.setOnClickTextDay
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.BEDTIME_DATA
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.TIME_SOUND
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.parcelable
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.adapter.BedtimeDateAdapter
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BedtimeSettingActivity :
    BaseVMActivity<ActivityBedtimeSettingBinding, BedTimeSettingViewModel>() {
    override val viewModel: BedTimeSettingViewModel by viewModels()

    private lateinit var bedtimeDateAdapter: BedtimeDateAdapter
    private var bedTimeData: BedTimeData? = null
    private var listDaySetTime: List<TimeSoundModel>? = null
    private val dialogChangeTimeBed = DialogChangeTimeBed()


    override fun makeBinding(layoutInflater: LayoutInflater): ActivityBedtimeSettingBinding {
        return ActivityBedtimeSettingBinding.inflate(layoutInflater)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)

        bedTimeData = intent.parcelable(BEDTIME_DATA)

        initView()

        bedTimeData?.let {
            viewModel.listDay.postValue(it.listTimes)
        }

        //listener change time
        dialogChangeTimeBed.setOnTimeChangedListener(object :
            DialogChangeTimeBed.OnTimeChangedListener {
            override fun onTimeChanged(updatedTime: TimeSoundModel) {
                bedtimeDateAdapter.updateTime(updatedTime)
            }
        })

        binding.imgBack.setOnSingleClickListener {
            finish()
        }

    }

    private fun initView() {

        bedtimeDateAdapter =
            BedtimeDateAdapter(this@BedtimeSettingActivity, onClickItem = { timeData ->
                val bundle = Bundle().apply {
                    putParcelable(TIME_SOUND, timeData)
                }
                dialogChangeTimeBed.arguments = bundle
                dialogChangeTimeBed.show(supportFragmentManager, "dialogChangeTime")
            })

        binding.baseTextview15.hint = returnDaySelectString1(
            this,
            bedTimeData?.listTimes?.map { it.day }?.toSet() ?: emptySet()
        )

        binding.recDay.apply {
            layoutManager = LinearLayoutManager(
                this@BedtimeSettingActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = bedtimeDateAdapter
        }

        viewModel.listDay.observe(this) { it ->
            listDaySetTime = it
            bedtimeDateAdapter.setUpList(it)
            binding.weekdayView.selectedDays.addAll(it.map { it.day })
            binding.weekdayView.setDaySelect()
            enabledSaveButton(it)
        }

        viewModel.enableSelectedDay.observe(this) {
            binding.imgDone.setImageResource(if (it) R.drawable.ic_done else R.drawable.ic_tick)
            binding.imgDone.isEnabled = it
        }

        binding.weekdayView.setOnClickTextDay { list, str ->
            binding.baseTextview15.clearFocus()
            binding.baseTextview15.hint =
                if (binding.weekdayView.selectedDays.isEmpty()) getString(R.string.enter) else str
            binding.baseTextview15.setHintTextColor(
                ContextCompat.getColor(
                    this,
                    if (binding.weekdayView.selectedDays.isEmpty()) R.color.newtral_6 else R.color.newtral_6
                )
            )

            val temp = mutableListOf<TimeSoundModel>()
            temp.addAll(viewModel.listDay.value ?: emptyList())

            viewModel.listDay.postValue(list.map { day ->
                TimeSoundModel(
                    day,
                    temp.firstOrNull { it.day == day }?.time ?: TimeSoundModel.timeDefault
                )
            })
        }

        binding.imgDone.setOnSingleClickListener {

            bedTimeData?.copy(
                listTimes = listDaySetTime ?: emptyList(),
                txt_Day = binding.weekdayView.returnDaySelectString(binding.weekdayView.selectedDays.toList())
            )
                ?.let { it2 ->
                    viewModel.updateBedtime(
                        it2
                    )
                }

            finish()
        }

    }

    private fun enabledSaveButton(timeData: List<TimeSoundModel>) {
        viewModel.enableSelectedDay.postValue(timeData.isNotEmpty())
    }

}

@HiltViewModel
class BedTimeSettingViewModel @Inject constructor(
    private val bedtimeRepository: BedtimeRepository,
    private val bedtimeHelper: BedtimeHelper
) : BaseViewModel() {
    val listDay = MutableLiveData<List<TimeSoundModel>>()

    val enableSelectedDay = MutableLiveData<Boolean>()

    val bedtimeData: LiveData<BedTimeData?> = bedtimeRepository.bedtimeData.map { it.firstOrNull() }


    //update time when change time dialog
    fun updateListDay(updatedTime: TimeSoundModel) {
        val currentList = listDay.value?.toMutableList() ?: mutableListOf()
        val position = currentList.indexOfFirst { it.day == updatedTime.day }
        if (position != -1) {
            currentList[position] = updatedTime
            listDay.postValue(currentList)
        }
    }


    fun insertBedtime(bedTimeData: BedTimeData) = viewModelScope.launch {
        bedtimeHelper.setBedtimeSleep(bedTimeData)
        bedtimeRepository.insertBedTime(bedTimeData)
    }

    fun updateBedtime(bedTimeData: BedTimeData) = viewModelScope.launch {
        if (bedTimeData.isOn) {
            bedtimeHelper.setBedtimeSleep(bedTimeData)
        } else {
            bedtimeHelper.cancelForBedtime(bedTimeData)
            bedtimeHelper.killService()
        }

        bedtimeRepository.updateBebTime(bedTimeData)

    }


    fun deleteBedtime(bedTimeData: BedTimeData) = viewModelScope.launch {
        bedtimeHelper.cancelForBedtime(bedTimeData)
        bedtimeHelper.killService()
        bedtimeRepository.deleteBebTime(bedTimeData)
    }

    suspend fun getBedTimeById(id: Int): BedTimeData {
        return bedtimeRepository.getBedTimeById(id)
    }
}