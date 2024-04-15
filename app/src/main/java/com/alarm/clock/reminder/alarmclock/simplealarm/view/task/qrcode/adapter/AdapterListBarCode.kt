package com.alarm.clock.reminder.alarmclock.simplealarm.view.task.qrcode.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseSingleAdapter
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewHolder
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ItemBarcodeBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.PopupMoreBarcodeBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.BarCodes

class AdapterListBarCode(
    val context: Context,
    private val onClickItem: OnClickItemBarcode,
    private val itemBarcode: (BarCodes, Int) -> Unit,
    private val onChangeName: (BarCodes) -> Unit,
    private val onDelete: (BarCodes) -> Unit
) : BaseSingleAdapter<BarCodes, ItemBarcodeBinding>() {
    override fun createViewBinding(parent: ViewGroup, viewType: Int): ItemBarcodeBinding =
        ItemBarcodeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

    fun setSelected(position: Int) {
        selectedItemPosition = position
        notifyDataSetChanged()
    }

    override fun bindingViewHolder(holder: BaseViewHolder<ItemBarcodeBinding>, position: Int) {
        holder.binding.txtNameSong.text = listItem[position]?.nameQr
        holder.binding.imgRadio.isSelected = selectedItemPosition == position
        holder.binding.bgRoot.setBackgroundResource(
            if (position == selectedItemPosition) R.drawable.bg_bacode else R.drawable.coner_child_task
        )

        holder.binding.imgMore.setOnClickListener {
            onClickItem.onClickSelect(listItem[position])
            showPopUpMore(it, onClickItem, listItem[position])
        }
    }

    private var selectedItemPosition = 0
    override fun createViewHolder(binding: ItemBarcodeBinding): BaseViewHolder<ItemBarcodeBinding> {
        return BaseViewHolder(binding).apply {
            binding.root.setOnSingleClickListener {
                getItemAt(adapterPosition)?.let { it1 -> itemBarcode(it1, adapterPosition) }
            }
        }
    }


    fun showPopUpMore(
        it: View?, onClickItem: OnClickItemBarcode, barcode: BarCodes?
    ) {
        val binding = PopupMoreBarcodeBinding.inflate(
            LayoutInflater.from(context), null, false
        )

        val width: Int = LinearLayout.LayoutParams.WRAP_CONTENT
        val height: Int = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = true

        binding.root.rootView.apply {
            setBackgroundResource(R.drawable.bg_popup_more)
            paddingEnd == 10
        }


        val popupWindow = PopupWindow(
            binding.root, width, height, focusable
        )
        it?.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

        val values = IntArray(2)
        it?.getLocationInWindow(values)
        val positionOfIcon = values[1]

        val displayMetrics = context.resources.displayMetrics
        val height1 = displayMetrics.heightPixels * 2 / 3

        if (positionOfIcon > height1) {
            popupWindow.showAsDropDown(it, 0, height1 - positionOfIcon)
        } else {
            popupWindow.showAsDropDown(it, 0, 0)
        }

        binding.linDelete.setOnSingleClickListener {
            popupWindow.dismiss()
            barcode?.let { it1 -> onDelete(it1) }
            notifyDataSetChanged()
        }

        binding.linChangeName.setOnSingleClickListener {
            barcode?.let(onChangeName)
            popupWindow.dismiss()
        }

    }

}

interface OnClickItemBarcode {
    fun onDelete(barcode: BarCodes?)
    fun onClickSelect(barcode: BarCodes?)
    fun onChangeName(barcode: BarCodes?, newName: String)
    fun onMore(barcode: BarCodes?)
}