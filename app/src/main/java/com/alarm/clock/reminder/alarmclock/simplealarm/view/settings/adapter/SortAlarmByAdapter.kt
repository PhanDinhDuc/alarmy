package com.alarm.clock.reminder.alarmclock.simplealarm.view.settings.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ItemSortTypeBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.settings.SortAlarmBy

class SortAlarmByAdapter(
    private val context: Context,
    private val list: List<SortAlarmBy>,
    var onClickItem: ((SortAlarmBy, Int) -> Unit)? = null
) : RecyclerView.Adapter<SortAlarmByAdapter.ViewHolder>() {

    var selectedID = 0

    fun setSelected(position: Int) {
        selectedID = position
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SortAlarmByAdapter.ViewHolder {
        val binding =
            ItemSortTypeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding).apply {
            binding.root.rootView.setOnSingleClickListener {
                onClickItem?.invoke(list[adapterPosition], adapterPosition)
            }
        }
    }

    override fun onBindViewHolder(holder: SortAlarmByAdapter.ViewHolder, position: Int) {
        holder.bindata(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(private val binding: ItemSortTypeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindata(sortAlarmBy: SortAlarmBy) {
            binding.txtType.text = context.getString(sortAlarmBy.title)
            binding.btnRadio.isChecked = adapterPosition == selectedID
        }
    }
}