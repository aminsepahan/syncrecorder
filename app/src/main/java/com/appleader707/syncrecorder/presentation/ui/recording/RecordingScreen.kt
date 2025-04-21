package com.appleader707.syncrecorder.presentation.ui.recording

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import com.appleader707.syncrecorder.navigation.Router

@Composable
fun RecordingScreen(
    router: Router? = null,
    viewModel: RecordingViewModel = hiltViewModel()
) {
    val viewState by viewModel.state.collectAsState()
    val viewEffect by viewModel.effect.asFlow().collectAsState(RecordingViewEffect.DoNothing)

    LaunchedEffect(viewEffect) {
        when (val effect = viewEffect) {
            RecordingViewEffect.DoNothing -> {}
        }

        // Reset effect after handling to prevent re-trigger
        viewModel.clearEffect()
    }

    RecordingLayout(
        viewState = viewState,
        onEventHandler = viewModel::processEvent,
    )
}

@Composable
fun RecordingLayout(
    viewState: RecordingViewState,
    onEventHandler: (RecordingViewEvent) -> Unit,
) {

}