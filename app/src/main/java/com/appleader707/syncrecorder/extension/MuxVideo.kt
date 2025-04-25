package com.appleader707.syncrecorder.extension

import com.appleader707.syncrecorder.TAG
import com.arthenica.ffmpegkit.FFmpegKit
import timber.log.Timber

fun muxVideoWithSensorData(
    videoPath: String,
    assPath: String,
    outputPath: String
) {
    val command = listOf(
        "-i", videoPath,
        "-f", "ass",
        "-i", assPath,
        "-c:v", "copy",
        "-c:a", "copy",
        "-c:s", "mov_text",
        outputPath
    ).joinToString(" ")

    FFmpegKit.executeAsync(command) { session ->
        val returnCode = session.returnCode
        if (returnCode.isValueSuccess) {
            Timber.tag(TAG).d("✅ Mux successful")
        } else {
            Timber.tag(TAG).e("❌ Mux failed: ${session.failStackTrace}")
        }
    }
}
