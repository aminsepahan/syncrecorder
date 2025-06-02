package com.syn2core.syn2corecamera.extension

import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile


fun File.getImuFile(): File = File(
    parentFile,
    name.replace(
        oldValue = ".mp4",
        newValue = "_imu.txt"
    )
)

fun File.getFramesFile(): File = File(
    parentFile,
    name.replace(
        oldValue = ".mp4",
        newValue = "_ft.txt"
    )
)

val File.lastLine: String?
    get() {
    var fileHandler: RandomAccessFile? = null
    try {
        fileHandler = RandomAccessFile(this, "r")
        val fileLength = fileHandler.length() - 1
        val sb = StringBuilder()

        for (filePointer in fileLength downTo -1 + 1) {
            fileHandler.seek(filePointer)
            val readByte = fileHandler.readByte().toInt()

            if (readByte == 0xA) {
                if (filePointer == fileLength) {
                    continue
                }
                break
            } else if (readByte == 0xD) {
                if (filePointer == fileLength - 1) {
                    continue
                }
                break
            }

            sb.append(readByte.toChar())
        }

        val lastLine = sb.reverse().toString()
        return lastLine
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        return null
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    } finally {
        if (fileHandler != null) try {
            fileHandler.close()
        } catch (e: IOException) {
            /* ignore */
        }
    }
}