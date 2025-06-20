package com.syn2core.syn2corecamera.presentation.ui.recording

import com.syn2core.common.ui.base.BaseViewState
import com.syn2core.syn2corecamera.domain.RecordingSettings
import com.syn2core.syn2corecamera.extension.formatAsDuration

/**
 *
 * @see BaseViewState
 */

data class RecordingViewState(
    val isRecording: Boolean = false,
    val durationMillis: Long = 0L,
    val settingsState: RecordingSettings = RecordingSettings(),
    val segmentCount: Int = 0,
    val timestampDifference: Long = 0,
    val latestFrameTimestamp: Long = 0L,
    val latestImuTimestamp: Long = 0L,
    val showPleaseWait: Boolean = false,
) : BaseViewState {
    val formattedDuration: String
        get() = durationMillis.formatAsDuration()
}