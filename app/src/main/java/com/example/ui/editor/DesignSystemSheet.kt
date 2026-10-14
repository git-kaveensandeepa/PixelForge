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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ComponentMaster
import com.example.model.DesignElement
import com.example.model.DesignToken
import com.example.ui.theme.ForgeAccentCyan
import com.example.ui.theme.ForgeBorder
import com.example.ui.theme.ForgeBorderLight
import com.example.ui.theme.ForgePrimary
import com.example.ui.theme.ForgePrimaryLight
import com.example.ui.theme.ForgeSurface
import com.example.ui.theme.ForgeSurfaceVariant
import com.example.ui.theme.ForgeTextMuted
import com.example.ui.theme.ForgeTextPrimary
import com.example.ui.theme.ForgeTextSecondary
import com.example.util.ArtboardPngExporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignSystemSheet(
    tokens: List<DesignToken>,
    components: List<ComponentMaster>,
    selectedElement: DesignElement?,
    onDismiss: () -> Unit,
    onInsertPrebuiltBlock: (String) -> Unit,
    onCreateComponentMaster: (elementId: String, name: String) -> Unit,
    onInsertComponentInstance: (componentMasterId: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var selectedTab by remember { mutableIntStateOf(0) }
    var newComponentName by remember { mutableStateOf(selectedElement?.name ?: "My Component") }

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
                .testTag("design_system_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Widgets, contentDescription = null, tint = ForgePrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Design System & Assets",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ForgeTextPrimary
                        )
                        Text(
                            text = "Reusable blocks, custom components & tokens",
                            style = MaterialTheme.typography.bodySmall,
                            color = ForgeTextSecondary
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = ForgeTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = ForgeSurfaceVariant,
                contentColor = ForgePrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Blocks", fontSize = 12.sp, color = if (selectedTab == 0) ForgePrimary else ForgeTextSecondary) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Components (${components.size})", fontSize = 12.sp, color = if (selectedTab == 1) ForgePrimary else ForgeTextSecondary) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Tokens", fontSize = 12.sp, color = if (selectedTab == 2) ForgePrimary else ForgeTextSecondary) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> PrebuiltBlocksTab(onInsertPrebuiltBlock = {
                    onInsertPrebuiltBlock(it)
                    onDismiss()
                })
                1 -> CustomComponentsTab(
                    components = components,
                    selectedElement = selectedElement,
                    newComponentName = newComponentName,
                    onNameChange = { newComponentName = it },
                    onCreateComponent = { elemId, name ->
                        onCreateComponentMaster(elemId, name)
                    },
                    onInsertInstance = {
                        onInsertComponentInstance(it)
                        onDismiss()
                    }
                )
                2 -> DesignTokensTab(tokens = tokens)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PrebuiltBlocksTab(onInsertPrebuiltBlock: (String) -> Unit) {
    val blocks = listOf(
        Triple("TOP_APP_BAR", "Top App Bar", "Mobile navigation bar with title"),
        Triple("HERO_CARD", "Feature Hero Card", "Rounded promotional card with title & description"),
        Triple("PROFILE_CARD", "User Profile Card", "Avatar circle, display name, handle & follow button"),
        Triple("PRODUCT_CARD", "E-Commerce Product Card", "Image slot, title, price tag & action button"),
        Triple("METRIC_CARD", "Analytics Metric Card", "KPI stat value, gradient background & trend badge"),
        Triple("SEARCH_BAR", "Mobile Search Bar", "Rounded search pill with icon & placeholder"),
        Triple("SETTINGS_ROW", "Interactive Setting Row", "Setting title, subtitle & active switch toggle"),
        Triple("INPUT_FIELD", "Form Input Field", "Outlined text input container with placeholder"),
        Triple("BOTTOM_NAV", "Bottom Navigation Bar", "3-tab mobile bottom dock")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(blocks) { (type, title, desc) ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ForgeSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onInsertPrebuiltBlock(type) }
                    .border(1.dp, ForgeBorder, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = ForgePrimary.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ForgePrimaryLight, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ForgeTextPrimary)
                        Text(text = desc, style = MaterialTheme.typography.bodySmall, color = ForgeTextMuted)
                    }
                    Button(
                        onClick = { onInsertPrebuiltBlock(type) },
                        colors = ButtonDefaults.buttonColors(containerColor = ForgePrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Insert", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomComponentsTab(
    components: List<ComponentMaster>,
    selectedElement: DesignElement?,
    newComponentName: String,
    onNameChange: (String) -> Unit,
    onCreateComponent: (elementId: String, name: String) -> Unit,
    onInsertInstance: (componentMasterId: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
    ) {
        // Create Component Section
        if (selectedElement != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ForgeSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ForgeBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(text = "Create Component from Selected Element", style = MaterialTheme.typography.labelMedium, color = ForgeTextPrimary, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newComponentName,
                            onValueChange = onNameChange,
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ForgePrimary,
                                unfocusedBorderColor = ForgeBorder,
                                focusedTextColor = ForgeTextPrimary,
                                unfocusedTextColor = ForgeTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onCreateComponent(selectedElement.id, newComponentName) },
                            colors = ButtonDefaults.buttonColors(containerColor = ForgePrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Save Master", fontSize = 12.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (components.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = "No custom component masters yet.\nSelect an element on canvas to convert it into a reusable component.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ForgeTextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(components, key = { it.id }) { comp ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ForgeSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ForgeBorder, RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = comp.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ForgeTextPrimary)
                                Text(text = "Type: ${comp.element.type.name.lowercase()}", style = MaterialTheme.typography.bodySmall, color = ForgeTextMuted)
                            }
                            Button(
                                onClick = { onInsertInstance(comp.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = ForgePrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Add Instance", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DesignTokensTab(tokens: List<DesignToken>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(tokens, key = { it.id }) { token ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ForgeSurfaceVariant)
                    .border(1.dp, ForgeBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (token.category == "COLOR") {
                        val c = Color(ArtboardPngExporter.parseColorSafe(token.value, android.graphics.Color.WHITE))
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(1.dp, ForgeBorderLight, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Text(
                        text = token.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ForgeTextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = ForgeSurface
                ) {
                    Text(
                        text = token.value,
                        style = MaterialTheme.typography.labelSmall,
                        color = ForgeAccentCyan,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
