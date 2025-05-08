package com.syn2core.syn2corecamera.presentation.ui.recording

import com.syn2core.common.ui.base.BaseViewEffect

/**
 *
 * @see BaseViewEffect
 */
sealed class RecordingViewEffect : BaseViewEffect {
    data object DoNothing : RecordingViewEffect()
    object RecordingStarted : RecordingViewEffect()
    object RecordingStopped : RecordingViewEffect()
    data object NavigateToShowByChart : RecordingViewEffect()
}