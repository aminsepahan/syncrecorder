package com.syn2core.syn2corecamera.extension

import android.widget.Toast
import com.syn2core.syn2corecamera.core.HComponentActivity

fun showMessage(message: String, duration: Int = Toast.LENGTH_SHORT) {
            Toast.makeText(
                HComponentActivity.currentActivity,
                message,
                duration
            ).show()
        }