package com.alarm.clock.reminder.alarmclock.simplealarm.view.task.qrcode

import android.util.Size
import android.view.LayoutInflater
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.PushType
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.popTo
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.pushTo
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ActivityBarcodeScanningBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.gone
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.showCustomToast
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.visible
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.dialog.DialogChangeNameBarcode
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.BarCodes
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.BaseTaskSettingFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.CUR_TASK
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.TaskType
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.action_task.ActionTaskActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.qrcode.analyzer.ScanningResultListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.qrcode.analyzer.ZXingBarcodeAnalyzer
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.settingTask.SettingTaskActivity
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

const val ARG_SCANNING_SDK = "scanning_SDK"

@AndroidEntryPoint
class BarcodeScanningFragment : BaseTaskSettingFragment<ActivityBarcodeScanningBinding>(),
    DialogChangeNameBarcode.Listener {

    private var scannedString: String? = null
    private lateinit var cameraProvider: ProcessCameraProvider


    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    override fun makeBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): ActivityBarcodeScanningBinding {
        return ActivityBarcodeScanningBinding.inflate(inflater, container, false)
    }

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService
    private var flashEnabled = false
    override val taskType: TaskType = TaskType.SCANQR

    private val imageAnalysis: ImageAnalysis by lazy {
        ImageAnalysis.Builder()
            .setTargetResolution(Size(binding.cameraPreview.width, binding.cameraPreview.height))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
    }

    override fun setupView() {
        super.setupView()
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(requireContext()))
        binding.overlay.post {
            binding.overlay.setViewFinder()
        }
        if (arguments != null && requireArguments().containsKey(ARG_SCANNING_SDK)) {
            binding.ivFlash2Control.gone()
            binding.clock.visible()
            hideHeaderActionTask()
            binding.clock.start(from = 30, onTick = { _, _ ->
            }, onFinish = {})
        } else {
            binding.ivFlash2Control.visible()
            binding.ivFlash2Control.setOnSingleClickListener {
                popTo()
            }
            hideHeaderSettingTask()
            binding.clock.gone()
        }

    }

    private fun hideHeaderSettingTask() {
        val activity = requireActivity() as? SettingTaskActivity
        activity?.binding?.header?.gone()
    }

    private fun hideHeaderActionTask() {
        val activity = mActivity
        if (activity is ActionTaskActivity) {
            activity.binding.apply {
                header.gone()
                progressBar.gone()
            }
        }
    }

    private val scanningListener = object : ScanningResultListener {
        override fun onScanned(result: String) {
            fragmentScope.launch {
                scannedString = result
                binding.overlay.stopAnimation()
                if (arguments != null && arguments!!.containsKey(ARG_SCANNING_SDK)) {
                    val scanningSdkData = arguments?.getString(ARG_SCANNING_SDK)
                    if (result == scanningSdkData) {
                        popTo()
                        parentFragmentManager.setFragmentResult(
                            "KEY_RESULT", bundleOf("bundleKey" to true)
                        )
                    } else {
                        binding.notFoundQr.visible()
                        delay(2000)
                        popTo()
                    }
                } else {
                    imageAnalysis.clearAnalyzer()
                    cameraProvider.unbindAll()
                    DialogChangeNameBarcode.newInstance(
                        result,
                        false
                    ).show(childFragmentManager, "")
                }

            }
        }
    }


    private fun bindPreview(cameraProvider: ProcessCameraProvider?) {
        if (activity?.isFinishing == true || activity?.isDestroyed == true) {
            return
        }

        cameraProvider?.unbindAll()

        val preview: Preview = Preview.Builder().build()

        val cameraSelector: CameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        val orientationEventListener = object : OrientationEventListener(requireContext()) {
            override fun onOrientationChanged(orientation: Int) {
                // Monitors orientation values to determine the target rotation value
                val rotation: Int = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }

                imageAnalysis.targetRotation = rotation
            }

        }
        orientationEventListener.enable()

        //switch the analyzers here, i.e. MLKitBarcodeAnalyzer, ZXingBarcodeAnalyzer
        imageAnalysis.setAnalyzer(cameraExecutor, ZXingBarcodeAnalyzer(scanningListener))
        preview.setSurfaceProvider(binding.cameraPreview.surfaceProvider)

        val camera = cameraProvider?.bindToLifecycle(this, cameraSelector, imageAnalysis, preview)

        if (camera?.cameraInfo?.hasFlashUnit() == true) {
            binding.ivFlashControl.visibility = View.VISIBLE

            camera.cameraControl.apply {
                binding.ivFlashControl.setOnClickListener {
                    enableTorch(!flashEnabled)
                }
            }

            camera.cameraInfo.torchState.observe(this) { torchState ->
                torchState?.let {
                    flashEnabled = it == TorchState.ON
                    binding.ivFlashControl.setImageResource(
                        if (flashEnabled) R.drawable.ic_flash_on else R.drawable.ic_flash_off
                    )
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        // Shut down our background executor
        cameraExecutor.shutdown()
    }

    override fun dialogChangeNameBarcodeOnDismiss() {
        bindPreview(cameraProvider)
        binding.overlay.startAnimation()
    }

    override fun dialogChangeNameBarcodeOnClickOk(value: String) {
        val scannedString = scannedString ?: return
        val task = taskSettingModel ?: return
        val isResultPresent = task.barcodes?.any { it.idQr == scannedString }
        if (!isResultPresent!!) {
            (task.barcodes as MutableList<BarCodes>).add(
                BarCodes(
                    nameQr = value,
                    idQr = scannedString
                )
            )
        } else {
            Toast(context).showCustomToast(
                getString(R.string.you_have_added),
                requireActivity()
            )
        }
        pushTo(
            R.id.action_barcodeScanningFragment2_to_fragmentListBarcode,
            bundleOf(CUR_TASK to task),
            PushType.NONE
        )

        this.scannedString = null
    }
}