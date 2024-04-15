package com.alarm.clock.reminder.alarmclock.simplealarm.view.task.memories

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.GridLayout
import android.widget.GridLayout.UNDEFINED
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.CustomGameMemoriesBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Converter.asPixels
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.gone
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.visible
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.math.NumPadView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random


class MemoriesGameView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), OnChangeSelectedSquareListener {

    var binding: CustomGameMemoriesBinding
    private lateinit var mListener: NumPadView.OnNumPadInteractionListener

    private val mPadNumbers: ArrayList<TextView> = ArrayList()
    val randomPositions: MutableSet<Int> = mutableSetOf()

    companion object {
        const val STATE_PREVIEW = 0
        const val STATE_NONE = 1
        const val STATE_SELECTED = 2
    }

    init {
        binding = CustomGameMemoriesBinding.inflate(LayoutInflater.from(context), this, true)
        SettingTaskMemoriesFragment.mOnChangeSelectedSquare = this
    }

    override fun onChangeSelectedSquare(numberSquare: Int, rowNumber: Int, positionState: Int) {
        setGridView(rowNumber)
        getRandomSelectedSquare(numberSquare, positionState)
    }

    fun setGridView(rowNumber: Int) {
        binding.gridLayout.removeAllViews()
        mPadNumbers.clear()
        binding.gridLayout.rowCount = rowNumber
        binding.gridLayout.columnCount = rowNumber
        for (i in 1..(rowNumber * rowNumber)) {
            val textView = TextView(context)
            val params = GridLayout.LayoutParams()
            params.apply {
                columnSpec = GridLayout.spec(UNDEFINED, 1f)
                rowSpec = GridLayout.spec(UNDEFINED, 1f)
                setMargins(asPixels(4), asPixels(4), asPixels(4), asPixels(4))
            }
            textView.apply {
                layoutParams = params
                isClickable = true
                isFocusable = true
                background = ResourcesCompat.getDrawable(
                    context.resources, R.drawable.bg_square_none, context.theme
                )
            }
            binding.gridLayout.addView(textView)
            mPadNumbers.add(textView)
        }

        mPadNumbers.forEachIndexed { index, textView ->
            textView.setOnSingleClickListener {
                if (::mListener.isInitialized) mListener.onNewValue(index.toString())
            }
        }
    }

    fun getRandomSelectedSquare(numberSquare: Int, positionState: Int) {
        randomPositions.clear()
        while (randomPositions.size < numberSquare) {
            val randomPosition = Random.nextInt(0, mPadNumbers.size)
            randomPositions.add(randomPosition)
        }
        setBackgroundView(randomPositions, positionState)
    }

    fun setBackgroundView(randomPositions: MutableSet<Int>, positionState: Int) {
        mPadNumbers.forEachIndexed { position, textView ->
            val state = if (position in randomPositions) positionState else STATE_NONE
            textView.setBackgroundResource(getBackgroundResource(state))
        }
    }

    fun checkPositionView(position: Int) {
        if (position !in randomPositions) {
            CoroutineScope(Dispatchers.Main).launch {
                mPadNumbers[position].setBackgroundResource(getBackgroundResource(STATE_SELECTED))
                showBackGroundView()
                delay(500)
                mPadNumbers[position].setBackgroundResource(getBackgroundResource(STATE_NONE))
                hiddenBackGroundView()
            }
        } else {
            mPadNumbers[position].setBackgroundResource(getBackgroundResource(STATE_SELECTED))
        }
    }

    fun setInteractionListener(onNewValue: (value: String) -> Unit = {}) {
        mListener = object : NumPadView.OnNumPadInteractionListener {
            override fun onNewValue(value: String) {
                onNewValue(value)
            }
        }
    }

    fun isEnabledFalse() {
        mPadNumbers.forEach { it.isEnabled = false }
    }

    fun isEnabledTrue() {
        mPadNumbers.forEach { it.isEnabled = true }
    }

    fun showBackGroundView() {
        binding.viewBgr.visible()
        mPadNumbers.forEach { it.isEnabled = false }
    }

    fun hiddenBackGroundView() {
        binding.viewBgr.gone()
        mPadNumbers.forEach { it.isEnabled = true }
    }

    private fun getBackgroundResource(state: Int): Int {
        return when (state) {
            STATE_PREVIEW -> R.drawable.bg_square_preview
            STATE_NONE -> R.drawable.bg_square_none
            STATE_SELECTED -> R.drawable.bg_square_selected
            else -> R.drawable.bg_square_none
        }
    }
}

interface OnChangeSelectedSquareListener {
    fun onChangeSelectedSquare(numberSquare: Int, rowNumber: Int, positionState: Int)
}
