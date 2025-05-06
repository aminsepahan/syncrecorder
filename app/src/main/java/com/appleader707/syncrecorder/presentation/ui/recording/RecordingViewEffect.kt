package com.appleader707.syncrecorder.presentation.ui.recording

import com.appleader707.common.ui.base.BaseViewEffect

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