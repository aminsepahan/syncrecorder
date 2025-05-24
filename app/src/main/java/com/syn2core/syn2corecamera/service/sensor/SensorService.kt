package com.syn2core.syn2corecamera.service.sensor

import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class SensorService @Inject constructor(
    private val unifiedSensorService: UnifiedSensorService,
    private val aggregator: SensorDataAggregator,
) {
    private var segmentCount = 0

    fun startSensors(
        imuFrequency: Int,
        segmentNumber: Int
    ) {
        unifiedSensorService.startSensors(imuFrequency)
        segmentCount = segmentNumber
    }

    suspend fun stopSensors() = coroutineScope {
        unifiedSensorService.stopSensors()
        aggregator.saveToJsonFile()
    }
}