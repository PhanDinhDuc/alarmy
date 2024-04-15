package com.alarm.clock.reminder.alarmclock.simplealarm.view.task.squat

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt


interface SquatCounter {
    fun start(
        threshold: Float = 2.2f,
        delay: Int = SensorManager.SENSOR_DELAY_GAME,
        delta: Float = 3.3f,
        onEvent: () -> Unit
    )

    fun cancel()
}


class SquatCounterImpl constructor(context: Context) : SensorEventListener,
    SquatCounter {

    private var mGravityX = 0f
    private var mGravityY = 0f
    private var mGravityZ = 0f
    private var transition = Transition.STANDING

    private var mSensorManager: SensorManager
    private var mLinearAccelerometer: Sensor?
    private var mGravity: Sensor?
    private var mAccelerometer: Sensor?
    private var alternativeType: AlternativeType = AlternativeType.TYPE_ACCELEROMETER

    private var delay = SensorManager.SENSOR_DELAY_GAME
    private var threshold = 3f
    private var delta = 3f
    private var onEvent: () -> Unit = {}
    private var isActive = false

    override fun start(
        threshold: Float,
        delay: Int,
        delta: Float,
        onEvent: () -> Unit
    ) {
        if (isActive) cancel()
        isActive = true
        this.threshold = threshold
        this.delay = delay
        this.delta = delta
        this.onEvent = onEvent

        alternativeType =
            if (mGravity != null && mLinearAccelerometer != null) AlternativeType.TYPE_LINEAR_ACCELERATION else AlternativeType.TYPE_ACCELEROMETER
        when (alternativeType) {
            AlternativeType.TYPE_ACCELEROMETER -> {
                mSensorManager.registerListener(this, mAccelerometer, delay)
            }

            AlternativeType.TYPE_LINEAR_ACCELERATION -> {
                mSensorManager.registerListener(this, mLinearAccelerometer, delay)
                mSensorManager.registerListener(this, mGravity, delay)
            }
        }
    }

    override fun cancel() {
        mGravityX = 0f
        mGravityY = 0f
        mGravityZ = 0f
        transition = Transition.STANDING
        isActive = false
        mSensorManager.unregisterListener(this)
    }

    private fun updateGravity(event: SensorEvent) {
        mGravityX = event.values[0]
        mGravityY = event.values[1]
        mGravityZ = event.values[2]
    }

    private var prev = 0f

    private fun onLinearAccelerometerChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val proj = projection(x, y, z, mGravityX, mGravityY, mGravityZ)
        Log.d("AAA", "onLinearAccelerometerChanged:$proj")
        onEventChange(proj)
    }

    private fun isMovingDown(proj: Float, prev: Float): Boolean {
        return proj in (-(threshold + delta)..-(threshold)) && proj < prev
    }

    private fun isDown(proj: Float, prev: Float): Boolean {
        return proj in (-(threshold)..-(threshold - delta)) && proj > prev
    }

    private fun isMovingUp(proj: Float, prev: Float): Boolean {
        return proj in ((threshold)..(threshold + delta)) && proj > prev
    }

    private fun isUp(proj: Float, prev: Float): Boolean {
        return proj in ((threshold - delta)..(threshold)) && proj < prev
    }

    private fun isStanding(proj: Float): Boolean {
        return proj.roundToInt() == 0
    }

    private fun unitVector(x: Float, y: Float, z: Float): FloatArray {
        val mag = sqrt(x * x + y * y + z * z)
        return floatArrayOf(x / mag, y / mag, z / mag)
    }

    private fun dotProduct(
        x: Float, y: Float, z: Float, otherX: Float, otherY: Float,
        otherZ: Float
    ): Float {
        return x * otherX + y * otherY + z * otherZ
    }

    private fun projection(
        x: Float, y: Float, z: Float, ontoX: Float, ontoY: Float,
        ontoZ: Float
    ): Float {
        val ontoUnitVector = unitVector(ontoX, ontoY, ontoZ)
        return dotProduct(
            x, y, z, ontoUnitVector[0], ontoUnitVector[1],
            ontoUnitVector[2]
        )
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val sensor = event?.sensor ?: return
        when (alternativeType) {
            AlternativeType.TYPE_ACCELEROMETER -> onAccelerometerChanged(event)
            AlternativeType.TYPE_LINEAR_ACCELERATION -> {
                if (sensor == mLinearAccelerometer) {
                    onLinearAccelerometerChanged(event)
                } else if (sensor == mGravity) {
                    updateGravity(event)
                }
            }
        }
    }

    private fun onEventChange(proj: Float) {
        if (abs(proj - prev) < delta) {
            when (transition) {
                Transition.STANDING -> {
                    Log.d("AAA", "Transition.STANDING")
                    if (isMovingDown(proj, prev)) {
                        transition = Transition.MOVING_DOWN
                        Log.d("AAA", "isMovingDown")
                    }
                }

                Transition.MOVING_DOWN -> {
                    Log.d("AAA", "Transition.MOVING_DOWN")
                    if (isDown(proj, prev)) {
                        transition = Transition.DOWN
                        Log.d("AAA", "isDown")
                    }
                }

                Transition.DOWN -> {
                    Log.d("AAA", "Transition.DOWN")
                    if (isMovingUp(proj, prev)) {
                        transition = Transition.MOVING_UP
                        Log.d("AAA", "isMovingUp")
                    }
                }

                Transition.MOVING_UP -> {
                    Log.d("AAA", "Transition.MOVING_UP")
                    if (isUp(proj, prev)) {
                        transition = Transition.UP
                        Log.d("AAA", "isUp")
                    }
                }

                Transition.UP -> {
                    Log.d("AAA", "Transition.UP")
                    if (isStanding(proj)) {
                        Log.d("AAA", "isStanding")
                        transition = Transition.STANDING
                        onEvent.invoke()
                    }
                }
            }
        }
        prev = proj
    }

    private fun onAccelerometerChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        if (abs(x) > delta) return
        val proj = sqrt(x * x + z * z + y * y) - SensorManager.GRAVITY_EARTH
        Log.d("AAA", "onLinearAccelerometerChanged:$proj")
        onEventChange(proj)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    init {
        mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mLinearAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }
}

enum class AlternativeType {
    TYPE_ACCELEROMETER,
    TYPE_LINEAR_ACCELERATION,
}

private enum class Transition {
    STANDING, UP, MOVING_DOWN, MOVING_UP, DOWN
}