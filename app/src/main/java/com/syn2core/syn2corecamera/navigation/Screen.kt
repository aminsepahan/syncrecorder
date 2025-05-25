package com.syn2core.syn2corecamera.navigation

import com.syn2core.syn2corecamera.navigation.Routes.ROUTE_PERMISSION
import com.syn2core.syn2corecamera.navigation.Routes.ROUTE_RECORDING
import com.syn2core.syn2corecamera.navigation.Routes.ROUTE_SETTING

object Routes {
    const val ROUTE_RECORDING = "ROUTE_RECORDING"
    const val ROUTE_PERMISSION = "ROUTE_PERMISSION"
    const val ROUTE_SETTING = "ROUTE_SETTING"
}

sealed class Screen(
    val route: String,
) {
    object Recording : Screen(ROUTE_RECORDING)
    object Permission : Screen(ROUTE_PERMISSION)
    object Setting : Screen(ROUTE_SETTING)
}