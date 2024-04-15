package com.alarm.clock.reminder.alarmclock.simplealarm.view.home

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.AttrRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.TabBarLayoutBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Converter
import com.alarm.clock.reminder.alarmclock.simplealarm.view.main.MainViewModel

class TabBarView : ConstraintLayout {

    var binding: TabBarLayoutBinding =
        TabBarLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    var currentSelectedIndex: Int = 0
        set(value) {
            listTabItem.forEachIndexed { index, tabBarItemView ->
                tabBarItemView.isSelectedTab = index == value
                tabBarItemView.binding.tabText.setPadding(
                    0,
                    if (index == MainViewModel.TabBarItem.ALARM.index) Converter.asPixels(20) else 0,
                    0,
                    0
                )
            }
            field = value
        }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(
        context: Context, attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    private var listTabItem = mutableListOf<TabBarItemView>()
    private var callback: ((Int) -> Boolean)? = null

    init {
        listTabItem.add(binding.tab0)
        listTabItem.add(binding.tab1)
        listTabItem.add(binding.tab2)
        listTabItem.add(binding.tab3)
        listTabItem.add(binding.tab4)
        setupOnClick()
    }

    private fun setupOnClick() {
        listTabItem.forEachIndexed { index, tabBarItemView ->
            tabBarItemView.setOnClickListener {
                if (currentSelectedIndex == index) return@setOnClickListener
                if (callback?.invoke(index) == false) return@setOnClickListener
                currentSelectedIndex = index
            }
        }
    }

    fun setOnTabChangeListener(callback: (Int) -> Boolean) {
        this.callback = callback
    }

}

