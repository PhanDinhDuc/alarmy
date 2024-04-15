package com.alarm.clock.reminder.alarmclock.simplealarm.view.first_alarm

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseSingleAdapter
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewHolder
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ItemChooseTaskSoundBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskType


interface FirstAlarmModel {
    val title: Int
    val image: Int
}

data class FirstSoundTaskModel(
    override val title: Int,
    override val image: Int,
    val sound: Int
) : FirstAlarmModel

data class FirstTaskModel(
    override val title: Int,
    override val image: Int,
    val taskType: TaskType? = null
) : FirstAlarmModel

class FirstTaskAlarmAdapter constructor(
    val context: Context,
    val selected: ((FirstAlarmModel, Int) -> Unit)
) :
    BaseSingleAdapter<FirstAlarmModel, ItemChooseTaskSoundBinding>() {
    var isIdSelected: Int? = null
    private var countdownTimer: CountDownTimer? = null
    private var mMediaPlayer: MediaPlayer? = null
    fun setSelectedId(position: Int) {
        isIdSelected = position
        notifyDataSetChanged()
    }

    override fun createViewBinding(parent: ViewGroup, viewType: Int): ItemChooseTaskSoundBinding {
        return ItemChooseTaskSoundBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    }


    @SuppressLint("ResourceType")
    override fun bindingViewHolder(
        holder: BaseViewHolder<ItemChooseTaskSoundBinding>,
        position: Int
    ) {

        holder.binding.tvTitle.text = listItem[position]?.let { context.getString(it.title) }
        holder.binding.rootView.background = AppCompatResources.getDrawable(
            context,
            if (position == isIdSelected) R.drawable.bgr_choose_task_sound_slected
            else R.drawable.bgr_choose_task_sound
        )
        holder.binding.imageView2.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                listItem[position]!!.image
            )
        )
        if (getItemAt(position) is FirstSoundTaskModel) {
            holder.binding.imgPlay.visibility =
                if (position == isIdSelected) View.VISIBLE else View.GONE
            if (mMediaPlayer?.isPlaying == true) {
                holder.binding.imgPlay.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.ic_play
                    )
                )
            } else {
                holder.binding.imgPlay.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.ic_pause
                    )
                )
            }
            holder.binding.imgPlay.setOnSingleClickListener {
                if (mMediaPlayer?.isPlaying == true) {
                    stop()
                } else {
                    play(context, (getItemAt(position) as FirstSoundTaskModel).sound)
                }
            }
        }

    }


    fun stop() {
        notifyDataSetChanged()
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
            mMediaPlayer = null
            countdownTimer?.cancel()
        }
    }

    private fun play(c: Context?, rid: Int) {
        stop()
        mMediaPlayer = MediaPlayer.create(c, rid)
        mMediaPlayer?.setOnCompletionListener {
            it.start()
        }
        mMediaPlayer?.start()
        cancelCountDown()
    }

    private fun cancelCountDown() {
        countdownTimer?.cancel()
        countdownTimer =
            object : CountDownTimer(15000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                }

                override fun onFinish() {
                    stop()
                }
            }.start()
    }

    override fun createViewHolder(binding: ItemChooseTaskSoundBinding): BaseViewHolder<ItemChooseTaskSoundBinding> {
        return BaseViewHolder(binding).apply {
            binding.rootView.setOnSingleClickListener {
                getItemAt(adapterPosition).let {
                    it?.let { it1 -> selected.invoke(it1, adapterPosition) }
                }
            }
        }
    }
}