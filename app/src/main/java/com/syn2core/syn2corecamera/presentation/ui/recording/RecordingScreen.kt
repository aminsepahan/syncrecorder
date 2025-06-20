package com.syn2core.syn2corecamera.presentation.ui.recording

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component3
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component4
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component5
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asFlow
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.syn2core.syn2corecamera.extension.showMessage
import com.syn2core.syn2corecamera.navigation.Router
import com.syn2core.syn2corecamera.presentation.components.CameraView
import com.syn2core.syn2corecamera.presentation.components.DropdownSelector
import com.syn2core.syn2corecamera.presentation.components.KeepScreenOn
import com.syn2core.syn2corecamera.presentation.theme.DarkGray
import com.syn2core.syn2corecamera.presentation.theme.ErrorColor
import com.syn2core.syn2corecamera.presentation.theme.ErrorColorAlpha
import com.syn2core.syn2corecamera.presentation.theme.ErrorDark
import kotlinx.coroutines.delay

@Composable
fun RecordingScreen(
    router: Router? = null,
    viewModel: RecordingViewModel = hiltViewModel()
) {
    KeepScreenOn()

    val viewState by viewModel.state.collectAsState()
    val viewEffect by viewModel.effect.asFlow().collectAsState(RecordingViewEffect.DoNothing)
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.processEvent(RecordingViewEvent.LoadSettings)
    }
    LaunchedEffect(lifecycleState) {
        // Do something with your state
        // You may want to use DisposableEffect or other alternatives
        // instead of LaunchedEffect
        when (lifecycleState) {
            Lifecycle.State.DESTROYED -> {}
            Lifecycle.State.INITIALIZED -> {}
            Lifecycle.State.CREATED -> {}
            Lifecycle.State.STARTED -> {}
            Lifecycle.State.RESUMED -> {}
        }
    }

    LaunchedEffect(viewEffect) {
        when (viewEffect) {
            RecordingViewEffect.DoNothing -> Unit
            RecordingViewEffect.RecordingStarted -> showMessage("Recording started.")
            RecordingViewEffect.RecordingStopped -> showMessage("Recording saved.")
            RecordingViewEffect.NavigateToSetting -> router?.goSetting()
        }
        viewModel.clearEffect()
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
        var shouldShowRecordingStopDialog by remember { mutableStateOf(false) }
        LaunchedEffect(shouldShowRecordingStopDialog) {
            if (shouldShowRecordingStopDialog) {
                delay(10000)
                shouldShowRecordingStopDialog = false
            }
        }
        CameraView(
            modifier = Modifier.fillMaxSize(),
            resolution = resolution,
            onSurfaceReady = {
                viewModel.updateSurface(it)
            }
        )

        RecordingScreenButtonsAndUi(
            viewState = viewState,
            onEventHandler = onEventHandler,
            onRecordButtonClick = {
                if (viewState.isRecording) {
                    shouldShowRecordingStopDialog = true
                } else {
                    viewModel.getSurface()?.let { surface ->
                        onEventHandler(
                            RecordingViewEvent.ToggleRecording(
                                context = context,
                                cameraSurface = surface
                            )
                        )
                    }
                }
            },
            onResolutionSet = {
                viewModel.processEvent(
                    RecordingViewEvent.SetResolution(
                        viewState.settingsState.copy(
                            resolution = it
                        )
                    )
                )
            },
            shouldShowDialog = shouldShowRecordingStopDialog,
            onDismissRequest = { shouldShowRecordingStopDialog = false }, onConfirmation = {
                shouldShowRecordingStopDialog = false
                viewModel.getSurface()?.let { surface ->
                    onEventHandler(
                        RecordingViewEvent.ToggleRecording(
                            context = context,
                            cameraSurface = surface
                        )
                    )
                }
            }
        )
    }
}

