package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Artboard
import com.example.ui.theme.ForgeAccentCyan
import com.example.ui.theme.ForgeBorder
import com.example.ui.theme.ForgeBorderLight
import com.example.ui.theme.ForgeError
import com.example.ui.theme.ForgePrimary
import com.example.ui.theme.ForgePrimaryLight
import com.example.ui.theme.ForgeSurface
import com.example.ui.theme.ForgeSurfaceVariant
import com.example.ui.theme.ForgeTextMuted
import com.example.ui.theme.ForgeTextPrimary
import com.example.ui.theme.ForgeTextSecondary

@Composable
fun ArtboardManagerDialog(
    artboards: List<Artboard>,
    activeArtboardId: String,
    onDismiss: () -> Unit,
    onSwitchArtboard: (String) -> Unit,
    onAddArtboard: (name: String, width: Float, height: Float, bgColor: String) -> Unit,
    onSetStartArtboard: (String) -> Unit,
    onRenameArtboard: (String, String) -> Unit,
    onDeleteArtboard: (String) -> Unit
) {
    var isAddingNew by remember { mutableStateOf(false) }
    var newScreenName by remember { mutableStateOf("") }
    var artboardToRename by remember { mutableStateOf<Artboard?>(null) }
    var renameInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, ForgeBorder, RoundedCornerShape(20.dp))
                .testTag("artboard_manager_dialog"),
            color = ForgeSurface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Artboard Screens",
                            style = MaterialTheme.typography.titleLarge,
                            color = ForgeTextPrimary
                        )
                        Text(
                            text = "${artboards.size} screens in this project",
                            style = MaterialTheme.typography.bodySmall,
                            color = ForgeTextSecondary
                        )
                    }

                    Button(
                        onClick = {
                            newScreenName = "Screen ${artboards.size + 1}"
                            isAddingNew = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForgePrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_artboard_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Screen", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = ForgeBorder)
                Spacer(modifier = Modifier.height(14.dp))

                if (isAddingNew) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ForgeSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "New Artboard",
                                style = MaterialTheme.typography.titleSmall,
                                color = ForgeTextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newScreenName,
                                onValueChange = { newScreenName = it },
                                label = { Text("Screen Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ForgePrimary,
                                    unfocusedBorderColor = ForgeBorder,
                                    focusedTextColor = ForgeTextPrimary,
                                    unfocusedTextColor = ForgeTextPrimary
                                )
                            )
                            var selectedPresetIdx by remember { mutableIntStateOf(0) }
                            val devicePresets = remember {
                                listOf(
                                    Triple("Standard", 360f, 640f),
                                    Triple("Modern", 390f, 844f),
                                    Triple("Large", 412f, 915f),
                                    Triple("Tablet", 768f, 1024f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "SCREEN PRESET",
                                style = MaterialTheme.typography.labelSmall,
                                color = ForgeTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                devicePresets.forEachIndexed { idx, (label, w, h) ->
                                    val isSel = idx == selectedPresetIdx
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSel) ForgePrimary else ForgeSurface,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedPresetIdx = idx }
                                            .border(1.dp, if (isSel) ForgePrimary else ForgeBorder, RoundedCornerShape(8.dp))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isSel) Color.White else ForgeTextPrimary
                                            )
                                            Text(
                                                text = "${w.toInt()}×${h.toInt()}",
                                                fontSize = 9.sp,
                                                color = if (isSel) Color.White.copy(alpha = 0.8f) else ForgeTextSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { isAddingNew = false }) {
                                    Text("Cancel", color = ForgeTextSecondary)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        val preset = devicePresets[selectedPresetIdx]
                                        onAddArtboard(newScreenName, preset.second, preset.third, "#0F172A")
                                        isAddingNew = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ForgePrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Create")
                                }
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(artboards, key = { it.id }) { artboard ->
                        val isActive = artboard.id == activeArtboardId

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isActive) ForgeSurfaceVariant else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isActive) ForgePrimary else ForgeBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    onSwitchArtboard(artboard.id)
                                    onDismiss()
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isActive) ForgePrimary else ForgeSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PhoneAndroid,
                                        contentDescription = null,
                                        tint = if (isActive) Color.White else ForgeTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = artboard.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isActive) ForgePrimary else ForgeTextPrimary
                                    )
                                    if (artboard.isStartArtboard) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = ForgePrimary.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "START",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ForgePrimaryLight,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${artboard.elements.size} elements • ${artboard.connections.size} links",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ForgeTextMuted
                                )
                            }

                            // Start Screen Toggle
                            IconButton(
                                onClick = { onSetStartArtboard(artboard.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flag,
                                    contentDescription = "Set as Start Screen",
                                    tint = if (artboard.isStartArtboard) ForgePrimary else ForgeTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Rename
                            IconButton(
                                onClick = {
                                    artboardToRename = artboard
                                    renameInput = artboard.name
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Rename",
                                    tint = ForgeTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Delete (only if > 1)
                            if (artboards.size > 1) {
                                IconButton(
                                    onClick = { onDeleteArtboard(artboard.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = ForgeError,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ForgeSurfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done", color = ForgeTextPrimary)
                }
            }
        }
    }

    // Rename Dialog
    artboardToRename?.let { artboard ->
        AlertDialog(
            onDismissRequest = { artboardToRename = null },
            title = { Text("Rename Artboard", color = ForgeTextPrimary) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForgePrimary,
                        unfocusedBorderColor = ForgeBorder,
                        focusedTextColor = ForgeTextPrimary,
                        unfocusedTextColor = ForgeTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            onRenameArtboard(artboard.id, renameInput)
                        }
                        artboardToRename = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForgePrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { artboardToRename = null }) {
                    Text("Cancel", color = ForgeTextSecondary)
                }
            },
            containerColor = ForgeSurface
        )
    }
}
