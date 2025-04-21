package com.appleader707.syncrecorder.navigation

interface Router {
    fun goRecording()
    fun goPermission()
    fun goBack(startDestination: String)
}