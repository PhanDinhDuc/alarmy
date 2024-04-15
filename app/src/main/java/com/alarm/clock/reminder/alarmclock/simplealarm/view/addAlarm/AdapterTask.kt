package com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseSingleAdapter
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewHolder
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ItemTaskBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskSettingModel
import com.bumptech.glide.Glide

interface NewAlarmTakModel

data class AddButton(val id: Int = 1) : NewAlarmTakModel

class AdapterTask(
    private val selected: () -> Unit,
    private val chooseTask: (NewAlarmTakModel, Int) -> Unit
) :
    BaseSingleAdapter<NewAlarmTakModel, ItemTaskBinding>() {
    override fun createViewBinding(parent: ViewGroup, viewType: Int): ItemTaskBinding {
        return ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    }

    override fun bindingViewHolder(holder: BaseViewHolder<ItemTaskBinding>, position: Int) {
        val context = holder.binding.root.context
        if (listItem[position] is AddButton) {
            holder.binding.nameTask.visibility = if (position == 0) View.GONE else View.VISIBLE
            holder.binding.nameTask.text = context.getText(R.string.txt_more_task)
            Glide.with(context).load(R.drawable.ic_add_1).into(holder.binding.imgTask)
            holder.binding.icExit.visibility = View.INVISIBLE
        } else {
            val model = listItem[position] as TaskSettingModel
            holder.binding.nameTask.visibility = View.VISIBLE
            holder.binding.nameTask.text =
                model.type.title.let { context.getText(it) }
            Glide.with(context).load(model.type.imageChoose)
                .into(holder.binding.imgTask)
            holder.binding.icExit.visibility = View.VISIBLE
        }

    }

    override fun createViewHolder(binding: ItemTaskBinding): BaseViewHolder<ItemTaskBinding> {

        return BaseViewHolder(binding).apply {
            binding.imgAddTask.setOnSingleClickListener {
                getItemAt(adapterPosition)?.let { it1 -> chooseTask(it1, adapterPosition) }
            }
            binding.icExit.setOnSingleClickListener {
                listItem[adapterPosition]?.let { it1 ->
                    val selectedTask = it1 as? TaskSettingModel ?: return@setOnSingleClickListener
                    SetDataTaskSetting.instance?.removeTaskSetting(
                        selectedTask
                    )
                }
                SetDataTaskSetting.instance?.getVTaskSettings()?.let { it1 -> setupData(it1) }
                notifyDataSetChanged()

                selected.invoke()
            }

        }
    }


}