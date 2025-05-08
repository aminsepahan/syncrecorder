package com.syn2core.syn2corecamera.presentation.ui.show_by_chart

import com.syn2core.common.ui.base.BaseViewEvent

/**
 *
 * @see BaseViewEvent
 */

sealed class ShowByChartViewEvent : BaseViewEvent {
    data object GoBackToRecordingPage: ShowByChartViewEvent()
    data object LoadData: ShowByChartViewEvent()
}