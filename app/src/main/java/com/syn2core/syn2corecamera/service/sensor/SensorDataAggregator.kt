package com.syn2core.syn2corecamera.service.sensor

import android.hardware.SensorEvent
import com.google.gson.Gson
import com.syn2core.syn2corecamera.TAG
import com.syn2core.syn2corecamera.business.usecase.convert.ConvertJsonToSrtUseCase
import com.syn2core.syn2corecamera.business.usecase.directory.GetSyn2CoreCameraDirectoryUseCase
import com.syn2core.syn2corecamera.domain.SensorSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
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
        val dataCopy = synchronized(sensorData) { ArrayList(sensorData) }

        if (dataCopy.isEmpty()) {
            Timber.w("No sensor data to save for recording $recordingCount")
            synchronized(sensorData) { sensorData.clear() }
            return@withContext
        }

        try {
            outputFile.bufferedWriter().use { writer ->
                val json = Gson().toJson(dataCopy)
                writer.write(json)
            }
            Timber.tag(TAG).d("JSON file saved: ${outputFile.absolutePath}")
            convertJsonToSrtUseCase(recordingCount)
            val srtFile = File(getSyn2CoreCameraDirectoryUseCase(), "sensor_data_$recordingCount.srt")
            if (!srtFile.exists()) {
                Timber.tag(TAG).e("SRT file not created: ${srtFile.absolutePath}")
            } else {
                Timber.tag(TAG).d("SRT file exists: ${srtFile.absolutePath}")
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to save JSON or create SRT for recording $recordingCount")
        } finally {
            synchronized(sensorData) { sensorData.clear() }
        }
    }
}