@Composable
private fun RecordingScreenButtonsAndUi(
    viewState: RecordingViewState,
    onEventHandler: (RecordingViewEvent) -> Unit,
    onRecordButtonClick: () -> Unit = {},
    onResolutionSet: (String) -> Unit = {},
    shouldShowDialog: Boolean,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit
) {
    val (focusRequesterRecord, focusRequesterSettings, focusRequesterResolution, dialogCancel, dialogConfirm) = remember { FocusRequester.createRefs() }
    val focusedItem = remember { mutableStateOf(Item.Record) }
    LaunchedEffect(Unit) {
        focusRequesterRecord.requestFocus()
    }
    Box(modifier = Modifier.fillMaxSize()) {
        SettingsPreview(viewState = viewState)
        DurationDisplay(
            modifier = Modifier.align(Alignment.TopCenter),
            viewState = viewState
        )

        if (viewState.isRecording.not()) {
            DropdownSelector(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .padding(bottom = 10.dp)
                    .width(150.dp)
                    .focusRequester(focusRequesterResolution)
                    .focusProperties {
                        this.next = focusRequesterSettings
                        this.previous = focusRequesterRecord
                    }
                    .onFocusChanged { focusedItem.value = Item.Resolution }
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                DarkGray,
                                DarkGray
                            )
                        ),
                        alpha = 0.8f,
                        shape = RoundedCornerShape(corner = CornerSize(10.dp))
                    )
                    .padding(10.dp),
                label = "Resolution",
                options = listOf("480p", "720p", "1080p"),
                selectedOption = viewState.settingsState.resolution,
            ) {
                onResolutionSet(it)
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 26.dp, bottom = 26.dp)
        ) {
            if (viewState.isRecording.not()) {
                FocusableIconButton(
                    icon = Icons.Default.Settings,
                    focusRequester = focusRequesterSettings,
                    next = focusRequesterRecord,
                    previous = focusRequesterRecord,
                    focusedItem = focusedItem,
                    item = Item.Setting,
                    backgroundColor = DarkGray,
                    onClick = { onEventHandler(RecordingViewEvent.NavigateToSettings) }
                )
            }

            Spacer(Modifier.width(10.dp))

            FocusableIconButton(
                icon = if (viewState.isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                focusRequester = focusRequesterRecord,
                next = focusRequesterSettings,
                previous = focusRequesterSettings,
                focusedItem = focusedItem,
                item = Item.Record,
                backgroundColor = ErrorColor
            ) {
                onRecordButtonClick()
            }

            if (viewState.isRecording && viewState.segmentCount > 0) {
                SegmentCountBadge(viewState.segmentCount)
            }
            if (viewState.showPleaseWait) {
                ImuWritingBadge(text = "Please wait")
                ImuWritingBadge(text = "${viewState.timestampDifference}")
            }
        }
    }
    if (shouldShowDialog) {
        ConfirmationDialog(
            onDismissRequest = onDismissRequest,
            onConfirmation = onConfirmation
        )
    }
}

@Composable
fun BoxScope.SettingsPreview(viewState: RecordingViewState) {
    Column(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(16.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DarkGray,
                        DarkGray
                    )
                ),
                alpha = 0.8f,
                shape = RoundedCornerShape(corner = CornerSize(3.dp))
            )
            .padding(5.dp),
    ) {
        Text(
            text = "Autosave intervals: ${viewState.settingsState.autoStopMinutes}",
            style = MaterialTheme.typography.bodySmall,
            color = White
        )
        Text(
            text = "Audio src: ${viewState.settingsState.audioSource}",
            style = MaterialTheme.typography.bodySmall,
            color = White
        )
        Text(
            text = "Rsolution: ${viewState.settingsState.resolution}",
            style = MaterialTheme.typography.bodySmall,
            color = White
        )
        Text(
            text = "IMU frequency: ${viewState.settingsState.imuFrequency}",
            style = MaterialTheme.typography.bodySmall,
            color = White
        )
        Text(
            text = "framerate: ${viewState.settingsState.frameRate}",
            style = MaterialTheme.typography.bodySmall,
            color = White
        )
        if (viewState.latestFrameTimestamp > 0L) {
            Text(
                text = "last frame ts: ${viewState.latestFrameTimestamp}",
                style = MaterialTheme.typography.bodySmall,
                color = White
            )
        }
        if (viewState.latestImuTimestamp > 0L) {
            Text(
                text = "last IMU ts: ${viewState.latestImuTimestamp}",
                style = MaterialTheme.typography.bodySmall,
                color = White
            )
        }
    }
}

