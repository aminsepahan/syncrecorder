package com.appleader707.syncrecorder.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.appleader707.syncrecorder.presentation.ui.permission.PermissionScreen
import com.appleader707.syncrecorder.presentation.ui.recording.RecordingScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavigationContainer(
    router: Router,
    navController: NavHostController,
) {
    val startDestination = remember { mutableStateOf(Screen.Recording.route) }
    LaunchedEffect(startDestination) {
        if (startDestination.value == Screen.Recording.route) {
            router.goRecording()
        }
    }
    NavHost(
        navController = navController,
        startDestination = startDestination.value,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable(Screen.Recording.route) {
            RecordingScreen(router)
        }
        composable(Screen.Permission.route) {
            PermissionScreen(router)
        }
    }
}