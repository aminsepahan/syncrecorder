package com.syn2core.syn2corecamera.service.sensor

import com.google.gson.Gson
import com.syn2core.syn2corecamera.domain.SensorSnapshot
import com.syn2core.syn2corecamera.extension.getImuFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class JsonFileWriter() {

    lateinit var file: File

    fun startNewFile(videoFile: File) {
        file = videoFile.getImuFile()
    }

    fun appendJsonObject(jsonObject: SensorSnapshot) {
        CoroutineScope(Dispatchers.Default).launch {
            file.appendText(
                "${jsonObject.timestamp}," +
                        "${jsonObject.type}," +
                        "${jsonObject.values[0]}," +
                        "${jsonObject.values[1]}," +
                        "${jsonObject.values[2]}\n"
            )
        }
    }
}
