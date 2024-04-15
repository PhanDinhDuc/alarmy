package com.alarm.clock.reminder.alarmclock.simplealarm.view.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseDialogFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.DialogSortAlarmBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.settings.adapter.SortAlarmByAdapter

class DialogSortAlarm : BaseDialogFragment<DialogSortAlarmBinding>() {

    private lateinit var sortAlarmByAdapter: SortAlarmByAdapter
    private var sortByType: SortAlarmBy = SortAlarmBy.NORMAL
    private val callback: OnClickSortAlarm
        get() = (parentFragment as? OnClickSortAlarm)
            ?: (activity as? OnClickSortAlarm) ?: error(
                "No callback"
            )


    override fun makeBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogSortAlarmBinding {
        return DialogSortAlarmBinding.inflate(inflater)
    }

    override fun setupView() {
        super.setupView()

        sortAlarmByAdapter = SortAlarmByAdapter(requireContext(),
            SortAlarmBy.lists,
            onClickItem = { sortBy, postion ->
                sortByType = sortBy
                sortAlarmByAdapter.setSelected(postion)
                binding.txtOk.isEnabled = true
                binding.txtOk.setBackgroundResource(R.drawable.bg_view_add_alarm)
            })

        arguments?.getInt(positions)?.let { sortAlarmByAdapter.setSelected(it) }


        binding.recSortType.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = sortAlarmByAdapter
        }

        binding.txtCancel.setOnSingleClickListener {
            dismiss()
        }

        binding.txtOk.setOnSingleClickListener {
            callback.sortAlarm(sortByType)
            dismiss()
        }
    }


    companion object {
        private const val positions = "position"
        var listener: OnClickSortAlarm? = null
        fun showDialogSort(fmg: FragmentManager, position: Int) {
            val dialog = DialogSortAlarm()
            dialog.arguments = bundleOf(positions to position)
            dialog.show(fmg, "DialogSortAlarm")
        }
    }

    interface OnClickSortAlarm {
        fun sortAlarm(type: SortAlarmBy)
    }

}


enum class SortAlarmBy(val id: Int) {
    NORMAL(0), ENABLE(1), TIME(2);

    val title: Int
        get() {
            return when (this) {
                NORMAL -> R.string.normal
                ENABLE -> R.string.sort_by_enable
                TIME -> R.string.sort_by_alarm_recent
            }
        }


    companion object {
        val lists = SortAlarmBy.values().toList()
    }
}