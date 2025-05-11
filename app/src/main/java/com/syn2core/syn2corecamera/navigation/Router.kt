package com.syn2core.syn2corecamera.navigation

interface Router {
    fun goRecording()
    fun goPermission()
    fun goShowByChart()
    fun goSetting()
    fun goBack(startDestination: String)
}