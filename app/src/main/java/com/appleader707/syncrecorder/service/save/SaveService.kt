package com.appleader707.syncrecorder.service.save

import com.appleader707.syncrecorder.TAG
import com.appleader707.syncrecorder.business.usecase.convert.EmbedSubtitleIntoVideoUseCase
import com.appleader707.syncrecorder.domain.SaveTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SaveService @Inject constructor(
    private val embedSubtitleIntoVideoUseCase: EmbedSubtitleIntoVideoUseCase
) {
    private val queue = Channel<SaveTask>(Channel.UNLIMITED)
    private var serviceStarted = false

    fun startService(scope: CoroutineScope) {
        if (serviceStarted) return
        serviceStarted = true

        scope.launch(Dispatchers.IO) {
            for (task in queue) {
                try {
                    embedSubtitleIntoVideoUseCase(
                        task.videoName,
                        task.sensorName,
                        task.outputName
                    )
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
}