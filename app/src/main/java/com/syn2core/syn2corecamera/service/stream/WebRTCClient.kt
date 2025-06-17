package com.syn2core.syn2corecamera.service.stream

import android.content.Context
import android.view.Surface
import com.syn2core.syn2corecamera.data.network.Sdp
import kotlinx.serialization.json.Json
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import timber.log.Timber

class WebRTCClient(
    observer: PeerConnection.Observer
) {
    private lateinit var webrtcInputSurface: Surface
    private lateinit var eglBase: EglBase
    private lateinit var factory: PeerConnectionFactory
    var remoteSessionDescription : SessionDescription? = null

    private val iceServer = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
            .createIceServer()
    )
    private val peerConnection by lazy { buildPeerConnection(observer) }
    val timber = Timber.tag("WebRTC Client")
    fun initialize(context: Context) {
        eglBase = EglBase.create()
        val eglBaseContext = eglBase.eglBaseContext
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .createInitializationOptions()
        )

        factory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBaseContext, true, true))
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBaseContext))
            .createPeerConnectionFactory()

    }

    private fun PeerConnection.call(sdpObserver: SdpObserver, meetingID: String) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }

        createOffer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                setLocalDescription(object : SdpObserver {
                    override fun onSetFailure(p0: String?) {
                        timber.e("onSetFailure: $p0")
                    }

                    override fun onSetSuccess() {
                        val sdp = Sdp(
                            type = desc?.type?.name,
                            sdp = desc?.description
                        )
                        val json = Json.encodeToString(sdp)
                        timber.d("onSetSuccess $json")
                        var remotesdp = ""
                        onRemoteSessionReceived(SessionDescription(SessionDescription.Type.ANSWER, remotesdp))
                    }

                    override fun onCreateSuccess(p0: SessionDescription?) {
                        timber.e("onCreateSuccess: Description $p0")
                    }

                    override fun onCreateFailure(p0: String?) {
                        timber.e("onCreateFailure: $p0")
                    }
                }, desc)
                sdpObserver.onCreateSuccess(desc)
                timber.d("onCreateSuccess: Description $desc")
            }

            override fun onSetFailure(p0: String?) {
                timber.e("onSetFailure: $p0")
            }

            override fun onCreateFailure(p0: String?) {
                timber.e("onCreateFailure: $p0")
            }
        }, constraints)
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

    private fun buildPeerConnection(observer: PeerConnection.Observer) =
        factory.createPeerConnection(
            iceServer,
            observer
        )

    fun createWebRTCSurface(width: Int = 720, height: Int = 480): Surface {
        val videoSource = factory.createVideoSource(false)
        val surfaceTextureHelper =
            SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)

        val webrtcSurfaceTexture = surfaceTextureHelper.surfaceTexture
        webrtcSurfaceTexture.setDefaultBufferSize(720, 480)

        videoSource.adaptOutputFormat(width, height, 30)
        webrtcInputSurface = Surface(webrtcSurfaceTexture)
        return webrtcInputSurface
    }

    fun release(){
        webrtcInputSurface.release()
    }
}