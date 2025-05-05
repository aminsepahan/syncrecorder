package com.appleader707.syncrecorder.business.usecase.convert

import android.hardware.Sensor
import com.appleader707.syncrecorder.business.usecase.directory.GetSyncRecorderDirectoryUseCase
import com.appleader707.syncrecorder.business.usecase.time.GetFormatTimeUseCase
import com.appleader707.syncrecorder.domain.SensorSnapshot
import com.google.gson.Gson
import java.io.File
import javax.inject.Inject

class ConvertJsonToSrtUseCase @Inject constructor(
    private val getSyncRecorderDirectoryUseCase: GetSyncRecorderDirectoryUseCase,
    private val getFormatTimeUseCase: GetFormatTimeUseCase,
) {
    operator fun invoke(recordingCount: Int) {
        val sensorFileJson = File(getSyncRecorderDirectoryUseCase(), "sensor_data_$recordingCount.json")
        val sensorFileSrt = File(getSyncRecorderDirectoryUseCase(), "sensor_data_$recordingCount.srt")

        if (!sensorFileJson.exists()) return

        val json = sensorFileJson.readText()
        val data = Gson().fromJson(json, Array<SensorSnapshot>::class.java)

        if (data.isEmpty()) return

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
    }
}