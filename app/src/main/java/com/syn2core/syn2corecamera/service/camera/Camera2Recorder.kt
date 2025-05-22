package com.syn2core.syn2corecamera.service.camera

import android.annotation.SuppressLint
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.MediaRecorder
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import com.syn2core.syn2corecamera.business.usecase.convert.ConvertFrameTimestampToSrtUseCase
import com.syn2core.syn2corecamera.business.usecase.directory.GetSyn2CoreCameraDirectoryUseCase
import com.syn2core.syn2corecamera.domain.RecordingSettings
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
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
    var finalizeDeferred: CompletableDeferred<Unit>? = null
        private set

    private val frameTimestamps = mutableListOf<Pair<Long, Long>>()

    private val cameraHandlerThread = HandlerThread("CameraBackground").apply { start() }
    private val cameraHandler = Handler(cameraHandlerThread.looper)

    private val cameraId: String by lazy {
        cameraManager.cameraIdList.first {
            val characteristics = cameraManager.getCameraCharacteristics(it)
            characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        }
    }

    @SuppressLint("MissingPermission")
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
        }, cameraHandler)
    }

    private fun createPreviewSession(surface: Surface) {
        cameraDevice?.createCaptureSession(
            listOf(surface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    val request = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                        addTarget(surface)
                    }
                    session.setRepeatingRequest(request.build(), null, cameraHandler)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Timber.e("Preview session configuration failed")
                }
            },
            cameraHandler
        )
    }

    suspend fun startRecording(
        surface: Surface,
        outputFile: File,
        settings: RecordingSettings,
        onStartTimestamp: () -> Unit,
        onFinalize: () -> Unit
    ) {
        frameTimestamps.clear()
        setupMediaRecorder(outputFile, settings)
        this.outputFile = outputFile
        this.finalizeCallback = onFinalize
        finalizeDeferred = CompletableDeferred()

        val recorderSurface = mediaRecorder!!.surface
        val surfaces = listOf(surface, recorderSurface)

        cameraDevice = openCameraSuspending()
        captureSession = createSessionSuspending(surfaces)

        val request = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_RECORD).apply {
            addTarget(surface)
            addTarget(recorderSurface)
            if (settings.autoFocus) {
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO)
            }
            if (settings.stabilization) {
                set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF)
            }
        }

        var didSendStart = false
        mediaRecorder?.start()
        captureSession?.setRepeatingRequest(
            request.build(),
            object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureStarted(session: CameraCaptureSession, request: CaptureRequest, timestamp: Long, frameNumber: Long) {
                    frameTimestamps.add(Pair(frameNumber, timestamp))
                    if (!didSendStart) {
                        onStartTimestamp()
                        didSendStart = true
                    }
                }
            },
            cameraHandler
        )
    }

    fun stopRecording(): File? {
        return try {
            mediaRecorder?.apply {
                stop()
                saveFrameTimestamps()
                convertFrameTimestampToSrtUseCase(
                    inputTxtName = "frame_timestamps.txt",
                    outputSrtName = "frame_data.srt"
                )
                reset()
                finalizeCallback?.invoke()
                finalizeDeferred?.complete(Unit)
            }
            synchronized(frameTimestamps) { frameTimestamps.clear() }
            outputFile
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop MediaRecorder")
            finalizeDeferred?.completeExceptionally(e)
            null
        }
    }

    private suspend fun setupMediaRecorder(file: File, settings: RecordingSettings) = withContext(Dispatchers.IO) {
        val (width, height) = settings.getResolutionSize()
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(settings.getAudioSource())
            setAudioSamplingRate(96000)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(file.absolutePath)
            setVideoEncodingBitRate(10000000)
            setVideoFrameRate(settings.frameRate)
            setVideoSize(width, height)
            setVideoEncoder(settings.getCodec())
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOrientationHint(0)
            prepare()
        }
    }

    private suspend fun openCameraSuspending(): CameraDevice =
        suspendCancellableCoroutine { cont ->
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) = cont.resume(camera)
                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    cont.cancel()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    cont.resumeWithException(RuntimeException("Camera error: $error"))
                }
            }, cameraHandler)
        }

    private suspend fun createSessionSuspending(surfaces: List<Surface>): CameraCaptureSession =
        suspendCancellableCoroutine { cont ->
            cameraDevice?.createCaptureSession(
                surfaces,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) = cont.resume(session)
                    override fun onConfigureFailed(session: CameraCaptureSession) =
                        cont.resumeWithException(RuntimeException("Session config failed"))
                },
                cameraHandler
            )
        }

    private fun saveFrameTimestamps() {
        val file = File(getSyn2CoreCameraDirectoryUseCase(), "frame_timestamps.txt")
        val list = synchronized(frameTimestamps) { ArrayList(frameTimestamps) }
        file.bufferedWriter().use {
            it.write("index,timestamp_ms\n")
            list.forEach { (frame, timestamp) ->
                it.write("${frame + 1},${timestamp}\n")
            }
        }
    }
}