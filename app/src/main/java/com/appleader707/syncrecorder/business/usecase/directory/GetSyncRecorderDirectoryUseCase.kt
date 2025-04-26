package com.appleader707.syncrecorder.business.usecase.directory

import android.os.Environment
import java.io.File
import javax.inject.Inject

class GetSyncRecorderDirectoryUseCase @Inject constructor() {
    operator fun invoke(): File {
        val externalRoot = Environment.getExternalStorageDirectory()
        val syncRecorderDir = File(externalRoot, "SyncRecorder")
        if (!syncRecorderDir.exists()) {
            syncRecorderDir.mkdirs()
        }
        return syncRecorderDir
    }
}
