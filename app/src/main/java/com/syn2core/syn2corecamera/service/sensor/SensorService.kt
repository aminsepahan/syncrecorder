package com.syn2core.syn2corecamera.service.sensor

import kotlinx.coroutines.coroutineScope
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorService @Inject constructor(
    private val unifiedSensorService: UnifiedSensorService,
    private val aggregator: SensorDataAggregator,
) {
    fun startSensors(
        imuFrequency: Int,
        currentVideoFile: File
    ) {
        aggregator.startNewFile(currentVideoFile)
        unifiedSensorService.startSensors(imuFrequency = imuFrequency)
    }

    suspend fun stopSensors() = coroutineScope {
        unifiedSensorService.stopSensors()
    }
}