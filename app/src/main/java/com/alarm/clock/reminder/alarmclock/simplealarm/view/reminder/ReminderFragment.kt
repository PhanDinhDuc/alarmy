package com.alarm.clock.reminder.alarmclock.simplealarm.view.reminder


import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseVMFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseViewModel
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Reminder
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.repository.ReminderRepository
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.FragmentReminderBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.PopupMoreReminderBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Converter.asPixels
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.gone
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.visible
import com.alarm.clock.reminder.alarmclock.simplealarm.view.adapter.ReminderAdapter
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.dialog.DialogDeleteBarcode
import com.alarm.clock.reminder.alarmclock.simplealarm.view.main.PopupWindowHandle
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class ReminderFragment : BaseVMFragment<FragmentReminderBinding, ReminderViewModel>(),
    DialogDeleteBarcode.OnOkDeleteListener, PopupWindowHandle {

    override val viewModel: ReminderViewModel by viewModels()
    private var popupWindow: PopupWindow? = null
    private val reminderAdapter by lazy {
        ReminderAdapter(
            selected = {
                editLauncher.launch(
                    Intent(
                        requireContext(),
                        AddReminderActivity::class.java
                    ).apply {
                        putExtra(AddReminderViewModel.REMINDER_KEY, it)
                    })
            },
            selectedMore = { reminder, view ->
                viewModel.showPopup.postValue(reminder to view)
            }
        )
    }

    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentReminderBinding {
        return FragmentReminderBinding.inflate(inflater)
    }


    override fun setupView() {
        super.setupView()
        binding.recyclerview.adapter = reminderAdapter
        binding.maskPopup.setOnSingleClickListener {
            dismissPopupWindow()
        }
        observeData()
    }

    private fun observeData() {
        viewModel.reminders.observe(this) {
            if (it.isNullOrEmpty()) {
                binding.recyclerview.visibility = View.GONE
                binding.noReminder.visibility = View.VISIBLE
                binding.noReminderDesc.visibility = View.VISIBLE
                binding.bgEmpty.visibility = View.VISIBLE
            } else {
                binding.recyclerview.visibility = View.VISIBLE
                binding.noReminder.visibility = View.GONE
                binding.noReminderDesc.visibility = View.GONE
                binding.bgEmpty.visibility = View.GONE
            }
            reminderAdapter.setupData(it)
        }
        viewModel.showPopup.observe(this) {
            if (it != null) {
                showPopUp(it.first, it.second)
                binding.maskPopup.visible()
            } else {
                popupWindow?.dismiss()
                binding.maskPopup.gone()
                popupWindow = null
            }
        }
        viewModel.isShowToast.observe(this) {
            if (it) binding.toast.visible() else binding.toast.gone()
        }
    }

    private val editLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val isDelete =
                        data.getBooleanExtra(AddReminderActivity.KEY_IS_DELETE, false)
                    if (isDelete) {
                        viewModel.showToast()
                    }
                }
            }
        }

    private fun showPopUp(
        reminder: Reminder,
        it: View
    ) {
        popupWindow?.dismiss()
        val layout = PopupMoreReminderBinding.inflate(
            LayoutInflater.from(requireContext()),
            null,
            false
        )

        val popupWindow = PopupWindow(
            layout.root
        ).apply {
            isFocusable = false
        }

        layout.delete.setOnSingleClickListener {
            viewModel.tempReminder = reminder
            DialogDeleteBarcode.show(
                childFragmentManager,
                getString(R.string.do_you_want_to_delete_reminder)
            )
            dismissPopupWindow()
        }
        layout.edit.setOnSingleClickListener {
            editLauncher.launch(Intent(requireContext(), AddReminderActivity::class.java).apply {
                putExtra(AddReminderViewModel.REMINDER_KEY, reminder)
            })
            dismissPopupWindow()
        }
        popupWindow.setOnDismissListener {
            dismissPopupWindow()
        }
        popupWindow.elevation = 10f
        popupWindow.setBackgroundDrawable(null)
        layout.root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val measuredWidth = layout.root.measuredWidth
        val measuredHeight = layout.root.measuredHeight
        popupWindow.width = measuredWidth
        popupWindow.height = measuredHeight
        popupWindow.showAsDropDown(
            it, - asPixels(16), -asPixels(16), Gravity.END
        )
        this.popupWindow = popupWindow
    }

    override fun onOkDeleteListener() {
        viewModel.delete()
    }

    override fun isPopupWindowShow(): Boolean = popupWindow?.isShowing == true

    override fun dismissPopupWindow() {
        viewModel.showPopup.value = null
    }
}


@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository
) :
    BaseViewModel() {
    fun delete() {
        viewModelScope.launch(Dispatchers.IO) {
            tempReminder?.id?.let {
                reminderRepository.delete(it)
                showToast()
            }
        }
    }

    fun showToast() {
        viewModelScope.launch(Dispatchers.IO) {
            isShowToast.postValue(true)
            delay(3000L)
            isShowToast.postValue(false)
        }
    }

    val isShowToast = MutableLiveData(false)
    var tempReminder: Reminder? = null
    val showPopup = MutableLiveData<Pair<Reminder, View>?>()
    val reminders = reminderRepository.reminders

}
