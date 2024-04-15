package com.alarm.clock.reminder.alarmclock.simplealarm.view.bedtime

import android.annotation.SuppressLint
import android.content.Intent
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.color
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseVMFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.TimeSoundModel
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.FragmentBedTimeBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.receiver.helper.canScheduleExactAlarms
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.returnDaySelectString1
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.BEDTIME_DATA
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.bedtime.data.SoundBedtime
import com.alarm.clock.reminder.alarmclock.simplealarm.view.permission.PermissionActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class BedTimeFragment : BaseVMFragment<FragmentBedTimeBinding, BedTimeSettingViewModel>() {

    override val viewModel: BedTimeSettingViewModel by viewModels()

    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBedTimeBinding {
        return FragmentBedTimeBinding.inflate(inflater)
    }

    @SuppressLint("StringFormatInvalid")
    override fun setupView() {
        super.setupView()

        viewModel.bedtimeData.observe(this) { bedtimeData ->
            bedtimeData ?: return@observe
            val color = if (bedtimeData.isOn) resources.getColor(
                R.color.primary_2_2,
                null
            ) else resources.getColor(R.color.newtral_3, null)
            binding.txtDuration.text = getString(R.string.minute, bedtimeData.timeSound / 60000)
            binding.baseTextview10.text = SpannableStringBuilder()
                .append(getString(R.string.bedtime_))
                .bold {
                    color(color) {
                        append(
                            " " + returnDaySelectString1(
                                this@BedTimeFragment.requireContext(),
                                bedtimeData.listTimes.map { it.day }.toSet()
                            )
                        )
                    }
                }


            binding.switchBedtime.isChecked = bedtimeData.isOn
            binding.txtNameSound.text = getString(SoundBedtime.listSound().firstOrNull {
                it.id == (bedtimeData.soundName?.toIntOrNull() ?: 0)
            }?.name ?: R.string.alpha_wave)
            val (today, time) = getCurrentDay(bedtimeData.listTimes)

            binding.txtDayNow.text = today
            binding.baseTextview11.text =
                "${time.split(":").getOrNull(0)}" + ":" + "${time.split(":").getOrNull(1)}"
            binding.baseTextview12.text = "${time.split(":").getOrNull(2)}"
        }

        binding.switchBedtime.setOnCheckedChangeListener { _, b ->
            if (mActivity?.canScheduleExactAlarms() == true) {
                setColorViewWhenSwitchChange(b)
                viewModel.bedtimeData.value?.let { updateData(b, it.id) }
            } else {
                val intent = Intent(mActivity, PermissionActivity::class.java)
                startActivity(intent)
            }
        }

        binding.cardView.setOnSingleClickListener {
            val intent = Intent(requireContext(), BedtimeSettingActivity::class.java)
            viewModel.bedtimeData.value?.let { it1 -> intent.putExtra(BEDTIME_DATA, it1) }
            startActivity(intent)
        }

        binding.cardSound.setOnSingleClickListener {
            val intent = Intent(requireContext(), SettingSoundActivity::class.java)
            viewModel.bedtimeData.value?.let { it1 -> intent.putExtra(BEDTIME_DATA, it1) }
            startActivity(intent)
        }


    }

    private fun updateData(isOn: Boolean, id: Int) {
        lifecycleScope.launch {
            val bedtime = viewModel.getBedTimeById(id)
            bedtime.isOn = isOn
            bedtime.let { viewModel.updateBedtime(it) }
        }
    }

    private fun setColorViewWhenSwitchChange(isON: Boolean) {
        val colorOn = if (isON) ContextCompat.getColor(
            requireContext(),
            R.color.primary_2_2
        ) else ContextCompat.getColor(requireContext(), R.color.newtral_3)

        val listTextView = listOf(
            binding.baseTextview11,
            binding.txtDayNow,
            binding.baseTextview12
        )

        listTextView.forEach { it.setTextColor(colorOn) }

    }

    private fun getCurrentDay(listDays: List<TimeSoundModel>): Pair<String, String> {
        val calendar = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        var nearestDay: TimeSoundModel? = listDays.find { it.day == calendar }

        //get nearest day after today if to day is not set
        if (nearestDay == null) {
            for (i in 1 until 7) {
                val checkingDay = (calendar + i) % 7
                nearestDay = listDays.find { it.day == checkingDay }
                if (nearestDay != null) break
            }
        }

        return if (nearestDay != null) {
            Pair(Util.getDayString(nearestDay.day, requireContext()), nearestDay.time)
        } else {
            Pair("", "")
        }
    }
}

enum class DayInWeek(val day: Int) {
    SUNDAY(1),
    MONDAY(2),
    TUESDAY(3),
    WEDNESDAY(4),
    THURSDAY(5),
    FRIDAY(6),
    SATURDAY(7);
}
