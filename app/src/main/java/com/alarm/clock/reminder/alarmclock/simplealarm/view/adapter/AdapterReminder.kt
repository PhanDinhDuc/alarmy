package com.alarm.clock.reminder.alarmclock.simplealarm.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseSingleAdapter
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewHolder
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Reminder
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ItemReminderBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import java.time.format.DateTimeFormatter
import java.util.Locale


class ReminderAdapter(
    private val selected: (Reminder) -> Unit,
    private val selectedMore: (Reminder, View) -> Unit
) :
    BaseSingleAdapter<Reminder, ItemReminderBinding>() {

    override fun createViewBinding(parent: ViewGroup, viewType: Int): ItemReminderBinding {
        return ItemReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }


    override fun bindingViewHolder(holder: BaseViewHolder<ItemReminderBinding>, position: Int) {
        listItem.getOrNull(position)?.let { value ->
            holder.binding.image.setImageResource(
                value.type.getIcon()
            )

            holder.binding.name.text = value.name
            val dateFormatters = DateTimeFormatter.ofPattern("MM/dd/yyyy")
            val date: String = value.date.format(dateFormatters)

            val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a").withLocale(Locale.US)
            val time: String = value.time.format(timeFormatter)
            holder.binding.date.text = "$date, $time"
        }
    }

    override fun createViewHolder(binding: ItemReminderBinding): BaseViewHolder<ItemReminderBinding> {
        return BaseViewHolder(binding).apply {
            binding.root.setOnSingleClickListener {
                getItemAt(bindingAdapterPosition)?.let {
                    selected.invoke(it)
                }
            }
            binding.moreImage.setOnClickListener {
                getItemAt(bindingAdapterPosition)?.let { reminder ->
                    selectedMore.invoke(reminder, it)
                }
            }
        }
    }
}