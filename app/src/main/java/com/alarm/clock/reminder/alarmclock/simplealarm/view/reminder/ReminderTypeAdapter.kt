package com.alarm.clock.reminder.alarmclock.simplealarm.view.reminder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.MainApplication
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseSingleAdapter
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewHolder
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.ReminderType
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ItemReminderTypeBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.getAppCompactDrawable
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener


class ReminderTypeAdapter(
    private val onClickItem: (ReminderType) -> Unit,
) : BaseSingleAdapter<ReminderType, ItemReminderTypeBinding>() {

    private var reminderType: ReminderType = ReminderType.NOTIFICATION

    fun update(reminderType: ReminderType) {
        this.reminderType = reminderType
        notifyDataSetChanged()
    }

    override fun createViewBinding(parent: ViewGroup, viewType: Int): ItemReminderTypeBinding {
        return ItemReminderTypeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun bindingViewHolder(
        holder: BaseViewHolder<ItemReminderTypeBinding>,
        position: Int
    ) {
        listItem[position]?.let { type ->
            if (type == reminderType) {
                holder.binding.image.background =
                    MainApplication.CONTEXT.getAppCompactDrawable(R.color.color_1E6AB0)
                holder.binding.image.setColorFilter(
                    ContextCompat.getColor(
                        MainApplication.CONTEXT,
                        R.color.color_background
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
            } else {
                holder.binding.image.background =
                    MainApplication.CONTEXT.getAppCompactDrawable(R.color.secondaryColor)
                holder.binding.image.setColorFilter(
                    ContextCompat.getColor(
                        MainApplication.CONTEXT,
                        R.color.secondaryColor2
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
            holder.binding.image.setImageResource(type.getIcon())
        }
    }

    override fun createViewHolder(binding: ItemReminderTypeBinding): BaseViewHolder<ItemReminderTypeBinding> {
        val holder = BaseViewHolder(binding)
        binding.root.setOnSingleClickListener {
            listItem[holder.bindingAdapterPosition]?.let { onClickItem.invoke(it) }
        }
        return holder
    }
}
