package com.appleader707.syncrecorder.navigation

import com.appleader707.syncrecorder.navigation.Routes.ROUTE_PERMISSION
import com.appleader707.syncrecorder.navigation.Routes.ROUTE_RECORDING
import com.appleader707.syncrecorder.navigation.Routes.ROUTE_SHOW_BY_CHART

object Routes {
    const val ROUTE_RECORDING = "ROUTE_RECORDING"
    const val ROUTE_PERMISSION = "ROUTE_PERMISSION"
    const val ROUTE_SHOW_BY_CHART = "ROUTE_SHOW_BY_CHART"
}

sealed class Screen(
    val route: String,
    var tag: String = route,
) {
    object Recording : Screen(ROUTE_RECORDING)
    object Permission : Screen(ROUTE_PERMISSION)
    object ShowByChart : Screen(ROUTE_SHOW_BY_CHART)
}