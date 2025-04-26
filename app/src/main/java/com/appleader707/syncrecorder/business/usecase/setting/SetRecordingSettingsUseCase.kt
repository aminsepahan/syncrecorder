package com.appleader707.syncrecorder.business.usecase.setting

import com.appleader707.syncrecorder.data.local.datastore.source.RecordingSettingsDataSource
import com.appleader707.syncrecorder.domain.RecordingSettingsState
import javax.inject.Inject

class SetRecordingSettingsUseCase @Inject constructor(
    private val dataSource: RecordingSettingsDataSource
) {
    suspend operator fun invoke(settings: RecordingSettingsState) = dataSource.setSettings(settings)
}
