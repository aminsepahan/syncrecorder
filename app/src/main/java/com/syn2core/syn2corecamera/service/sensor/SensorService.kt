package com.syn2core.syn2corecamera.service.sensor

import kotlinx.coroutines.coroutineScope
import java.io.File
import javax.inject.Inject

class SensorService @Inject constructor(
    private val unifiedSensorService: UnifiedSensorService,
    private val aggregator: SensorDataAggregator,
) {

    fun startSensors(
        imuFrequency: Int,
        segmentNumber: Int,
        currentVideoFile: File
    ) {
        if (segmentNumber == 1) {
            aggregator.startNewFile(currentVideoFile)
        }
        unifiedSensorService.startSensors(
            imuFrequency = imuFrequency,
            segmentNumber = segmentNumber,
            currentVideoFile = currentVideoFile
        )
    }

    suspend fun stopSensors() = coroutineScope {
        unifiedSensorService.stopSensors()
        aggregator.closeJsonFile()
    }
}