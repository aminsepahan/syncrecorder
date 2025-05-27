package com.syn2core.syn2corecamera.presentation.ui.permission

import com.syn2core.common.ui.base.BaseViewState

/**
 *
 * @see BaseViewState
 */

data class PermissionViewState(
    val permissions: List<String> = listOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.RECORD_AUDIO,
    )
) : BaseViewState