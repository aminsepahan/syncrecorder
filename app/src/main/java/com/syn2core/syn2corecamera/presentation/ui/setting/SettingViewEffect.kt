package com.syn2core.syn2corecamera.presentation.ui.setting

import com.syn2core.common.ui.base.BaseViewEffect

/**
 *
 * @see BaseViewEffect
 */
sealed class SettingViewEffect : BaseViewEffect {
    data object DoNothing : SettingViewEffect()
    data object GoBackRecordingPage : SettingViewEffect()
}