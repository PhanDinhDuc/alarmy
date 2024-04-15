package com.alarm.clock.reminder.alarmclock.simplealarm.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.recyclerview.widget.RecyclerView
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Alarm
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.getDayTxt
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ItemAlarmBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.PopupMoreBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Converter.asPixels
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.visible

@Suppress("DEPRECATION")
class AlarmAdapter(
    val context: Context,
    val onSwitch: (switch: Boolean, alarmId: Int) -> Unit,
    val onItemAlarmClick: (alarm: Alarm) -> Unit,
    private val onClickItem: OnClickItemAlarm,
    private val popupMenuVisible: (Boolean) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.ViewHolder>() {

    private val listAlarms: MutableList<Alarm> = mutableListOf()
    var popupWindow: PopupWindow? = null

    fun dismissPopupWindow() {
        popupWindow?.dismiss()
        popupMenuVisible.invoke(false)
    }

    fun isPopupMenuShow(): Boolean {
        return popupWindow?.isShowing == true
    }

    override fun getItemCount() = listAlarms.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAlarmBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding).apply {
            binding.switch3.setOnCheckedChangeListener { _, b ->
                dismissPopupWindow()
                listAlarms.getOrNull(this.adapterPosition)?.let {
                    onSwitch(b, it.id)
                }
            }
            binding.imageView11.setOnSingleClickListener {
                dismissPopupWindow()
                listAlarms.getOrNull(this.adapterPosition)
                    ?.let { it1 ->
                        popupWindow = showPopUpMore(it, onClickItem, it1)
                        popupMenuVisible.invoke(true)
                    }
            }

            binding.root.setOnSingleClickListener {
                dismissPopupWindow()
                listAlarms.getOrNull(this.adapterPosition)?.let { it1 -> onItemAlarmClick(it1) }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindata(listAlarms[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setUpList(list: List<Alarm>) {
        listAlarms.clear()
        listAlarms.addAll(list)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun showPopUpMore(
        it: View?,
        onClickItemAlarm: OnClickItemAlarm,
        alarm: Alarm
    ): PopupWindow {
        val binding = PopupMoreBinding.inflate(
            LayoutInflater.from(context),
            null,
            false
        )
        val popupWindow = PopupWindow(
            binding.root
        )
        val width: Int = LinearLayout.LayoutParams.WRAP_CONTENT

        binding.linSkipNext.visibility = if (alarm.days?.size!! < 2) View.GONE else View.VISIBLE

        binding.imgSkip.setImageResource(if (alarm.isSkipNext) R.drawable.ic_more_undo else R.drawable.ic_next_skip)
        binding.txtSkip.text =
            if (alarm.isSkipNext) context.getString(R.string.undo_skip) else context.getString(
                R.string.skip_next
            )


        binding.root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val height: Int = binding.root.measuredHeight
        popupWindow.width = width
        popupWindow.height = height
        popupWindow.isFocusable = false
        popupWindow.setBackgroundDrawable(null)
        popupWindow.showAsDropDown(
            it, asPixels(-140), asPixels(-7)
        )


        binding.linDelete.setOnSingleClickListener {
            alarm.let { it1 -> onClickItemAlarm.onDelete(it1) }
            popupWindow.dismiss()
            notifyDataSetChanged()
        }

        binding.linPreview.setOnSingleClickListener {
            alarm.let { it1 -> onClickItemAlarm.onPreview(it1) }
            popupWindow.dismiss()
        }

        binding.linDuplicate.setOnSingleClickListener {
            alarm.let { it1 -> onClickItemAlarm.onCopy(it1) }
            popupWindow.dismiss()
            notifyDataSetChanged()
        }

        binding.linTemplate.setOnSingleClickListener {
            alarm.let { it1 -> onClickItemAlarm.onSetTemPlate(it1) }
            popupWindow.dismiss()
        }

        binding.linSkipNext.setOnSingleClickListener {
            alarm.isSkipNext = !alarm.isSkipNext
            alarm.let { it1 -> onClickItemAlarm.onUndoSkip(it1) }
            popupWindow.dismiss()
        }

        return popupWindow
    }


    inner class ViewHolder(private val binding: ItemAlarmBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n", "ResourceType")
        fun bindata(alarm: Alarm) {

//            binding.root.rootView.alpha = if (alarm.isON) 1f else 0.5f
            setColorItemIfAlarmOff(alarm.isON)
            binding.switch3.isChecked = alarm.isON
            val time = alarm.time.split(":")
            val hour = time[0].toInt()
            val minute = time[1].toInt()
            val txt_IsAM = time[2]
            binding.txtTime.text =
                "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
            binding.txtAmOrpm.text = txt_IsAM

            checkIfQuickAlarm(alarm)

            binding.imgClose.apply {
                visibility = if (alarm.tasks.isNullOrEmpty()) View.VISIBLE else View.GONE
                setImageResource(if (alarm.isON) R.drawable.ic_exit else R.drawable.ic_exit_1)
            }


            val listImagesBinding = listOf(
                binding.imgTaskMath,
                binding.imgTaskMemory,
                binding.imgTaskReview,
                binding.imgTaskScanQR,
                binding.imgTaskSquat,
                binding.imgTaskStep,
            )

            listImagesBinding.forEachIndexed { index, imageView ->
                val task = alarm.tasks?.getOrNull(index)
                if (task != null) {
                    imageView.visible()
                    imageView.setImageResource(task.type.getAlarmTaskIcon(alarm))
                } else {
                    imageView.visibility = View.INVISIBLE
                }
            }
        }

        private fun checkIfQuickAlarm(alarm: Alarm) {

            binding.linearLayout2.visibility =
                if (alarm.isQuickAlarm) View.GONE else View.VISIBLE
            binding.imageView11.visibility = if (alarm.isQuickAlarm) View.GONE else View.VISIBLE
            binding.switch3.setThumbResource(if (alarm.isQuickAlarm) R.drawable.thumb_switch_quick_alarm else R.drawable.thumb_switch)

            if (alarm.isQuickAlarm) {
                binding.txtDay.text =
                    context.getString(R.string.quick_alarm)
            } else if (alarm.isSkipNext) {
                binding.txtDay.text =
                    alarm.skipDay?.let { strikethroughSkipDay(alarm.getDayTxt(context), it) }
            } else {
                binding.txtDay.text =
                    if (alarm.label.isNullOrEmpty()) alarm.getDayTxt(context) else alarm.label
            }
        }

        private fun setColorItemIfAlarmOff(isOff: Boolean) {
            val textColor =
                if (!isOff) context.resources.getColor(R.color.newtral_3) else context.resources.getColor(
                    R.color.newtral_6
                )
            val taskColor =
                if (!isOff) context.resources.getColor(R.color.newtral_3) else context.resources.getColor(
                    R.color.color_1E6AB0
                )
            val dayColor =
                if (!isOff) context.resources.getColor(R.color.newtral_3) else context.resources.getColor(
                    R.color.primary_2
                )

            binding.imageView11.alpha = if (isOff) 1f else 0.5f

            val textViewsToChange = listOf(
                binding.txtAmOrpm, binding.txtTime
            )


            textViewsToChange.forEach { it.setTextColor(textColor) }

            binding.txtDay.setTextColor(dayColor)
            binding.txtTask.setTextColor(taskColor)
        }
    }

    fun strikethroughSkipDay(listDaysString: String, number: Int): SpannableString {
        val dayStringToNumber = mapOf(
            "MO" to 2,
            "TU" to 3,
            "WE" to 4,
            "TH" to 5,
            "FR" to 6,
            "SA" to 7,
            "SU" to 1
        )

        val spannableString = SpannableString(listDaysString)

        dayStringToNumber.filter { it.value == number }.forEach { (day, _) ->
            val start = listDaysString.indexOf(day)
            if (start != -1) {
                spannableString.setSpan(
                    StrikethroughSpan(),
                    start,
                    start + 2,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        return spannableString
    }


}

interface OnClickItemAlarm {
    fun onDelete(alarm: Alarm)
    fun onPreview(alarm: Alarm)
    fun onSetTemPlate(alarm: Alarm)
    fun onUndoSkip(alarm: Alarm)
    fun onCopy(alarm: Alarm)
}