package com.syn2core.syn2corecamera.service.writer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class FrameFileWriter() {

    lateinit var file: File

    fun startNewSegment(segmentNumber: Int, videoFile: File) {
        if (segmentNumber == 1) {
            file = File(
                videoFile.parentFile,
                videoFile.name.replace(
                    oldValue = ".mp4",
                    newValue = "_ft.txt"
                )
            )

            file.appendText("frameNumber, frameTimestamp\n")
        }
        file.appendText("----- Segment $segmentNumber ---\n")
    }

    fun appendNewFrame(frameNumber: Long, frameTimestamp: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            file.appendText("$frameNumber,$frameTimestamp\n")
        }
    }
}