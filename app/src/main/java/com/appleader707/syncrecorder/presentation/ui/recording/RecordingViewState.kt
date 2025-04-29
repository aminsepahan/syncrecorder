package com.appleader707.syncrecorder.presentation.ui.recording

import com.appleader707.common.ui.base.BaseViewState
import com.appleader707.syncrecorder.domain.RecordingSettings

/**
 *
 * @see BaseViewState
 */

data class RecordingViewState(
    val isRecording: Boolean = false,
    val durationMillis: Long = 0L,
    val settingsDialogVisible: Boolean = false,
    val settingsState: RecordingSettings = RecordingSettings(),
    var recordingStartNanos: Long = 0,
    var recordingCount: Int = 1
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