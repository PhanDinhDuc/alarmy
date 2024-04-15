package com.alarm.clock.reminder.alarmclock.simplealarm.view.permission

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
import android.view.LayoutInflater
import android.widget.Switch
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseVMActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.Settings.*
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.BedTimeData
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ActivityPermissionBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.gone
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.hasPermissions
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.bedtime.BedTimeSettingViewModel
import com.alarm.clock.reminder.alarmclock.simplealarm.view.first_alarm.FirstTimeAlarmActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.permission.PermissionActivity.Companion.postNotificationPermission
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PermissionActivity : BaseVMActivity<ActivityPermissionBinding, BedTimeSettingViewModel>() {

    override val viewModel: BedTimeSettingViewModel by viewModels()
    override fun makeBinding(layoutInflater: LayoutInflater): ActivityPermissionBinding {
        return ActivityPermissionBinding.inflate(layoutInflater)
    }
    val nm by lazy {
        getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager
    }


    private fun TextView.setEnable(enable: Boolean = true) {
        setTextColor(
            if (enable) ContextCompat.getColor(
                this@PermissionActivity,
                R.color.textColor
            ) else Color.GRAY
        )
        isEnabled = enable
    }

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)

        binding.txtGo.setEnable(true)

        binding.txtGo.setOnSingleClickListener {
            viewModel.insertBedtime(BedTimeData.Default)
            val intent = Intent(this, FirstTimeAlarmActivity::class.java)
            startActivity(intent)
            finish()
        }


        binding.switchAccessNotification.setOnCheckedChangeListener { view, isChecked ->
            if (isChecked && view.isPressed) {
                val intent = Intent(ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                startActivity(intent)
            }

            if (!isChecked && view.isPressed) {
                binding.switchAccessNotification.isChecked = true
                val intent = Intent(ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                startActivity(intent)
            }
        }

        binding.txtPermissionDevice.text =
            buildString {
                append(getString(R.string.app_name))
                append(" ")
                append(getString(R.string.permission_device))
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            binding.switchExactAlarm.setOnCheckedChangeListener { view, isChecked ->
                if (isChecked && view.isPressed) {
                    requestAlarmPermission()
                }

                if (!isChecked && view.isPressed) {
                    binding.switchExactAlarm.isChecked = true
                    requestAlarmPermission()
                }
            }
        } else {
            binding.containerExactAlarm.gone()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            binding.switchNoti.setOnCheckedChangeListener { view, isChecked ->
                if (isChecked && view.isPressed) {
                    requestNotificationPermission {
                        binding.switchNoti.isChecked = false
                        goToSettingApp()
                    }
                }

                if (!isChecked && view.isPressed) {
                    binding.switchNoti.isChecked = true
                    goToSettingApp()
                }
            }
        } else {
            binding.containerPermission.gone()
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && packageManager.hasSystemFeature(
//                PackageManager.FEATURE_SENSOR_STEP_DETECTOR
//            )
//        ) {
//            binding.switchRecognition.setOnCheckedChangeListener { view, isChecked ->
//                if (isChecked && view.isPressed) {
//                    requestActionPermission {
//                        binding.switchRecognition.isChecked = false
//                        goToSettingApp()
//                    }
//                }
//
//                if (!isChecked && view.isPressed) {
//                    binding.switchRecognition.isChecked = true
//                    goToSettingApp()
//                }
//            }
//        } else {
//            binding.containerRecognition.gone()
//        }
        binding.switchCamera.setOnCheckedChangeListener { view, isChecked ->
            if (isChecked && view.isPressed) {
                binding.switchCamera.isChecked = false
                requestCameraPermission()
            }

            if (!isChecked && view.isPressed) {
                binding.switchCamera.isChecked = true
                goToSettingApp()
            }
        }
        setEnableNextButton()
    }

    private fun requestAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent()
            intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
            startActivity(intent)
        }
    }

    private fun requestCameraPermission() {
        if (!shouldShowRequestPermissionRationale(cameraPermission)) {
            permissionsResult.launch(arrayOf(cameraPermission))
        } else {
            binding.switchCamera.isChecked = false
            goToSettingApp()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            binding.switchExactAlarm.isChecked = alarmManager.canScheduleExactAlarms()
        }
        val array = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(postNotificationPermission, cameraPermission)
        } else {
            arrayOf(cameraPermission)
        }

        array.forEach {
            if (this.hasPermissions(arrayOf(it))) {
                onPermissionGranted(mapOf(it to true))
            } else {
                onPermissionDenied(mapOf(it to false))
            }
        }

        binding.switchAccessNotification.isChecked = nm.isNotificationPolicyAccessGranted

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            if (this.hasPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION))) {
//                onPermissionGranted(mapOf(Manifest.permission.ACTIVITY_RECOGNITION to true))
//            } else {
//                onPermissionDenied(mapOf(Manifest.permission.ACTIVITY_RECOGNITION to false))
//            }
//        }
        setEnableNextButton()
    }

    private fun goToSettingApp() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", this.packageName, null)
        intent.data = uri
        this.startActivity(intent)
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun Switch.setCheck(checked: Boolean) {
        isChecked = checked
//        isEnabled = !isChecked
    }

    override fun onPermissionGranted(permissions: Map<String, Boolean>) {
        super.onPermissionGranted(permissions)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && permissions.containsKey(
                postNotificationPermission
            )
        ) {
            binding.switchNoti.setCheck(true)
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && permissions.containsKey(
//                Manifest.permission.ACTIVITY_RECOGNITION
//            )
//        ) {
//            binding.switchRecognition.setCheck(true)
//        }
        if (permissions.containsKey(cameraPermission)) {
            binding.switchCamera.setCheck(true)
        }
        setEnableNextButton()
    }

    override fun onPermissionDenied(permissions: Map<String, Boolean>) {
        super.onPermissionDenied(permissions)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && permissions.containsKey(
                postNotificationPermission
            )
        ) {
            binding.switchNoti.setCheck(false)
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && permissions.containsKey(
//                Manifest.permission.ACTIVITY_RECOGNITION
//            )
//        ) {
//            binding.switchRecognition.setCheck(false)
//        }
        if (permissions.containsKey(cameraPermission)) {
            binding.switchCamera.setCheck(false)
        }
        setEnableNextButton()
    }

    private fun setEnableNextButton() {
        val isCameraChecked = binding.switchCamera.isChecked
        val isExactAlarmChecked = binding.switchExactAlarm.isChecked
//        val isRecognitionChecked = binding.switchRecognition.isChecked
        val isNotiChecked = binding.switchNoti.isChecked

//        val isStepDetectorAvailable =
//            packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR)

        val enableCondition = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> isExactAlarmChecked && isCameraChecked && isNotiChecked
//            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isStepDetectorAvailable -> isExactAlarmChecked && isCameraChecked && isRecognitionChecked
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> isExactAlarmChecked && isCameraChecked
//            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && isStepDetectorAvailable -> isCameraChecked && isRecognitionChecked
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> isCameraChecked
            else -> isCameraChecked
        }
        binding.txtGo.setEnable(enableCondition)
    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        const val postNotificationPermission = Manifest.permission.POST_NOTIFICATIONS
        const val cameraPermission = Manifest.permission.CAMERA
    }
}

fun BaseActivity<*>.requestActionPermission(action: () -> Unit) {
    if (!shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION)) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsResult.launch(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION))
        } else {
            action.invoke()
        }
    } else {
        action.invoke()
    }
}

fun BaseActivity<*>.requestNotificationPermission(action: () -> Unit) {
    if (!shouldShowRequestPermissionRationale(postNotificationPermission)) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsResult.launch(arrayOf(postNotificationPermission))
        } else {
            action.invoke()
        }
    } else {
        action.invoke()
    }
}