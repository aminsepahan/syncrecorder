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
    operator fun invoke(sensorFileJsonName: String, sensorFileSrtName: String) {
        val sensorFileJson = File(getSyncRecorderDirectoryUseCase(), sensorFileJsonName)
        val sensorFileSrt = File(getSyncRecorderDirectoryUseCase(), sensorFileSrtName)

        val json = sensorFileJson.readText()
        val data = Gson().fromJson(json, Array<SensorSnapshot>::class.java)

        if (data.isEmpty()) return

        val baseTime = data.first().timestampNanos / 1_000_000  // اولین timestamp

        return sensorFileSrt.bufferedWriter().use { out ->
            var index = 1
            var previousTime = 0L

            data.forEach { snapshot ->
                val currentTime = (snapshot.timestampNanos / 1_000_000) - baseTime
                val values = snapshot.values.joinToString(",") { "%.5f".format(it) }
                val type = when (snapshot.type) {
                    Sensor.TYPE_ACCELEROMETER -> "ACC"
                    Sensor.TYPE_GYROSCOPE -> "GYRO"
                    Sensor.TYPE_MAGNETIC_FIELD -> "MAG"
                    else -> "SENSOR_${snapshot.type}"
                }

                out.write("$index\n")
                out.write(
                    "${getFormatTimeUseCase(previousTime)} --> ${
                        getFormatTimeUseCase(
                            currentTime
                        )
                    }\n"
                )
                out.write("$type: $values\n\n")

                previousTime = currentTime
                index++
            }
        }
    }
}
