package com.appleader707.syncrecorder.business.usecase.time

import javax.inject.Inject

class GetFormatTimeUseCase @Inject constructor(
) {
    operator fun invoke(ms: Long): String {
        val hours = ms / 3600000
        val minutes = (ms % 3600000) / 60000
        val seconds = (ms % 60000) / 1000
        val millis = ms % 1000
        return "%02d:%02d:%02d,%03d".format(hours, minutes, seconds, millis)
    }
}
