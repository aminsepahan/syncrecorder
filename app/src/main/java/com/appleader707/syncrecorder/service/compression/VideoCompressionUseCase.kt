package com.appleader707.syncrecorder.service.compression

import com.appleader707.syncrecorder.TAG
import com.appleader707.syncrecorder.business.usecase.directory.GetSyncRecorderDirectoryUseCase
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class VideoCompressionUseCase @Inject constructor(
    private val getSyncRecorderDirectoryUseCase: GetSyncRecorderDirectoryUseCase
) {
    operator fun invoke(inputVideoName: String, outputVideoName: String) {
        val inputFile = File(getSyncRecorderDirectoryUseCase(), inputVideoName)
        val outputFile = File(getSyncRecorderDirectoryUseCase(), outputVideoName)

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
