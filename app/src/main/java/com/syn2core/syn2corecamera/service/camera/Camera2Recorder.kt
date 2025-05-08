package com.syn2core.syn2corecamera.service.camera

import android.annotation.SuppressLint
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
import com.syn2core.syn2corecamera.business.usecase.convert.ConvertFrameTimestampToSrtUseCase
import com.syn2core.syn2corecamera.business.usecase.directory.GetSyn2CoreCameraDirectoryUseCase
import com.syn2core.syn2corecamera.domain.RecordingSettings
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Suppress("DEPRECATION")
@Singleton
class Camera2Recorder @Inject constructor(
    private val cameraManager: CameraManager,
    private val getSyn2CoreCameraDirectoryUseCase: GetSyn2CoreCameraDirectoryUseCase,
    private val convertFrameTimestampToSrtUseCase: ConvertFrameTimestampToSrtUseCase,
) {
    private var cameraDevice: CameraDevice? = null
    private var mediaRecorder: MediaRecorder? = null
    private var captureSession: CameraCaptureSession? = null
    private var outputFile: File? = null
    private var finalizeCallback: (() -> Unit)? = null
    private var previewSurface: Surface? = null

    private var cameraStartTimestamp: Long = 0L
    private val frameTimestamps = mutableListOf<Long>()

    private val cameraId: String by lazy {
        cameraManager.cameraIdList.first { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            facing == CameraCharacteristics.LENS_FACING_BACK
        }
    }

    fun startPreview(surface: Surface) {
        previewSurface = surface

        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(device: CameraDevice) {
                cameraDevice = device
                createPreviewSession(surface)
            }

            override fun onDisconnected(camera: CameraDevice) {
                camera.close()
                cameraDevice = null
            }

            override fun onError(camera: CameraDevice, error: Int) {
                camera.close()
                cameraDevice = null
                Timber.e("Camera open error: $error")
            }
        }, Handler(Looper.getMainLooper()))
    }

    private fun createPreviewSession(surface: Surface) {
        cameraDevice?.createCaptureSession(
            listOf(surface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    val previewRequest =
                        cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                            addTarget(surface)
                        }
                    session.setRepeatingRequest(
                        previewRequest.build(),
                        null,
                        Handler(Looper.getMainLooper())
                    )
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Timber.e("Preview session configuration failed")
                }
            },
            Handler(Looper.getMainLooper())
        )
    }

    @SuppressLint("MissingPermission")
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

        cameraDevice = openCameraSuspending()

        captureSession = createSessionSuspending(surfaces)

        val captureRequest =
            cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_RECORD).apply {
                addTarget(surface)
                addTarget(recorderSurface)
            }

        var didSendStart = false

        captureSession?.setRepeatingRequest(
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
                        onStartTimestamp(frameTimestampElapsed) // based on elapsedRealtimeNanos
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
                saveFrameTimestamps()
                convertFrameTimestampToSrtUseCase(
                    inputTxtName = "frame_timestamps.txt",
                    outputSrtName = "frame_data.srt"
                )
                reset()
                finalizeCallback?.invoke()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop MediaRecorder")
        } finally {
            releaseResources()
            previewSurface?.let { startPreview(it) }
        }
    }

    private fun setupMediaRecorder(file: File, settings: RecordingSettings) {
        val (width, height) = settings.getResolutionSize()

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(settings.getAudioSource())
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(file.absolutePath)
            setVideoEncodingBitRate(10000000)
            setVideoFrameRate(settings.frameRate)
            setVideoSize(width, height)
            setVideoEncoder(settings.getCodec())
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            prepare()
        }
    }

    private suspend fun openCameraSuspending(): CameraDevice =
        suspendCancellableCoroutine { continuation ->
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) = continuation.resume(camera)
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

    private suspend fun createSessionSuspending(surfaces: List<Surface>): CameraCaptureSession =
        suspendCancellableCoroutine { continuation ->
            cameraDevice?.createCaptureSession(
                surfaces,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) =
                        continuation.resume(session)

                    override fun onConfigureFailed(session: CameraCaptureSession) =
                        continuation.resumeWithException(RuntimeException("Session config failed"))
                },
                Handler(Looper.getMainLooper())
            )
        }

    private fun saveFrameTimestamps() {
        val frameLogFile = File(getSyn2CoreCameraDirectoryUseCase(), "frame_timestamps.txt")
        frameLogFile.bufferedWriter().use { writer ->
            writer.write("index,timestamp_ms\n")
            val bootOffset = SystemClock.elapsedRealtimeNanos() - System.nanoTime()
            frameTimestamps.forEachIndexed { index, timestamp ->
                val alignedTimestamp = timestamp + bootOffset
                val timeMs = alignedTimestamp - cameraStartTimestamp
                writer.write("${index + 1},${timeMs / 1_000_000}\n")
            }
        }
    }

    private fun releaseResources() {
        mediaRecorder?.release()
        captureSession?.close()
        cameraDevice?.close()
        mediaRecorder = null
        captureSession = null
        cameraDevice = null
    }
}