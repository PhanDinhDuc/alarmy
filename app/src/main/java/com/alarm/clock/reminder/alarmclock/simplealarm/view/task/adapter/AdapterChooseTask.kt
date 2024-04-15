package com.alarm.clock.reminder.alarmclock.simplealarm.view.task.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ItemChildTaskBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ItemHeaderTaskBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.BaseSectionedRecyclerViewAdapter
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.BaseViewHolder
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.GroupTaskType
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskType
import com.bumptech.glide.Glide

class AdapterChooseTask(
    private val context: Context,
    private val selecteds: List<TaskType>,
    private val listener: (TaskType) -> Unit,
) : BaseSectionedRecyclerViewAdapter<GroupTaskType, ItemHeaderTaskBinding, ItemChildTaskBinding>() {

    override fun mOnCreateHeaderViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<ItemHeaderTaskBinding> {
        return BaseViewHolder(
            ItemHeaderTaskBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun mOnCreateItemViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<ItemChildTaskBinding> {
        return BaseViewHolder(
            ItemChildTaskBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun mOnBindItemViewHolder(
        holder: BaseViewHolder<ItemChildTaskBinding>?,
        section: Int,
        relativePosition: Int,
        absolutePosition: Int
    ) {
        holder?.apply {
            val item: TaskType =
                datas.getOrNull(section)?.tasks?.getOrNull(relativePosition) ?: return
            Glide.with(context).asDrawable().load(item.imageChoose).into(binding.img)
            binding.txtMess.text = context.getString(item.titleChoseTask)

            binding.background.setBackgroundResource(if (selecteds.contains(item)) R.drawable.coner_child_task_selected else R.drawable.coner_child_task)

            binding.root.setOnSingleClickListener {
                listener.invoke(item)
            }
        }
    }

    override fun mOnBindHeaderViewHolder(
        holder: BaseViewHolder<ItemHeaderTaskBinding>?,
        section: Int
    ) {
        holder?.binding?.let { binding ->
            val item: GroupTaskType = datas.getOrNull(section) ?: return
            Glide.with(context).asDrawable().load(item.image).into(binding.img)
            binding.txtMess.text = binding.root.context.getString(item.title)
        }
    }

    override fun getItemCount(section: Int): Int {
        return datas[section]?.tasks?.size ?: 0
    }
}
