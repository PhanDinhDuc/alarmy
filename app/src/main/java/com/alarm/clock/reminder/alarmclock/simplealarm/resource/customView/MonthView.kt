package com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.MainApplication
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseSingleAdapter
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewHolder
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ItemMonthBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.LayoutMonthBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.getAppCompactDrawable
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.gone
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.visible
import java.time.Month


class MonthView : FrameLayout {

    constructor(context: Context) : super(context) {
        initView(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(attrs, 0)
    }

    constructor(
        context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        initView(attrs, defStyleAttr)
    }

    private lateinit var binding: LayoutMonthBinding
    var selectedMonth = mutableListOf<Month>()
        set(value) {
            adapter.onSelectedChange(value)
            field = value
        }

    private val months = Month.values().toList()

    val adapter by lazy {
        AdapterMonth(
            selectedMonth
        )
    }

    fun setOnChange(onChange: (List<Int>, Boolean) -> Unit) {
        val onChangeValue: (List<Month>) -> Unit = {
            onChange.invoke(it.map { month -> month.value }, it.size == months.size)
        }
        adapter.setOnChange(onChangeValue)
    }


    private fun initView(attrs: AttributeSet?, @AttrRes defStyleAttr: Int) {
        binding = LayoutMonthBinding.inflate(LayoutInflater.from(context), this, true)
        binding.recyclerview.adapter = adapter
        adapter.setupData(months)
    }
}

class AdapterMonth(private var selected: MutableList<Month>) :
    BaseSingleAdapter<Month, ItemMonthBinding>() {

    private var onChange: (List<Month>) -> Unit = {}

    fun setOnChange(onChange: (List<Month>) -> Unit) {
        this.onChange = onChange
    }

    fun onSelectedChange(selected: MutableList<Month>) {
        this.selected = selected
        notifyDataSetChanged()
    }

    override fun createViewBinding(parent: ViewGroup, viewType: Int): ItemMonthBinding {
        return ItemMonthBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun bindingViewHolder(holder: BaseViewHolder<ItemMonthBinding>, position: Int) {
        listItem.getOrNull(position)?.let { value ->
            if (position == listItem.size - 1) holder.binding.space.gone()
            else holder.binding.space.visible()
            holder.binding.textView.text = "${value.value}."
            holder.binding.textView.background =
                MainApplication.CONTEXT.getAppCompactDrawable(if (selected.contains(value)) R.drawable.bg_choose_day else R.drawable.bg_choose_day_none)
        }
    }

    override fun createViewHolder(binding: ItemMonthBinding): BaseViewHolder<ItemMonthBinding> {
        return BaseViewHolder(binding).apply {
            binding.root.setOnSingleClickListener {
                getItemAt(bindingAdapterPosition)?.let {
                    if (selected.contains(it)) selected.remove(it)
                    else selected.add(it)
                    onChange.invoke(selected.toList())
                    notifyItemChanged(bindingAdapterPosition)
                }
            }
        }
    }
}





