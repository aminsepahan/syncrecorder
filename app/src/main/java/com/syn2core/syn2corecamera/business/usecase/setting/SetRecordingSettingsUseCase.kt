package com.syn2core.syn2corecamera.business.usecase.setting

import com.syn2core.syn2corecamera.data.local.datastore.source.RecordingSettingsDataSource
import com.syn2core.syn2corecamera.domain.RecordingSettings
import javax.inject.Inject

class SetRecordingSettingsUseCase @Inject constructor(
    private val dataSource: RecordingSettingsDataSource
) {
    suspend operator fun invoke(settings: RecordingSettings) = dataSource.setSettings(settings)
}
