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
        val KEY_AUTO_FOCUS = booleanPreferencesKey("auto_focus")
        val KEY_STABILIZATION = booleanPreferencesKey("stabilization")
        val KEY_IMU_FREQ = intPreferencesKey("imu_freq")
    }

    suspend fun getSettings(): RecordingSettings {
        val prefs = dataStore.data.first()
        return RecordingSettings(
            resolution = prefs[KEY_RESOLUTION] ?: "720p",
            frameRate = prefs[KEY_FRAME_RATE] ?: 30,
            autoFocus = prefs[KEY_AUTO_FOCUS] ?: true,
            stabilization = prefs[KEY_STABILIZATION] ?: true,
            imuFrequency = prefs[KEY_IMU_FREQ] ?: 100
        )
    }

    suspend fun setSettings(settings: RecordingSettings) {
        dataStore.updateData {
            it.toMutablePreferences().apply {
                this[KEY_RESOLUTION] = settings.resolution
                this[KEY_FRAME_RATE] = settings.frameRate
                this[KEY_AUTO_FOCUS] = settings.autoFocus
                this[KEY_STABILIZATION] = settings.stabilization
                this[KEY_IMU_FREQ] = settings.imuFrequency
            }
        }
    }
}
