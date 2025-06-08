package com.syn2core.syn2corecamera.service.writer

import com.syn2core.syn2corecamera.domain.SensorSnapshot
import com.syn2core.syn2corecamera.extension.getImuFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JsonFileWriter @Inject constructor() {

    lateinit var file: File
    var cashString = ""
    var counter = 0

    fun startNewFile(videoFile: File) {
        CoroutineScope(Dispatchers.Default).launch {
            cashString = ""
            counter = 0
            file = videoFile.getImuFile
            file.appendText("timestamp,type,x,y,z\n")
        }
    }

    fun appendJsonObject(jsonObject: SensorSnapshot) {
        CoroutineScope(Dispatchers.Default).launch {
            cashString = cashString +
                    "${jsonObject.timestamp}," +
                    "${jsonObject.type}," +
                    "${jsonObject.values[0]}," +
                    "${jsonObject.values[1]}," +
                    "${jsonObject.values[2]}\n"

            if (counter < 50) {
                counter++
            } else {
                file.appendText(cashString)
                cashString = ""
                counter = 0
            }
        }
    }
}