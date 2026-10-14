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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DesignElement
import com.example.model.ElementType
import com.example.ui.theme.ForgeAccentCyan
import com.example.ui.theme.ForgeBorder
import com.example.ui.theme.ForgeError
import com.example.ui.theme.ForgePrimary
import com.example.ui.theme.ForgeSurface
import com.example.ui.theme.ForgeSurfaceVariant
import com.example.ui.theme.ForgeTextMuted
import com.example.ui.theme.ForgeTextPrimary
import com.example.ui.theme.ForgeTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayersSheet(
    elements: List<DesignElement>,
    selectedElementIds: Set<String>,
    onDismiss: () -> Unit,
    onSelectElement: (String, Boolean) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onReorder: (elementId: String, moveUp: Boolean) -> Unit,
    onDelete: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var searchQuery by remember { mutableStateOf("") }

    val reversedList = remember(elements) { elements.reversed() }
    val filteredList = remember(reversedList, searchQuery) {
        if (searchQuery.isBlank()) reversedList
        else reversedList.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.text.contains(searchQuery, ignoreCase = true) ||
            it.type.name.contains(searchQuery, ignoreCase = true)
        }
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
                .testTag("layers_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Layers (${elements.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ForgeTextPrimary
                    )
                    Text(
                        text = "Hierarchy, search, lock & reorder controls",
                        style = MaterialTheme.typography.bodySmall,
                        color = ForgeTextSecondary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_layers_btn")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = ForgeTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search filter field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search layers...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ForgeTextSecondary, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = ForgeTextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ForgePrimary,
                    unfocusedBorderColor = ForgeBorder,
                    focusedTextColor = ForgeTextPrimary,
                    unfocusedTextColor = ForgeTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = ForgeBorder)
            Spacer(modifier = Modifier.height(10.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "No elements on this artboard yet" else "No matching layers found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ForgeTextMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(filteredList, key = { _, item -> item.id }) { revIndex, element ->
                        val isSelected = selectedElementIds.contains(element.id)
                        val canMoveUp = revIndex > 0
                        val canMoveDown = revIndex < filteredList.lastIndex

                        LayerItemRow(
                            element = element,
                            isSelected = isSelected,
                            canMoveUp = canMoveUp,
                            canMoveDown = canMoveDown,
                            onClick = { isToggle -> onSelectElement(element.id, isToggle) },
                            onToggleVisibility = { onToggleVisibility(element.id) },
                            onToggleLock = { onToggleLock(element.id) },
                            onMoveUp = { onReorder(element.id, true) },
                            onMoveDown = { onReorder(element.id, false) },
                            onDelete = { onDelete(element.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun LayerItemRow(
    element: DesignElement,
    isSelected: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onClick: (isToggle: Boolean) -> Unit,
    onToggleVisibility: () -> Unit,
    onToggleLock: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    val icon: ImageVector = when (element.type) {
        ElementType.RECTANGLE -> Icons.Default.CropSquare
        ElementType.CIRCLE -> Icons.Default.Circle
        ElementType.LINE -> Icons.Default.HorizontalRule
        ElementType.TEXT -> Icons.Default.TextFields
        ElementType.BUTTON -> Icons.Default.SmartButton
        ElementType.IMAGE_PLACEHOLDER -> Icons.Default.Image
        ElementType.ICON -> com.example.util.IconRegistry.getIcon(element.iconName)
        ElementType.TEXT_FIELD -> Icons.Default.TextFields
        ElementType.SWITCH -> Icons.Default.SmartButton
        ElementType.CHECKBOX -> Icons.Default.CropSquare
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) ForgeSurfaceVariant else Color.Transparent)
            .border(
                1.dp,
                if (isSelected) ForgePrimary else ForgeBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = { onClick(false) })
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("layer_item_${element.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = if (isSelected) ForgePrimary else ForgeSurfaceVariant,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else ForgeTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = element.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) ForgePrimary else ForgeTextPrimary
                )
                if (element.groupId != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(shape = RoundedCornerShape(4.dp), color = ForgeAccentCyan.copy(alpha = 0.2f)) {
                        Text(text = "GRP", fontSize = 9.sp, color = ForgeAccentCyan, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                    }
                }
                if (element.isComponentInstance) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(shape = RoundedCornerShape(4.dp), color = ForgePrimary.copy(alpha = 0.2f)) {
                        Text(text = "CMP", fontSize = 9.sp, color = ForgePrimary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                    }
                }
            }
            Text(
                text = "${element.width.toInt()} × ${element.height.toInt()} dp",
                style = MaterialTheme.typography.bodySmall,
                color = ForgeTextMuted,
                maxLines = 1
            )
        }

        // Lock toggle
        IconButton(onClick = onToggleLock, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = if (element.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = "Lock",
                tint = if (element.isLocked) ForgePrimary else ForgeTextMuted,
                modifier = Modifier.size(16.dp)
            )
        }

        // Visibility toggle
        IconButton(onClick = onToggleVisibility, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = if (element.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = "Visibility",
                tint = if (element.isVisible) ForgeTextSecondary else ForgeTextMuted,
                modifier = Modifier.size(16.dp)
            )
        }

        // Move Up
        IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Up",
                tint = if (canMoveUp) ForgeTextSecondary else ForgeBorder,
                modifier = Modifier.size(16.dp)
            )
        }

        // Move Down
        IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Down",
                tint = if (canMoveDown) ForgeTextSecondary else ForgeBorder,
                modifier = Modifier.size(16.dp)
            )
        }

        // Delete
        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = ForgeError,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
