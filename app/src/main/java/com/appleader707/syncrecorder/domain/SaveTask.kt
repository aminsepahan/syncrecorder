package com.appleader707.syncrecorder.domain

import androidx.annotation.Keep

@Keep
data class SaveTask(
    val videoName: String,
    val outputName: String,
    val recordingCount: Int,
)