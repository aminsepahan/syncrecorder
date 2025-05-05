package com.appleader707.syncrecorder.service.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnifiedSensorService @Inject constructor(
    private val sensorManager: SensorManager,
    private val aggregator: SensorDataAggregator
) {
    private val listeners = mutableMapOf<Int, SensorEventListener>()

    private fun createListener(sensorType: Int, sensorName: String): SensorEventListener {
        return object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                aggregator.recordEvent(sensorType, sensorName, event)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }.also {
            listeners[sensorType] = it
        }
    }

    fun startSensors(
        imuFrequency: Int
    ) {
        //startSensor(Sensor.TYPE_ACCELEROMETER, "accelerometer", imuFrequency)
        //startSensor(Sensor.TYPE_GYROSCOPE, "gyroscope", imuFrequency)
        //startSensor(Sensor.TYPE_MAGNETIC_FIELD, "magnetometer", imuFrequency)
        startSensor(Sensor.TYPE_ACCELEROMETER, "accelerometer", imuFrequency)
    }

    private fun startSensor(type: Int, name: String, delay: Int) {
        val sensor = sensorManager.getDefaultSensor(type) ?: return
        val listener = createListener(type, name)
        sensorManager.registerListener(listener, sensor, delay)
    }

    suspend fun stopSensors() = withContext(Dispatchers.IO) {
        listeners.values.forEach { sensorManager.unregisterListener(it) }
        listeners.clear()
    }
}
