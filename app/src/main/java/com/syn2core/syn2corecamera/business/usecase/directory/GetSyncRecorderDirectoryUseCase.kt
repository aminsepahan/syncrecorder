package com.syn2core.syn2corecamera.business.usecase.directory

import android.os.Environment
import java.io.File
import javax.inject.Inject

class GetSyn2CoreCameraDirectoryUseCase @Inject constructor() {
    operator fun invoke(): File {
        val externalRoot = Environment.getExternalStorageDirectory()
        val syncRecorderDir = File(externalRoot, "Syn2CoreCamera")
        if (!syncRecorderDir.exists()) {
            syncRecorderDir.mkdirs()
        }
        return syncRecorderDir
    }
}
