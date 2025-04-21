package com.appleader707.syncrecorder.presentation.ui.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import com.appleader707.syncrecorder.navigation.Router

@Composable
fun PermissionScreen(
    router: Router? = null,
    viewModel: PermissionViewModel = hiltViewModel()
) {
    val viewState by viewModel.state.collectAsState()
    val viewEffect by viewModel.effect.asFlow().collectAsState(PermissionViewEffect.DoNothing)

    LaunchedEffect(viewEffect) {
        when (val effect = viewEffect) {
            PermissionViewEffect.DoNothing -> {}
        }

        // Reset effect after handling to prevent re-trigger
        viewModel.clearEffect()
    }

    PermissionLayout(
        viewState = viewState,
        onEventHandler = viewModel::processEvent,
    )
}

@Composable
fun PermissionLayout(
    viewState: PermissionViewState,
    onEventHandler: (PermissionViewEvent) -> Unit,
) {

}