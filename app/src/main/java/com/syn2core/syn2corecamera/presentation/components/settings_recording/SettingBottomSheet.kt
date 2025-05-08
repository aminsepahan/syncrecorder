package com.syn2core.syn2corecamera.presentation.components.settings_recording

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.syn2core.syn2corecamera.domain.RecordingSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    initialSettings: RecordingSettings,
    onDismiss: () -> Unit,
    onSave: (RecordingSettings) -> Unit
) {
    var resolution by remember { mutableStateOf(initialSettings.resolution) }
    var frameRate by remember { mutableIntStateOf(initialSettings.frameRate) }
    var codec by remember { mutableStateOf(initialSettings.codec) }
    var autoFocus by remember { mutableStateOf(initialSettings.autoFocus) }
    var stabilization by remember { mutableStateOf(initialSettings.stabilization) }
    var audioSource by remember { mutableStateOf(initialSettings.audioSource) }
    var imuFrequency by remember { mutableIntStateOf(initialSettings.imuFrequency) }

    val windowHeight = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.height.toDp()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .heightIn(max = windowHeight * 0.8f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Settings", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            // Resolution Dropdown
            DropdownSelector(
                label = "Resolution",
                options = listOf("480p", "720p", "1080p", "4K"),
                selectedOption = resolution
            ) { resolution = it }

            // Frame Rate
            DropdownSelector(
                label = "Frame Rate",
                options = listOf(24, 30, 60),
                selectedOption = frameRate
            ) { frameRate = it }

            // Codec Dropdown
            DropdownSelector(
                label = "Video Codec",
                options = listOf("H.264", "HEVC"),
                selectedOption = codec
            ) { codec = it }

            // Audio Source
            DropdownSelector(
                label = "Audio Source",
                options = listOf("MIC", "CAMCORDER", "VOICE_RECOGNITION"),
                selectedOption = audioSource
            ) { audioSource = it }

            // IMU Frequency
            DropdownSelector(
                label = "IMU Frequency",
                options = listOf(10, 50, 100),
                selectedOption = imuFrequency
            ) {
                imuFrequency = it
            }

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
                    onSave(
                        RecordingSettings(
                            resolution = resolution,
                            frameRate = frameRate,
                            codec = codec,
                            autoFocus = autoFocus,
                            stabilization = stabilization,
                            audioSource = audioSource,
                            imuFrequency = imuFrequency
                        )
                    )
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        }
    }
}