package com.appleader707.syncrecorder.service.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.appleader707.syncrecorder.business.usecase.convert.ConvertJsonToSrtUseCase
import com.appleader707.syncrecorder.business.usecase.directory.GetSyncRecorderDirectoryUseCase
import com.appleader707.syncrecorder.domain.SensorSnapshot
import com.google.gson.Gson
import java.io.BufferedWriter
import java.io.File
import javax.inject.Inject

class SensorService @Inject constructor(
    private val getSyncRecorderDirectoryUseCase: GetSyncRecorderDirectoryUseCase,
    private val convertJsonToSrtUseCase: ConvertJsonToSrtUseCase,
) {
    private lateinit var sensorManager: SensorManager
    private val sensorData = mutableListOf<SensorSnapshot>()
    private var sensorWriter: BufferedWriter? = null
    private var recordingStartNanos: Long = 0L

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val snapshot = SensorSnapshot(
                type = event.sensor.type,
                timestampMills = (event.timestamp - recordingStartNanos) / 1_000_000,
                values = event.values.toList()
            )
            sensorData.add(snapshot)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    fun startSensors(
        context: Context,
        recordingStartNanos: Long,
        recordingCount: Int,
        imuFrequency: Int
    ) {
        this.recordingStartNanos = recordingStartNanos

        val sensorFile = File(
            getSyncRecorderDirectoryUseCase(),
            "sensor_data_${recordingCount}.jsonl"
        )
        sensorWriter = sensorFile.bufferedWriter()

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val sensors = listOf(
            Sensor.TYPE_LINEAR_ACCELERATION,
        )

        sensors.forEach { type ->
            val sensor = sensorManager.getDefaultSensor(type)
            sensorManager.registerListener(sensorListener, sensor, imuFrequency)
        }
    }

    fun stopSensors(recordingCount: Int) {
        sensorManager.unregisterListener(sensorListener)
        val json = Gson().toJson(sensorData)
        sensorWriter?.apply {
            write(json)
            flush()
            close()
        }
        convertJsonToSrtUseCase(
            "sensor_data_${recordingCount}.jsonl",
            "sensor_data_${recordingCount}.srt"
        )
        sensorData.clear()
    }
}