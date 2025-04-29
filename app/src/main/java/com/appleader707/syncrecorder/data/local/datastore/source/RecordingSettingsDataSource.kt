package com.appleader707.syncrecorder.data.local.datastore.source

import com.appleader707.syncrecorder.data.local.datastore.service.RecordingSettingsPreferences
import com.appleader707.syncrecorder.domain.RecordingSettings
import javax.inject.Inject

class RecordingSettingsDataSource @Inject constructor(
    private val prefs: RecordingSettingsPreferences
) {
    suspend fun getSettings(): RecordingSettings = prefs.getSettings()
    suspend fun setSettings(settings: RecordingSettings) = prefs.setSettings(settings)
}
