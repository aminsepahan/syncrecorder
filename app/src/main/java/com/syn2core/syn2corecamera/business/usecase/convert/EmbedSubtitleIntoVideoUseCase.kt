package com.syn2core.syn2corecamera.business.usecase.convert

import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.syn2core.syn2corecamera.TAG
import com.syn2core.syn2corecamera.business.usecase.directory.GetSyn2CoreCameraDirectoryUseCase
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class EmbedSubtitleIntoVideoUseCase @Inject constructor(
    private val getSyn2CoreCameraDirectoryUseCase: GetSyn2CoreCameraDirectoryUseCase,
) {
    operator fun invoke(
        videoNameFile: String,
        subtitleNameFile: String,
        outputNameFile: String
    ) {
        val videoFile = File(getSyn2CoreCameraDirectoryUseCase(), videoNameFile)
        val subtitleFile = File(getSyn2CoreCameraDirectoryUseCase(), subtitleNameFile)
        val outputFile = File(getSyn2CoreCameraDirectoryUseCase(), outputNameFile)

        if (!subtitleFile.exists()) {
            Timber.tag(TAG).e("❌ Subtitle file not found: $subtitleNameFile")
            return
        }

        val cmd = listOf(
            "-y",
            "-i", videoFile.absolutePath,
            "-vf", "subtitles=${subtitleFile.absolutePath.replace(" ", "\\ ")}",
            "-c:v", "libx264",
            "-c:a", "copy",
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
