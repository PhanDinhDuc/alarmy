package com.alarm.clock.reminder.alarmclock.simplealarm.view.reminder

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.MainApplication
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseSingleAdapter
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewHolder
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ItemRepeatSeveralBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class RepeatSeveralAdapter(
    private val context: Context,
    private val onClickItem: (LocalTime, Int) -> Unit,
) : BaseSingleAdapter<LocalTime, ItemRepeatSeveralBinding>() {


    override fun createViewBinding(parent: ViewGroup, viewType: Int): ItemRepeatSeveralBinding {
        return ItemRepeatSeveralBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun bindingViewHolder(
        holder: BaseViewHolder<ItemRepeatSeveralBinding>,
        position: Int
    ) {
        listItem[position]?.let { time ->
            val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a").withLocale(Locale.US)
            holder.binding.textTime.text = time.format(timeFormatter)
            holder.binding.textTitle.text = context.getString(R.string.time_idx, position + 1)
        }
    }

    override fun createViewHolder(binding: ItemRepeatSeveralBinding): BaseViewHolder<ItemRepeatSeveralBinding> {
        val holder = BaseViewHolder(binding)
        binding.textTime.setOnSingleClickListener {
            listItem[holder.bindingAdapterPosition]?.let { onClickItem.invoke(it, holder.bindingAdapterPosition) }
        }
        return holder
    }
}
