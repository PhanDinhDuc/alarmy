package com.alarm.clock.reminder.alarmclock.simplealarm.view.task.math

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.NumpadviewLayoutBinding


class NumPadView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : GridLayout(context, attrs, defStyleAttr), View.OnClickListener {

    companion object {
        const val DEFAULT_LEFT_ICON_VALUE = "LEFT"
        const val DEFAULT_RIGHT_ICON_VALUE = "RIGHT"
        const val DEFAULT_COLOR_HEX = "#1A1C28"
        const val DEFAULT_TEXT_SIZE_SP = 24F
        const val DEFAULT_TEXT_RIPPLE_EFFECT = true
        const val DEFAULT_LEFT_ICON_RIPPLE_EFFECT = true
        const val DEFAULT_RIGHT_ICON_RIPPLE_EFFECT = true
        const val DEFAULT_DRAWABLE_LEFT = true
        const val DEFAULT_DRAWABLE_RIGHT = true
        const val DEFAULT_BACKGROUND_TINT_NO_TINT = -1
    }

    private val mPadNumbers: MutableList<TextView> = mutableListOf()
    private val mDefaultColor by lazy { Color.parseColor(DEFAULT_COLOR_HEX) }

    private var binding: NumpadviewLayoutBinding

    // Listener of the custom view
    private lateinit var mListener: OnNumPadInteractionListener

    // Variable properties of the custom view
    private var mTextSize: Float = 0F
    private var mTextColor: Int = 0
    private var mTextStyle: Int = 0
    private var mTextRippleEffect: Boolean = true
    private var mDrawableLeft: Boolean = true
    private var mDrawableRight: Boolean = true
    private lateinit var mLeftIcon: Drawable
    private lateinit var mRightIcon: Drawable
    private var mLeftIconTint: Int = 0
    private var mRightIconTint: Int = 0
    private var mLeftIconRippleEffect: Boolean = true
    private var mRightIconRippleEffect: Boolean = true
    private var mBackgroundDrawableResource: Int = 0

