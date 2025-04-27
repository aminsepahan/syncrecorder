package com.appleader707.syncrecorder.business.usecase.convert

import com.appleader707.syncrecorder.TAG
import com.appleader707.syncrecorder.business.usecase.directory.GetSyncRecorderDirectoryUseCase
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class EmbedSubtitleIntoVideoUseCase @Inject constructor(
    private val getSyncRecorderDirectoryUseCase: GetSyncRecorderDirectoryUseCase,
) {
    operator fun invoke(videoNameFile: String, subtitleNameFile: String, outputNameFile: String) {
        val videoFile = File(getSyncRecorderDirectoryUseCase(), videoNameFile)
        val subtitleFile = File(getSyncRecorderDirectoryUseCase(), subtitleNameFile)
        val outputFile = File(getSyncRecorderDirectoryUseCase(), outputNameFile)

        val cmd = listOf(
            "-y",
            "-i", videoFile.absolutePath,
            "-i", subtitleFile.absolutePath,
            "-c:v", "copy",
            "-c:a", "copy",
            "-c:s", "mov_text",
            outputFile.absolutePath
        )

        FFmpegKit.executeAsync(cmd.joinToString(" ")) { session ->
            val returnCode = session.returnCode
            if (ReturnCode.isSuccess(returnCode)) {
                Timber.tag(TAG).d("✅ Subtitle embedded successfully into video.")
            } else {
                Timber.tag(TAG).e("❌ FFmpeg failed: ${session.failStackTrace}")
            }
        }
    }
}
