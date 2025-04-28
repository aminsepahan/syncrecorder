package com.appleader707.syncrecorder.domain

import androidx.annotation.Keep

@Keep
data class SensorSnapshot(
    val type: Int,
    val timestampNanos: Long,
    val values: List<Float>
)