package com.syn2core.syn2corecamera.service.sensor

import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class SensorService @Inject constructor(
    private val unifiedSensorService: UnifiedSensorService,
    private val aggregator: SensorDataAggregator,
) {
    fun startSensors(
        startTimeStamp: Long,
        imuFrequency: Int
    ) {
        aggregator.setRecordingStartTime(startTimeStamp)
        unifiedSensorService.startSensors(imuFrequency)
    }

    suspend fun stopSensors(recordingCount: Int) = coroutineScope {
        unifiedSensorService.stopSensors()
        aggregator.saveToJsonFile(recordingCount)
    }
}