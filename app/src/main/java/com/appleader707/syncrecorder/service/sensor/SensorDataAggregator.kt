package com.appleader707.syncrecorder.service.sensor

import android.hardware.SensorEvent
import com.appleader707.syncrecorder.business.usecase.convert.ConvertJsonToSrtUseCase
import com.appleader707.syncrecorder.business.usecase.directory.GetSyncRecorderDirectoryUseCase
import com.appleader707.syncrecorder.domain.SensorSnapshot
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorDataAggregator @Inject constructor(
    private val getSyncRecorderDirectoryUseCase: GetSyncRecorderDirectoryUseCase,
    private val convertJsonToSrtUseCase: ConvertJsonToSrtUseCase
) {
    private val sensorData = Collections.synchronizedList(mutableListOf<SensorSnapshot>())
    private var timeStamp: Long = 0L

    fun setRecordingStartTime(timeStamp: Long) {
        this.timeStamp = timeStamp
    }

    fun recordEvent(type: Int, name: String, event: SensorEvent) {
        val relativeTimeMillis = (event.timestamp - timeStamp) / 1_000_000

        val snapshot = SensorSnapshot(
            type = type,
            name = name,
            timestampMillis = relativeTimeMillis,
            values = event.values.toList()
        )
        sensorData.add(snapshot)
    }

    suspend fun saveToJsonFile(recordingCount: Int) = withContext(Dispatchers.IO) {
        val outputFile = File(getSyncRecorderDirectoryUseCase(), "sensor_data_$recordingCount.json")
        outputFile.bufferedWriter().use { writer ->
            val json = Gson().toJson(sensorData)
            writer.write(json)
        }
        sensorData.clear()
        convertJsonToSrtUseCase(recordingCount)
    }
}
