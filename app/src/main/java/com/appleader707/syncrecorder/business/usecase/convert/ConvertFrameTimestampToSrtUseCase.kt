package com.appleader707.syncrecorder.business.usecase.convert

import com.appleader707.syncrecorder.business.usecase.directory.GetSyncRecorderDirectoryUseCase
import com.appleader707.syncrecorder.business.usecase.time.GetFormatTimeUseCase
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class ConvertFrameTimestampToSrtUseCase @Inject constructor(
    private val getSyncRecorderDirectoryUseCase: GetSyncRecorderDirectoryUseCase,
    private val getFormatTimeUseCase: GetFormatTimeUseCase,
) {
    operator fun invoke(inputTxtName: String, outputSrtName: String) {
        val directory = getSyncRecorderDirectoryUseCase()
        val inputFile = File(directory, inputTxtName)
        val outputFile = File(directory, outputSrtName)

        if (!inputFile.exists()) {
            Timber.e("❌ Input file not found: $inputTxtName")
            return
        }

        val lines = inputFile.readLines()
        if (lines.isEmpty()) return

        // Parse timestamps
        val timestamps = lines.mapNotNull { line ->
            val parts = line.split(",")
            if (parts.size == 2) {
                parts[1].trim().toLongOrNull()
            } else null
        }

        val baseTime = timestamps.firstOrNull() ?: return

        outputFile.bufferedWriter().use { out ->
            timestamps.forEachIndexed { index, timestamp ->
                val relativeStart = timestamp - baseTime
                val relativeEnd = relativeStart + 40L // Display for about 40 milliseconds

                out.write("${index + 1}\n")
                out.write("${getFormatTimeUseCase(relativeStart)} --> ${getFormatTimeUseCase(relativeEnd)}\n")
                out.write("FRAME ${index + 1}\n\n")
            }
        }

        Timber.d("✅ Frame timestamps converted to SRT: $outputSrtName")
    }
}
