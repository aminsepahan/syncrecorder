package com.syn2core.syn2corecamera.business.usecase.time

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class GetFormattedDateUseCase @Inject constructor() {
    operator fun invoke(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
}
