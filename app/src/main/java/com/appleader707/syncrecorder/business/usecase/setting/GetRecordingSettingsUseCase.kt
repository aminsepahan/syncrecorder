package com.appleader707.syncrecorder.business.usecase.setting

import com.appleader707.syncrecorder.data.local.datastore.source.RecordingSettingsDataSource
import com.appleader707.syncrecorder.domain.RecordingSettings
import javax.inject.Inject

class GetRecordingSettingsUseCase @Inject constructor(
    private val dataSource: RecordingSettingsDataSource
) {
    suspend operator fun invoke(): RecordingSettings = dataSource.getSettings()
}
