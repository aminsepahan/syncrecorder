package com.appleader707.syncrecorder.presentation.ui.show_by_chart

import android.net.Uri
import com.appleader707.common.ui.base.BaseViewState
import com.github.mikephil.charting.data.Entry

/**
 *
 * @see BaseViewState
 */

data class ShowByChartViewState(
    val videoUri: Uri? = null,
    val chartDataX: List<Entry> = emptyList(),
    val chartDataY: List<Entry> = emptyList(),
    val chartDataZ: List<Entry> = emptyList(),
    val gyroDataX: List<Entry> = emptyList(),
    val gyroDataY: List<Entry> = emptyList(),
    val gyroDataZ: List<Entry> = emptyList(),
    val magnetDataX: List<Entry> = emptyList(),
    val magnetDataY: List<Entry> = emptyList(),
    val magnetDataZ: List<Entry> = emptyList(),
) : BaseViewState