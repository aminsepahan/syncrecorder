package com.syn2core.syn2corecamera.presentation.ui.recording

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component3
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import com.syn2core.syn2corecamera.extension.Helper
import com.syn2core.syn2corecamera.navigation.Router
import com.syn2core.syn2corecamera.presentation.components.CameraView
import com.syn2core.syn2corecamera.presentation.components.SavingOverlay

@Composable
fun RecordingScreen(
    router: Router? = null,
    viewModel: RecordingViewModel = hiltViewModel()
) {
    val viewState by viewModel.state.collectAsState()
    val viewEffect by viewModel.effect.asFlow().collectAsState(RecordingViewEffect.DoNothing)
    val (focusRequesterRecord, focusRequesterSettings, focusRequesterChart) = remember { FocusRequester.createRefs() }

    LaunchedEffect(Unit) {
        focusRequesterRecord.requestFocus()
    }

    LaunchedEffect(viewEffect) {
        when (viewEffect) {
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

            RecordingViewEffect.NavigateToSetting -> {
                router?.goSetting()
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
        focusRequesterRecord = focusRequesterRecord,
        focusRequesterChart = focusRequesterChart,
        focusRequesterSettings = focusRequesterSettings
    )
}

@Composable
fun RecordingLayout(
    viewState: RecordingViewState,
    viewModel: RecordingViewModel,
    onEventHandler: (RecordingViewEvent) -> Unit,
    focusRequesterRecord: FocusRequester,
    focusRequesterChart: FocusRequester,
    focusRequesterSettings: FocusRequester,
) {
    val context = LocalContext.current
    val focusedItem = remember { mutableStateOf(Item.Record) }

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
            color = White,
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
                onClick = {
                    onEventHandler(RecordingViewEvent.NavigateToSettings)
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .focusRequester(focusRequesterSettings)
                    .focusProperties {
                        next = focusRequesterChart
                        previous = focusRequesterRecord
                    }
                    .onFocusChanged { focusedItem.value = Item.Setting }
                    .border(
                        width = if (focusedItem.value == Item.Setting) 8.dp else 0.dp,
                        color = White,
                        shape = CircleShape,
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            IconButton(
                onClick = {
                    onEventHandler(RecordingViewEvent.NavigateToShowByChart)
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .focusRequester(focusRequesterChart)
                    .focusProperties {
                        next = focusRequesterRecord
                        previous = focusRequesterSettings
                    }
                    .onFocusChanged { focusedItem.value = Item.Charts }
                    .border(
                        width = if (focusedItem.value == Item.Charts) 8.dp else 0.dp,
                        color = White,
                        shape = CircleShape,
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.AreaChart,
                    contentDescription = null,
                    tint = White,
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
                    .focusRequester(focusRequesterRecord)
                    .focusProperties {
                        next = focusRequesterSettings
                        previous = focusRequesterChart
                    }
                    .onFocusChanged { focusedItem.value = Item.Record }
                    .border(
                        width = if (focusedItem.value == Item.Record) 8.dp else 0.dp,
                        color = White,
                        shape = CircleShape,
                    )
            ) {
                Icon(
                    imageVector = if (viewState.isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = viewState.isSaving || viewState.pendingSaveTasks > 0,
            enter = fadeIn(animationSpec = tween(600)) + scaleIn(initialScale = 0.8f),
            exit = fadeOut(animationSpec = tween(600)) + scaleOut(targetScale = 0.8f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                SavingOverlay(
                    modifier = Modifier.align(Alignment.Center),
                    message = if (viewState.pendingSaveTasks > 0)
                        "Saving ${viewState.pendingSaveTasks} file."
                    else "Saving..."
                )
            }
        }

    }
}

enum class Item {
    Record,
    Setting,
    Charts,
}