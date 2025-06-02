package com.syn2core.syn2corecamera.domain

import android.hardware.SensorManager
import android.media.MediaRecorder
import androidx.annotation.Keep

@Keep
data class RecordingSettings(
    val resolution: String = "1080p",
    val frameRate: Int = 30,
    val codec: String = "H.264",
    val autoFocus: Boolean = true,
    val stabilization: Boolean = true,
    val audioSource: String = "CAMCORDER",
    val imuFrequency: Int = 100,
    val autoStopMinutes: Int = 15
) {
    fun getResolutionSize(): Pair<Int, Int> = when (resolution) {
        "480p" -> 720 to 480
        "720p" -> 1280 to 720
        "1080p" -> 1920 to 1080
        "4K" -> 3840 to 2160
        else -> 1280 to 720
    }

    fun getImuSensorDelay() = when (imuFrequency) {
        10 -> SensorManager.SENSOR_DELAY_UI
        50 -> SensorManager.SENSOR_DELAY_GAME
        100 -> SensorManager.SENSOR_DELAY_FASTEST
        else -> SensorManager.SENSOR_DELAY_UI
    }

    fun getAudioSource() = when (audioSource) {
        "MIC" -> MediaRecorder.AudioSource.MIC
        "CAMCORDER" -> MediaRecorder.AudioSource.CAMCORDER
        "VOICE_RECOGNITION" -> MediaRecorder.AudioSource.VOICE_RECOGNITION
        else -> MediaRecorder.AudioSource.CAMCORDER
    }

    fun getCodec() = when (codec) {
        "H.264" -> MediaRecorder.VideoEncoder.H264
        "HEVC" -> MediaRecorder.VideoEncoder.HEVC
        else -> MediaRecorder.VideoEncoder.H264
    }
}