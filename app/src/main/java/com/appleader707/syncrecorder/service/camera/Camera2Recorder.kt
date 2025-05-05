package com.appleader707.syncrecorder.service.camera

import android.content.Context
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Surface
import com.appleader707.syncrecorder.business.usecase.convert.ConvertFrameTimestampToSrtUseCase
import com.appleader707.syncrecorder.business.usecase.directory.GetSyncRecorderDirectoryUseCase
import com.appleader707.syncrecorder.domain.RecordingSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class Camera2Recorder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getSyncRecorderDirectoryUseCase: GetSyncRecorderDirectoryUseCase,
    private val convertFrameTimestampToSrtUseCase: ConvertFrameTimestampToSrtUseCase,
) {
    private var cameraDevice: CameraDevice? = null
    private var mediaRecorder: MediaRecorder? = null
    private var captureSession: CameraCaptureSession? = null
    private var outputFile: File? = null
    private var finalizeCallback: (() -> Unit)? = null

    private var cameraStartTimestamp: Long = 0L
    private val frameTimestamps = mutableListOf<Long>()

    private val cameraId: String by lazy {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraManager.cameraIdList.first { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            facing == CameraCharacteristics.LENS_FACING_BACK
        }
    }

    suspend fun startRecording(
        surface: Surface,
        outputFile: File,
        settings: RecordingSettings,
        onStartTimestamp: (Long) -> Unit,
        onFinalize: () -> Unit
    ) {
        frameTimestamps.clear()

        setupMediaRecorder(outputFile, settings)

        this.outputFile = outputFile
        this.finalizeCallback = onFinalize

        val recorderSurface = mediaRecorder!!.surface
        val surfaces = listOf(surface, recorderSurface)

        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraOpen = suspendCancellableCoroutine { continuation ->
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    continuation.resume(camera)
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    continuation.cancel()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    continuation.resumeWithException(RuntimeException("Camera error: $error"))
                }
            }, Handler(Looper.getMainLooper()))
        }

        cameraDevice = cameraOpen

        val session = suspendCancellableCoroutine { continuation ->
            cameraDevice?.createCaptureSession(
                surfaces,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        continuation.resume(session)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        continuation.resumeWithException(RuntimeException("Session config failed"))
                    }
                },
                Handler(Looper.getMainLooper())
            )
        }

        captureSession = session

        val captureRequest =
            cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_RECORD).apply {
                addTarget(surface)
                addTarget(recorderSurface)
            }

        var didSendStart = false

        session.setRepeatingRequest(
            captureRequest.build(),
            object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureStarted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    timestamp: Long,
                    frameNumber: Long
                ) {
                    if (!didSendStart) {
                        val elapsedNow = SystemClock.elapsedRealtimeNanos()
                        val nanoNow = System.nanoTime()
                        val bootOffset = elapsedNow - nanoNow
                        val frameTimestampElapsed = timestamp + bootOffset

                        cameraStartTimestamp = frameTimestampElapsed

                        onStartTimestamp(frameTimestampElapsed) // بر مبنای elapsedRealtimeNanos
                        didSendStart = true
                    }

                    frameTimestamps.add(timestamp)
                }
            },
            Handler(Looper.getMainLooper())
        )

        mediaRecorder?.start()
    }

    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()

                val frameLogFile = File(getSyncRecorderDirectoryUseCase(), "frame_timestamps.txt")
                frameLogFile.bufferedWriter().use { writer ->
                    writer.write("index,timestamp_ms\n")

                    val bootOffset = SystemClock.elapsedRealtimeNanos() - System.nanoTime()
                    frameTimestamps.forEachIndexed { index, timestamp ->
                        val alignedTimestamp = timestamp + bootOffset
                        val timeMs = alignedTimestamp - cameraStartTimestamp
                        writer.write("${index + 1},${timeMs / 1_000_000}\n")
                    }
                }
                convertFrameTimestampToSrtUseCase(
                    inputTxtName = "frame_timestamps.txt",
                    outputSrtName = "frame_data.srt"
                )

                reset()

                finalizeCallback?.invoke()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop MediaRecorder")
        }

        mediaRecorder?.release()
        captureSession?.close()
        cameraDevice?.close()

        mediaRecorder = null
        captureSession = null
        cameraDevice = null
    }

    private fun setupMediaRecorder(file: File, settings: RecordingSettings) {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)

            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(file.absolutePath)
            setVideoEncodingBitRate(10000000)
            setVideoFrameRate(settings.frameRate)
            setVideoSize(1920, 1080)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            prepare()
        }
    }
}