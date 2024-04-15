package com.alarm.clock.reminder.alarmclock.simplealarm.view.shake

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager


interface ShakeCounter {

    fun start(
        threshold: Int = 11,
        delay: Int = SensorManager.SENSOR_DELAY_GAME,
        onEvent: () -> Unit
    )

    fun cancel()
}


class ShakeCounterImpl(context: Context) : ShakeCounter, SensorEventListener {

    private val queue = SampleQueue()
    private var mSensorManager: SensorManager
    private var accelerometer: Sensor?
    private var isActive = false


    private var accelerationThreshold = 11
    private var sensorDelay: Int = SensorManager.SENSOR_DELAY_GAME
    private var onEvent: () -> Unit = {}


    init {
        mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun start(
        threshold: Int,
        delay: Int,
        onEvent: () -> Unit
    ) {
        if (isActive) cancel()
        isActive = true
        this.accelerationThreshold = threshold
        this.sensorDelay = delay
        this.onEvent = onEvent
        mSensorManager.registerListener(this, accelerometer, delay)
    }


    override fun cancel() {
        isActive = false
        queue.clear()
        mSensorManager.unregisterListener(this, accelerometer)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val accelerating = isAccelerating(event)
        val timestamp = event.timestamp
        queue.add(timestamp, accelerating)
        if (queue.isShaking) {
            queue.clear()
            onEvent.invoke()
        }
    }

    private fun isAccelerating(event: SensorEvent): Boolean {
        val ax = event.values[0]
        val ay = event.values[1]
        val az = event.values[2]

        val magnitudeSquared = (ax * ax + ay * ay + az * az).toDouble()
        return magnitudeSquared > accelerationThreshold * accelerationThreshold
    }


    internal class SampleQueue {
        private val pool = SamplePool()
        private var oldest: Sample? = null
        private var newest: Sample? = null
        private var sampleCount = 0
        private var acceleratingCount = 0

        /**
         * Adds a sample.
         *
         * @param timestamp    in nanoseconds of sample
         * @param accelerating true if > [.accelerationThreshold].
         */
        fun add(timestamp: Long, accelerating: Boolean) {
            // Purge samples that proceed window.
            purge(timestamp - MAX_WINDOW_SIZE)

            // Add the sample to the queue.
            val added = pool.acquire()
            added.timestamp = timestamp
            added.accelerating = accelerating
            added.next = null
            if (newest != null) {
                newest?.next = added
            }
            newest = added
            if (oldest == null) {
                oldest = added
            }

            // Update running average.
            sampleCount++
            if (accelerating) {
                acceleratingCount++
            }
        }

        /** Removes all samples from this queue.  */
        fun clear() {
            while (oldest != null) {
                val removed = oldest
                oldest = removed?.next
                removed?.let { pool.release(it) }
            }
            newest = null
            sampleCount = 0
            acceleratingCount = 0
        }

        /** Purges samples with timestamps older than cutoff.  */
        private fun purge(cutoff: Long) {
            while (sampleCount >= MIN_QUEUE_SIZE && oldest != null && cutoff - oldest!!.timestamp > 0) {
                val removed = oldest
                if (removed?.accelerating == true) {
                    acceleratingCount--
                }
                sampleCount--
                oldest = removed?.next
                if (oldest == null) {
                    newest = null
                }
                removed?.let { pool.release(it) }
            }
        }

        /** Copies the samples into a list, with the oldest entry at index 0.  */
        fun asList(): List<Sample> {
            val list: MutableList<Sample> = ArrayList()
            var s = oldest
            while (s != null) {
                list.add(s)
                s = s.next
            }
            return list
        }

        val isShaking: Boolean
            /**
             * Returns true if we have enough samples and more than 3/4 of those samples
             * are accelerating.
             */
            get() = newest != null && oldest != null && newest!!.timestamp - oldest!!.timestamp >= MIN_WINDOW_SIZE && acceleratingCount >= (sampleCount shr 1) + (sampleCount shr 2)

        companion object {
            /** Window size in ns. Used to compute the average.  */
            private const val MAX_WINDOW_SIZE: Long = 500000000 // 0.5s
            private const val MIN_WINDOW_SIZE = MAX_WINDOW_SIZE shr 1 // 0.25s

            /**
             * Ensure the queue size never falls below this size, even if the device
             * fails to deliver this many events during the time window. The LG Ally
             * is one such device.
             */
            private const val MIN_QUEUE_SIZE = 4
        }
    }

    /** An accelerometer sample.  */
    internal class Sample {
        /** Time sample was taken.  */
        var timestamp: Long = 0

        /** If acceleration > [.accelerationThreshold].  */
        var accelerating = false

        /** Next sample in the queue or pool.  */
        var next: Sample? = null
    }

    /** Pools samples. Avoids garbage collection.  */
    internal class SamplePool {
        private var head: Sample? = null

        /** Acquires a sample from the pool.  */
        fun acquire(): Sample {
            var acquired = head
            if (acquired == null) {
                acquired = Sample()
            } else {
                // Remove instance from pool.
                head = acquired.next
            }
            return acquired
        }

        /** Returns a sample to the pool.  */
        fun release(sample: Sample) {
            sample.next = head
            head = sample
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

}