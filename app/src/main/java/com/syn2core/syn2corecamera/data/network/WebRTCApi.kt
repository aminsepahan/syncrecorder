package com.syn2core.syn2corecamera.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import timber.log.Timber
import javax.inject.Inject

val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            }
        )
    }
    install(Logging){
        logger = Logger.DEFAULT
        level = LogLevel.HEADERS
    }
}

class WebRTCApiService @Inject constructor() {

    suspend fun getWebRtcAnswerBasedOnOffer(offer: String, meetingId: String): Sdp {
        val response =  httpClient
            .post("http://10.0.2.2:4001/stream/receive_stream?uuid=$meetingId"){
                contentType()
                contentType(ContentType.Application.Json)
                setBody(offer)
            }
            .body<Sdp>()
        Timber.d(response.toString())
        return response
    }
}