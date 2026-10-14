package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import com.example.util.IconRegistry
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignCenter
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Artboard
import com.example.model.DesignElement
import com.example.model.ElementType
import com.example.model.TextAlignOption
import com.example.model.TransitionType
import com.example.ui.theme.ElementPresetColors
import com.example.ui.theme.ForgeAccentCyan
import com.example.ui.theme.ForgeBorder
import com.example.ui.theme.ForgeBorderLight
import com.example.ui.theme.ForgeError
import com.example.ui.theme.ForgePrimary
import com.example.ui.theme.ForgeSurface
import com.example.ui.theme.ForgeSurfaceVariant
import com.example.ui.theme.ForgeTextMuted
import com.example.ui.theme.ForgeTextPrimary
import com.example.ui.theme.ForgeTextSecondary
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PropertySheet(
    element: DesignElement?,
    selectedCount: Int,
    allArtboards: List<Artboard>,
    currentArtboardId: String,
    onDismiss: () -> Unit,
    onUpdateElement: (DesignElement) -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onAlign: (alignment: String) -> Unit,
    onDistribute: (horizontal: Boolean) -> Unit,
    onGroup: () -> Unit,
    onUngroup: () -> Unit,
    onBringToFront: () -> Unit,
    onSendToBack: () -> Unit,
    onBringForward: () -> Unit,
    onSendBackward: () -> Unit,
    onConnectToArtboard: (targetArtboardId: String, transition: TransitionType, isOverlay: Boolean) -> Unit,
    onRemoveConnection: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val currentArtboard = allArtboards.find { it.id == currentArtboardId }
    val existingConnection = if (element != null) currentArtboard?.findConnectionForElement(element.id) else null
    val targetArtboard = if (existingConnection != null) {
        allArtboards.find { it.id == existingConnection.targetArtboardId }
    } else null

    var linkDropdownExpanded by remember { mutableStateOf(false) }
    var selectedTransition by remember(existingConnection) {
        mutableStateOf(existingConnection?.transition ?: TransitionType.INSTANT)
    }
    var isOverlay by remember(existingConnection) {
        mutableStateOf(existingConnection?.isOverlay ?: false)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ForgeSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
                .testTag("property_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (selectedCount > 1) "$selectedCount Elements Selected" else (element?.name ?: "Properties"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ForgeTextPrimary
                    )
                    Text(
                        text = if (selectedCount > 1) "Multi-selection layout & alignment" else "Type: ${element?.type?.name?.lowercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ForgeTextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDuplicate, modifier = Modifier.testTag("prop_duplicate_btn")) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = ForgeTextSecondary)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.testTag("prop_delete_btn")) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ForgeError)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("prop_close_btn")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ForgeTextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = ForgeBorder)
            Spacer(modifier = Modifier.height(12.dp))

            // Multi-Selection Alignment & Distribution Toolbar
            Text(
                text = "Align & Distribute",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = ForgeTextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { onAlign("LEFT") }) {
                    Icon(Icons.AutoMirrored.Filled.FormatAlignLeft, contentDescription = "Align Left", tint = ForgeTextPrimary)
                }
                IconButton(onClick = { onAlign("CENTER") }) {
                    Icon(Icons.Default.FormatAlignCenter, contentDescription = "Align Center", tint = ForgeTextPrimary)
                }
                IconButton(onClick = { onAlign("RIGHT") }) {
                    Icon(Icons.AutoMirrored.Filled.FormatAlignRight, contentDescription = "Align Right", tint = ForgeTextPrimary)
                }
                IconButton(onClick = { onAlign("TOP") }) {
                    Icon(Icons.Default.VerticalAlignTop, contentDescription = "Align Top", tint = ForgeTextPrimary)
                }
                IconButton(onClick = { onAlign("MIDDLE") }) {
                    Icon(Icons.Default.VerticalAlignCenter, contentDescription = "Align Middle", tint = ForgeTextPrimary)
                }
                IconButton(onClick = { onAlign("BOTTOM") }) {
                    Icon(Icons.Default.VerticalAlignBottom, contentDescription = "Align Bottom", tint = ForgeTextPrimary)
                }
            }

            if (selectedCount > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onDistribute(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = ForgeSurfaceVariant),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Distribute H", fontSize = 11.sp, color = ForgeTextPrimary)
                    }
                    Button(
                        onClick = { onDistribute(false) },
                        colors = ButtonDefaults.buttonColors(containerColor = ForgeSurfaceVariant),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Distribute V", fontSize = 11.sp, color = ForgeTextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onGroup,
                        colors = ButtonDefaults.buttonColors(containerColor = ForgePrimary),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Group Elements", fontSize = 12.sp)
                    }
                    Button(
                        onClick = onUngroup,
                        colors = ButtonDefaults.buttonColors(containerColor = ForgeSurfaceVariant),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Ungroup", fontSize = 12.sp, color = ForgeTextPrimary)
                    }
                }
            }

            // Layer Arrange (Z-Order)
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = ForgeBorder)
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Z-Order Layer Arrange",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = ForgeTextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onBringToFront,
                    colors = ButtonDefaults.buttonColors(containerColor = ForgeSurfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("To Front", fontSize = 11.sp, color = ForgeTextPrimary)
                }
                Button(
                    onClick = onBringForward,
                    colors = ButtonDefaults.buttonColors(containerColor = ForgeSurfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Forward", fontSize = 11.sp, color = ForgeTextPrimary)
                }
                Button(
                    onClick = onSendBackward,
                    colors = ButtonDefaults.buttonColors(containerColor = ForgeSurfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Backward", fontSize = 11.sp, color = ForgeTextPrimary)
                }
                Button(
                    onClick = onSendToBack,
                    colors = ButtonDefaults.buttonColors(containerColor = ForgeSurfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("To Back", fontSize = 11.sp, color = ForgeTextPrimary)
                }
            }

            // Single Element Specific Controls
            if (element != null && selectedCount == 1) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = ForgeBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Geometry & Transform",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ForgeTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumericInputBox(
                        label = "X",
                        value = element.x.roundToInt(),
                        onValueChange = { onUpdateElement(element.copy(x = it.toFloat())) },
                        modifier = Modifier.weight(1f)
                    )
                    NumericInputBox(
                        label = "Y",
                        value = element.y.roundToInt(),
                        onValueChange = { onUpdateElement(element.copy(y = it.toFloat())) },
                        modifier = Modifier.weight(1f)
                    )
                    NumericInputBox(
                        label = "W",
                        value = element.width.roundToInt(),
                        onValueChange = { onUpdateElement(element.copy(width = it.toFloat().coerceAtLeast(10f))) },
                        modifier = Modifier.weight(1f)
                    )
                    NumericInputBox(
                        label = "H",
                        value = element.height.roundToInt(),
                        onValueChange = { onUpdateElement(element.copy(height = it.toFloat().coerceAtLeast(2f))) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Rotation
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Rotation: ${element.rotation.roundToInt()}°", style = MaterialTheme.typography.bodySmall, color = ForgeTextSecondary)
                    Row {
                        IconButton(onClick = { onUpdateElement(element.copy(rotation = ((element.rotation - 45f) + 360f) % 360f)) }) {
                            Text("-45°", fontSize = 11.sp, color = ForgePrimary, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { onUpdateElement(element.copy(rotation = (element.rotation + 45f) % 360f)) }) {
                            Text("+45°", fontSize = 11.sp, color = ForgePrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Slider(
                    value = element.rotation,
                    onValueChange = { onUpdateElement(element.copy(rotation = it)) },
                    valueRange = 0f..360f,
                    colors = SliderDefaults.colors(
                        thumbColor = ForgePrimary,
                        activeTrackColor = ForgePrimary,
                        inactiveTrackColor = ForgeBorder
                    )
                )

                // Text Properties (if TEXT or BUTTON)
                if (element.type == ElementType.TEXT || element.type == ElementType.BUTTON) {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = ForgeBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (element.type == ElementType.BUTTON) "Button Label" else "Text Content",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ForgeTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = element.text,
                        onValueChange = { onUpdateElement(element.copy(text = it)) },
                        singleLine = (element.type == ElementType.BUTTON),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ForgePrimary,
                            unfocusedBorderColor = ForgeBorder,
                            focusedTextColor = ForgeTextPrimary,
                            unfocusedTextColor = ForgeTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Font Size: ${element.fontSize.roundToInt()}sp", style = MaterialTheme.typography.bodySmall, color = ForgeTextSecondary)
                        Row {
                            IconButton(onClick = { onUpdateElement(element.copy(fontSize = (element.fontSize - 2f).coerceAtLeast(10f))) }) {
                                Text("-", color = ForgePrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { onUpdateElement(element.copy(fontSize = (element.fontSize + 2f).coerceAtMost(72f))) }) {
                                Text("+", color = ForgePrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Slider(
                        value = element.fontSize,
                        onValueChange = { onUpdateElement(element.copy(fontSize = it)) },
                        valueRange = 10f..72f,
                        colors = SliderDefaults.colors(thumbColor = ForgePrimary, activeTrackColor = ForgePrimary, inactiveTrackColor = ForgeBorder)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilterChip(
                                selected = element.isBold,
                                onClick = { onUpdateElement(element.copy(isBold = !element.isBold)) },
                                label = { Icon(Icons.Default.FormatBold, contentDescription = "Bold", modifier = Modifier.size(18.dp)) }
                            )
                            FilterChip(
                                selected = element.isItalic,
                                onClick = { onUpdateElement(element.copy(isItalic = !element.isItalic)) },
                                label = { Icon(Icons.Default.FormatItalic, contentDescription = "Italic", modifier = Modifier.size(18.dp)) }
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilterChip(
                                selected = element.textAlign == TextAlignOption.LEFT,
                                onClick = { onUpdateElement(element.copy(textAlign = TextAlignOption.LEFT)) },
                                label = { Icon(Icons.AutoMirrored.Filled.FormatAlignLeft, contentDescription = "Left", modifier = Modifier.size(18.dp)) }
                            )
                            FilterChip(
                                selected = element.textAlign == TextAlignOption.CENTER,
                                onClick = { onUpdateElement(element.copy(textAlign = TextAlignOption.CENTER)) },
                                label = { Icon(Icons.Default.FormatAlignCenter, contentDescription = "Center", modifier = Modifier.size(18.dp)) }
                            )
                            FilterChip(
                                selected = element.textAlign == TextAlignOption.RIGHT,
                                onClick = { onUpdateElement(element.copy(textAlign = TextAlignOption.RIGHT)) },
                                label = { Icon(Icons.AutoMirrored.Filled.FormatAlignRight, contentDescription = "Right", modifier = Modifier.size(18.dp)) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Text Color", style = MaterialTheme.typography.labelSmall, color = ForgeTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    ColorPaletteSelector(selectedHex = element.textColorHex, onColorSelected = { onUpdateElement(element.copy(textColorHex = it)) })
                }

                // Fill Color
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = ForgeBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (element.type == ElementType.LINE) "Line Color" else "Fill Color",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ForgeTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                ColorPaletteSelector(selectedHex = element.fillColorHex, onColorSelected = { onUpdateElement(element.copy(fillColorHex = it)) })

                // Corner Radius
                if (element.type == ElementType.RECTANGLE || element.type == ElementType.BUTTON || element.type == ElementType.IMAGE_PLACEHOLDER) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Corner Radius", style = MaterialTheme.typography.bodySmall, color = ForgeTextSecondary)
                        Text(text = "${element.cornerRadius.roundToInt()} dp", style = MaterialTheme.typography.bodySmall, color = ForgeTextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                    Slider(
                        value = element.cornerRadius,
                        onValueChange = { onUpdateElement(element.copy(cornerRadius = it)) },
                        valueRange = 0f..48f,
                        colors = SliderDefaults.colors(thumbColor = ForgePrimary, activeTrackColor = ForgePrimary, inactiveTrackColor = ForgeBorder)
                    )
                }

                // Opacity
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Opacity", style = MaterialTheme.typography.bodySmall, color = ForgeTextSecondary)
                    Text(text = "${(element.opacity * 100).roundToInt()}%", style = MaterialTheme.typography.bodySmall, color = ForgeTextPrimary, fontWeight = FontWeight.SemiBold)
                }
                Slider(
                    value = element.opacity,
                    onValueChange = { onUpdateElement(element.copy(opacity = it)) },
                    valueRange = 0.05f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = ForgePrimary, activeTrackColor = ForgePrimary, inactiveTrackColor = ForgeBorder)
                )

                // Shadow Elevation
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Shadow Elevation", style = MaterialTheme.typography.bodySmall, color = ForgeTextSecondary)
                    Text(text = "${element.shadowElevation.roundToInt()} dp", style = MaterialTheme.typography.bodySmall, color = ForgeTextPrimary, fontWeight = FontWeight.SemiBold)
                }
                Slider(
                    value = element.shadowElevation,
                    onValueChange = { onUpdateElement(element.copy(shadowElevation = it)) },
                    valueRange = 0f..24f,
                    colors = SliderDefaults.colors(thumbColor = ForgeAccentCyan, activeTrackColor = ForgeAccentCyan, inactiveTrackColor = ForgeBorder)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Flat" to 0f,
                        "2dp" to 2f,
                        "6dp" to 6f,
                        "12dp" to 12f,
                        "20dp" to 20f
                    ).forEach { (label, elev) ->
                        val isSel = element.shadowElevation.roundToInt() == elev.roundToInt()
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSel) ForgeAccentCyan else ForgeSurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onUpdateElement(element.copy(shadowElevation = elev, hasShadow = elev > 0f)) }
                                .border(1.dp, if (isSel) ForgeAccentCyan else ForgeBorder, RoundedCornerShape(6.dp))
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSel) Color.Black else ForgeTextSecondary,
                                modifier = Modifier.padding(vertical = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // Icon Picker Section (if ICON)
                if (element.type == ElementType.ICON) {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = ForgeBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Choose Icon (${element.iconName})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ForgeTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconRegistry.AVAILABLE_ICONS.forEach { (iconKey, vector) ->
                            val isChosen = element.iconName.equals(iconKey, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isChosen) ForgePrimary.copy(alpha = 0.3f) else ForgeSurfaceVariant,
                                modifier = Modifier
                                    .size(40.dp)
                                    .border(1.dp, if (isChosen) ForgePrimary else ForgeBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        onUpdateElement(element.copy(iconName = iconKey, name = "Icon (${iconKey})"))
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = vector,
                                        contentDescription = iconKey,
                                        tint = if (isChosen) ForgePrimary else ForgeTextPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Input Field Properties (if TEXT_FIELD)
                if (element.type == ElementType.TEXT_FIELD) {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = ForgeBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Input Placeholder",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ForgeTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = element.placeholder,
                        onValueChange = { onUpdateElement(element.copy(placeholder = it)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ForgePrimary,
                            unfocusedBorderColor = ForgeBorder,
                            focusedTextColor = ForgeTextPrimary,
                            unfocusedTextColor = ForgeTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Initial Text",
                        style = MaterialTheme.typography.labelSmall,
                        color = ForgeTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = element.text,
                        onValueChange = { onUpdateElement(element.copy(text = it)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ForgePrimary,
                            unfocusedBorderColor = ForgeBorder,
                            focusedTextColor = ForgeTextPrimary,
                            unfocusedTextColor = ForgeTextPrimary
                        )
                    )
                }

                // Switch and Checkbox Properties
                if (element.type == ElementType.SWITCH || element.type == ElementType.CHECKBOX) {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = ForgeBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Initial State: ${if (element.isChecked) "Checked / ON" else "Unchecked / OFF"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ForgeTextPrimary
                        )
                        Switch(
                            checked = element.isChecked,
                            onCheckedChange = { onUpdateElement(element.copy(isChecked = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = ForgePrimary, checkedTrackColor = ForgePrimary.copy(alpha = 0.5f))
                        )
                    }

                    if (element.type == ElementType.CHECKBOX) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Checkbox Label",
                            style = MaterialTheme.typography.labelSmall,
                            color = ForgeTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = element.text,
                            onValueChange = { onUpdateElement(element.copy(text = it)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ForgePrimary,
                                unfocusedBorderColor = ForgeBorder,
                                focusedTextColor = ForgeTextPrimary,
                                unfocusedTextColor = ForgeTextPrimary
                            )
                        )
                    }
                }

                // Gradient Background Controls
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = ForgeBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Linear Gradient Fill", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ForgeTextPrimary)
                        Text(text = "Apply two-tone gradient fill", style = MaterialTheme.typography.bodySmall, color = ForgeTextSecondary)
                    }
                    Switch(
                        checked = element.hasGradient,
                        onCheckedChange = { enableGrad ->
                            if (enableGrad) {
                                onUpdateElement(
                                    element.copy(
                                        gradientStartHex = element.fillColorHex,
                                        gradientEndHex = "#06B6D4"
                                    )
                                )
                            } else {
                                onUpdateElement(element.copy(gradientStartHex = null, gradientEndHex = null))
                            }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = ForgeAccentCyan, checkedTrackColor = ForgeAccentCyan.copy(alpha = 0.4f))
                    )
                }

                if (element.hasGradient) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Gradient Presets", style = MaterialTheme.typography.labelSmall, color = ForgeTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    val gradientPresets = listOf(
                        Triple("Neon Indigo", "#6366F1", "#06B6D4"),
                        Triple("Sunset Coral", "#F97316", "#EC4899"),
                        Triple("Cyber Emerald", "#10B981", "#06B6D4"),
                        Triple("Royal Violet", "#8B5CF6", "#EC4899"),
                        Triple("Midnight Slate", "#1E293B", "#0F172A"),
                        Triple("Golden Sun", "#F59E0B", "#EF4444")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        gradientPresets.forEach { (name, start, end) ->
                            val sCol = Color(com.example.util.ArtboardPngExporter.parseColorSafe(start, android.graphics.Color.GRAY))
                            val eCol = Color(com.example.util.ArtboardPngExporter.parseColorSafe(end, android.graphics.Color.GRAY))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        androidx.compose.ui.graphics.Brush.linearGradient(listOf(sCol, eCol))
                                    )
                                    .border(1.dp, ForgeBorderLight, RoundedCornerShape(6.dp))
                                    .clickable {
                                        onUpdateElement(
                                            element.copy(
                                                gradientStartHex = start,
                                                gradientEndHex = end,
                                                fillColorHex = start
                                            )
                                        )
                                    }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Custom Gradient End Color", style = MaterialTheme.typography.labelSmall, color = ForgeTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    ColorPaletteSelector(
                        selectedHex = element.gradientEndHex ?: "#06B6D4",
                        onColorSelected = { onUpdateElement(element.copy(gradientEndHex = it)) }
                    )
                }

                // Prototype Link Section
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = ForgeBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Link, contentDescription = null, tint = ForgeAccentCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Prototype Interaction", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = ForgeTextPrimary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Tap destination screen and animated transition:", style = MaterialTheme.typography.bodySmall, color = ForgeTextSecondary)
                Spacer(modifier = Modifier.height(10.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { linkDropdownExpanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ForgeSurfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ForgeBorderLight, RoundedCornerShape(10.dp))
                            .testTag("prop_link_destination_btn")
                    ) {
                        Text(
                            text = if (targetArtboard != null) "Linked to: ${targetArtboard.name}" else "Choose Destination Screen...",
                            color = if (targetArtboard != null) ForgeAccentCyan else ForgeTextSecondary
                        )
                    }

                    DropdownMenu(
                        expanded = linkDropdownExpanded,
                        onDismissRequest = { linkDropdownExpanded = false },
                        modifier = Modifier.background(ForgeSurfaceVariant)
                    ) {
                        val candidateArtboards = allArtboards.filter { it.id != currentArtboardId }
                        if (candidateArtboards.isEmpty()) {
                            DropdownMenuItem(text = { Text("No other screens", color = ForgeTextMuted) }, onClick = { linkDropdownExpanded = false })
                        } else {
                            candidateArtboards.forEach { artboard ->
                                DropdownMenuItem(
                                    text = { Text(artboard.name, color = ForgeTextPrimary) },
                                    onClick = {
                                        linkDropdownExpanded = false
                                        onConnectToArtboard(artboard.id, selectedTransition, isOverlay)
                                    }
                                )
                            }
                        }
                    }
                }

                // Transition Picker
                if (targetArtboard != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Transition Preset", style = MaterialTheme.typography.labelSmall, color = ForgeTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TransitionType.values().take(3).forEach { trans ->
                            FilterChip(
                                selected = selectedTransition == trans,
                                onClick = {
                                    selectedTransition = trans
                                    onConnectToArtboard(targetArtboard.id, trans, isOverlay)
                                },
                                label = { Text(trans.label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ForgePrimary)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TransitionType.values().drop(3).forEach { trans ->
                            FilterChip(
                                selected = selectedTransition == trans,
                                onClick = {
                                    selectedTransition = trans
                                    onConnectToArtboard(targetArtboard.id, trans, isOverlay)
                                },
                                label = { Text(trans.label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ForgePrimary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = onRemoveConnection,
                        colors = ButtonDefaults.buttonColors(containerColor = ForgeBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.LinkOff, contentDescription = null, tint = ForgeError, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Remove Interaction", color = ForgeError, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NumericInputBox(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var textVal by remember(value) { mutableStateOf(value.toString()) }

    OutlinedTextField(
        value = textVal,
        onValueChange = {
            textVal = it
            it.toIntOrNull()?.let { v -> onValueChange(v) }
        },
        label = { Text(label, fontSize = 11.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ForgePrimary,
            unfocusedBorderColor = ForgeBorder,
            focusedTextColor = ForgeTextPrimary,
            unfocusedTextColor = ForgeTextPrimary,
            focusedLabelColor = ForgePrimary,
            unfocusedLabelColor = ForgeTextSecondary
        )
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorPaletteSelector(
    selectedHex: String,
    onColorSelected: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ElementPresetColors.forEach { color ->
            val hexString = String.format("#%06X", (0xFFFFFF and color.value.toLong().toInt()))
            val isSelected = selectedHex.equals(hexString, ignoreCase = true)

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) Color.White else ForgeBorderLight,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(hexString) }
            )
        }
    }
}
