package com.appleader707.syncrecorder.service.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import com.appleader707.syncrecorder.business.usecase.convert.ConvertJsonToSrtUseCase
import com.appleader707.syncrecorder.business.usecase.directory.GetSyncRecorderDirectoryUseCase
import com.appleader707.syncrecorder.domain.SensorSnapshot
import com.google.gson.Gson
import java.io.BufferedWriter
import java.io.File
import javax.inject.Inject

class GyroscopeService @Inject constructor(
    private val sensorManager: SensorManager,
    private val getSyncRecorderDirectoryUseCase: GetSyncRecorderDirectoryUseCase,
    private val convertJsonToSrtUseCase: ConvertJsonToSrtUseCase,
) {
    private val sensorData = mutableListOf<SensorSnapshot>()
    private var sensorWriter: BufferedWriter? = null
    private var recordingStartNanos: Long = 0L
    private var bootTimeNanos: Long = 0L

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val wallClockTimeNanos = bootTimeNanos + event.timestamp
            val relativeTimeMillis = (wallClockTimeNanos - recordingStartNanos) / 1_000_000

            val snapshot = SensorSnapshot(
                type = event.sensor.type,
                timestampMills = relativeTimeMillis,
                values = event.values.toList()
            )
            sensorData.add(snapshot)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    fun startGyroscopeSensor(
        recordingCount: Int,
        recordingStartNanos: Long,
        imuFrequency: Int
    ) {
        this.bootTimeNanos = SystemClock.elapsedRealtimeNanos() - System.nanoTime()
        this.recordingStartNanos = recordingStartNanos

        val sensorFile = File(
            getSyncRecorderDirectoryUseCase(),
            "gyroscope_data_$recordingCount.jsonl"
        )
        sensorWriter = sensorFile.bufferedWriter()

        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        sensorManager.registerListener(sensorListener, sensor, imuFrequency)
    }

    fun stopGyroscopeSensor(recordingCount: Int) {
        sensorManager.unregisterListener(sensorListener)
        val json = Gson().toJson(sensorData)
        sensorWriter?.apply {
            write(json)
            flush()
            close()
        }

        convertJsonToSrtUseCase("gyroscope_data_$recordingCount.jsonl", "gyroscope_data_$recordingCount.srt")
        sensorData.clear()
    }
}
