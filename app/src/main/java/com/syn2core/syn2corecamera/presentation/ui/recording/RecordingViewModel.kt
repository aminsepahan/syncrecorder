package com.syn2core.syn2corecamera.presentation.ui.recording

import android.view.Surface
import androidx.lifecycle.viewModelScope
import com.syn2core.common.ui.base.BaseViewModel
import com.syn2core.common.ui.livedata.SingleLiveData
import com.syn2core.syn2corecamera.BaseApplication
import com.syn2core.syn2corecamera.TAG
import com.syn2core.syn2corecamera.business.usecase.setting.GetRecordingSettingsUseCase
import com.syn2core.syn2corecamera.business.usecase.setting.SetRecordingSettingsUseCase
import com.syn2core.syn2corecamera.business.usecase.time.GetFormattedDateUseCase
import com.syn2core.syn2corecamera.business.usecase.time.GetFormattedTimeUseCase
import com.syn2core.syn2corecamera.data.network.Sdp
import com.syn2core.syn2corecamera.data.network.WebRTCApiService
import com.syn2core.syn2corecamera.domain.RecordingSettings
import com.syn2core.syn2corecamera.extension.lastLine
import com.syn2core.syn2corecamera.service.camera.CameraService
import com.syn2core.syn2corecamera.service.durationmillis.DurationMillisService
import com.syn2core.syn2corecamera.service.stream.PeerConnectionObserver
import com.syn2core.syn2corecamera.service.stream.WebRTCClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 *
 * @see BaseViewModel
 */

