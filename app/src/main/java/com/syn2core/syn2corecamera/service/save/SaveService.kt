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
    private val queue = Channel<Pair<SaveTask, () -> Unit>>(Channel.UNLIMITED)
    private var serviceStarted = false

    fun startService(scope: CoroutineScope) {
        if (serviceStarted) return
        serviceStarted = true

        scope.launch(Dispatchers.IO) {
            for ((task, onDone) in queue) {
                try {
                    val subtitleFile = "sensor_data_${task.recordingCount}.srt"
                    embedSubtitleIntoVideoUseCase(
                        task.videoName,
                        subtitleFile,
                        task.outputName
                    )
                    onDone()
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Failed to save task for ${task.videoName}")
                    onDone()
                }
            }
        }
    }

    suspend fun addTask(
        task: SaveTask,
        onDone: () -> Unit = {}
    ) {
        queue.send(Pair(task, onDone))
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