package com.appleader707.syncrecorder.presentation.ui.show_by_chart

import com.appleader707.common.ui.base.BaseViewEffect

/**
 *
 * @see BaseViewEffect
 */
sealed class ShowByChartViewEffect : BaseViewEffect {
    data object DoNothing : ShowByChartViewEffect()
    data object GoBackRecordingPage : ShowByChartViewEffect()
}