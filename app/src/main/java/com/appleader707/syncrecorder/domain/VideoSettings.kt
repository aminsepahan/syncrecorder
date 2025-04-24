package com.appleader707.syncrecorder.domain

import androidx.annotation.Keep

@Keep
data class VideoSettings(
    val resolution: String = "720p",
    val frameRate: Int = 30,
    val autoFocus: Boolean = true,
    val stabilization: Boolean = true
)