package com.syn2core.syn2corecamera.presentation.ui.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import com.syn2core.syn2corecamera.domain.RecordingSettings
import com.syn2core.syn2corecamera.navigation.Router
import com.syn2core.syn2corecamera.navigation.Screen
import com.syn2core.syn2corecamera.presentation.components.DropdownSelector

@Composable
fun SettingScreen(
    router: Router? = null,
    viewModel: SettingViewModel = hiltViewModel()
) {
    val viewState by viewModel.state.collectAsState()
    val viewEffect by viewModel.effect.asFlow().collectAsState(SettingViewEffect.DoNothing)

    LaunchedEffect(viewEffect) {
        when (viewEffect) {
            SettingViewEffect.DoNothing -> {}
            SettingViewEffect.GoBackRecordingPage -> {
                router?.goBack(Screen.Recording.route)
            }
        }
        viewModel.clearEffect()
    }

    LaunchedEffect(Unit) {
        viewModel.processEvent(SettingViewEvent.LoadData)
    }

    SettingLayout(
        viewState = viewState,
        onEventHandler = viewModel::processEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingLayout(
    viewState: SettingViewState,
    onEventHandler: (SettingViewEvent) -> Unit
) {
    var resolution by remember(viewState.settingsState.resolution) { mutableStateOf(viewState.settingsState.resolution) }
    var frameRate by remember(viewState.settingsState.frameRate) { mutableIntStateOf(viewState.settingsState.frameRate) }
    var codec by remember(viewState.settingsState.codec) { mutableStateOf(viewState.settingsState.codec) }
    var autoFocus by remember(viewState.settingsState.autoFocus) { mutableStateOf(viewState.settingsState.autoFocus) }
    var stabilization by remember(viewState.settingsState.stabilization) { mutableStateOf(viewState.settingsState.stabilization) }
    var audioSource by remember(viewState.settingsState.audioSource) { mutableStateOf(viewState.settingsState.audioSource) }
    var imuFrequency by remember(viewState.settingsState.imuFrequency) { mutableIntStateOf(viewState.settingsState.imuFrequency) }
    var autoStopMinutes by remember(viewState.settingsState.autoStopMinutes) {
        mutableIntStateOf(
            viewState.settingsState.autoStopMinutes
        )
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(start = 25.dp, end = 25.dp, top = 25.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    DropdownSelector(
                        label = "Resolution",
                        options = listOf("480p", "720p", "1080p", "4K"),
                        selectedOption = resolution
                    ) {
                        resolution = it
                    }

                    DropdownSelector(
                        label = "Frame Rate",
                        options = listOf(24, 30, 60),
                        selectedOption = frameRate
                    ) {
                        frameRate = it
                    }

                    DropdownSelector(
                        label = "Video Codec",
                        options = listOf("H.264", "HEVC"),
                        selectedOption = codec
                    ) {
                        codec = it
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    DropdownSelector(
                        label = "Audio Source",
                        options = listOf("MIC", "CAMCORDER", "VOICE_RECOGNITION"),
                        selectedOption = audioSource
                    ) {
                        audioSource = it
                    }

                    DropdownSelector(
                        label = "IMU Frequency",
                        options = listOf(10, 50, 100),
                        selectedOption = imuFrequency
                    ) {
                        imuFrequency = it
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    DropdownSelector(
                        label = "IMU Frequency",
                        options = listOf(1,2, 5, 10, 15, 30),
                        selectedOption = autoStopMinutes
                    ) {
                        autoStopMinutes = it
                    }
                    Column(
                        Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Top
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = autoFocus,
                                onCheckedChange = { autoFocus = it }
                            )
                            Text(
                                text = "Auto Focus",
                                fontSize = 16.sp
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = stabilization,
                                onCheckedChange = { stabilization = it })
                            Text(
                                text = "Stabilization",
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Row {
                Button(
                    modifier = Modifier.weight(1f).padding(end = 16.dp),
                    onClick = {
                        onEventHandler(SettingViewEvent.GoBackToRecordingPage)
                    }
                ) {
                    Text("back")
                }

                Button(
                    modifier = Modifier.weight(4f),
                    onClick = {
                        onEventHandler(
                            SettingViewEvent.Save(
                                RecordingSettings(
                                    resolution = resolution,
                                    frameRate = frameRate,
                                    codec = codec,
                                    autoFocus = autoFocus,
                                    stabilization = stabilization,
                                    audioSource = audioSource,
                                    imuFrequency = imuFrequency,
                                    autoStopMinutes = autoStopMinutes
                                )
                            )
                        )
                    }
                ) {
                    Text("Save")
                }
            }
        }
    }
}