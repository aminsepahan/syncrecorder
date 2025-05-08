package com.syn2core.syn2corecamera.presentation.ui.permission

import com.syn2core.common.ui.base.BaseViewEffect

/**
 *
 * @see BaseViewEffect
 */
sealed class PermissionViewEffect : BaseViewEffect {
    data object DoNothing : PermissionViewEffect()
    data object GoRecordingPage : PermissionViewEffect()
}