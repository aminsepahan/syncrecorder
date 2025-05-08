package com.syn2core.syn2corecamera.presentation.ui.main

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.syn2core.syn2corecamera.navigation.NavigationContainer
import com.syn2core.syn2corecamera.navigation.Router
import com.syn2core.syn2corecamera.navigation.RouterImpl
import com.syn2core.syn2corecamera.navigation.Screen

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(finish: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val route = navBackStackEntry?.destination?.route ?: Screen.Recording.route
    val router: Router = remember { RouterImpl(navController) }

    when (route) {
        Screen.Recording.route -> {
            BackHandler {
                finish()
            }
        }
    }
    Scaffold { innerPadding ->
        NavigationContainer(
            modifier = Modifier.padding(innerPadding),
            router = router,
            navController = navController
        )
    }
}