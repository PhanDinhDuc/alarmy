package com.alarm.clock.reminder.alarmclock.simplealarm.view.settings.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ItemSensitivityBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.view.settings.Sensitivity

class SensitivityAdapter(
    private val context: Context,
    private val dataSet: List<Sensitivity>,
    var didSelect: ((Sensitivity, Int) -> Unit)? = null
) : RecyclerView.Adapter<SensitivityAdapter.ViewHolder>() {

    var selectedID = 0

    fun setSelected(position: Int) {
        selectedID = position
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemSensitivityBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(language: Sensitivity) {
            binding.txtNameLanguage.text = context.getString(language.title)
            binding.radioButtonLanguage.isChecked = adapterPosition == selectedID
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSensitivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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