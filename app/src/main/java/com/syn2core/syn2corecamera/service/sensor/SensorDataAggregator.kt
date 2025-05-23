package com.syn2core.syn2corecamera.service.sensor

import android.hardware.SensorEvent
import com.google.gson.Gson
import com.syn2core.syn2corecamera.TAG
import com.syn2core.syn2corecamera.business.usecase.directory.GetSensorFileUseCase
import com.syn2core.syn2corecamera.domain.SensorSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorDataAggregator @Inject constructor(
    private val getSensorFileUseCase: GetSensorFileUseCase,
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

    suspend fun saveToJsonFile(segmentCount: Int) = withContext(Dispatchers.IO) {
        val sensorFile = getSensorFileUseCase(segmentCount)
        val dataCopy = synchronized(sensorData) { ArrayList(sensorData) }

        if (dataCopy.isEmpty()) {
            synchronized(sensorData) { sensorData.clear() }
            return@withContext
        }

        try {
            sensorFile.bufferedWriter().use { writer ->
                val json = Gson().toJson(dataCopy)
                writer.write(json)
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to save JSON or create SRT for recording $segmentCount")
        } finally {
            synchronized(sensorData) { sensorData.clear() }
        }
    }
}