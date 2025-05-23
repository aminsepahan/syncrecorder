package com.syn2core.syn2corecamera.business.usecase.directory

import com.syn2core.syn2corecamera.business.usecase.time.GetFormattedDateUseCase
import com.syn2core.syn2corecamera.business.usecase.time.GetFormattedTimeUseCase
import java.io.File
import javax.inject.Inject

class GetVideoFileUseCase @Inject constructor(
    private val getSyn2CoreCameraDirectoryUseCase: GetSyn2CoreCameraDirectoryUseCase,
    private val getFormattedDateUseCase: GetFormattedDateUseCase,
    private val getFormattedTimeUseCase: GetFormattedTimeUseCase,
) {
    operator fun invoke(segmentCount: Int): File {
        val directory = getSyn2CoreCameraDirectoryUseCase()
        val date = getFormattedDateUseCase()
        val time = getFormattedTimeUseCase()
        val fileName = "${date}_${time}_${segmentCount}_s2c.mp4"
        val videoFile = File(directory, fileName)
        return videoFile
    }
}
