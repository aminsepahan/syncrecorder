package com.appleader707.syncrecorder.data.local.datastore.source

import com.appleader707.syncrecorder.data.local.datastore.service.RecordingSettingsPreferences
import com.appleader707.syncrecorder.domain.RecordingSettingsState
import javax.inject.Inject

class RecordingSettingsDataSource @Inject constructor(
    private val prefs: RecordingSettingsPreferences
) {
    suspend fun getSettings(): RecordingSettingsState = prefs.getSettings()
    suspend fun setSettings(settings: RecordingSettingsState) = prefs.setSettings(settings)
}
