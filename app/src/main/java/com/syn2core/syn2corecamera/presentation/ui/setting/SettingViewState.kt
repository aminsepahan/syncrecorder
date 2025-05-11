package com.syn2core.syn2corecamera.presentation.ui.setting

import com.syn2core.common.ui.base.BaseViewState
import com.syn2core.syn2corecamera.domain.RecordingSettings

/**
 *
 * @see BaseViewState
 */

data class SettingViewState(
    val settingsState: RecordingSettings = RecordingSettings(),
) : BaseViewState