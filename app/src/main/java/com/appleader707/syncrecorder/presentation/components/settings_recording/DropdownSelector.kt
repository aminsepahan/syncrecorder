package com.appleader707.syncrecorder.presentation.components.settings_recording

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> DropdownSelector(
    label: String,
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Show Dropdown and selected item
    Column {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
        ) {
            Text(
                text = selectedItem.toString(),
                modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    },
                    text = {
                        Text(item.toString())
                    }
                )
            }
        }
    }
}
