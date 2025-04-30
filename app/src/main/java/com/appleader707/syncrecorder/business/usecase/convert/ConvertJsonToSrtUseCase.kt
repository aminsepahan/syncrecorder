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

        val baseTime = data.first().timestampMills   // first timestamp

        return sensorFileSrt.bufferedWriter().use { out ->
            var index = 1

            data.forEach { snapshot ->
                val currentTime = snapshot.timestampMills - baseTime
                val displayDuration = 50L // At least 50 milliseconds to be visible in the player
                val endTime = currentTime + displayDuration

                val values = snapshot.values.joinToString(",") { "%.5f".format(it) }
                val type = when (snapshot.type) {
                    Sensor.TYPE_ACCELEROMETER -> "ACC"
                    Sensor.TYPE_GYROSCOPE -> "GYRO"
                    Sensor.TYPE_MAGNETIC_FIELD -> "MAG"
                    Sensor.TYPE_LINEAR_ACCELERATION -> "L_ACC"
                    else -> "SENSOR_${snapshot.type}"
                }

                out.write("$index\n")
                out.write("${getFormatTimeUseCase(currentTime)} --> ${getFormatTimeUseCase(endTime)}\n")
                out.write("$type: $values\n\n")

                index++
            }
        }
    }
}
