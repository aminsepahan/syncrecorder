package com.syn2core.syn2corecamera.extension

import java.io.File

fun File.getImuFile(): File = File(
    parentFile,
    name.replace(
        oldValue = ".mp4",
        newValue = "_imu.json"
    )
)

fun File.getFramesFile(): File = File(
    parentFile,
    name.replace(
        oldValue = ".mp4",
        newValue = "_ft.txt"
    )
)