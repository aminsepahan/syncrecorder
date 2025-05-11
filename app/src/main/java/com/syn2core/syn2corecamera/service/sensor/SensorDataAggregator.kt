package com.syn2core.syn2corecamera.service.sensor

import android.hardware.SensorEvent
import com.google.gson.Gson
import com.syn2core.syn2corecamera.business.usecase.convert.ConvertJsonToSrtUseCase
import com.syn2core.syn2corecamera.business.usecase.directory.GetSyn2CoreCameraDirectoryUseCase
import com.syn2core.syn2corecamera.domain.SensorSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorDataAggregator @Inject constructor(
    private val getSyn2CoreCameraDirectoryUseCase: GetSyn2CoreCameraDirectoryUseCase,
    private val convertJsonToSrtUseCase: ConvertJsonToSrtUseCase
) {
    private val sensorData = Collections.synchronizedList(mutableListOf<SensorSnapshot>())

    fun recordEvent(type: Int, name: String, event: SensorEvent) {

        val snapshot = SensorSnapshot(
            type = type,
            name = name,
            timestampMillis = event.timestamp,
            values = event.values.toList()
        )
        sensorData.add(snapshot)
    }

    suspend fun saveToJsonFile(recordingCount: Int) = withContext(Dispatchers.IO) {
        val outputFile = File(getSyn2CoreCameraDirectoryUseCase(), "sensor_data_$recordingCount.json")
        outputFile.bufferedWriter().use { writer ->
            val json = Gson().toJson(sensorData)
            writer.write(json)
        }
        sensorData.clear()
        convertJsonToSrtUseCase(recordingCount)
    }
}
