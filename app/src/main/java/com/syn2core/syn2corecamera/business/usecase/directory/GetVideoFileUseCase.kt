package com.syn2core.syn2corecamera.business.usecase.directory

import com.syn2core.syn2corecamera.business.usecase.time.GetFormattedDateUseCase
import com.syn2core.syn2corecamera.business.usecase.time.GetFormattedTimeUseCase
import com.syn2core.syn2corecamera.extension.syn2CoreDownloadsDir
import java.io.File
import javax.inject.Inject

class GetVideoFileUseCase @Inject constructor(
    private val getFormattedTimeUseCase: GetFormattedTimeUseCase,
) {
    operator fun invoke(
        videoDirectory: String,
        segmentCount: Int
    ): File {
        val time = getFormattedTimeUseCase()
        val directory = File(syn2CoreDownloadsDir, "$videoDirectory/part-$segmentCount - ${time}")
        if (!directory.exists()) directory.mkdirs()

        val fileName = "Part-${segmentCount}.mp4"
        return File(directory, fileName)
    }
}
