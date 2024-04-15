package com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.dialog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseDialogFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.DialogSnoozeBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.view.settings.Sensitivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.settings.adapter.SensitivityAdapter

class DialogSensitivity : BaseDialogFragment<DialogSnoozeBinding>() {
    override fun makeBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogSnoozeBinding {
        return DialogSnoozeBinding.inflate(inflater)
    }

    private var timeSnooze: Sensitivity = Sensitivity.NORMAL
    private lateinit var adapter: SensitivityAdapter


    override fun setupView() {
        super.setupView()
        arguments?.getInt(title)?.let { binding.textView2.setText(it) }
        binding.recycle.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        adapter = SensitivityAdapter(requireContext(), Sensitivity.getAll()) { list, position ->
            timeSnooze = list
            binding.txtOk.isEnabled = true
            binding.txtOk.setBackgroundResource(R.drawable.bg_view_add_alarm)
            adapter.setSelected(position)
        }
        arguments?.getInt(positions)?.let { adapter.setSelected(it) }
        binding.recycle.adapter = adapter

        binding.txtCancel.setOnClickListener { dismiss() }
        binding.txtOk.setOnClickListener {

            callback.onSelectItemDialog(timeSnooze)

            dismiss()
        }
    }

    private val callback: CallbackListeners
        get() = (parentFragment as? CallbackListeners) ?: (activity as? CallbackListeners) ?: error(
            "No callback"
        )


    companion object {

        private const val title = "title"
        private const val positions = "position"
        fun show(
            fmg: FragmentManager,
            @StringRes tvTitle: Int = R.string.shake_sensitivity,
            position: Int
        ) {
            val dialog = DialogSensitivity()
            dialog.arguments = bundleOf(
                title to tvTitle, positions to position
            )
            dialog.show(fmg, "Dialog")
        }
    }

    interface CallbackListeners {
        fun onSelectItemDialog(timeSnooze: Sensitivity)
    }
}
