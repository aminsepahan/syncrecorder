package com.syn2core.syn2corecamera.service.compression

import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.syn2core.syn2corecamera.TAG
import com.syn2core.syn2corecamera.business.usecase.directory.GetSyn2CoreCameraDirectoryUseCase
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class VideoCompressionService @Inject constructor(
    private val getSyn2CoreCameraDirectoryUseCase: GetSyn2CoreCameraDirectoryUseCase
) {
    operator fun invoke(inputVideoName: String, outputVideoName: String) {
        val inputFile = File(getSyn2CoreCameraDirectoryUseCase(), inputVideoName)
        val outputFile = File(getSyn2CoreCameraDirectoryUseCase(), outputVideoName)

        val cmd = listOf(
            "-y",                      // Overwrite existing files
            "-i", inputFile.absolutePath, // Video input
            "-vcodec", "libx264",        // Use H.264 codec for video compression
            "-crf", "23",                // Set quality (between 0 and 51, 23 is usually good)
            "-preset", "fast",           // Use fast preset (for faster speed)
            "-acodec", "aac",            // Audio codec
            "-b:a", "192k",              // Audio bitrate
            outputFile.absolutePath      // Output path completed
        )

        FFmpegKit.executeAsync(cmd.joinToString(" ")) { session ->
            val returnCode = session.returnCode
            if (ReturnCode.isSuccess(returnCode)) {
                Timber.tag(TAG).d("✅ Video compression successful.")
            } else {
                Timber.tag(TAG).e("❌ Video compression failed: ${session.failStackTrace}")
            }
        }
    }
}