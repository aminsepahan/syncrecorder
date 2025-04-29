package com.appleader707.syncrecorder.presentation.ui.recording

import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.appleader707.syncrecorder.extension.Helper
import com.appleader707.syncrecorder.navigation.Router
import com.appleader707.syncrecorder.presentation.components.CameraPreviewView
import com.appleader707.syncrecorder.presentation.components.settings_recording.SettingsBottomSheet

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
                Helper.showMessage("Recording stoped.")
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
        onEventHandler = viewModel::processEvent,
    )
}

@Composable
fun RecordingLayout(
    viewState: RecordingViewState,
    onEventHandler: (RecordingViewEvent) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val surfaceProvider = previewView.surfaceProvider

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreviewView(
            context = context,
            lifecycleOwner = lifecycleOwner,
            previewView = previewView,
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            onClick = { onEventHandler(RecordingViewEvent.ShowSettings) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 37.dp, start = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White
            )
        }

        Text(
            text = viewState.formattedDuration,
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 45.dp)
        )

        IconButton(
            onClick = {
                onEventHandler(
                    RecordingViewEvent.ToggleRecording(
                        context,
                        lifecycleOwner,
                        surfaceProvider
                    )
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 70.dp)
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    if (viewState.isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
        ) {
            Icon(
                imageVector = if (viewState.isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        AnimatedVisibility(viewState.settingsDialogVisible && !viewState.isRecording) {
            SettingsBottomSheet(
                initialSettings = viewState.settingsState,
                onDismiss = { onEventHandler(RecordingViewEvent.HideSettings) },
                onSave = { onEventHandler(RecordingViewEvent.SaveSettings(it)) })
        }
    }
}