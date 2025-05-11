package com.syn2core.syn2corecamera.presentation.ui.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
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
    onEventHandler: (SettingViewEvent) -> Unit,
) {
    var resolution by remember { mutableStateOf(viewState.settingsState.resolution) }
    var frameRate by remember { mutableIntStateOf(viewState.settingsState.frameRate) }
    var codec by remember { mutableStateOf(viewState.settingsState.codec) }
    var autoFocus by remember { mutableStateOf(viewState.settingsState.autoFocus) }
    var stabilization by remember { mutableStateOf(viewState.settingsState.stabilization) }
    var audioSource by remember { mutableStateOf(viewState.settingsState.audioSource) }
    var imuFrequency by remember { mutableIntStateOf(viewState.settingsState.imuFrequency) }

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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Video Settings", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            DropdownSelector("Resolution", listOf("480p", "720p", "1080p", "4K"), resolution) {
                resolution = it
            }

            DropdownSelector("Frame Rate", listOf(24, 30, 60), frameRate) {
                frameRate = it
            }

            DropdownSelector("Video Codec", listOf("H.264", "HEVC"), codec) {
                codec = it
            }

            DropdownSelector(
                "Audio Source",
                listOf("MIC", "CAMCORDER", "VOICE_RECOGNITION"),
                audioSource
            ) {
                audioSource = it
            }

            DropdownSelector("IMU Frequency", listOf(10, 50, 100), imuFrequency) {
                imuFrequency = it
            }

            Spacer(Modifier.height(8.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = autoFocus, onCheckedChange = { autoFocus = it })
                    Text("Auto Focus")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = stabilization, onCheckedChange = { stabilization = it })
                    Text("Stabilization")
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val settings = RecordingSettings(
                        resolution,
                        frameRate,
                        codec,
                        autoFocus,
                        stabilization,
                        audioSource,
                        imuFrequency
                    )
                    onEventHandler(SettingViewEvent.Save(settings))
                }
            ) {
                Text("Save")
            }
        }
    }
}