package com.alarm.clock.reminder.alarmclock.simplealarm.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ItemTypeSoundDetailBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.bedtime.data.SoundBedtime
import com.bumptech.glide.Glide

class TypeSoundAdapter(
    private val data: List<SoundBedtime>,
    private val context: Context,
    private val onClick: (sound: SoundBedtime) -> Unit,
    private val onClickSelect: (sound: SoundBedtime) -> Unit
) :
    RecyclerView.Adapter<TypeSoundAdapter.ViewHolder>() {

    private var currentPlaying: Int? = null
    private var currentSelected: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TypeSoundAdapter.ViewHolder {
        val binding =
            ItemTypeSoundDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding).apply {

            binding.root.rootView.setOnSingleClickListener {
                currentSelected = data.getOrNull(this.adapterPosition)?.id
                data.getOrNull(adapterPosition)?.let { it1 -> onClickSelect(it1) }
                data.getOrNull(adapterPosition)?.let { it1 -> onClick(it1) }
                setCurrentPlaying(data.getOrNull(this.adapterPosition)?.id ?: -1)
            }


            binding.imgPlay.isClickable = true
            binding.imgPlay.setOnSingleClickListener {
                data.getOrNull(adapterPosition)?.let { it1 -> onClick(it1) }
                setCurrentPlaying(data.getOrNull(this.adapterPosition)?.id ?: -1)
            }

        }
    }

    override fun onBindViewHolder(holder: TypeSoundAdapter.ViewHolder, position: Int) {
        holder.bindata(data[position], position)
    }

    override fun getItemCount() = data.size

    fun setCurrentPlaying(adapterPosition: Int) {
        currentPlaying = adapterPosition
        notifyDataSetChanged()

    }

    fun setCurrentSelected(currentSelected: Int) {
        this.currentSelected = currentSelected
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemTypeSoundDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindata(sound: SoundBedtime, position: Int) = binding.apply {

            Glide.with(context).load(sound.thumb).into(imgThumb)
            txtNameSound.text = context.getString(sound.name)

            imgDone.visibility = if (sound.id == currentSelected) View.VISIBLE else View.INVISIBLE

            imgRuningSound.visibility =
                if (sound.id == currentPlaying) View.VISIBLE else View.INVISIBLE
            imgPlay.visibility = if (sound.id == currentPlaying) View.INVISIBLE else View.VISIBLE
        }
    }
}