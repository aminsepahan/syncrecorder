package com.appleader707.syncrecorder.presentation.components.settings_recording

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    initialSettings: RecordingSettingsState,
    onDismiss: () -> Unit,
    onSave: (RecordingSettingsState) -> Unit
) {
    var resolution by remember { mutableStateOf(initialSettings.resolution) }
    var frameRate by remember { mutableStateOf(initialSettings.frameRate) }
    var autoFocus by remember { mutableStateOf(initialSettings.autoFocus) }
    var stabilization by remember { mutableStateOf(initialSettings.stabilization) }
    var audioSource by remember { mutableStateOf(initialSettings.audioSource) }
    var imuFrequency by remember { mutableStateOf(initialSettings.imuFrequency) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Settings", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            // Resolution Dropdown
            DropdownSelector("Resolution", listOf("480p" ,"720p", "1080p", "4K"), resolution) { resolution = it }

            // Frame Rate
            DropdownSelector("Frame Rate", listOf(24, 30, 60), frameRate) { frameRate = it }

            // Audio Source
            DropdownSelector("Audio Source", listOf("MIC", "CAMCORDER", "VOICE_RECOGNITION"), audioSource) { audioSource = it }

            // IMU Frequency
            DropdownSelector("IMU Frequency", listOf(10, 50, 100), imuFrequency) { imuFrequency = it }

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
                        RecordingSettingsState(
                            resolution,
                            frameRate,
                            codec = "H.264",
                            autoFocus,
                            stabilization,
                            audioSource,
                            imuFrequency
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