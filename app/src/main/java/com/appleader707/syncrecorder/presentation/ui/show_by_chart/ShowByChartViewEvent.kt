package com.appleader707.syncrecorder.presentation.ui.show_by_chart

import com.appleader707.common.ui.base.BaseViewEvent

/**
 *
 * @see BaseViewEvent
 */

sealed class ShowByChartViewEvent : BaseViewEvent {
    data object GoBackToRecordingPage: ShowByChartViewEvent()
    data object LoadData: ShowByChartViewEvent()
}