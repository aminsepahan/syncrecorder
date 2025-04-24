package com.appleader707.syncrecorder.presentation.ui.recording

import android.content.Context
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import com.appleader707.common.ui.base.BaseViewEvent
import com.appleader707.syncrecorder.domain.RecordingSettingsState

/**
 *
 * @see BaseViewEvent
 */

sealed class RecordingViewEvent : BaseViewEvent {
    data class ToggleRecording(
        val context: Context,
        val lifecycleOwner: LifecycleOwner,
        val surfaceProvider: Preview.SurfaceProvider
    ) : RecordingViewEvent()

    data class SaveSettings(val settings: RecordingSettingsState) : RecordingViewEvent()
    object ShowSettings : RecordingViewEvent()
    object HideSettings : RecordingViewEvent()
    object LoadSettings : RecordingViewEvent()
}