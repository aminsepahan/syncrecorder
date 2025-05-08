package com.syn2core.syn2corecamera.presentation.ui.permission

import com.syn2core.common.ui.base.BaseViewEvent

/**
 *
 * @see BaseViewEvent
 */

sealed class PermissionViewEvent : BaseViewEvent {
    data object GoRecordingPage: PermissionViewEvent()
}