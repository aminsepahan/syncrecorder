package com.appleader707.syncrecorder.domain

import android.media.MediaRecorder
import androidx.annotation.Keep

@Keep
data class RecordingSettingsState(
    val resolution: String = "720p",
    val frameRate: Int = 30,
    val autoFocus: Boolean = true,
    val stabilization: Boolean = true,
    val audioSource: String = "MIC",
    val codec: String = "H.264",
    val imuFrequency: Int = 100
) {
    fun getAudioSource(): Int {
        when (audioSource) {
            "MIC" -> MediaRecorder.AudioSource.MIC
            "CAMCORDER" -> MediaRecorder.AudioSource.CAMCORDER
            "VOICE_RECOGNITION" -> MediaRecorder.AudioSource.VOICE_RECOGNITION
        }
        return MediaRecorder.AudioSource.MIC
    }

    fun toVideoSettings(): VideoSettings {
        return VideoSettings(
            resolution = this.resolution,
            frameRate = this.frameRate,
            autoFocus = this.autoFocus,
            stabilization = this.stabilization
        )
    }
}