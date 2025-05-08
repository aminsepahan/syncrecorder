package com.syn2core.syn2corecamera.service.durationmillis

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

class DurationMillisService @Inject constructor() {
    private var recordingJob: Job? = null
    private var currentDuration: Long = 0L

    fun start(
        coroutineScope: CoroutineScope,
        onTick: (Long) -> Unit
    ) {
        stop()
        recordingJob = coroutineScope.launch {
            tickerFlow().collect {
                currentDuration += 1000L
                onTick(currentDuration)
            }
        }
    }

    fun stop() {
        recordingJob?.cancel()
        recordingJob = null
        currentDuration = 0L
    }

    private fun tickerFlow(): Flow<Unit> = flow {
        while (currentCoroutineContext().isActive) {
            emit(Unit)
            delay(1000L)
        }
    }
}