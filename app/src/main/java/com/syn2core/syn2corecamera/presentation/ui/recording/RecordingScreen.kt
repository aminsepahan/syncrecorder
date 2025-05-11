package com.syn2core.syn2corecamera.presentation.ui.recording

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AreaChart
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import com.syn2core.syn2corecamera.extension.Helper
import com.syn2core.syn2corecamera.navigation.Router
import com.syn2core.syn2corecamera.presentation.components.CameraView
import com.syn2core.syn2corecamera.presentation.components.SavingOverlay
import com.syn2core.syn2corecamera.presentation.components.settings_recording.SettingsBottomSheet

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
            RecordingViewEffect.RecordingStarted -> {
                Helper.showMessage("Recording started.")
            }

            RecordingViewEffect.RecordingStopped -> {
                Helper.showMessage("Recording saved.")
            }

            RecordingViewEffect.NavigateToShowByChart -> {
                router?.goShowByChart()
            }
        }

        // Reset effect after handling to prevent re-trigger
        viewModel.clearEffect()
    }

    LaunchedEffect(Unit) {
        viewModel.processEvent(RecordingViewEvent.LoadSettings)
    }

    RecordingLayout(
        viewState = viewState,
        viewModel = viewModel,
        onEventHandler = viewModel::processEvent,
    )
}

@Composable
fun RecordingLayout(
    viewState: RecordingViewState,
    viewModel: RecordingViewModel,
    onEventHandler: (RecordingViewEvent) -> Unit,
) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        val resolution = viewState.settingsState.getResolutionSize()

        key(resolution) {
            CameraView(
                modifier = Modifier.fillMaxSize(),
                resolution = resolution,
                onSurfaceReady = {
                    viewModel.updateSurface(it)
                }
            )
        }

        Text(
            text = viewState.formattedDuration,
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 20.dp)
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 26.dp, bottom = 26.dp)
        ) {
            IconButton(
                onClick = { onEventHandler(RecordingViewEvent.ShowSettings) },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            IconButton(
                onClick = {
                    if (!viewState.isRecording) {
                        onEventHandler(RecordingViewEvent.NavigateToShowByChart)
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.AreaChart,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            IconButton(
                onClick = {
                    viewModel.getSurface()?.let { surface ->
                        onEventHandler(
                            RecordingViewEvent.ToggleRecording(
                                context = context,
                                cameraSurface = surface
                            )
                        )
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (viewState.isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
            ) {
                Icon(
                    imageVector = if (viewState.isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        AnimatedVisibility(viewState.settingsDialogVisible && !viewState.isRecording) {
            SettingsBottomSheet(
                initialSettings = viewState.settingsState,
                onDismiss = { onEventHandler(RecordingViewEvent.HideSettings) },
                onSave = { onEventHandler(RecordingViewEvent.SaveSettings(it)) })
        }

        AnimatedVisibility(
            visible = viewState.isSaving,
            enter = fadeIn(animationSpec = tween(600)) + scaleIn(initialScale = 0.8f),
            exit = fadeOut(animationSpec = tween(600)) + scaleOut(targetScale = 0.8f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                SavingOverlay(
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}