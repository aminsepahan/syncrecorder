import com.google.gson.Gson
import com.syn2core.syn2corecamera.domain.SensorSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.RandomAccessFile

class JsonFileWriter() {
    private val gson = Gson()
    lateinit var file: File

    fun startNewFile(videoFile: File) {
        file = File(
            videoFile.parentFile,
            videoFile.name.replace(
                oldValue = ".mp4",
                newValue = "_imu.json"
            )
        )
        file.appendText("[")
    }

    fun appendJsonObject(jsonObject: SensorSnapshot) {
        CoroutineScope(Dispatchers.Default).launch {
            file.appendText("${gson.toJson(jsonObject)},\n")
        }
    }

    fun closeJsonArray() {
        file.appendText("\n]")
    }
}
