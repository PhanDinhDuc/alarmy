package com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.dialog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseDialogFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.DialogSnoozeBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.serializable
import com.alarm.clock.reminder.alarmclock.simplealarm.view.settings.adapter.TimeAdapter
import java.io.Serializable

class DialogItemSetting : BaseDialogFragment<DialogSnoozeBinding>() {
    override fun makeBinding(inflater: LayoutInflater, container: ViewGroup?): DialogSnoozeBinding {
        return DialogSnoozeBinding.inflate(inflater)
    }

    private var timeSnooze: Duration = Duration.OFF
    private lateinit var adapter: TimeAdapter

    override fun setupView() {
        super.setupView()
        arguments?.getInt(title)?.let { binding.textView2.setText(it) }
        val lists = arguments?.serializable(list) as? List<Duration>
        binding.recycle.layoutManager =
            arguments?.getInt(count)?.let { GridLayoutManager(context, it) }
        adapter = TimeAdapter(requireContext(), lists!!) { list, position ->
            timeSnooze = list
            binding.txtOk.isEnabled = true
            binding.txtOk.setBackgroundResource(R.drawable.bg_view_add_alarm)
            adapter.setSelected(position)
        }
        arguments?.getInt(positions)?.let { adapter.setSelected(it) }
        binding.recycle.adapter = adapter

        binding.txtCancel.setOnClickListener { dismiss() }
        binding.txtOk.setOnClickListener {
            arguments?.getInt(title)?.let { it1 -> callback.onSelectItemDialog(timeSnooze, it1) }

            dismiss()
        }
    }

    private val callback: CallbackListener
        get() = (parentFragment as? CallbackListener) ?: (activity as? CallbackListener)
        ?: error("No callback")


    companion object {

        private const val title = "title"
        private const val count = "count"
        private const val list = "list"
        private const val positions = "position"
        fun show(
            fmg: FragmentManager,
            @StringRes tvTitle: Int = R.string.snooze,
            countGrid: Int,
            listDuration: List<Duration>,
            position: Int
        ) {
            val dialog = DialogItemSetting()
            dialog.arguments = bundleOf(
                title to tvTitle, count to countGrid, list to listDuration, positions to position
            )
            dialog.show(fmg, "Dialog")
        }
    }

    enum class Duration : Serializable {
        OFF, D1M, D5M, D10M, D15M, D20M, D25M, D30M, D60M, D10S, D15S, D20S, D30S, D40S, D50S, D60S, UNLIMITED, D1T, D2T, D3T, D4T, D5T;

        val duration: Long
            get() {
                return when (this) {
                    OFF -> 0
                    D1M -> 60000L
                    D5M -> 5 * 60000L
                    D10M -> 10 * 60000L
                    D15M -> 15 * 60000L
                    D20M -> 20 * 60000L
                    D25M -> 25 * 60000L
                    D30M -> 30 * 60000L
                    D60M -> 60 * 60000L
                    D10S -> 10
                    D15S -> 15
                    D20S -> 20
                    D30S -> 30
                    D40S -> 40
                    D50S -> 50
                    D60S -> 60
                    UNLIMITED -> 0
                    D1T -> 1
                    D2T -> 2
                    D3T -> 3
                    D4T -> 4
                    D5T -> 5
                }
            }

        @get:StringRes
        val names: Int
            get() {
                return when (this) {
                    OFF -> R.string.off
                    D1M -> R.string.duration_1m
                    D5M -> R.string.duration_5m
                    D10M -> R.string.duration_10m
                    D15M -> R.string.duration_15m
                    D20M -> R.string.duration_20m
                    D25M -> R.string.duration_25m
                    D30M -> R.string.duration_30m
                    D60M -> R.string.duration_60m
                    D10S -> R.string.duration_10s
                    D15S -> R.string.duration_15s
                    D20S -> R.string.duration_20s
                    D30S -> R.string.duration_30s
                    D40S -> R.string.duration_40s
                    D50S -> R.string.duration_50s
                    D60S -> R.string.duration_60s
                    UNLIMITED -> R.string.unlimited
                    D1T -> R.string.time1
                    D2T -> R.string.time2
                    D3T -> R.string.time3
                    D4T -> R.string.time4
                    D5T -> R.string.time5
                }
            }

        @get:StringRes
        val title: Int
            get() {
                return when (this) {
                    OFF -> R.string.off
                    D1M -> R.string.title_duration_1m
                    D5M -> R.string.title_duration_5m
                    D10M -> R.string.title_duration_10m
                    D15M -> R.string.title_duration_15m
                    D20M -> R.string.title_duration_20m
                    D25M -> R.string.title_duration_25m
                    D30M -> R.string.title_duration_30m
                    D60M -> R.string.title_duration_60m
                    D10S -> R.string.title_duration_10s
                    D15S -> R.string.title_duration_15s
                    D20S -> R.string.title_duration_20s
                    D30S -> R.string.title_duration_30s
                    D40S -> R.string.title_duration_40s
                    D50S -> R.string.title_duration_50s
                    D60S -> R.string.title_duration_60s
                    UNLIMITED -> R.string.unlimited
                    D1T -> R.string.time1
                    D2T -> R.string.time2
                    D3T -> R.string.time3
                    D4T -> R.string.time4
                    D5T -> R.string.time5
                }
            }

        companion object {
            fun get(position: Int) = Duration.values().getOrNull(position)
            fun getAllVolume() = listOf(OFF, D30S, D60S, D5M, D10M, D30M, D60M)
            fun getAllAutoDismiss() = listOf(OFF, D1M, D5M, D10M, D20M, D25M, D30M)
            fun getAllSnoozeSetting() = listOf(UNLIMITED, D1T, D2T, D3T, D4T, D5T)
            fun getAllTimeLimit() = listOf(D10S, D15S, D20S, D30S, D40S, D50S, D60S)
            fun index(duration: Duration): Int = Duration.values().indexOf(duration)

        }


    }

    interface CallbackListener {
        fun onSelectItemDialog(timeSnooze: Duration, stringResId: Int)
    }
}

