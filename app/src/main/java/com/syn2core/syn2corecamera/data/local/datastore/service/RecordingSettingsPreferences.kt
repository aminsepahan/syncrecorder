package com.syn2core.syn2corecamera.data.local.datastore.service

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.syn2core.syn2corecamera.domain.RecordingSettings
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RecordingSettingsPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val KEY_RESOLUTION = stringPreferencesKey("resolution")
        val KEY_FRAME_RATE = intPreferencesKey("frame_rate")
        val KEY_CODEC = stringPreferencesKey("codec")
        val KEY_AUTO_FOCUS = booleanPreferencesKey("auto_focus")
        val KEY_STABILIZATION = booleanPreferencesKey("stabilization")
        val KEY_AUDIO_SOURCE = stringPreferencesKey("audio_source")
        val KEY_IMU_FREQ = intPreferencesKey("imu_freq")
        val KEY_AUTO_STOP_MINUTES = intPreferencesKey("auto_stop_minutes")
    }

    suspend fun getSettings(): RecordingSettings {
        val prefs = dataStore.data.first()
        return RecordingSettings(
            resolution = prefs[KEY_RESOLUTION] ?: "720p",
            frameRate = prefs[KEY_FRAME_RATE] ?: 30,
            codec = prefs[KEY_CODEC] ?: "H264",
            autoFocus = prefs[KEY_AUTO_FOCUS] ?: true,
            stabilization = prefs[KEY_STABILIZATION] ?: true,
            audioSource = prefs[KEY_AUDIO_SOURCE] ?: "CAMCORDER",
            imuFrequency = prefs[KEY_IMU_FREQ] ?: 100,
            autoStopMinutes = prefs[KEY_AUTO_STOP_MINUTES] ?: 10
        )
    }

    suspend fun setSettings(settings: RecordingSettings) {
        dataStore.updateData {
            it.toMutablePreferences().apply {
                this[KEY_RESOLUTION] = settings.resolution
                this[KEY_FRAME_RATE] = settings.frameRate
                this[KEY_CODEC] = settings.codec
                this[KEY_AUTO_FOCUS] = settings.autoFocus
                this[KEY_STABILIZATION] = settings.stabilization
                this[KEY_AUDIO_SOURCE] = settings.audioSource
                this[KEY_IMU_FREQ] = settings.imuFrequency
                this[KEY_AUTO_STOP_MINUTES] = settings.autoStopMinutes
            }
        }
    }
}
