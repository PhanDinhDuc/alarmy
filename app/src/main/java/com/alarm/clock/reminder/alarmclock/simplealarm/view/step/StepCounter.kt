package com.alarm.clock.reminder.alarmclock.simplealarm.view.step

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.hasPermissions
import kotlin.math.abs

interface StepCounter {

    fun start(
        threshold: Float = 0.6f,
        delta: Float = 4f,
        delay: Int = SensorManager.SENSOR_DELAY_GAME,
        onEvent: () -> Unit
    )

    fun cancel()
}

enum class AlternativeType {
    TYPE_STEP_DETECTOR,
    TYPE_ACCELEROMETER
}

class StepCounterImpl(
    private val context: Context
) : SensorEventListener, StepCounter {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private var isActive = false
    private var delay: Int = SensorManager.SENSOR_DELAY_GAME
    private var threshold: Float = 1f
    private var delta: Float = 4f
    private var onEvent: () -> Unit = {}

    private var alternativeType: AlternativeType = AlternativeType.TYPE_STEP_DETECTOR
    private var step = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    private var default = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    override fun start(
        threshold: Float,
        delta: Float,
        delay: Int,
        onEvent: () -> Unit
    ) {
        this.threshold = threshold
        this.delta = delta
        this.delay = delay
        this.onEvent = onEvent
        if (isActive) cancel()
        isActive = true
        alternativeType = when {
            step != null && context.hasPermissions(arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION)) -> AlternativeType.TYPE_STEP_DETECTOR
            else -> AlternativeType.TYPE_ACCELEROMETER
        }

        sensorManager.registerListener(
            this,
            when (alternativeType) {
                AlternativeType.TYPE_STEP_DETECTOR -> step
                AlternativeType.TYPE_ACCELEROMETER -> default
            }, delay
        )
    }


    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        when (alternativeType) {
            AlternativeType.TYPE_ACCELEROMETER -> updateCurrentSteps(event.values)
            AlternativeType.TYPE_STEP_DETECTOR -> onEvent.invoke()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun cancel() {
        isActive = false
        sensorManager.unregisterListener(this)
    }

    private var x1 = 0f
    private var y1 = 0f
    private var z1 = 0f

    private fun updateCurrentSteps(eventData: FloatArray) {
        val x = eventData[0]
        val y = eventData[1]
        val z = eventData[2]
        val acceleration = kotlin.math.sqrt((x * x + y * y + z * z))
        if (abs(x) < 1.5f && y > 0 && z > 0 && acceleration in (SensorManager.GRAVITY_EARTH - threshold..SensorManager.GRAVITY_EARTH + threshold + delta) && (abs(
                acceleration - prev
            ) < delta || prev == 0f)
        ) {
            Log.d("AAA", "updateCurrentSteps:$acceleration")
            detect(acceleration)
        }
        x1 = x
        y1 = y
        z1 = z
    }

    private var currentSample = 0
    private var isActiveCounter = true
    private val inActiveSample = 12
    private var currentStepType: StepType = StepType.INITIAL
    private var prev = 0f
    private var top = 0f
    private var lastDetect = 0L

    private fun detect(accelerometerValue: Float) {
        if (currentSample == inActiveSample) {
            currentSample = 0
            if (!isActiveCounter) isActiveCounter = true
        }
        if (isActiveCounter) {
            when (currentStepType) {
                StepType.INITIAL -> {
                    if (accelerometerValue > SensorManager.GRAVITY_EARTH + threshold) {
                        currentStepType = StepType.TOP
                        top = accelerometerValue
                        Log.d("AAA", "isTop")
                    }
                }

                StepType.TOP -> {
                    if (accelerometerValue < top) {
                        Log.d("AAA", "isInit")
                        val current = System.currentTimeMillis()
                        if (current - lastDetect > 500L) {
                            Log.d("AAA", "detected ${current - lastDetect}")
                            isActiveCounter = false
                            currentSample = 0
                            onEvent.invoke()
                        }
                        currentStepType = StepType.INITIAL
                        lastDetect = current
                    }
                }
            }
        }
        prev = accelerometerValue
        ++currentSample
    }

}

enum class StepType {
    INITIAL, TOP
}

