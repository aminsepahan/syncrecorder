package com.syn2core.syn2corecamera.presentation.ui.recording

import com.syn2core.common.ui.base.BaseViewState
import com.syn2core.syn2corecamera.domain.RecordingSettings

/**
 *
 * @see BaseViewState
 */

data class RecordingViewState(
    val isRecording: Boolean = false,
    val durationMillis: Long = 0L,
    val settingsDialogVisible: Boolean = false,
    val settingsState: RecordingSettings = RecordingSettings(),
    var recordingCount: Int = 1,
    val isSaving: Boolean = false
) : BaseViewState {
    val formattedDuration: String
        get() {
            val totalSeconds = durationMillis / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return "%02d:%02d:%02d".format(hours, minutes, seconds)
        }
}