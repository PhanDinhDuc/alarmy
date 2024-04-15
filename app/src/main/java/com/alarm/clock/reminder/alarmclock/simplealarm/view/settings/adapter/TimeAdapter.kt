package com.alarm.clock.reminder.alarmclock.simplealarm.view.settings.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ItemTimeBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.dialog.DialogItemSetting

class TimeAdapter(
    val context: Context,
    private val dataSet: List<DialogItemSetting.Duration>,
    var didSelect: ((DialogItemSetting.Duration, Int) -> Unit)? = null
) : RecyclerView.Adapter<TimeAdapter.ViewHolder>() {

    var selectedID = 0

    fun setSelected(position: Int) {
        selectedID = position
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemTimeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(language: DialogItemSetting.Duration) {
            binding.txtOff.text = context.getString(language.names)
            binding.txtOff.setBackgroundResource(
                (if (adapterPosition == selectedID) R.drawable.bg_choose_snooze else R.drawable.coner_child_task)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemTimeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        ).apply {
            binding.root.setOnClickListener {
                didSelect?.invoke(dataSet[adapterPosition], adapterPosition)
            }
        }
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }


}