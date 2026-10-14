package com.example.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlignHorizontalCenter
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.ui.theme.ThemeSelectionDialog
import com.example.util.ArtboardPngExporter
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ElementType
import com.example.ui.theme.ForgeAccentCyan
import com.example.ui.theme.ForgeBackground
import com.example.ui.theme.ForgeBorder
import com.example.ui.theme.ForgeBorderLight
import com.example.ui.theme.ForgeError
import com.example.ui.theme.ForgePrimary
import com.example.ui.theme.ForgePrimaryLight
import com.example.ui.theme.ForgeSuccess
import com.example.ui.theme.ForgeSurface
import com.example.ui.theme.ForgeSurfaceVariant
import com.example.ui.theme.ForgeTextMuted
import com.example.ui.theme.ForgeTextPrimary
import com.example.ui.theme.ForgeTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit,
    onRunPrototype: (projectId: String) -> Unit
) {
    val density = LocalDensity.current
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsState()

    var showCanvasControls by remember { mutableStateOf(false) }
    var showInsertSheet by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showGuideDialog by remember { mutableStateOf(false) }
    var showNudgePad by remember { mutableStateOf(false) }
    var nudgeStep by remember { mutableStateOf(1f) }
    val brokenLinks = remember(uiState.project) { viewModel.getBrokenConnections() }

    BackHandler {
        when {
            showNudgePad -> showNudgePad = false
            showGuideDialog -> showGuideDialog = false
            showThemeDialog -> showThemeDialog = false
            showInsertSheet -> showInsertSheet = false
            uiState.isHandoffSheetOpen -> viewModel.toggleHandoffSheet(false)
            uiState.isDesignSystemSheetOpen -> viewModel.toggleDesignSystemSheet(false)
            uiState.isPropertySheetOpen -> viewModel.togglePropertySheet(false)
            uiState.isLayersSheetOpen -> viewModel.toggleLayersSheet(false)
            uiState.isArtboardManagerOpen -> viewModel.toggleArtboardManager(false)
            uiState.selectedElementIds.isNotEmpty() -> viewModel.selectElement(null, false)
            else -> onNavigateBack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("editor_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Projects",
                            tint = ForgeTextPrimary
                        )
                    }
                },
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = uiState.project?.name ?: "Editor",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ForgeTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Save Status Indicator
                            Surface(
                                shape = CircleShape,
                                color = when (uiState.saveStatus) {
                                    SaveStatus.SAVED -> ForgeSuccess.copy(alpha = 0.2f)
                                    SaveStatus.SAVING -> ForgePrimary.copy(alpha = 0.2f)
                                    SaveStatus.DIRTY -> ForgeBorder
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(
                                                color = when (uiState.saveStatus) {
                                                    SaveStatus.SAVED -> ForgeSuccess
                                                    SaveStatus.SAVING -> ForgePrimary
                                                    SaveStatus.DIRTY -> ForgeTextMuted
                                                },
                                                shape = CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = when (uiState.saveStatus) {
                                            SaveStatus.SAVED -> "Saved"
                                            SaveStatus.SAVING -> "Saving..."
                                            SaveStatus.DIRTY -> "Editing"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = ForgeTextSecondary
                                    )
                                }
                            }

                            if (brokenLinks.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = ForgeError.copy(alpha = 0.2f)) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = ForgeError, modifier = Modifier.size(10.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${brokenLinks.size} Broken Links", fontSize = 9.sp, color = ForgeError)
                                    }
                                }
                            }
                        }

                        // Mode Selector (Design vs Prototype) & Active Screen Picker
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ForgeSurfaceVariant,
                                modifier = Modifier
                                    .clickable { viewModel.toggleArtboardManager(true) }
                                    .testTag("switch_screen_chip")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = ForgePrimaryLight, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = uiState.project?.getActiveArtboard()?.name ?: "Screen",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ForgeTextPrimary,
                                        maxLines = 1
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            FilterChip(
                                selected = uiState.mode == EditorMode.DESIGN,
                                onClick = { viewModel.setMode(EditorMode.DESIGN) },
                                label = { Text("Design", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ForgePrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.height(24.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            FilterChip(
                                selected = uiState.mode == EditorMode.PROTOTYPE,
                                onClick = { viewModel.setMode(EditorMode.PROTOTYPE) },
                                label = { Text("Prototype", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ForgeAccentCyan,
                                    selectedLabelColor = Color.Black
                                ),
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = uiState.canUndo,
                        modifier = Modifier.testTag("editor_undo_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (uiState.canUndo) ForgeTextPrimary else ForgeTextMuted
                        )
                    }

                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = uiState.canRedo,
                        modifier = Modifier.testTag("editor_redo_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (uiState.canRedo) ForgeTextPrimary else ForgeTextMuted
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleHandoffSheet(true) },
                        modifier = Modifier.testTag("editor_export_handoff_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Code & Export Handoff",
                            tint = ForgePrimaryLight
                        )
                    }

                    IconButton(
                        onClick = { showThemeDialog = true },
                        modifier = Modifier.testTag("editor_theme_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Switch Theme",
                            tint = ForgePrimaryLight
                        )
                    }

                    IconButton(
                        onClick = { showGuideDialog = true },
                        modifier = Modifier.testTag("editor_guide_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Gesture Guide & Tips",
                            tint = ForgeTextSecondary
                        )
                    }

                    IconButton(
                        onClick = {
                            uiState.project?.let { onRunPrototype(it.id) }
                        },
                        modifier = Modifier.testTag("editor_play_prototype_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Run Prototype",
                            tint = ForgeAccentCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ForgeBackground,
                    titleContentColor = ForgeTextPrimary
                )
            )
        },
        containerColor = ForgeBackground
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val containerWidthPx = constraints.maxWidth.toFloat()
            val containerHeightPx = constraints.maxHeight.toFloat()

            if (uiState.isLoading || uiState.project == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ForgePrimary)
                }
            } else {
                val project = uiState.project!!
                val activeArtboard = project.getActiveArtboard()

                // Interactive Pro Canvas
                CanvasView(
                    artboard = activeArtboard,
                    allArtboards = project.artboards,
                    selectedElementIds = uiState.selectedElementIds,
                    primarySelectedId = uiState.primarySelectedId,
                    mode = uiState.mode,
                    scale = uiState.canvasScale,
                    offset = uiState.canvasOffset,
                    showGrid = uiState.showGrid,
                    snapToGrid = uiState.snapToGrid,
                    snapToElements = uiState.snapToElements,
                    onTransform = { scaleChange, panChange ->
                        viewModel.updateCanvasTransform(scaleChange, panChange)
                    },
                    onSelectElement = { id, isMultiToggle ->
                        viewModel.selectElement(id, isMultiToggle)
                    },
                    onMoveElements = { deltas, commit ->
                        viewModel.moveElements(deltas, commit)
                    },
                    onResizeElement = { id, x, y, w, h, commit ->
                        viewModel.updateElementBounds(id, x, y, w, h, commit)
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Zoom percentage indicator overlay
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ForgeSurface.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .border(1.dp, ForgeBorder, RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${(uiState.canvasScale * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = ForgeTextSecondary
                        )
                    }
                }

                // Contextual Selection Quick Bar (when 1 or more elements selected)
                if (uiState.selectedElementIds.isNotEmpty()) {
                    val primaryElement = uiState.primarySelectedId?.let { activeArtboard.findElement(it) }
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ForgeSurface,
                            tonalElevation = 6.dp,
                            modifier = Modifier
                                .border(1.dp, ForgeBorderLight, RoundedCornerShape(16.dp))
                                .testTag("selection_quick_bar")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Badge with dimension & position
                                    if (primaryElement != null) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = ForgeSurfaceVariant
                                        ) {
                                            Text(
                                                text = "${primaryElement.width.toInt()}×${primaryElement.height.toInt()} (${primaryElement.x.toInt()},${primaryElement.y.toInt()})",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.sp,
                                                color = ForgeAccentCyan,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "${uiState.selectedElementIds.size} sel",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ForgeAccentCyan,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                    }

                                    // Quick Center Button
                                    IconButton(
                                        onClick = { viewModel.alignSelected("CENTER") },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.AlignHorizontalCenter,
                                            contentDescription = "Center Horizontally",
                                            tint = ForgeTextPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Nudge Pad Toggle
                                    IconButton(
                                        onClick = { showNudgePad = !showNudgePad },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.OpenWith,
                                            contentDescription = "Precision Nudge D-Pad",
                                            tint = if (showNudgePad) ForgePrimary else ForgeTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Duplicate
                                    IconButton(
                                        onClick = { viewModel.duplicateSelected() },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ContentPaste,
                                            contentDescription = "Duplicate",
                                            tint = ForgePrimaryLight,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Delete
                                    IconButton(
                                        onClick = { viewModel.deleteSelected() },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = ForgeError,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Open Properties
                                    IconButton(
                                        onClick = { viewModel.togglePropertySheet(true) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Tune,
                                            contentDescription = "Properties",
                                            tint = ForgePrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                // Quick Palette dots strip (1-tap color styling)
                                Row(
                                    modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Quick:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = ForgeTextMuted
                                    )
                                    listOf(
                                        "#6366F1", "#06B6D4", "#10B981", "#F59E0B", "#EF4444", "#EC4899", "#FFFFFF", "#1E293B", "#0F172A"
                                    ).forEach { hex ->
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(Color(ArtboardPngExporter.parseColorSafe(hex, android.graphics.Color.GRAY)))
                                                .border(1.dp, ForgeBorderLight, CircleShape)
                                                .clickable {
                                                    viewModel.setQuickColorForSelected(hex)
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Floating Precision Nudge Pad
                if (showNudgePad && uiState.selectedElementIds.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ForgeSurface,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                            .border(1.dp, ForgeBorderLight, RoundedCornerShape(16.dp))
                            .testTag("floating_nudge_pad")
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.width(108.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "NUDGE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForgeTextMuted
                                )
                                // Step toggle pill
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (nudgeStep == 8f) ForgePrimary else ForgeSurfaceVariant,
                                    modifier = Modifier.clickable {
                                        nudgeStep = if (nudgeStep == 1f) 8f else 1f
                                    }
                                ) {
                                    Text(
                                        text = if (nudgeStep == 1f) "1dp" else "8dp Grid",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (nudgeStep == 8f) Color.White else ForgeTextSecondary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            IconButton(
                                onClick = { viewModel.nudgeSelected(0f, -nudgeStep) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Nudge Up", tint = ForgeTextPrimary)
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { viewModel.nudgeSelected(-nudgeStep, 0f) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Nudge Left", tint = ForgeTextPrimary)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(ForgeSurfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${nudgeStep.toInt()}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForgePrimaryLight
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.nudgeSelected(nudgeStep, 0f) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Nudge Right", tint = ForgeTextPrimary)
                                }
                            }

                            IconButton(
                                onClick = { viewModel.nudgeSelected(0f, nudgeStep) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Nudge Down", tint = ForgeTextPrimary)
                            }
                        }
                    }
                }

                // Bottom Floating Studio Dock
                StudioDockBar(
                    selectedCount = uiState.selectedElementIds.size,
                    hasClipboard = uiState.clipboardElements.isNotEmpty(),
                    onOpenInsertSheet = { showInsertSheet = true },
                    onAddRectangle = { viewModel.addElement(ElementType.RECTANGLE) },
                    onAddCircle = { viewModel.addElement(ElementType.CIRCLE) },
                    onAddLine = { viewModel.addElement(ElementType.LINE) },
                    onAddText = { viewModel.addElement(ElementType.TEXT) },
                    onAddButton = { viewModel.addElement(ElementType.BUTTON) },
                    onAddImage = { viewModel.addElement(ElementType.IMAGE_PLACEHOLDER) },
                    onPaste = { viewModel.pasteClipboard() },
                    onOpenDesignSystem = { viewModel.toggleDesignSystemSheet(true) },
                    onOpenLayers = { viewModel.toggleLayersSheet(true) },
                    onOpenProperties = { viewModel.togglePropertySheet(true) },
                    onToggleCanvasControls = { showCanvasControls = !showCanvasControls },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp, start = 8.dp, end = 8.dp)
                )

                // Quick Canvas View Controls Popup
                if (showCanvasControls) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ForgeSurface,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 80.dp, end = 16.dp)
                            .border(1.dp, ForgeBorder, RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(onClick = { viewModel.zoomIn() }) {
                                Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = ForgeTextPrimary)
                            }
                            IconButton(onClick = { viewModel.zoomOut() }) {
                                Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = ForgeTextPrimary)
                            }
                            IconButton(
                                onClick = {
                                    viewModel.fitToScreen(containerWidthPx, containerHeightPx, density.density)
                                }
                            ) {
                                Icon(Icons.Default.FitScreen, contentDescription = "Fit to Screen", tint = ForgePrimaryLight)
                            }
                            IconButton(onClick = { viewModel.toggleGrid() }) {
                                Icon(
                                    Icons.Default.GridOn,
                                    contentDescription = "Toggle Grid",
                                    tint = if (uiState.showGrid) ForgePrimary else ForgeTextMuted
                                )
                            }
                        }
                    }
                }

                // Layers Bottom Sheet
                if (uiState.isLayersSheetOpen) {
                    LayersSheet(
                        elements = activeArtboard.elements,
                        selectedElementIds = uiState.selectedElementIds,
                        onDismiss = { viewModel.toggleLayersSheet(false) },
                        onSelectElement = { elemId, isToggle ->
                            viewModel.selectElement(elemId, isToggle)
                        },
                        onToggleVisibility = { elemId -> viewModel.toggleElementVisibility(elemId) },
                        onToggleLock = { elemId -> viewModel.toggleElementLock(elemId) },
                        onReorder = { elemId, moveUp ->
                            if (moveUp) viewModel.bringForward() else viewModel.sendBackward()
                        },
                        onDelete = { elemId ->
                            viewModel.selectElement(elemId, false)
                            viewModel.deleteSelected()
                        }
                    )
                }

                // Property Inspector Bottom Sheet
                if (uiState.isPropertySheetOpen && uiState.selectedElementIds.isNotEmpty()) {
                    val primaryElement = uiState.primarySelectedId?.let { activeArtboard.findElement(it) }
                    PropertySheet(
                        element = primaryElement,
                        selectedCount = uiState.selectedElementIds.size,
                        allArtboards = project.artboards,
                        currentArtboardId = activeArtboard.id,
                        onDismiss = { viewModel.togglePropertySheet(false) },
                        onUpdateElement = { updated -> viewModel.updateElementProperties(updated) },
                        onDuplicate = { viewModel.duplicateSelected() },
                        onDelete = {
                            viewModel.deleteSelected()
                            viewModel.togglePropertySheet(false)
                        },
                        onAlign = { align -> viewModel.alignSelected(align) },
                        onDistribute = { horizontal -> viewModel.distributeSelected(horizontal) },
                        onGroup = { viewModel.groupSelected() },
                        onUngroup = { viewModel.ungroupSelected() },
                        onBringToFront = { viewModel.bringToFront() },
                        onSendToBack = { viewModel.sendToBack() },
                        onBringForward = { viewModel.bringForward() },
                        onSendBackward = { viewModel.sendBackward() },
                        onConnectToArtboard = { targetId, trans, isOver ->
                            primaryElement?.let {
                                viewModel.connectElementToArtboard(it.id, targetId, trans, isOver)
                            }
                        },
                        onRemoveConnection = {
                            primaryElement?.let { viewModel.removeConnection(it.id) }
                        }
                    )
                }

                // Design System & Components Sheet
                if (uiState.isDesignSystemSheetOpen) {
                    val primaryElement = uiState.primarySelectedId?.let { activeArtboard.findElement(it) }
                    DesignSystemSheet(
                        tokens = project.tokens,
                        components = project.components,
                        selectedElement = primaryElement,
                        onDismiss = { viewModel.toggleDesignSystemSheet(false) },
                        onInsertPrebuiltBlock = { blockType -> viewModel.insertPrebuiltBlock(blockType) },
                        onCreateComponentMaster = { elemId, name -> viewModel.createComponentMaster(elemId, name) },
                        onInsertComponentInstance = { masterId -> viewModel.insertComponentInstance(masterId) }
                    )
                }

                // Handoff & Export Sheet
                if (uiState.isHandoffSheetOpen) {
                    val primaryElement = uiState.primarySelectedId?.let { activeArtboard.findElement(it) }
                    HandoffExportSheet(
                        project = project,
                        activeArtboard = activeArtboard,
                        selectedElement = primaryElement,
                        onDismiss = { viewModel.toggleHandoffSheet(false) }
                    )
                }

                // Artboard Screen Manager Dialog
                if (uiState.isArtboardManagerOpen) {
                    ArtboardManagerDialog(
                        artboards = project.artboards,
                        activeArtboardId = project.activeArtboardId,
                        onDismiss = { viewModel.toggleArtboardManager(false) },
                        onSwitchArtboard = { artboardId -> viewModel.switchArtboard(artboardId) },
                        onAddArtboard = { name, w, h, bg -> viewModel.addArtboard(name, w, h, bg) },
                        onSetStartArtboard = { artboardId -> viewModel.setStartArtboard(artboardId) },
                        onRenameArtboard = { id, name -> viewModel.updateArtboardProperties(id, name, activeArtboard.backgroundColorHex, activeArtboard.isScrollable) },
                        onDeleteArtboard = { id -> viewModel.deleteArtboard(id) }
                    )
                }

                // Quick Insert Component Sheet
                if (showInsertSheet) {
                    InsertElementSheet(
                        onDismiss = { showInsertSheet = false },
                        onSelectType = { type -> viewModel.addElement(type) }
                    )
                }

                // Theme Selection Dialog
                if (showThemeDialog) {
                    ThemeSelectionDialog(onDismiss = { showThemeDialog = false })
                }

                // Gesture & Studio Guide Dialog
                if (showGuideDialog) {
                    StudioGuideDialog(onDismiss = { showGuideDialog = false })
                }
            }
        }
    }
}

@Composable
private fun StudioDockBar(
    selectedCount: Int,
    hasClipboard: Boolean,
    onOpenInsertSheet: () -> Unit,
    onAddRectangle: () -> Unit,
    onAddCircle: () -> Unit,
    onAddLine: () -> Unit,
    onAddText: () -> Unit,
    onAddButton: () -> Unit,
    onAddImage: () -> Unit,
    onPaste: () -> Unit,
    onOpenDesignSystem: () -> Unit,
    onOpenLayers: () -> Unit,
    onOpenProperties: () -> Unit,
    onToggleCanvasControls: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ForgeSurface,
        modifier = modifier
            .border(1.dp, ForgeBorderLight, RoundedCornerShape(20.dp))
            .testTag("studio_dock_bar")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Main Insert Component "+" Button
            Surface(
                shape = CircleShape,
                color = ForgePrimary,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onOpenInsertSheet)
                    .testTag("open_insert_sheet_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Insert Component",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Box(modifier = Modifier.height(20.dp).width(1.dp).padding(horizontal = 1.dp).background(ForgeBorder))

            IconButton(onClick = onAddRectangle, modifier = Modifier.size(34.dp).testTag("add_rect_btn")) {
                Icon(Icons.Default.CropSquare, contentDescription = "Add Rectangle", tint = ForgePrimaryLight, modifier = Modifier.size(17.dp))
            }
            IconButton(onClick = onAddCircle, modifier = Modifier.size(34.dp).testTag("add_circle_btn")) {
                Icon(Icons.Default.Circle, contentDescription = "Add Circle", tint = ForgeAccentCyan, modifier = Modifier.size(17.dp))
            }
            IconButton(onClick = onAddText, modifier = Modifier.size(34.dp).testTag("add_text_btn")) {
                Icon(Icons.Default.TextFields, contentDescription = "Add Text", tint = ForgeTextPrimary, modifier = Modifier.size(17.dp))
            }
            IconButton(onClick = onAddButton, modifier = Modifier.size(34.dp).testTag("add_button_btn")) {
                Icon(Icons.Default.SmartButton, contentDescription = "Add Button", tint = ForgePrimary, modifier = Modifier.size(17.dp))
            }

            Box(modifier = Modifier.height(20.dp).width(1.dp).padding(horizontal = 1.dp).background(ForgeBorder))

            if (hasClipboard) {
                IconButton(onClick = onPaste, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = ForgePrimaryLight, modifier = Modifier.size(17.dp))
                }
            }

            IconButton(onClick = onOpenDesignSystem, modifier = Modifier.size(34.dp).testTag("open_design_system_btn")) {
                Icon(Icons.Default.Widgets, contentDescription = "Design System", tint = ForgeAccentCyan, modifier = Modifier.size(17.dp))
            }

            IconButton(onClick = onOpenLayers, modifier = Modifier.size(34.dp).testTag("open_layers_btn")) {
                Icon(Icons.Default.Layers, contentDescription = "Layers", tint = ForgeTextSecondary, modifier = Modifier.size(17.dp))
            }

            IconButton(
                onClick = onOpenProperties,
                enabled = selectedCount > 0,
                modifier = Modifier.size(34.dp).testTag("open_properties_btn")
            ) {
                Icon(Icons.Default.Tune, contentDescription = "Properties", tint = if (selectedCount > 0) ForgePrimary else ForgeTextMuted, modifier = Modifier.size(17.dp))
            }

            IconButton(onClick = onToggleCanvasControls, modifier = Modifier.size(34.dp).testTag("open_canvas_controls_btn")) {
                Icon(Icons.Default.FitScreen, contentDescription = "Canvas Controls", tint = ForgeTextSecondary, modifier = Modifier.size(17.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudioGuideDialog(onDismiss: () -> Unit) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ForgeSurface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ForgeBorder, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = ForgePrimary.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = ForgePrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Canvas & Studio Guide",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ForgeTextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ForgeTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val tips = listOf(
                    Triple("Pinch to Zoom", "Use two fingers to zoom smoothly in and out (10% to 500%). Double-tap Fit to reset.", Icons.Default.ZoomIn),
                    Triple("Two-Finger Pan", "Drag anywhere on canvas with two fingers to pan around without moving objects.", Icons.Default.OpenWith),
                    Triple("Select & Drag", "Single tap any element to select it and drag it freely across the artboard.", Icons.Default.TouchApp),
                    Triple("Corner Handles", "Drag corner points to resize elements with instant aspect-ratio feedback.", Icons.Default.CropSquare),
                    Triple("Pixel Nudge", "Tap the Nudge icon in the quick bar to move objects with 1dp or 8dp arrow buttons.", Icons.Default.KeyboardArrowUp),
                    Triple("1-Tap Themes", "Tap the palette icon in the top bar to switch between 5 workspace themes anytime.", Icons.Default.Palette),
                    Triple("Interactive Prototype", "Switch to Prototype mode, tap elements to link to other screens, and hit Play!", Icons.Default.PlayArrow)
                )

                LazyColumn(
                    modifier = Modifier.height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tips) { (title, desc, icon) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ForgeSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(icon, contentDescription = null, tint = ForgePrimaryLight, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = ForgeTextPrimary)
                                    Text(desc, style = MaterialTheme.typography.labelSmall, fontSize = 11.sp, color = ForgeTextSecondary)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ForgePrimary),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Got It!")
                }
            }
        }
    }
}
