package com.syn2core.syn2corecamera.business.usecase.convert

import android.hardware.Sensor
import com.google.gson.Gson
import com.syn2core.syn2corecamera.TAG
import com.syn2core.syn2corecamera.business.usecase.directory.GetSyn2CoreCameraDirectoryUseCase
import com.syn2core.syn2corecamera.business.usecase.time.GetFormatTimeUseCase
import com.syn2core.syn2corecamera.domain.SensorSnapshot
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class ConvertJsonToSrtUseCase @Inject constructor(
    private val getSyn2CoreCameraDirectoryUseCase: GetSyn2CoreCameraDirectoryUseCase,
    private val getFormatTimeUseCase: GetFormatTimeUseCase,
) {
    operator fun invoke(recordingCount: Int) {
        val sensorFileJson = File(getSyn2CoreCameraDirectoryUseCase(), "sensor_data_$recordingCount.json")
        val sensorFileSrt = File(getSyn2CoreCameraDirectoryUseCase(), "sensor_data_$recordingCount.srt")

        if (!sensorFileJson.exists()) {
            Timber.e("JSON file not found: ${sensorFileJson.absolutePath}")
            return
        }

        try {
            val json = sensorFileJson.readText()
            val data = Gson().fromJson(json, Array<SensorSnapshot>::class.java) ?: run {
                Timber.e("Failed to parse JSON for recording $recordingCount: JSON is null")
                return
            }

            if (data.isEmpty()) {
                Timber.w("No sensor data found in JSON for recording $recordingCount")
                return
            }

            val baseTime = data.first().timestampMillis
            sensorFileSrt.bufferedWriter().use { out ->
                var index = 1

                data.forEach { snapshot ->
                    val currentTime = snapshot.timestampMillis - baseTime
                    val displayDuration = 1L
                    val endTime = currentTime + displayDuration

                    val values = snapshot.values.joinToString(",") { "%.5f".format(it) }
                    val label = when (snapshot.type) {
                        Sensor.TYPE_ACCELEROMETER -> "ACC"
                        Sensor.TYPE_GYROSCOPE -> "GYRO"
                        Sensor.TYPE_MAGNETIC_FIELD -> "MAG"
                        Sensor.TYPE_LINEAR_ACCELERATION -> "L_ACC"
                        else -> snapshot.name.uppercase()
                    }

                    out.write("$index\n")
                    out.write("${getFormatTimeUseCase(currentTime)} --> ${getFormatTimeUseCase(endTime)}\n")
                    out.write("$label: $values\n\n")

                    index++
                }
            }
            Timber.tag(TAG).d("SRT file created successfully: ${sensorFileSrt.absolutePath}")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to create SRT file for recording $recordingCount")
        }
    }
}