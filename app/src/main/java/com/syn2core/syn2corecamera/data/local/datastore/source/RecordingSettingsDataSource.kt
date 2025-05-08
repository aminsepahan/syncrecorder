package com.syn2core.syn2corecamera.data.local.datastore.source

import com.syn2core.syn2corecamera.data.local.datastore.service.RecordingSettingsPreferences
import com.syn2core.syn2corecamera.domain.RecordingSettings
import javax.inject.Inject

class RecordingSettingsDataSource @Inject constructor(
    private val prefs: RecordingSettingsPreferences
) {
    suspend fun getSettings(): RecordingSettings = prefs.getSettings()
    suspend fun setSettings(settings: RecordingSettings) = prefs.setSettings(settings)
}