@HiltViewModel
class RecordingViewModel @Inject constructor(
    private val getRecordingSettingsUseCase: GetRecordingSettingsUseCase,
    private val setRecordingSettingsUseCase: SetRecordingSettingsUseCase,
    private val durationMillisService: DurationMillisService,
    private val cameraService: CameraService,
    private val getFormattedDateUseCase: GetFormattedDateUseCase,
    private val getFormattedTimeUseCase: GetFormattedTimeUseCase,
    private val rtcClient: WebRTCClient,
    private val webRTCApiService: WebRTCApiService
) : BaseViewModel<RecordingViewEvent>() {

    val timber = Timber.tag("RecordingViewModel")
    private val _state = MutableStateFlow(RecordingViewState())
    val state: StateFlow<RecordingViewState> = _state.asStateFlow()

    val effect = SingleLiveData<RecordingViewEffect>()

    private var cameraSurface: Surface? = null
    private var autoRestartJob: Job? = null
    private var progressCheck: Job? = null
    private var segmentCount: Int = 0

    private var currentVideoFile: File? = null


    fun updateSurface(surface: Surface) {
        cameraSurface = surface
        cameraService.startPreview(surface)
    }

    fun getSurface(): Surface? = cameraSurface

    override fun processEvent(event: RecordingViewEvent) {
        when (event) {
            is RecordingViewEvent.ToggleRecording -> {
                if (_state.value.isRecording) stopAll()
                else startAll()
            }

            RecordingViewEvent.LoadSettings -> {
                viewModelScope.launch {
                    val settingsState = getRecordingSettingsUseCase()
                    updateState { it.copy(settingsState = settingsState) }
                }
            }

            RecordingViewEvent.NavigateToSettings -> {
                if (!_state.value.isRecording) {
                    effect.postValue(RecordingViewEffect.NavigateToSetting)
                }
            }

            is RecordingViewEvent.SetResolution -> {
                viewModelScope.launch {
                    setRecordingSettingsUseCase(event.setting)
                }
                updateState { it.copy(settingsState = event.setting) }
            }

            RecordingViewEvent.ToggleStreaming -> {
                if (_state.value.isStreaming) {
                    stopAll()
                } else {
                    startStreaming()
                    updateState { it.copy(isStreaming = true) }
                }
            }
        }
    }

    private fun startStreaming() {

        viewModelScope.launch {
            initWebRtcClient()
            val webRtcSurface = rtcClient.createWebRTCSurface()
            rtcClient.call(object : SdpObserver {
                override fun onCreateSuccess(p0: SessionDescription?) {
                    timber.d("onCreateSuccess p0 : $p0")
                    val sdp = Sdp(
                        type = "offer",
                        sdp = p0?.description
                    )
                    val json = Json.encodeToString(sdp)
                    timber.d("onCreateSuccess $json")
                    viewModelScope.launch {
                        val answer = webRTCApiService.getWebRtcAnswerBasedOnOffer(
                            offer = json,
                            meetingId = "123456"
                        )
                        rtcClient.onRemoteSessionReceived(
                            SessionDescription(
                                SessionDescription.Type.ANSWER,
                                answer.sdp
                            )
                        )
                        cameraService.startStreaming(
                            previewSurface = cameraSurface!!,
                            webRtcSurface = webRtcSurface,
                            settings = getRecordingSettingsUseCase()
                        )
                    }

                }

                override fun onSetSuccess() {
                    timber.d("onSetSuccess p0")
                }

                override fun onCreateFailure(p0: String?) {

                    timber.d("onCreateFailure p0 : $p0")
                }

                override fun onSetFailure(p0: String?) {

                    timber.d("onSetFailure p0 : $p0")
                }

            }, "12345")
        }

    }

    private fun initWebRtcClient() {
        rtcClient.initialize(BaseApplication.appContext, object : PeerConnectionObserver() {
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
                super.onIceGatheringChange(p0)
                timber.d("onIceGatheringChange p0 : $p0")
            }
            override fun onIceCandidate(p0: IceCandidate?) {
                super.onIceCandidate(p0)
                timber.d("onIceCandidate: $p0")
                rtcClient.addIceCandidate(p0)
            }

            override fun onAddStream(p0: MediaStream?) {
                super.onAddStream(p0)
                timber.d("onAddStream: $p0")
                //                    p0?.videoTracks?.get(0)?.addSink(remote_view)
            }

            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                timber.d("onIceConnectionChange: $p0")
            }

            override fun onIceConnectionReceivingChange(p0: Boolean) {
                timber.d("onIceConnectionReceivingChange: $p0")
            }

            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                timber.d("onConnectionChange: $newState")
            }

            override fun onDataChannel(p0: DataChannel?) {
                timber.d("onDataChannel: $p0")
            }

            override fun onStandardizedIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
                timber.d("onStandardizedIceConnectionChange: $newState")
            }

            override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
                timber.d("onAddTrack: $p0 \n $p1")
            }

            override fun onTrack(transceiver: RtpTransceiver?) {
                timber.d("onTrack: $transceiver")
            }
        })
    }

    private fun startAll() {
        viewModelScope.launch {
            val videoDirectory = "${getFormattedDateUseCase()}T${getFormattedTimeUseCase()}"
            val surface = cameraSurface
            if (surface == null) {
                Timber.tag(TAG).w("🚫 Cannot start recording: Surface is null")
                return@launch
            }

            val settings = getRecordingSettingsUseCase()

            currentVideoFile = cameraService.startRecordingAndSensors(
                surface = surface,
                recordingSettings = settings,
                videoDirectory = videoDirectory,
            )

            durationMillisService.start(viewModelScope) { newDuration ->
                updateState {
                    it.copy(
                        durationMillis = newDuration,
                    )
                }
            }

            startAutoRestartLoop(
                surface = surface,
                settings = settings,
                videoDirectory = videoDirectory
            )

            updateState {
                it.copy(
                    isRecording = true,
                    durationMillis = 0L,
                    segmentCount = 1
                )
            }
            effect.postValue(RecordingViewEffect.RecordingStarted)
        }
    }

    private fun stopAll() {
        viewModelScope.launch(Dispatchers.IO) {
            autoRestartJob?.cancel()
            autoRestartJob = null
            durationMillisService.stop()
            if (_state.value.isRecording) {
                cameraService.stopRecordingAndSensors()
            } else {
                rtcClient.release()
                cameraService.stopStreaming()
            }
            updateState {
                it.copy(
                    isRecording = false,
                    isStreaming = false,
                    durationMillis = 0,
                    segmentCount = 0
                )
            }

            effect.postValue(RecordingViewEffect.RecordingStopped)
            cameraService.startPreview(cameraSurface!!)
        }
    }

    private fun startAutoRestartLoop(
        surface: Surface,
        settings: RecordingSettings,
        videoDirectory: String
    ) {
        autoRestartJob?.cancel()
        autoRestartJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(settings.autoStopMinutes * 60 * 1000L)
                segmentCount = cameraService.switchToNewSegment(
                    surface = surface,
                    videoDirectory = videoDirectory
                )
                updateState { it.copy(segmentCount = segmentCount) }
            }
        }
        progressCheck?.cancel()
        progressCheck = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                checkIMUWritingProgress()
            }
        }
    }

    private fun checkIMUWritingProgress() {
        currentVideoFile?.let { videoFile ->
            val lastFrameLine = videoFile.parentFile?.parentFile?.listFiles()?.maxByOrNull {
                it.name
            }?.listFiles { _, name ->
                name?.contains("ft") == true
            }?.first()?.lastLine ?: return
            var index = lastFrameLine.indexOf(",")
            val lastFrameTimestamp = if (index == -1) {
                return
            } else {
                lastFrameLine.substring(index + 1).toLongOrNull() ?: return
            }
            val lastImuFile = videoFile.parentFile?.parentFile?.listFiles { folder ->
                folder.isDirectory && folder.listFiles()?.any { file ->
                    file.name.contains("imu")
                } == true
            }?.maxByOrNull {
                it.name
            }?.listFiles()?.first {
                it.name.contains("imu")
            }?.lastLine ?: return
            index = lastImuFile.indexOf(",")
            val lastImuTimeStamp = if (index == -1) {
                return
            } else {
                lastImuFile.substring(0, index).toLongOrNull() ?: return
            }

            val timestampDifference = (lastFrameTimestamp - lastImuTimeStamp) / 1_000_000_000
            updateState { state ->
                state.copy(
                    timestampDifference = timestampDifference,
                    latestImuTimestamp = lastImuTimeStamp / 1_000_000,
                    latestFrameTimestamp = lastFrameTimestamp / 1_000_000,
                    showPleaseWait = timestampDifference >= 1
                )
            }
        }
    }

    private fun updateState(update: (RecordingViewState) -> RecordingViewState) {
        _state.update(update)
    }

    fun clearEffect() {
        effect.postValue(RecordingViewEffect.DoNothing)
    }

    override fun onCleared() {
        cameraService.stopCamera()
        super.onCleared()
    }
}