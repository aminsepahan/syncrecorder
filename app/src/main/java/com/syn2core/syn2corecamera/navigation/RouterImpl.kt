package com.syn2core.syn2corecamera.navigation

import androidx.navigation.NavHostController

class RouterImpl(
    private val navHostController: NavHostController,
) : Router {

    override fun goRecording() {
        navigate(Screen.Recording, removeFromHistory = true, singleTop = true)
    }

    override fun goPermission() {
        navigate(Screen.Permission)
    }

    override fun goSetting() {
        navigate(Screen.Setting)
    }

    override fun goBack(startDestination: String) {
        val popped = navHostController.popBackStack()
        if (!popped) {
            navHostController.navigate(startDestination) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    private fun navigate(
        leafScreen: Screen,
        removeFromHistory: Boolean = false,
        singleTop: Boolean = false
    ) {
        navHostController.apply {
            navigate(leafScreen.route) {
                if (removeFromHistory) {
                    if (singleTop) {
                        popUpTo(Screen.Recording.route)
                    } else {
                        popUpTo(0) {
                            saveState = false
                        }
                    }

                } else {
                    restoreState = true
                }
                launchSingleTop = singleTop
            }
        }
    }
}