package com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseSingleAdapter
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewHolder
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ItemMusicBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener

class AdapterSnooze(
    private val context: Context,
    private val onSelected: (snooze: Snooze) -> Unit,
) :
    BaseSingleAdapter<Snooze, ItemMusicBinding>() {

    override fun createViewBinding(parent: ViewGroup, viewType: Int): ItemMusicBinding {
        return ItemMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }


    fun setSelected(position: Long) {
        selectedItemPosition = position
        notifyDataSetChanged()
    }

    private var selectedItemPosition: Long = -1
    override fun bindingViewHolder(holder: BaseViewHolder<ItemMusicBinding>, position: Int) {
        holder.binding.txtNameSong.text = when {
            getItemAt(position)?.duration?.toInt() == 0 -> context.getString(R.string.off)
            else -> context.getString(R.string.space_min, listItem[position]?.title)
        }
        holder.binding.imgRadio.isSelected = selectedItemPosition == getItemAt(position)?.duration
        holder.binding.imgPlay.visibility = View.INVISIBLE
    }

    override fun createViewHolder(binding: ItemMusicBinding): BaseViewHolder<ItemMusicBinding> {
        return BaseViewHolder(binding).apply {
            binding.root.setOnSingleClickListener {
                getItemAt(adapterPosition).let {
                    it?.let { onSelected.invoke(it) }
                }
            }
        }
    }


}