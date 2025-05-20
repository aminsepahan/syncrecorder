package com.syn2core.syn2corecamera.presentation.ui.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.input.KeyboardType
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
        when (val effect = viewEffect) {
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
    var autoStopMinutes by remember(viewState.settingsState.autoStopMinutes) { mutableIntStateOf(viewState.settingsState.autoStopMinutes) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = {
                        onEventHandler(SettingViewEvent.GoBackToRecordingPage)
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(start = 40.dp, end = 40.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    DropdownSelector("Resolution", listOf("480p", "720p", "1080p", "4K"), resolution) {
                        resolution = it
                    }

                    DropdownSelector("Frame Rate", listOf(24, 30, 60), frameRate) {
                        frameRate = it
                    }

                    DropdownSelector("Video Codec", listOf("H.264", "HEVC"), codec) {
                        codec = it
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    DropdownSelector("Audio Source", listOf("MIC", "CAMCORDER", "VOICE_RECOGNITION"), audioSource) {
                        audioSource = it
                    }

                    DropdownSelector("IMU Frequency", listOf(10, 50, 100), imuFrequency) {
                        imuFrequency = it
                    }

                    Row(
                        Modifier.fillMaxWidth().padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = autoFocus, onCheckedChange = { autoFocus = it })
                            Text("Auto Focus", fontSize = 16.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = stabilization, onCheckedChange = { stabilization = it })
                            Text("Stabilization", fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = autoStopMinutes.toString(),
                        onValueChange = {
                            autoStopMinutes = it.toIntOrNull() ?: 0
                        },
                        label = { Text("Auto Stop (minutes)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
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