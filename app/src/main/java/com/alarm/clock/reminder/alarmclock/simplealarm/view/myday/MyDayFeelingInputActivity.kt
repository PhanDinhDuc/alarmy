package com.alarm.clock.reminder.alarmclock.simplealarm.view.myday

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseSingleAdapter
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseVMActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewHolder
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewModel
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Feeling
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.MyDayFeelingModel
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.repository.MyDayFeelingRepo
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ActivityMyDayFeelingBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ItemInputFeelingBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.setOnBackClick
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class MyDayFeelingInputActivity :
    BaseVMActivity<ActivityMyDayFeelingBinding, MyDayFeelingInputViewModel>() {
    override val viewModel: MyDayFeelingInputViewModel by viewModels()

    companion object {
        const val ARG_DATE = "ARG_DATE"
    }

    override fun makeBinding(layoutInflater: LayoutInflater): ActivityMyDayFeelingBinding =
        ActivityMyDayFeelingBinding.inflate(layoutInflater)

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)

        intent?.extras?.getLong(ARG_DATE)?.let {
            viewModel.date = LocalDate.ofEpochDay(it)
            viewModel.fetchMorningFeeling()
        }

        binding.btnNext.setOnSingleClickListener {
            viewModel.insertFeeling()
            finish()
        }

        binding.header.setOnBackClick {
            finish()
        }

        binding.recyclerView.apply {
            val mAdapter = object : BaseSingleAdapter<Feeling, ItemInputFeelingBinding>() {
                override fun createViewBinding(
                    parent: ViewGroup, viewType: Int
                ): ItemInputFeelingBinding {
                    return ItemInputFeelingBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                }

                override fun bindingViewHolder(
                    holder: BaseViewHolder<ItemInputFeelingBinding>, position: Int
                ) {
                    holder.binding.apply {
                        tvTitle.text = ""
                        getItemAt(position)?.icon?.let { imgView.setImageResource(it) }
                        getItemAt(position)?.stringName?.let {
                            tvTitle.text = this.imgView.context.getString(it)
                        }

                        bg.setBackgroundResource(
                            if (viewModel.selectedFeeling.value == getItemAt(
                                    position
                                )
                            ) R.drawable.bg_feeling_input_sellect else R.drawable.bg_feeling_input
                        )
                    }
                }

                override fun createViewHolder(binding: ItemInputFeelingBinding): BaseViewHolder<ItemInputFeelingBinding> {
                    return BaseViewHolder(binding).apply {
                        binding.root.setOnSingleClickListener {
                            viewModel.selectedFeeling.value = getItemAt(this.adapterPosition)
                        }
                    }
                }
            }

            adapter = mAdapter
            layoutManager = GridLayoutManager(this@MyDayFeelingInputActivity, 3)
            mAdapter.setupData(Feeling.values().toList())

            viewModel.selectedFeeling.observe(this@MyDayFeelingInputActivity) {
                binding.btnNext.setImageResource(if (it == null) R.drawable.ic_arrow_next else R.drawable.ic_arrow_next_active)
                binding.btnNext.isActivated = it != null
                mAdapter.notifyDataSetChanged()
            }
        }
    }
}

@HiltViewModel
class MyDayFeelingInputViewModel @Inject constructor(private val myDayFeelingRepo: MyDayFeelingRepo) :
    BaseViewModel() {
    val selectedFeeling = MutableLiveData<Feeling?>()
    var date: LocalDate = LocalDate.now()

    fun fetchMorningFeeling() {
        viewModelScope.launch {
            selectedFeeling.postValue(myDayFeelingRepo.getFeeling(date)?.feeling)
        }
    }

    fun insertFeeling() {
        CoroutineScope(Dispatchers.IO).launch {
            myDayFeelingRepo.insertFeeling(MyDayFeelingModel(selectedFeeling.value, date))
        }
    }
}