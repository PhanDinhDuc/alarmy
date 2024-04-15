package com.alarm.clock.reminder.alarmclock.simplealarm.view.sound.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseSingleAdapter
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewHolder
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.AudioModel
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ItemMusicBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.view.sound.AlarmSoundActivity

class AdapterMusic(
    private val activity: AlarmSoundActivity?,
    private val onSelected: (music: AudioModel, position: Int) -> Unit,
) :
    BaseSingleAdapter<AudioModel, ItemMusicBinding>() {
    private var isRingtone: Boolean = true

    override fun createViewBinding(parent: ViewGroup, viewType: Int): ItemMusicBinding {
        return ItemMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    fun checkRingtone(boolean: Boolean) {
        isRingtone = boolean
        clickCount = -1
    }

    fun setSelected(path: String) {
        activity?.selectedItemPosition = path
        notifyDataSetChanged()
    }

    //    private var selectedItemPosition = AlarmDefault(context)
    private var clickCount = -1
    override fun bindingViewHolder(holder: BaseViewHolder<ItemMusicBinding>, position: Int) {
        holder.binding.txtNameSong.text = when {
            isRingtone && position == 0 -> "Default"
            else -> listItem[position]?.aName.orEmpty()
        }

        holder.binding.imgRadio.isSelected =
            activity?.selectedItemPosition == if (getItemAt(position)?.realPath == null) getItemAt(
                position
            )?.aPath
            else getItemAt(position)?.realPath
        holder.binding.imgPlay.visibility =
            if (clickCount == position) View.VISIBLE else View.INVISIBLE

        holder.binding.root.setOnClickListener {
            clickCount = if (clickCount != position) position else -1
            notifyDataSetChanged()
            listItem[position]?.let { onSelected.invoke(it, position) }
        }


    }

    override fun createViewHolder(binding: ItemMusicBinding): BaseViewHolder<ItemMusicBinding> {
        return BaseViewHolder(binding)
    }


}