@Composable
private fun DurationDisplay(
    modifier: Modifier,
    viewState: RecordingViewState
) {
    Text(
        text = viewState.formattedDuration,
        color = White,
        style = MaterialTheme.typography.headlineMedium,
        modifier = modifier
            .padding(top = 20.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        if (viewState.isRecording) ErrorColor else ErrorColorAlpha,
                        DarkGray
                    )
                ),
                alpha = 0.6f,
                shape = RoundedCornerShape(5.dp)
            )
            .padding(5.dp)
    )
}

@Composable
private fun FocusableIconButton(
    icon: ImageVector,
    focusRequester: FocusRequester,
    next: FocusRequester,
    previous: FocusRequester,
    focusedItem: MutableState<Item>,
    item: Item,
    backgroundColor: Color,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .focusRequester(focusRequester)
            .focusProperties {
                this.next = next
                this.previous = previous
            }
            .onFocusChanged { focusedItem.value = item }
            .border(
                width = if (focusedItem.value == item) 8.dp else 2.dp,
                color = White,
                shape = CircleShape,
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = White,
            modifier = Modifier.size(25.dp)
        )
    }
}

@Composable
private fun SegmentCountBadge(count: Int) {
    Spacer(Modifier.width(2.dp))
    Text(
        text = count.toString(),
        color = White,
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .height(60.dp)
            .padding(vertical = 10.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ErrorColor,
                        ErrorDark
                    )
                ),
                alpha = 0.8f,
                shape = RoundedCornerShape(corner = CornerSize(3.dp))
            )
            .padding(8.dp),
    )
}

@Composable
fun ConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit
) {
    AlertDialog(
        title = {
            Text(text = "Stop recording?")
        },
        text = {
            Text(text = "Are you sure you want to stop recording?")
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Stop Recording")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ImuWritingBadge(text: String) {
    Spacer(Modifier.width(2.dp))
    Text(
        text = text,
        color = White,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .height(60.dp)
            .padding(vertical = 10.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DarkGray,
                        DarkGray
                    )
                ),
                alpha = 0.8f,
                shape = RoundedCornerShape(corner = CornerSize(3.dp))
            )
            .padding(8.dp),
    )
}

@Preview(heightDp = 360, widthDp = 640)
@Composable
fun RecordScreenPreview() {
    RecordingScreenButtonsAndUi(
        viewState = RecordingViewState(),
        onEventHandler = {},
        shouldShowDialog = false,
        onDismissRequest = {},
        onConfirmation = {}
    )
}

@Preview(heightDp = 360, widthDp = 640)
@Composable
fun RecordScreenPreviewWithDialog() {
    RecordingScreenButtonsAndUi(
        viewState = RecordingViewState(),
        onEventHandler = {},
        shouldShowDialog = true,
        onDismissRequest = {},
        onConfirmation = {}
    )
}

@Preview(heightDp = 360, widthDp = 640)
@Composable
fun RecordScreenPreviewRecrding() {
    RecordingScreenButtonsAndUi(
        viewState = RecordingViewState().copy(
            isRecording = true,
            durationMillis = 125000,
            segmentCount = 3
        ),
        onEventHandler = {},
        shouldShowDialog = false,
        onDismissRequest = {},
        onConfirmation = {}
    )
}

enum class Item {
    Record,
    Setting,
    Resolution,
    DialogCancel,
    DialogConfirm
}