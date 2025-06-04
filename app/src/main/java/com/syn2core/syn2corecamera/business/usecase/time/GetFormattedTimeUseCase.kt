package com.syn2core.syn2corecamera.business.usecase.time

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class GetFormattedTimeUseCase @Inject constructor() {
    operator fun invoke(): String {
        val timeFormat = SimpleDateFormat("hh-mm", Locale.getDefault())
        return timeFormat.format(Date())
    }
}
