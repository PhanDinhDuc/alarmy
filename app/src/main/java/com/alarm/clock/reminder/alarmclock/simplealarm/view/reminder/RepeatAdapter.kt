package com.alarm.clock.reminder.alarmclock.simplealarm.view.reminder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseSingleAdapter
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewHolder
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Repeat
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.getString
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ItemRepeatBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener


class RepeatAdapter(
    private val onClickItem: (Repeat) -> Unit,
) : BaseSingleAdapter<Repeat, ItemRepeatBinding>() {


    override fun createViewBinding(parent: ViewGroup, viewType: Int): ItemRepeatBinding {
        return ItemRepeatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun bindingViewHolder(holder: BaseViewHolder<ItemRepeatBinding>, position: Int) {
        listItem[position]?.let { repeat ->
            holder.binding.textView.setText(repeat.getString())
            holder.binding.root.setOnSingleClickListener {
                onClickItem.invoke(repeat)
            }
        }
    }

    override fun createViewHolder(binding: ItemRepeatBinding): BaseViewHolder<ItemRepeatBinding> {
        return BaseViewHolder(binding)
    }
}
