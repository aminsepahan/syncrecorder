package com.syn2core.syn2corecamera.business.usecase.setting

import com.syn2core.syn2corecamera.data.local.datastore.source.RecordingSettingsDataSource
import com.syn2core.syn2corecamera.domain.RecordingSettings
import javax.inject.Inject

class GetRecordingSettingsUseCase @Inject constructor(
    private val dataSource: RecordingSettingsDataSource
) {
    suspend operator fun invoke(): RecordingSettings = dataSource.getSettings()
}
