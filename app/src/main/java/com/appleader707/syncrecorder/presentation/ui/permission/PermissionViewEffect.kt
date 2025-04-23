package com.appleader707.syncrecorder.presentation.ui.permission

import com.appleader707.common.ui.base.BaseViewEffect

/**
 *
 * @see BaseViewEffect
 */
sealed class PermissionViewEffect : BaseViewEffect {
    data object DoNothing : PermissionViewEffect()
    data object GoRecordingPage : PermissionViewEffect()
}