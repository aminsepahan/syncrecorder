package com.syn2core.syn2corecamera.data.network

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import org.webrtc.SessionDescription.Type

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class Sdp(val type: String?, val sdp: String?, val id: Int = 0)