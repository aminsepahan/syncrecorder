package com.syn2core.syn2corecamera.service.sensor

import JsonFileWriter
import android.hardware.SensorEvent
import com.syn2core.syn2corecamera.business.usecase.directory.GetSensorFileUseCase
import com.syn2core.syn2corecamera.domain.SensorSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorDataAggregator @Inject constructor(
    getSensorFileUseCase: GetSensorFileUseCase,
) {
    private val jsonFileWriter = JsonFileWriter(getSensorFileUseCase(1))

    fun recordEvent(type: Int, name: String, event: SensorEvent) {

        val snapshot = SensorSnapshot(
            type = type,
            name = name,
            timestampMillis = event.timestamp,
            values = event.values.toList()
        )
        jsonFileWriter.appendJsonObject(snapshot)
    }

    suspend fun saveToJsonFile() = withContext(Dispatchers.IO) {
        jsonFileWriter.closeJsonArray()
    }
}