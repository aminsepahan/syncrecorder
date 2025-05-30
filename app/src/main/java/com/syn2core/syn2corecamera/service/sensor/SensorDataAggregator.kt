package com.syn2core.syn2corecamera.service.sensor

import android.hardware.SensorEvent
import com.syn2core.syn2corecamera.domain.SensorSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorDataAggregator @Inject constructor() {
    private val jsonFileWriter = JsonFileWriter()

    fun recordEvent(type: Int, name: String, event: SensorEvent) {

        val snapshot = SensorSnapshot(
            type = type,
            name = name,
            timestamp = event.timestamp,
            values = event.values.toList()
        )
        jsonFileWriter.appendJsonObject(snapshot)
    }

    suspend fun closeJsonFile() = withContext(Dispatchers.IO) {
        jsonFileWriter.closeJsonArray()
    }

    fun startNewFile(currentVideoFile: File) {
        jsonFileWriter.startNewFile(currentVideoFile)
    }
}