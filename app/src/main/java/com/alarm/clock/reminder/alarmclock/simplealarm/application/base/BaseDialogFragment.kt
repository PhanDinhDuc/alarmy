package com.alarm.clock.reminder.alarmclock.simplealarm.application.base

import android.app.Dialog
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.R

abstract class BaseDialogFragment<B : ViewBinding> : DialogFragment() {
    lateinit var binding: B

    companion object {
        const val MAXSDK = 33
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = makeBinding(inflater, container)
        setupView()

        if (Build.VERSION.SDK_INT > MAXSDK) {
            val parent = FrameLayout(inflater.context)
            val percent = 85.toFloat() / 100
            val dm = Resources.getSystem().displayMetrics
            val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
            val percentWidth = rect.width() * percent
            binding.root.layoutParams = FrameLayout.LayoutParams(
                percentWidth.toInt(),
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
            parent.addView(binding.root)
            return parent
        } else {
            return binding.root
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            if (Build.VERSION.SDK_INT > MAXSDK) R.style.DialogTheme_transparent_34 else R.style.DialogTheme_transparent
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = true
        if (Build.VERSION.SDK_INT > MAXSDK) {
            dialog?.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        } else {
            setWidthPercent(85)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
//            window?.setFlags(
//                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//            )
        }
    }

    abstract fun makeBinding(inflater: LayoutInflater, container: ViewGroup?): B

    open fun setupView() {}
}

fun DialogFragment.setWidthPercent(percentage: Int) {
    val percent = percentage.toFloat() / 100
    val dm = Resources.getSystem().displayMetrics
    val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
    val percentWidth = rect.width() * percent
    dialog?.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
}