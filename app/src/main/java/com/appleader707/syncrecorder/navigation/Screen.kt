package com.appleader707.syncrecorder.navigation

import com.appleader707.syncrecorder.navigation.Routes.ROUTE_PERMISSION
import com.appleader707.syncrecorder.navigation.Routes.ROUTE_RECORDING

object Routes {
    const val ROUTE_RECORDING = "ROUTE_RECORDING"
    const val ROUTE_PERMISSION = "ROUTE_PERMISSION"
}

sealed class Screen(
    val route: String,
    var tag: String = route,
) {
    object Recording : Screen(ROUTE_RECORDING)
    object Permission : Screen(ROUTE_PERMISSION)
}