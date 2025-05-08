package com.syn2core.syn2corecamera.presentation.ui.show_by_chart

import com.syn2core.common.ui.base.BaseViewEffect

/**
 *
 * @see BaseViewEffect
 */
sealed class ShowByChartViewEffect : BaseViewEffect {
    data object DoNothing : ShowByChartViewEffect()
    data object GoBackRecordingPage : ShowByChartViewEffect()
}