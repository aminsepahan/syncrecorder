package com.appleader707.syncrecorder.service.save

import com.appleader707.syncrecorder.TAG
import com.appleader707.syncrecorder.business.usecase.convert.EmbedSubtitleIntoVideoUseCase
import com.appleader707.syncrecorder.business.usecase.directory.GetSyncRecorderDirectoryUseCase
import com.appleader707.syncrecorder.domain.SaveTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class SaveService @Inject constructor(
    private val embedSubtitleIntoVideoUseCase: EmbedSubtitleIntoVideoUseCase,
    private val getSyncRecorderDirectoryUseCase: GetSyncRecorderDirectoryUseCase,
    //private val videoCompressionUseCase: VideoCompressionUseCase,
) {
    fun saveOutpuVideo(task: SaveTask) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val subtitleFiles = listOf(
                    "accelerometer_data_${task.recordingCount}.srt",
                    "gyroscope_data_${task.recordingCount}.srt",
                    "magnetometer_data_${task.recordingCount}.srt"
                )

                embedSubtitleIntoVideoUseCase(
                    task.videoName,
                    subtitleFiles,
                    task.outputName
                )

                /*val compressedVideoName = "compressed_${task.outputName}"
                val compressJob = async {
                    videoCompressionUseCase(task.outputName, compressedVideoName)
                }
                compressJob.await()

                deleteOldVideoFiles(task.videoName, task.outputName)*/

                Timber.tag(TAG).d("✅ Embedded and saved: ${task.outputName}")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "❌ Failed saving: ${task.outputName}")
            }
        }
    }

    // Delete old files
    private fun deleteOldVideoFiles(vararg files: String) {
        files.forEach { fileName ->
            val file = File(getSyncRecorderDirectoryUseCase(), fileName)
            if (file.exists()) {
                val deleted = file.delete()
                if (deleted) {
                    Timber.tag(TAG).d("✅ File deleted: $fileName")
                } else {
                    Timber.tag(TAG).e("❌ Failed to delete file: $fileName")
                }
            }
        }
    }
}