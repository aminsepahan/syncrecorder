package com.appleader707.syncrecorder.presentation.ui.permission

import com.appleader707.common.ui.base.BaseViewState

/**
 *
 * @see BaseViewState
 */

data class PermissionViewState(
    val permissions: List<String> = listOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.BODY_SENSORS,
    )
) : BaseViewState