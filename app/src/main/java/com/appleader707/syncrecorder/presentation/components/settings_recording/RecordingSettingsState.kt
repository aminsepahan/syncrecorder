package com.appleader707.syncrecorder.presentation.components.settings_recording

data class RecordingSettingsState(
    val resolution: String = "720p",
    val frameRate: Int = 30,
    val codec: String = "H.264",
    val autoFocus: Boolean = true,
    val stabilization: Boolean = true,
    val audioSource: String = "MIC",
    val imuFrequency: Int = 100
)