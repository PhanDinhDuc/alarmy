package com.alarm.clock.reminder.alarmclock.simplealarm.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.TimeSoundModel
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ItemDayBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.bedtime.BedtimeSettingActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.bedtime.DayInWeek

class BedtimeDateAdapter(
    private val context: Context,
    private val onClickItem: (timeSound: TimeSoundModel) -> Unit
) :
    RecyclerView.Adapter<BedtimeDateAdapter.ViewHoder>() {

    private val listTimeSounds: MutableList<TimeSoundModel> = mutableListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BedtimeDateAdapter.ViewHoder {
        val binding = ItemDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHoder(binding).apply {
            binding.root.rootView.setOnSingleClickListener {
                listTimeSounds.getOrNull(this.adapterPosition).let {
                    it?.let { it1 -> onClickItem(it1) }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: BedtimeDateAdapter.ViewHoder, position: Int) {
        holder.bindata(listTimeSounds[position])
    }

    override fun getItemCount() = listTimeSounds.size

    fun setUpList(time: List<TimeSoundModel>) {
        listTimeSounds.clear()
        val sortedDay = time.sortedWith(compareBy({ it.day == DayInWeek.SUNDAY.day }, { it.day }))
        listTimeSounds.addAll(sortedDay)
        notifyDataSetChanged()
    }

    fun updateTime(updatedTime: TimeSoundModel) {
        val position = listTimeSounds.indexOfFirst { it.day == updatedTime.day }
        if (position != -1) {
            listTimeSounds[position] = updatedTime
            notifyItemChanged(position)

            (context as? BedtimeSettingActivity)?.viewModel?.updateListDay(updatedTime)
        }
    }


    inner class ViewHoder(private val binding: ItemDayBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindata(timeSoundModel: TimeSoundModel) {
            val time = timeSoundModel.time.split(":")
            val hour = time[0]
            val minute = time[1]
            val isAm = time[2]
            binding.txtTime.text = buildString {
                append(hour)
                append(":")
                append(minute)
            }
            binding.txtIsAM.text = isAm
            binding.txtDay.text = Util.getDayString(timeSoundModel.day, context)
        }
    }
}