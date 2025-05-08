package com.syn2core.syn2corecamera.service.save

import com.syn2core.syn2corecamera.TAG
import com.syn2core.syn2corecamera.business.usecase.convert.EmbedSubtitleIntoVideoUseCase
import com.syn2core.syn2corecamera.business.usecase.directory.GetSyn2CoreCameraDirectoryUseCase
import com.syn2core.syn2corecamera.domain.SaveTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class SaveService @Inject constructor(
    private val embedSubtitleIntoVideoUseCase: EmbedSubtitleIntoVideoUseCase,
    private val getSyn2CoreCameraDirectoryUseCase: GetSyn2CoreCameraDirectoryUseCase,
    //private val videoCompressionUseCase: VideoCompressionUseCase,
) {
    private val queue = Channel<SaveTask>(Channel.UNLIMITED)
    private var serviceStarted = false

    fun startService(scope: CoroutineScope) {
        if (serviceStarted) return
        serviceStarted = true

        scope.launch(Dispatchers.IO) {
            for (task in queue) {
                try {
                    val subtitleFile = "sensor_data_${task.recordingCount}.srt"
                    embedSubtitleIntoVideoUseCase(
                        task.videoName,
                        subtitleFile,
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
    }

    suspend fun addTask(task: SaveTask) {
        Timber.tag(TAG).d("📥 Task added to queue: $task")
        queue.send(task)
    }

    // Delete old files
    private fun deleteOldVideoFiles(vararg files: String) {
        files.forEach { fileName ->
            val file = File(getSyn2CoreCameraDirectoryUseCase(), fileName)
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