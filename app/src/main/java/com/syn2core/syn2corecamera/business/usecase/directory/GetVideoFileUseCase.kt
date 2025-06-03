package com.syn2core.syn2corecamera.business.usecase.directory

import android.os.Environment
import com.syn2core.syn2corecamera.business.usecase.time.GetFormattedDateUseCase
import com.syn2core.syn2corecamera.business.usecase.time.GetFormattedTimeUseCase
import java.io.File
import javax.inject.Inject

class GetVideoFileUseCase @Inject constructor(
    private val getFormattedDateUseCase: GetFormattedDateUseCase,
    private val getFormattedTimeUseCase: GetFormattedTimeUseCase,
) {
    operator fun invoke(segmentCount: Int): File {
        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "syn2core")
        if (!directory.exists()) directory.mkdirs()

        val date = getFormattedDateUseCase()
        val time = getFormattedTimeUseCase()
        val fileName = "${date}_${time}_${segmentCount}_s2c.mp4"
        return File(directory, fileName)
    }
}
