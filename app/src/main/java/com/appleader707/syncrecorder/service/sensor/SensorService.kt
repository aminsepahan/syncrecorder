package com.appleader707.syncrecorder.service.sensor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class SensorService @Inject constructor(
    private val accelerometerService: AccelerometerService,
    private val gyroscopeService: GyroscopeService,
    private val magnetometerService: MagnetometerService
) {
    fun startSensors(
        recordingCount: Int,
        recordingStartNanos: Long,
        imuFrequency: Int
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            accelerometerService.startAccelerometerSensor(
                recordingCount = recordingCount,
                recordingStartNanos = recordingStartNanos,
                imuFrequency = imuFrequency
            )
        }

        CoroutineScope(Dispatchers.IO).launch {
            gyroscopeService.startGyroscopeSensor(
                recordingCount = recordingCount,
                recordingStartNanos = recordingStartNanos,
                imuFrequency = imuFrequency
            )
        }

        CoroutineScope(Dispatchers.IO).launch {
            magnetometerService.startMagnetometerSensor(
                recordingCount = recordingCount,
                recordingStartNanos = recordingStartNanos,
                imuFrequency = imuFrequency
            )
        }
    }

    fun stopSensors(recordingCount: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            accelerometerService.stopAccelerometerSensor(recordingCount)
        }

        CoroutineScope(Dispatchers.IO).launch {
            gyroscopeService.stopGyroscopeSensor(recordingCount)
        }

        CoroutineScope(Dispatchers.IO).launch {
            magnetometerService.stopMagnetometerSensor(recordingCount)
        }
    }
}