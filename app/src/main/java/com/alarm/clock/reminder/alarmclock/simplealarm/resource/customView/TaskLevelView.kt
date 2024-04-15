package com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.CustomTaskLevelBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskLevel
import kotlin.math.roundToInt

class TaskLevelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    private val binding: CustomTaskLevelBinding
    var list: List<TaskLevel>
    var mOnChangeListener: OnChangeLevelListener? = null

    var taskLevel: TaskLevel = TaskLevel.EASIEST
        get() {
            return TaskLevel.get(list[binding.taskLevelView.value.roundToInt()].id)!!
        }
        set(value) {
            field = value
            binding.tvLevel.text = context.getText(value.title)
            binding.taskLevelView.value = value.id.toFloat()
        }

    init {
        binding = CustomTaskLevelBinding.inflate(LayoutInflater.from(context), this, true)
        list = TaskLevel.getListLevel()
        binding.taskLevelView.apply {
            valueTo = list.size.toFloat() - 1

        }
        binding.tvLevel.text = context.getText(list[binding.taskLevelView.value.roundToInt()].title)
        binding.taskLevelView.addOnChangeListener { slider, value, fromUser ->
            binding.tvLevel.text = context.getText(list[value.toInt()].title)
            mOnChangeListener?.onChangeLevel()
        }

    }


}

interface OnChangeLevelListener {
    fun onChangeLevel()
}

