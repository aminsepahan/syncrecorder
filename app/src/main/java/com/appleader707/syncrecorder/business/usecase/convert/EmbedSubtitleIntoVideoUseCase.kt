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
    operator fun invoke(
        videoNameFile: String,
        subtitleNameFiles: List<String>,
        outputNameFile: String
    ) {
        val videoFile = File(getSyncRecorderDirectoryUseCase(), videoNameFile)
        val outputFile = File(getSyncRecorderDirectoryUseCase(), outputNameFile)

        val cmd = mutableListOf(
            "-y",
            "-i", videoFile.absolutePath
        )

        subtitleNameFiles.forEach { subtitleFile ->
            val subtitleFilePath = File(getSyncRecorderDirectoryUseCase(), subtitleFile).absolutePath
            cmd.add("-i")
            cmd.add(subtitleFilePath)
        }

        cmd.add("-map")
        cmd.add("0:v:0")
        subtitleNameFiles.forEachIndexed { index, _ ->
            cmd.add("-map")
            cmd.add("${index + 1}:s:0")
        }

        cmd.add("-c:v")
        cmd.add("copy")
        cmd.add("-c:a")
        cmd.add("copy")
        cmd.add("-c:s")
        cmd.add("mov_text")

        cmd.add(outputFile.absolutePath)

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
