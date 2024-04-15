package com.alarm.clock.reminder.alarmclock.simplealarm.view.settings.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ItemLanguageBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.Language

@Suppress("DEPRECATION")
class LanguageAdapter(
    val context: Context,
    private val dataSet: List<Language>,
    var didSelect: ((Language) -> Unit)? = null
) : RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {
    var selectedID = 0

    fun setSelected(position: Int) {
        selectedID = position
        notifyDataSetChanged()
    }


    inner class ViewHolder(val binding: ItemLanguageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(language: Language) {
            binding.imgFlagLanguage.setImageResource(language.flag)
            binding.txtNameLanguage.text = context.getString(language.nameFlag)
            binding.radioButtonLanguage.isChecked = language.id == selectedID
//            val customDrawableNone = CustomDrawable(context,
//                Color.parseColor("#FFFFFF"),
//                Color.parseColor("#FFFFFF"),
//                binding.relItemLanguage.paddingLeft, 12
//            )
//            binding.relItemLanguage.background =customDrawableNone
//            binding.relItemLanguage.setBackgroundResource(
//                (if (language.id == selectedID)  R.drawable.bg_item_none else R.drawable.bg_item_none) as Int
//            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        ).apply {
            binding.root.setOnClickListener {
                didSelect?.invoke(dataSet[adapterPosition])
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