    init {
        binding = NumpadviewLayoutBinding.inflate(LayoutInflater.from(context), this, true)
        context.theme.obtainStyledAttributes(attrs, R.styleable.NumPadView, 0, 0).apply {

            try {

                // Fetching all attributes
                mTextSize = getDimension(R.styleable.NumPadView_text_sizes, DEFAULT_TEXT_SIZE_SP)
                mTextColor = getColor(R.styleable.NumPadView_text_color, mDefaultColor)
                mTextRippleEffect = getBoolean(
                    R.styleable.NumPadView_text_ripple_effect,
                    DEFAULT_TEXT_RIPPLE_EFFECT
                )
                mDrawableLeft =
                    getBoolean(R.styleable.NumPadView_drawable_left, DEFAULT_DRAWABLE_LEFT)
                mDrawableRight =
                    getBoolean(R.styleable.NumPadView_drawable_right, DEFAULT_DRAWABLE_RIGHT)
                mLeftIconTint = getColor(R.styleable.NumPadView_left_icon_tint, mDefaultColor)
                mRightIconTint = getColor(
                    R.styleable.NumPadView_right_icon_tint,
                    DEFAULT_BACKGROUND_TINT_NO_TINT
                )
                mLeftIcon =
                    ((getDrawable(R.styleable.NumPadView_left_icon) ?: ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.ic_delete_math,
                        context.theme
                    )) as Drawable)
                mRightIcon =
                    ((getDrawable(R.styleable.NumPadView_right_icon) ?: ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.ic_save_math,
                        context.theme
                    )) as Drawable)
                mLeftIconRippleEffect = getBoolean(
                    R.styleable.NumPadView_left_icon_ripple_effect,
                    DEFAULT_LEFT_ICON_RIPPLE_EFFECT
                )
                mRightIconRippleEffect = getBoolean(
                    R.styleable.NumPadView_right_icon_ripple_effect,
                    DEFAULT_RIGHT_ICON_RIPPLE_EFFECT
                )

            } finally {
                recycle()
            } // TypedArray objects are a shared resource and must be recycled after use.
        }

        // Adding TextView to a list in order to perform multiple operations on each.
        mPadNumbers.add(binding.padNumber0)
        mPadNumbers.add(binding.padNumber1)
        mPadNumbers.add(binding.padNumber2)
        mPadNumbers.add(binding.padNumber3)
        mPadNumbers.add(binding.padNumber4)
        mPadNumbers.add(binding.padNumber5)
        mPadNumbers.add(binding.padNumber6)
        mPadNumbers.add(binding.padNumber7)
        mPadNumbers.add(binding.padNumber8)
        mPadNumbers.add(binding.padNumber9)
        mPadNumbers.add(binding.padNumber0)
        setup()

    }

    /**
     * Method that sets all attributes of the view
     */
    fun isEnabledFalse() {
        mPadNumbers.forEach { it.isEnabled = false }
        binding.padNumberLeftIcon.isEnabled = false
        binding.padNumberRightIcon.isEnabled = false
    }

    fun isEnabledTrue() {
        mPadNumbers.forEach { it.isEnabled = true }
        binding.padNumberLeftIcon.isEnabled = true
        binding.padNumberRightIcon.isEnabled = true
    }

    private fun setup() {


        // Text parameters

        mPadNumbers.forEach {
            it.textSize = mTextSize
            it.setTextColor(mTextColor)
            it.setOnClickListener(this) // Text mListener
        }

        // Drawable  parameters

        if (mDrawableLeft) {
            mLeftIcon.setTint(mLeftIconTint)
            binding.padNumberLeftIcon.setImageDrawable(mLeftIcon)
            binding.padNumberLeftIcon.setOnClickListener(this)
        } else {
            binding.padNumberLeftIcon.setOnClickListener(null)
        }

        if (mDrawableRight) {
            if (mRightIconTint != DEFAULT_BACKGROUND_TINT_NO_TINT) mRightIcon.setTint(mRightIconTint)
            binding.padNumberRightIcon.setImageDrawable(mRightIcon)
            binding.padNumberRightIcon.setOnClickListener(this)
        } else {
            binding.padNumberRightIcon.setOnClickListener(null)
        }
    }

    fun setTextSize(sp: Float): NumPadView {
        mTextSize =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, sp, resources.displayMetrics)
        return this
    }

    fun setTextStyle(textStyle: Int): NumPadView {
        mTextStyle = textStyle
        return this
    }

    fun setTextColor(@ColorRes textColor: Int): NumPadView {
        mTextColor = resources.getColor(textColor, context.theme)
        return this
    }

    fun setTextColor(hex: String): NumPadView {
        mTextColor = Color.parseColor(hex)
        return this
    }

    fun setTextRippleEffect(rippleEffect: Boolean): NumPadView {
        mTextRippleEffect = rippleEffect
        return this
    }

    fun setDrawableLeft(exist: Boolean): NumPadView {
        mDrawableLeft = exist
        return this
    }

    fun setDrawableRight(exist: Boolean): NumPadView {
        mDrawableRight = exist
        return this
    }

    fun setLeftIcon(leftIcon: Drawable): NumPadView {
        mLeftIcon = leftIcon
        return this
    }

    fun setLeftIcon(@DrawableRes leftIcon: Int): NumPadView {
        mLeftIcon = resources.getDrawable(leftIcon, context.theme)
        return this
    }

    fun setLeftIconTint(@ColorRes leftIconTint: Int): NumPadView {
        mLeftIconTint = resources.getColor(leftIconTint, context.theme)
        return this
    }

    fun setLeftIconTint(r: Int, g: Int, b: Int): NumPadView {
        mLeftIconTint = Color.rgb(r, g, b)
        return this
    }

    fun setLeftIconTint(hex: String): NumPadView {
        mLeftIconTint = Color.parseColor(hex)
        return this
    }

    fun setLeftIconRippleEffect(rippleEffect: Boolean): NumPadView {
        mLeftIconRippleEffect = rippleEffect
        return this
    }

    fun setRightIcon(rightIcon: Drawable): NumPadView {
        mRightIcon = rightIcon
        return this
    }

    fun setRightIcon(@DrawableRes rightIcon: Int): NumPadView {
        mRightIcon = resources.getDrawable(rightIcon, context.theme)
        return this
    }

    fun setRightIconTint(@ColorRes rightIconTint: Int): NumPadView {
        mRightIconTint = resources.getColor(rightIconTint, context.theme)
        return this
    }

    fun setRightIconTint(r: Int, g: Int, b: Int): NumPadView {
        mRightIconTint = Color.rgb(r, g, b)
        return this
    }

    fun setRightIconTint(hex: String): NumPadView {
        mRightIconTint = Color.parseColor(hex)
        return this
    }

    fun setRightIconRippleEffect(rippleEffect: Boolean): NumPadView {
        mRightIconRippleEffect = rippleEffect
        return this
    }

    fun setBackgroundDrawableResource(@DrawableRes drawableId: Int): NumPadView {
        mBackgroundDrawableResource = drawableId
        return this
    }

    /**
     * This method must be called every time a change is made on the view's attributes.
     */
    fun apply() {
        invalidate()
        setup()
        requestLayout()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.pad_number_left_icon -> {
                if (::mListener.isInitialized) mListener.onLeftIconClick()
            }

            R.id.pad_number_right_icon -> {
                if (::mListener.isInitialized) mListener.onRightIconClick()
            }

            else -> {
                if (::mListener.isInitialized) mListener.onNewValue(
                    resources.getResourceEntryName(
                        view.id
                    ).substringAfterLast("_")
                )
            }
        }
    }

    /**
     * Method receiving lambda expressions to declare listeners of the different keypad buttons.
     * The numeric keypad is divided in 3 parts, the numbers, the left icon, and the right one.
     * By default, if [onLeftIconClick] and/or [onRightIconClick] lambdas are not passed in arguments
     * the function calls onNewValue method with specific values defining right and left icon clicks.
     */
    fun setOnInteractionListener(
        onLeftIconClick: () -> Unit = {},
        onRightIconClick: () -> Unit = {},
        onNewValue: (value: String) -> Unit = {}
    ) {
        mListener = object : OnNumPadInteractionListener {
            override fun onLeftIconClick() {
                onLeftIconClick()
            }

            override fun onRightIconClick() {
                onRightIconClick()
            }

            override fun onNewValue(value: String) {
                onNewValue(value)
            }
        }
    }

    fun setOnInteractionListener(onNewValue: (value: String) -> Unit = {}) {
        mListener = object : OnNumPadInteractionListener {
            override fun onNewValue(value: String) {
                onNewValue(value)
            }
        }
    }

    fun setOnInteractionListener(listener: OnNumPadInteractionListener) {
        mListener = listener
    }

    interface OnNumPadInteractionListener {
        fun onLeftIconClick() = onNewValue(DEFAULT_LEFT_ICON_VALUE)
        fun onRightIconClick() = onNewValue(DEFAULT_RIGHT_ICON_VALUE)
        fun onNewValue(value: String)
    }
}