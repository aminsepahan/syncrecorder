package com.syn2core.syn2corecamera.domain

import androidx.annotation.Keep

@Keep
data class SensorSnapshot(
    val type: Int,
    val name: String,
    val timestampMillis: Long,
    val values: List<Float>
)