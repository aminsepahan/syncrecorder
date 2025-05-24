import com.google.gson.Gson
import com.syn2core.syn2corecamera.domain.SensorSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.RandomAccessFile

class JsonFileWriter(private val file: File) {
    private val gson = Gson()

    init {
        if (!file.exists()) {
            file.writeText("[\n")
        } else {
            // Check if the file ends with ] and remove it temporarily
            val raf = RandomAccessFile(file, "rw")
            if (raf.length() > 1) {
                raf.seek(raf.length() - 2)
                val lastBytes = ByteArray(2)
                raf.readFully(lastBytes)
                if (String(lastBytes) == "\n]") {
                    raf.setLength(raf.length() - 2) // Remove "\n]"
                }
            }
            raf.close()
        }
    }

    fun appendJsonObject(jsonObject: SensorSnapshot) {
        CoroutineScope(Dispatchers.IO).launch {
            file.appendText("${gson.toJson(jsonObject)}\n")
        }
    }

    fun closeJsonArray() {
        val writer = BufferedWriter(FileWriter(file, true))
        writer.append("\n]")
        writer.flush()
        writer.close()
    }
}
