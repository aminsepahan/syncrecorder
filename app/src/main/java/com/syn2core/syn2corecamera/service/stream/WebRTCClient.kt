package com.syn2core.syn2corecamera.service.stream

import android.content.Context
import android.view.Surface
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnection.Observer
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import timber.log.Timber
import javax.inject.Inject

class WebRTCClient @Inject constructor() {
    private lateinit var eglBase: EglBase
    private lateinit var factory: PeerConnectionFactory
    private lateinit var observer: Observer
    private var remoteSessionDescription : SessionDescription? = null
    lateinit var surfaceTextureHelper: SurfaceTextureHelper
    lateinit var videoSource: VideoSource
    lateinit var videoTrack: VideoTrack
    lateinit var webrtcInputSurface: Surface

    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
            .createIceServer()
    )
    private val peerConnection by lazy { buildPeerConnection(observer) }
    val timber = Timber.tag("WebRTC Client")
    fun initialize(context: Context, observer: Observer) {
        this.observer = observer
        eglBase = EglBase.create()
        val eglBaseContext = eglBase.eglBaseContext
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )


        factory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBaseContext, true, true))
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBaseContext))
            .createPeerConnectionFactory()

    }

    private fun PeerConnection.call(sdpObserver: SdpObserver, meetingID: String) {
        val mediaStreamLabels = listOf("ARDAMS")
        peerConnection?.addTrack(videoTrack, mediaStreamLabels)

        createOffer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                setLocalDescription(this, desc)
                sdpObserver.onCreateSuccess(desc)
                timber.d("onCreateSuccess: Description $desc")
            }

            override fun onSetFailure(p0: String?) {
                timber.e("onSetFailure: $p0")
            }

            override fun onCreateFailure(p0: String?) {
                timber.e("onCreateFailure: $p0")
            }
        }, MediaConstraints().apply {
            optional.add(MediaConstraints.KeyValuePair("TrickleIce", "false"))
        })
    }

    private fun PeerConnection.answer(sdpObserver: SdpObserver, meetingID: String) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }
        createAnswer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                val answer = hashMapOf(
                    "sdp" to desc?.description,
                    "type" to desc?.type
                )
                setLocalDescription(object : SdpObserver {
                    override fun onSetFailure(p0: String?) {
                        timber.e("onSetFailure: $p0")
                    }

                    override fun onSetSuccess() {
                        timber.e("onSetSuccess")
                    }

                    override fun onCreateSuccess(p0: SessionDescription?) {
                        timber.e("onCreateSuccess: Description $p0")
                    }

                    override fun onCreateFailure(p0: String?) {
                        timber.e("onCreateFailureLocal: $p0")
                    }
                }, desc)
                sdpObserver.onCreateSuccess(desc)
            }

            override fun onCreateFailure(p0: String?) {
                timber.e("onCreateFailureRemote: $p0")
            }
        }, constraints)
    }

    fun call(sdpObserver: SdpObserver, meetingID: String) =
        peerConnection?.call(sdpObserver, meetingID)

    fun answer(sdpObserver: SdpObserver, meetingID: String) =
        peerConnection?.answer(sdpObserver, meetingID)

    fun onRemoteSessionReceived(sessionDescription: SessionDescription) {
        remoteSessionDescription = sessionDescription
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetFailure(p0: String?) {
                timber.d("onSetFailure: $p0")
            }

            override fun onSetSuccess() {
                timber.d("onSetSuccessRemoteSession")
            }

            override fun onCreateSuccess(p0: SessionDescription?) {
                timber.d("onCreateSuccessRemoteSession: Description $p0")
            }

            override fun onCreateFailure(p0: String?) {
                timber.e("onCreateFailure : $p0")
            }
        }, sessionDescription)

    }

    fun addIceCandidate(iceCandidate: IceCandidate?) {
        peerConnection?.addIceCandidate(iceCandidate)
    }

    private fun buildPeerConnection(observer: Observer) =
        factory.createPeerConnection(
            iceServers,
            observer
        )

    fun createWebRTCSurface(width: Int = 720, height: Int = 480): Surface {
        surfaceTextureHelper =
            SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)

        videoSource = factory.createVideoSource(false)
        videoSource.adaptOutputFormat(width, height, 30)

        val captureObserver = videoSource.capturerObserver

        // Start listening and forward frames to WebRTC
        surfaceTextureHelper.startListening { frame ->
            captureObserver.onFrameCaptured(frame)
        }

        val surfaceTexture = surfaceTextureHelper.surfaceTexture
        surfaceTexture.setDefaultBufferSize(width, height)
        webrtcInputSurface = Surface(surfaceTexture)

        // Create video track to attach to peer connection later
        videoTrack = factory.createVideoTrack("video", videoSource)

        return webrtcInputSurface
    }

    fun release(){
        webrtcInputSurface.release()
        peerConnection?.close()
    }
}