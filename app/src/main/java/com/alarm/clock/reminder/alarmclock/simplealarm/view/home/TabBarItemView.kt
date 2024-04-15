package com.alarm.clock.reminder.alarmclock.simplealarm.view.home

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.AttrRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.TabBarItemLayoutBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.gone
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.visible


class TabBarItemView : ConstraintLayout {

    var text: String? = null
        set(value) {
            binding.tabText.text = value
            field = value
        }

    var icon: Drawable? = null
        set(value) {
            field = value
            value ?: return
            binding.imageIcon.setImageDrawable(value)
        }

    var selectedIcon: Drawable? = null

    var isSelectedTab: Boolean = false
        set(value) {
            val iconSrc = if (value) selectedIcon else icon
            if (iconSrc == null) {
                binding.imageIcon.gone()
            } else {
                binding.imageIcon.visible()
                binding.imageIcon.setImageDrawable(iconSrc)
            }

            binding.imageIcon.scaleX = if (value) 1f else 0.7f
            binding.imageIcon.scaleY = if (value) 1f else 0.7f
            binding.tabText.visibility = if (value) VISIBLE else GONE
            field = value
        }

    constructor(context: Context) : super(context) {
        initView(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(attrs, 0)
    }

    constructor(
        context: Context, attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        initView(attrs, defStyleAttr)
    }

    lateinit var binding: TabBarItemLayoutBinding

    private fun initView(attrs: AttributeSet?, @AttrRes defStyleAttr: Int) {
        binding = TabBarItemLayoutBinding.inflate(LayoutInflater.from(context), this, true)
        val customAttributesStyle =
            context.obtainStyledAttributes(attrs, R.styleable.TabBarItemView, defStyleAttr, 0)

        try {
            isSelectedTab = false
            icon = customAttributesStyle.getDrawable(
                R.styleable.TabBarItemView_tab_icon,
            )
            selectedIcon = customAttributesStyle.getDrawable(
                R.styleable.TabBarItemView_tab_icon_selected,
            )
            text =
                customAttributesStyle.getString(R.styleable.TabBarItemView_tab_text)
        } finally {
            customAttributesStyle.recycle()
        }
    }

}