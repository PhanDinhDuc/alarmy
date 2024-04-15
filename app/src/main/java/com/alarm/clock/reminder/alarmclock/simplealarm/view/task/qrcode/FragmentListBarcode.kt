package com.alarm.clock.reminder.alarmclock.simplealarm.view.task.qrcode

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.PushType
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.pushTo
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.FragmentLitsBarcodeBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.dialog.DialogChangeNameBarcode
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.dialog.DialogDeleteBarcode
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.BarCodes
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.BaseTaskSettingFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.CUR_TASK
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskSettingModel
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskType
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.qrcode.adapter.AdapterListBarCode
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.qrcode.adapter.OnClickItemBarcode

class FragmentListBarcode : BaseTaskSettingFragment<FragmentLitsBarcodeBinding>(),
    OnClickItemBarcode, DialogChangeNameBarcode.Listener, DialogDeleteBarcode.OnOkDeleteListener {
    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLitsBarcodeBinding {
        return FragmentLitsBarcodeBinding.inflate(inflater)
    }

    private var selectedBarcode: BarCodes? = null
    lateinit var adapter: AdapterListBarCode
    override val taskType: TaskType = TaskType.SCANQR
    lateinit var task: TaskSettingModel

    private var editbarcodeTmp: BarCodes? = null
    override fun setupView() {
        super.setupView()
        task = taskSettingModel ?: return
        selectedBarcode = task.barcodes?.first()
        adapter =
            AdapterListBarCode(requireContext(), this,
                itemBarcode = { barcode, position ->
                    selectedBarcode = barcode
                    adapter.setSelected(position)
                }, onChangeName = { barcode ->
                    editbarcodeTmp = barcode
                    DialogChangeNameBarcode.newInstance(barcode.nameQr, true)
                        .show(childFragmentManager, "")
                }, onDelete = {
                    editbarcodeTmp = it
                    DialogDeleteBarcode.show(childFragmentManager, null)
                })

        binding.addQR.setOnSingleClickListener {
            pushTo(
                R.id.action_fragmentListBarcode_to_barcodeScanningFragment2,
                bundleOf(CUR_TASK to task),
                PushType.NONE
            )
        }
        taskSettingModel?.barcodes?.let { adapter.setupData(it) }
        binding.rc.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding.rc.adapter = adapter

        binding.btnPreview.setOnSingleClickListener {
            actionCallback?.onPreview(task.copy(barcodes = listOfNotNull(selectedBarcode)))
        }


        binding.btnSave.setOnSingleClickListener {
            actionCallback?.onSave(task.copy(barcodes = task.barcodes))
        }

    }

    override fun onDelete(barcode: BarCodes?) {
        task.barcodes = task.barcodes?.filter { it != barcode }
        task.barcodes?.let { adapter.setupData(it) }
    }


    override fun onClickSelect(barcode: BarCodes?) {

    }

    override fun onChangeName(barcode: BarCodes?, newName: String) {
        if (newName.isNotEmpty()) {
            val updatedBarcodes = task.barcodes?.map {
                if (it == barcode) {
                    it.copy(nameQr = newName)
                } else {
                    it
                }
            }
            task.barcodes = updatedBarcodes
            updatedBarcodes?.let { adapter.setupData(it) }
        }
    }


    override fun onMore(barcode: BarCodes?) {

    }

    override fun onResume() {
        super.onResume()
        showHeaderSettingTask()
    }

    override fun dialogChangeNameBarcodeOnDismiss() {

    }

    override fun dialogChangeNameBarcodeOnClickOk(value: String) {
        editbarcodeTmp?.let { onChangeName(it, value) }
        editbarcodeTmp = null
    }

    override fun onOkDeleteListener() {
        editbarcodeTmp?.let { onDelete(it) }
        editbarcodeTmp = null
    }
}
