package com.appleader707.syncrecorder.domain

import android.hardware.SensorManager
import androidx.annotation.Keep
import androidx.camera.video.Quality

@Keep
data class RecordingSettings(
    val resolution: String = "720p",
    val frameRate: Int = 30,
    val autoFocus: Boolean = true,
    val stabilization: Boolean = true,
    val imuFrequency: Int = 100
) {
    fun getQuality() = when (resolution) {
        "480p" -> Quality.SD
        "720p" -> Quality.HD
        "1080p" -> Quality.FHD
        "4K" -> Quality.UHD
        else -> Quality.HD
    }!!

    fun getImuSensorDelay() = when (imuFrequency) {
        10 -> SensorManager.SENSOR_DELAY_UI
        50 -> SensorManager.SENSOR_DELAY_GAME
        100 -> SensorManager.SENSOR_DELAY_FASTEST
        else -> SensorManager.SENSOR_DELAY_UI
    }
}