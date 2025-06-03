package com.syn2core.syn2corecamera.service.writer

import com.syn2core.syn2corecamera.extension.getFramesFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FrameFileWriter @Inject constructor() {

    lateinit var file: File

    fun startNewSegment(segmentNumber: Int, videoFile: File) {
        if (segmentNumber == 1) {
            file = videoFile.getFramesFile()
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