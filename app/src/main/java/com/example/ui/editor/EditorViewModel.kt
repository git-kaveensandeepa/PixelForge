package com.example.ui.editor

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ProjectRepository
import com.example.model.Artboard
import com.example.model.ComponentMaster
import com.example.model.DesignElement
import com.example.model.DesignToken
import com.example.model.ElementType
import com.example.model.Project
import com.example.model.PrototypeConnection
import com.example.model.TextAlignOption
import com.example.model.TransitionType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class EditorMode {
    DESIGN,
    PROTOTYPE
}

enum class SaveStatus {
    SAVED,
    SAVING,
    DIRTY
}

data class EditorUiState(
    val isLoading: Boolean = true,
    val project: Project? = null,
    val selectedElementIds: Set<String> = emptySet(),
    val primarySelectedId: String? = null,
    val mode: EditorMode = EditorMode.DESIGN,
    val canvasScale: Float = 1.0f,
    val canvasOffset: Offset = Offset.Zero,
    val saveStatus: SaveStatus = SaveStatus.SAVED,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isLayersSheetOpen: Boolean = false,
    val isPropertySheetOpen: Boolean = false,
    val isDesignSystemSheetOpen: Boolean = false,
    val isHandoffSheetOpen: Boolean = false,
    val isArtboardManagerOpen: Boolean = false,
    val showGrid: Boolean = true,
    val snapToGrid: Boolean = false,
    val snapToElements: Boolean = true,
    val clipboardElements: List<DesignElement> = emptyList(),
    val errorMessage: String? = null
)

class EditorViewModel(
    application: Application,
    private val projectId: String
) : AndroidViewModel(application) {

    private val repository = ProjectRepository(application)
    private val undoRedoManager = UndoRedoManager()

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private var autosaveJob: Job? = null

    init {
        loadProject()
    }

    private fun loadProject() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val proj = repository.loadProject(projectId)
            if (proj != null) {
                undoRedoManager.clear()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    project = proj,
                    showGrid = proj.settings.showGrid,
                    snapToGrid = proj.settings.snapToGrid,
                    snapToElements = proj.settings.snapToElements,
                    canUndo = false,
                    canRedo = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load project."
                )
            }
        }
    }

    private fun mutateProject(
        pushToHistory: Boolean = true,
        mutator: (Project) -> Project
    ) {
        val current = _uiState.value.project ?: return
        if (pushToHistory) {
            undoRedoManager.pushState(current)
        }
        val updated = mutator(current).copy(updatedAt = System.currentTimeMillis())
        _uiState.value = _uiState.value.copy(
            project = updated,
            canUndo = undoRedoManager.canUndo,
            canRedo = undoRedoManager.canRedo,
            saveStatus = SaveStatus.DIRTY
        )
        triggerAutosave()
    }

    private fun triggerAutosave() {
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            delay(500)
            _uiState.value = _uiState.value.copy(saveStatus = SaveStatus.SAVING)
            val current = _uiState.value.project
            if (current != null) {
                val success = repository.saveProject(current)
                _uiState.value = _uiState.value.copy(
                    saveStatus = if (success) SaveStatus.SAVED else SaveStatus.DIRTY
                )
            }
        }
    }

    // Selection
    fun selectElement(elementId: String?, isMultiToggle: Boolean = false) {
        if (elementId == null) {
            _uiState.value = _uiState.value.copy(
                selectedElementIds = emptySet(),
                primarySelectedId = null,
                isPropertySheetOpen = false
            )
            return
        }

        if (isMultiToggle) {
            val currentSet = _uiState.value.selectedElementIds.toMutableSet()
            if (currentSet.contains(elementId)) {
                currentSet.remove(elementId)
            } else {
                currentSet.add(elementId)
            }
            _uiState.value = _uiState.value.copy(
                selectedElementIds = currentSet,
                primarySelectedId = currentSet.lastOrNull(),
                isPropertySheetOpen = currentSet.isNotEmpty()
            )
        } else {
            _uiState.value = _uiState.value.copy(
                selectedElementIds = setOf(elementId),
                primarySelectedId = elementId,
                isPropertySheetOpen = true
            )
        }
    }

    fun selectAllElements() {
        val active = _uiState.value.project?.getActiveArtboard() ?: return
        val allIds = active.elements.map { it.id }.toSet()
        _uiState.value = _uiState.value.copy(
            selectedElementIds = allIds,
            primarySelectedId = allIds.firstOrNull(),
            isPropertySheetOpen = allIds.isNotEmpty()
        )
    }

    fun setMode(mode: EditorMode) {
        _uiState.value = _uiState.value.copy(mode = mode)
    }

    fun toggleLayersSheet(open: Boolean? = null) {
        _uiState.value = _uiState.value.copy(
            isLayersSheetOpen = open ?: !_uiState.value.isLayersSheetOpen
        )
    }

    fun togglePropertySheet(open: Boolean? = null) {
        _uiState.value = _uiState.value.copy(
            isPropertySheetOpen = open ?: !_uiState.value.isPropertySheetOpen
        )
    }

    fun toggleDesignSystemSheet(open: Boolean? = null) {
        _uiState.value = _uiState.value.copy(
            isDesignSystemSheetOpen = open ?: !_uiState.value.isDesignSystemSheetOpen
        )
    }

    fun toggleHandoffSheet(open: Boolean? = null) {
        _uiState.value = _uiState.value.copy(
            isHandoffSheetOpen = open ?: !_uiState.value.isHandoffSheetOpen
        )
    }

    fun toggleArtboardManager(open: Boolean? = null) {
        _uiState.value = _uiState.value.copy(
            isArtboardManagerOpen = open ?: !_uiState.value.isArtboardManagerOpen
        )
    }

    fun toggleGrid() {
        val newGrid = !_uiState.value.showGrid
        _uiState.value = _uiState.value.copy(showGrid = newGrid)
        mutateProject(pushToHistory = false) { proj ->
            proj.copy(settings = proj.settings.copy(showGrid = newGrid))
        }
    }

    fun toggleSnapToGrid() {
        val newSnap = !_uiState.value.snapToGrid
        _uiState.value = _uiState.value.copy(snapToGrid = newSnap)
        mutateProject(pushToHistory = false) { proj ->
            proj.copy(settings = proj.settings.copy(snapToGrid = newSnap))
        }
    }

    // Adding Elements
    fun addElement(type: ElementType) {
        val proj = _uiState.value.project ?: return
        val activeArtboard = proj.getActiveArtboard()
        val count = activeArtboard.elements.size + 1

        val newElement = when (type) {
            ElementType.RECTANGLE -> DesignElement(
                name = "Rectangle $count",
                type = ElementType.RECTANGLE,
                x = (activeArtboard.width / 2f) - 60f,
                y = (activeArtboard.height / 2f) - 40f,
                width = 120f,
                height = 80f,
                fillColorHex = "#6366F1",
                cornerRadius = 12f
            )
            ElementType.CIRCLE -> DesignElement(
                name = "Circle $count",
                type = ElementType.CIRCLE,
                x = (activeArtboard.width / 2f) - 45f,
                y = (activeArtboard.height / 2f) - 45f,
                width = 90f,
                height = 90f,
                fillColorHex = "#06B6D4"
            )
            ElementType.LINE -> DesignElement(
                name = "Divider $count",
                type = ElementType.LINE,
                x = 24f,
                y = (activeArtboard.height / 2f),
                width = activeArtboard.width - 48f,
                height = 2f,
                fillColorHex = "#374151",
                strokeColorHex = "#374151",
                strokeWidth = 2f
            )
            ElementType.TEXT -> DesignElement(
                name = "Text $count",
                type = ElementType.TEXT,
                x = 24f,
                y = (activeArtboard.height / 2f) - 16f,
                width = activeArtboard.width - 48f,
                height = 36f,
                text = "Heading Text",
                fontSize = 20f,
                textColorHex = if (activeArtboard.backgroundColorHex.uppercase() == "#FFFFFF") "#111827" else "#F9FAFB",
                isBold = true
            )
            ElementType.BUTTON -> DesignElement(
                name = "Button $count",
                type = ElementType.BUTTON,
                x = 24f,
                y = (activeArtboard.height / 2f) - 24f,
                width = activeArtboard.width - 48f,
                height = 50f,
                fillColorHex = "#6366F1",
                cornerRadius = 14f,
                text = "Action Button",
                fontSize = 15f,
                textColorHex = "#FFFFFF",
                isBold = true
            )
            ElementType.IMAGE_PLACEHOLDER -> DesignElement(
                name = "Image Placeholder $count",
                type = ElementType.IMAGE_PLACEHOLDER,
                x = 24f,
                y = (activeArtboard.height / 2f) - 60f,
                width = activeArtboard.width - 48f,
                height = 120f,
                fillColorHex = "#1E293B",
                cornerRadius = 14f,
                strokeColorHex = "#334155",
                strokeWidth = 1f
            )
            ElementType.ICON -> DesignElement(
                name = "Icon $count",
                type = ElementType.ICON,
                x = (activeArtboard.width / 2f) - 24f,
                y = (activeArtboard.height / 2f) - 24f,
                width = 48f,
                height = 48f,
                iconName = "star",
                fillColorHex = "#00000000",
                textColorHex = "#6366F1",
                cornerRadius = 8f
            )
            ElementType.TEXT_FIELD -> DesignElement(
                name = "Text Field $count",
                type = ElementType.TEXT_FIELD,
                x = 24f,
                y = (activeArtboard.height / 2f) - 26f,
                width = activeArtboard.width - 48f,
                height = 52f,
                fillColorHex = if (activeArtboard.backgroundColorHex.uppercase() == "#FFFFFF") "#F3F4F6" else "#1F2937",
                strokeColorHex = "#6366F1",
                strokeWidth = 1.5f,
                cornerRadius = 12f,
                placeholder = "Enter text...",
                text = "",
                textColorHex = if (activeArtboard.backgroundColorHex.uppercase() == "#FFFFFF") "#111827" else "#F9FAFB",
                fontSize = 15f
            )
            ElementType.SWITCH -> DesignElement(
                name = "Switch $count",
                type = ElementType.SWITCH,
                x = (activeArtboard.width / 2f) - 28f,
                y = (activeArtboard.height / 2f) - 16f,
                width = 56f,
                height = 32f,
                isChecked = true,
                fillColorHex = "#6366F1",
                textColorHex = "#FFFFFF"
            )
            ElementType.CHECKBOX -> DesignElement(
                name = "Checkbox $count",
                type = ElementType.CHECKBOX,
                x = 24f,
                y = (activeArtboard.height / 2f) - 16f,
                width = 160f,
                height = 32f,
                isChecked = true,
                fillColorHex = "#6366F1",
                textColorHex = if (activeArtboard.backgroundColorHex.uppercase() == "#FFFFFF") "#111827" else "#F9FAFB",
                text = "Option Label",
                fontSize = 14f,
                cornerRadius = 6f
            )
        }

        mutateProject(pushToHistory = true) { p ->
            val updatedArtboard = activeArtboard.copy(
                elements = activeArtboard.elements + newElement
            )
            p.updateActiveArtboard(updatedArtboard)
        }

        selectElement(newElement.id, false)
    }

    // Moving and Resizing
    fun moveElements(deltas: Map<String, Pair<Float, Float>>, commit: Boolean) {
        val proj = _uiState.value.project ?: return
        val activeArtboard = proj.getActiveArtboard()

        mutateProject(pushToHistory = commit) { p ->
            val updatedElements = activeArtboard.elements.map { elem ->
                val delta = deltas[elem.id]
                if (delta != null) {
                    elem.copy(
                        x = elem.x + delta.first,
                        y = elem.y + delta.second
                    )
                } else elem
            }
            p.updateActiveArtboard(activeArtboard.copy(elements = updatedElements))
        }
    }

    fun updateElementBounds(
        elementId: String,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        commit: Boolean
    ) {
        val proj = _uiState.value.project ?: return
        val activeArtboard = proj.getActiveArtboard()

        mutateProject(pushToHistory = commit) { p ->
            val updatedElements = activeArtboard.elements.map {
                if (it.id == elementId) {
                    it.copy(
                        x = x,
                        y = y,
                        width = width.coerceAtLeast(10f),
                        height = height.coerceAtLeast(10f)
                    )
                } else it
            }
            p.updateActiveArtboard(activeArtboard.copy(elements = updatedElements))
        }
    }

    fun updateElementProperties(updatedElement: DesignElement) {
        val proj = _uiState.value.project ?: return
        val activeArtboard = proj.getActiveArtboard()

        mutateProject(pushToHistory = true) { p ->
            val updatedElements = activeArtboard.elements.map {
                if (it.id == updatedElement.id) updatedElement else it
            }
            p.updateActiveArtboard(activeArtboard.copy(elements = updatedElements))
        }
    }

    // Alignment and Distribution
    fun alignSelected(alignment: String) {
        val proj = _uiState.value.project ?: return
        val activeArtboard = proj.getActiveArtboard()
        val targets = activeArtboard.elements.filter { _uiState.value.selectedElementIds.contains(it.id) }
        if (targets.isEmpty()) return

        mutateProject(pushToHistory = true) { p ->
            val updatedElements = activeArtboard.elements.map { elem ->
                if (_uiState.value.selectedElementIds.contains(elem.id)) {
                    when (alignment) {
                        "LEFT" -> {
                            val minX = targets.minOf { it.x }
                            elem.copy(x = minX)
                        }
                        "CENTER" -> {
                            if (targets.size > 1) {
                                val minX = targets.minOf { it.x }
                                val maxX = targets.maxOf { it.x + it.width }
                                val groupCenter = (minX + maxX) / 2f
                                elem.copy(x = groupCenter - elem.width / 2f)
                            } else {
                                elem.copy(x = (activeArtboard.width - elem.width) / 2f)
                            }
                        }
                        "RIGHT" -> {
                            val maxRight = targets.maxOf { it.x + it.width }
                            elem.copy(x = maxRight - elem.width)
                        }
                        "TOP" -> {
                            val minY = targets.minOf { it.y }
                            elem.copy(y = minY)
                        }
                        "MIDDLE" -> {
                            if (targets.size > 1) {
                                val minY = targets.minOf { it.y }
                                val maxY = targets.maxOf { it.y + it.height }
                                val groupMiddle = (minY + maxY) / 2f
                                elem.copy(y = groupMiddle - elem.height / 2f)
                            } else {
                                elem.copy(y = (activeArtboard.height - elem.height) / 2f)
                            }
                        }
                        "BOTTOM" -> {
                            val maxBottom = targets.maxOf { it.y + it.height }
                            elem.copy(y = maxBottom - elem.height)
                        }
                        else -> elem
                    }
                } else elem
            }
            p.updateActiveArtboard(activeArtboard.copy(elements = updatedElements))
        }
    }

    fun distributeSelected(horizontal: Boolean) {
        val proj = _uiState.value.project ?: return
        val activeArtboard = proj.getActiveArtboard()
        val targets = activeArtboard.elements.filter { _uiState.value.selectedElementIds.contains(it.id) }
        if (targets.size < 3) return

        mutateProject(pushToHistory = true) { p ->
            val sorted = if (horizontal) targets.sortedBy { it.x } else targets.sortedBy { it.y }
            val first = sorted.first()
            val last = sorted.last()

            val totalSpan = if (horizontal) {
                (last.x + last.width) - first.x
            } else {
                (last.y + last.height) - first.y
            }
            val totalElementSpan = sorted.sumOf { if (horizontal) it.width.toDouble() else it.height.toDouble() }
            val spacing = ((totalSpan - totalElementSpan) / (sorted.size - 1)).toFloat()

            var currentOffset = if (horizontal) first.x else first.y
            val newPositions = mutableMapOf<String, Float>()
            for (elem in sorted) {
                newPositions[elem.id] = currentOffset
                currentOffset += (if (horizontal) elem.width else elem.height) + spacing
            }

            val updatedElements = activeArtboard.elements.map { elem ->
                val newPos = newPositions[elem.id]
                if (newPos != null) {
                    if (horizontal) elem.copy(x = newPos) else elem.copy(y = newPos)
                } else elem
            }
            p.updateActiveArtboard(activeArtboard.copy(elements = updatedElements))
        }
    }

    // Grouping
    fun groupSelected() {
        val proj = _uiState.value.project ?: return
        val activeArtboard = proj.getActiveArtboard()
        if (_uiState.value.selectedElementIds.size < 2) return

        val newGroupId = UUID.randomUUID().toString()
        mutateProject(pushToHistory = true) { p ->
            val updated = activeArtboard.elements.map {
                if (_uiState.value.selectedElementIds.contains(it.id)) {
                    it.copy(groupId = newGroupId)
                } else it
            }
            p.updateActiveArtboard(activeArtboard.copy(elements = updated))
        }
    }

    fun ungroupSelected() {
        val proj = _uiState.value.project ?: return
        val activeArtboard = proj.getActiveArtboard()
        mutateProject(pushToHistory = true) { p ->
            val updated = activeArtboard.elements.map {
                if (_uiState.value.selectedElementIds.contains(it.id)) {
                    it.copy(groupId = null)
                } else it
            }
            p.updateActiveArtboard(activeArtboard.copy(elements = updated))
        }
    }

    // Layer Depth (Z-Order)
    fun bringToFront() {
        val proj = _uiState.value.project ?: return
        val activeArtboard = proj.getActiveArtboard()
        val selIds = _uiState.value.selectedElementIds
        if (selIds.isEmpty()) return

        val selected = activeArtboard.elements.filter { selIds.contains(it.id) }
        val rest = activeArtboard.elements.filterNot { selIds.contains(it.id) }
        mutateProject(pushToHistory = true) { p ->
            p.updateActiveArtboard(activeArtboard.copy(elements = rest + selected))
        }
    }

    fun sendToBack() {
        val proj = _uiState.value.project ?: return
        val activeArtboard = proj.getActiveArtboard()
        val selIds = _uiState.value.selectedElementIds
        if (selIds.isEmpty()) return

        val selected = activeArtboard.elements.filter { selIds.contains(it.id) }
        val rest = activeArtboard.elements.filterNot { selIds.contains(it.id) }
        mutateProject(pushToHistory = true) { p ->
            p.updateActiveArtboard(activeArtboard.copy(elements = selected + rest))
        }
    }

    fun bringForward() {
        val selId = _uiState.value.primarySelectedId ?: return
        val proj = _uiState.value.project ?: return
        val activeArtboard = proj.getActiveArtboard()
        val list = activeArtboard.elements.toMutableList()
        val idx = list.indexOfFirst { it.id == selId }
        if (idx in 0 until list.lastIndex) {
            val item = list.removeAt(idx)
            list.add(idx + 1, item)
            mutateProject(pushToHistory = true) { p ->
                p.updateActiveArtboard(activeArtboard.copy(elements = list))
            }
        }
    }

    fun sendBackward() {
        val selId = _uiState.value.primarySelectedId ?: return
        val proj = _uiState.value.project ?: return
        val activeArtboard = proj.getActiveArtboard()
        val list = activeArtboard.elements.toMutableList()
        val idx = list.indexOfFirst { it.id == selId }
        if (idx > 0) {
            val item = list.removeAt(idx)
            list.add(idx - 1, item)
            mutateProject(pushToHistory = true) { p ->
                p.updateActiveArtboard(activeArtboard.copy(elements = list))
            }
        }
    }

    // Clipboard
    fun copySelected() {
        val proj = _uiState.value.project ?: return
        val activeArtboard = proj.getActiveArtboard()
        val selected = activeArtboard.elements.filter { _uiState.value.selectedElementIds.contains(it.id) }
        if (selected.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(clipboardElements = selected)
        }
    }

    fun pasteClipboard() {
        val clip = _uiState.value.clipboardElements
        if (clip.isEmpty()) return
        val proj = _uiState.value.project ?: return
        val activeArtboard = proj.getActiveArtboard()

        val newCopies = clip.map { it.copyWithNewId(20f) }
        mutateProject(pushToHistory = true) { p ->
            p.updateActiveArtboard(activeArtboard.copy(elements = activeArtboard.elements + newCopies))
        }
        _uiState.value = _uiState.value.copy(
            selectedElementIds = newCopies.map { it.id }.toSet(),
            primarySelectedId = newCopies.first().id
        )
    }

    fun duplicateSelected() {
        val proj = _uiState.value.project ?: return
        val activeArtboard = proj.getActiveArtboard()
        val selected = activeArtboard.elements.filter { _uiState.value.selectedElementIds.contains(it.id) }
        if (selected.isEmpty()) return

        val newCopies = selected.map { it.copyWithNewId(16f) }
        mutateProject(pushToHistory = true) { p ->
            p.updateActiveArtboard(activeArtboard.copy(elements = activeArtboard.elements + newCopies))
        }
        _uiState.value = _uiState.value.copy(
            selectedElementIds = newCopies.map { it.id }.toSet(),
            primarySelectedId = newCopies.first().id
        )
    }

    fun deleteSelected() {
        val proj = _uiState.value.project ?: return
        val activeArtboard = proj.getActiveArtboard()
        val selIds = _uiState.value.selectedElementIds
        if (selIds.isEmpty()) return

        mutateProject(pushToHistory = true) { p ->
            val remainingElements = activeArtboard.elements.filterNot { selIds.contains(it.id) }
            val remainingConnections = activeArtboard.connections.filterNot { selIds.contains(it.sourceElementId) }
            p.updateActiveArtboard(activeArtboard.copy(elements = remainingElements, connections = remainingConnections))
        }
        _uiState.value = _uiState.value.copy(
            selectedElementIds = emptySet(),
            primarySelectedId = null,
            isPropertySheetOpen = false
        )
    }

    fun toggleElementVisibility(elementId: String) {
        val proj = _uiState.value.project ?: return
        val activeArtboard = proj.getActiveArtboard()
        mutateProject(pushToHistory = true) { p ->
            val updated = activeArtboard.elements.map {
                if (it.id == elementId) it.copy(isVisible = !it.isVisible) else it
            }
            p.updateActiveArtboard(activeArtboard.copy(elements = updated))
        }
    }

    fun toggleElementLock(elementId: String) {
        val proj = _uiState.value.project ?: return
        val activeArtboard = proj.getActiveArtboard()
        mutateProject(pushToHistory = true) { p ->
            val updated = activeArtboard.elements.map {
                if (it.id == elementId) it.copy(isLocked = !it.isLocked) else it
            }
            p.updateActiveArtboard(activeArtboard.copy(elements = updated))
        }
    }

    // Component Masters & Design System
    fun createComponentMaster(elementId: String, name: String) {
        val proj = _uiState.value.project ?: return
        val activeArtboard = proj.getActiveArtboard()
        val element = activeArtboard.findElement(elementId) ?: return

        val compMaster = ComponentMaster(
            name = name.ifBlank { element.name },
            element = element
        )

        mutateProject(pushToHistory = true) { p ->
            p.copy(components = p.components + compMaster)
        }
    }

    fun insertComponentInstance(componentMasterId: String) {
        val proj = _uiState.value.project ?: return
        val master = proj.components.find { it.id == componentMasterId } ?: return
        val activeArtboard = proj.getActiveArtboard()

        val instance = master.element.copy(
            id = UUID.randomUUID().toString(),
            name = "${master.name} Instance",
            x = (activeArtboard.width - master.element.width) / 2f,
            y = (activeArtboard.height - master.element.height) / 2f,
            isComponentInstance = true,
            componentMasterId = master.id
        )

        mutateProject(pushToHistory = true) { p ->
            p.updateActiveArtboard(activeArtboard.copy(elements = activeArtboard.elements + instance))
        }
        selectElement(instance.id, false)
    }

    fun insertPrebuiltBlock(blockType: String) {
        val proj = _uiState.value.project ?: return
        val activeArtboard = proj.getActiveArtboard()
        val aw = activeArtboard.width

        val newElements = when (blockType) {
            "TOP_APP_BAR" -> listOf(
                DesignElement(
                    name = "Top Bar Container",
                    type = ElementType.RECTANGLE,
                    x = 0f,
                    y = 0f,
                    width = aw,
                    height = 64f,
                    fillColorHex = "#1E293B",
                    cornerRadius = 0f
                ),
                DesignElement(
                    name = "App Bar Title",
                    type = ElementType.TEXT,
                    x = 20f,
                    y = 20f,
                    width = 200f,
                    height = 24f,
                    text = "Screen Title",
                    fontSize = 18f,
                    isBold = true,
                    textColorHex = "#F8FAFC"
                )
            )
            "HERO_CARD" -> listOf(
                DesignElement(
                    name = "Hero Card Bg",
                    type = ElementType.RECTANGLE,
                    x = 20f,
                    y = 80f,
                    width = aw - 40f,
                    height = 140f,
                    fillColorHex = "#6366F1",
                    cornerRadius = 18f
                ),
                DesignElement(
                    name = "Hero Title",
                    type = ElementType.TEXT,
                    x = 36f,
                    y = 104f,
                    width = aw - 72f,
                    height = 28f,
                    text = "Special Offer",
                    fontSize = 20f,
                    textColorHex = "#FFFFFF",
                    isBold = true
                ),
                DesignElement(
                    name = "Hero Subtext",
                    type = ElementType.TEXT,
                    x = 36f,
                    y = 138f,
                    width = aw - 72f,
                    height = 40f,
                    text = "Get 50% discount on all offline UI kits today.",
                    fontSize = 13f,
                    textColorHex = "#E0E7FF"
                )
            )
            "INPUT_FIELD" -> listOf(
                DesignElement(
                    name = "Input Box",
                    type = ElementType.RECTANGLE,
                    x = 20f,
                    y = 120f,
                    width = aw - 40f,
                    height = 52f,
                    fillColorHex = "#1E293B",
                    cornerRadius = 12f,
                    strokeColorHex = "#334155",
                    strokeWidth = 1f
                ),
                DesignElement(
                    name = "Input Placeholder",
                    type = ElementType.TEXT,
                    x = 36f,
                    y = 136f,
                    width = aw - 72f,
                    height = 20f,
                    text = "Enter text...",
                    fontSize = 14f,
                    textColorHex = "#64748B"
                )
            )
            "BOTTOM_NAV" -> listOf(
                DesignElement(
                    name = "Nav Bar Background",
                    type = ElementType.RECTANGLE,
                    x = 0f,
                    y = activeArtboard.height - 64f,
                    width = aw,
                    height = 64f,
                    fillColorHex = "#1E293B",
                    cornerRadius = 0f
                ),
                DesignElement(
                    name = "Nav Tab 1",
                    type = ElementType.TEXT,
                    x = (aw / 6f) - 24f,
                    y = activeArtboard.height - 40f,
                    width = 48f,
                    height = 20f,
                    text = "Home",
                    fontSize = 12f,
                    textColorHex = "#6366F1",
                    textAlign = TextAlignOption.CENTER,
                    isBold = true
                ),
                DesignElement(
                    name = "Nav Tab 2",
                    type = ElementType.TEXT,
                    x = (aw / 2f) - 24f,
                    y = activeArtboard.height - 40f,
                    width = 48f,
                    height = 20f,
                    text = "Explore",
                    fontSize = 12f,
                    textColorHex = "#94A3B8",
                    textAlign = TextAlignOption.CENTER
                ),
                DesignElement(
                    name = "Nav Tab 3",
                    type = ElementType.TEXT,
                    x = (aw * 5f / 6f) - 24f,
                    y = activeArtboard.height - 40f,
                    width = 48f,
                    height = 20f,
                    text = "Profile",
                    fontSize = 12f,
                    textColorHex = "#94A3B8",
                    textAlign = TextAlignOption.CENTER
                )
            )
            "PROFILE_CARD" -> listOf(
                DesignElement(
                    name = "Profile Card Bg",
                    type = ElementType.RECTANGLE,
                    x = 20f,
                    y = 80f,
                    width = aw - 40f,
                    height = 160f,
                    fillColorHex = "#1E293B",
                    cornerRadius = 20f,
                    hasShadow = true,
                    shadowElevation = 4f
                ),
                DesignElement(
                    name = "Avatar Circle",
                    type = ElementType.CIRCLE,
                    x = 36f,
                    y = 96f,
                    width = 56f,
                    height = 56f,
                    fillColorHex = "#6366F1"
                ),
                DesignElement(
                    name = "Profile Name",
                    type = ElementType.TEXT,
                    x = 104f,
                    y = 100f,
                    width = aw - 150f,
                    height = 24f,
                    text = "Sarah Connor",
                    fontSize = 16f,
                    textColorHex = "#F8FAFC",
                    isBold = true
                ),
                DesignElement(
                    name = "Profile Handle",
                    type = ElementType.TEXT,
                    x = 104f,
                    y = 126f,
                    width = aw - 150f,
                    height = 20f,
                    text = "@sarah.design",
                    fontSize = 12f,
                    textColorHex = "#94A3B8"
                ),
                DesignElement(
                    name = "Follow Button",
                    type = ElementType.BUTTON,
                    x = 36f,
                    y = 172f,
                    width = aw - 72f,
                    height = 44f,
                    fillColorHex = "#6366F1",
                    cornerRadius = 12f,
                    text = "Follow Profile",
                    fontSize = 14f,
                    textColorHex = "#FFFFFF",
                    isBold = true
                )
            )
            "PRODUCT_CARD" -> listOf(
                DesignElement(
                    name = "Product Card Container",
                    type = ElementType.RECTANGLE,
                    x = 20f,
                    y = 70f,
                    width = aw - 40f,
                    height = 220f,
                    fillColorHex = "#1E293B",
                    cornerRadius = 18f,
                    hasShadow = true,
                    shadowElevation = 6f
                ),
                DesignElement(
                    name = "Product Image Slot",
                    type = ElementType.IMAGE_PLACEHOLDER,
                    x = 32f,
                    y = 82f,
                    width = aw - 64f,
                    height = 95f,
                    cornerRadius = 12f,
                    fillColorHex = "#0F172A"
                ),
                DesignElement(
                    name = "Product Title",
                    type = ElementType.TEXT,
                    x = 32f,
                    y = 188f,
                    width = aw - 140f,
                    height = 22f,
                    text = "Minimalist Wireless Earbuds",
                    fontSize = 14f,
                    textColorHex = "#F8FAFC",
                    isBold = true
                ),
                DesignElement(
                    name = "Product Price",
                    type = ElementType.TEXT,
                    x = 32f,
                    y = 212f,
                    width = 80f,
                    height = 24f,
                    text = "$89.00",
                    fontSize = 16f,
                    textColorHex = "#38BDF8",
                    isBold = true
                ),
                DesignElement(
                    name = "Buy Button",
                    type = ElementType.BUTTON,
                    x = aw - 120f,
                    y = 242f,
                    width = 88f,
                    height = 36f,
                    fillColorHex = "#10B981",
                    cornerRadius = 10f,
                    text = "Add +",
                    fontSize = 13f,
                    textColorHex = "#FFFFFF",
                    isBold = true
                )
            )
            "METRIC_CARD" -> listOf(
                DesignElement(
                    name = "Metric Card Bg",
                    type = ElementType.RECTANGLE,
                    x = 20f,
                    y = 90f,
                    width = aw - 40f,
                    height = 110f,
                    fillColorHex = "#1E293B",
                    cornerRadius = 16f,
                    hasGradient = true,
                    gradientStartHex = "#1E293B",
                    gradientEndHex = "#283548",
                    hasShadow = true,
                    shadowElevation = 3f
                ),
                DesignElement(
                    name = "Metric Title",
                    type = ElementType.TEXT,
                    x = 36f,
                    y = 106f,
                    width = aw - 72f,
                    height = 20f,
                    text = "TOTAL REVENUE",
                    fontSize = 11f,
                    textColorHex = "#94A3B8",
                    isBold = true
                ),
                DesignElement(
                    name = "Metric Value",
                    type = ElementType.TEXT,
                    x = 36f,
                    y = 130f,
                    width = aw - 72f,
                    height = 36f,
                    text = "$42,850.00",
                    fontSize = 24f,
                    textColorHex = "#F8FAFC",
                    isBold = true
                ),
                DesignElement(
                    name = "Trend Badge",
                    type = ElementType.TEXT,
                    x = 36f,
                    y = 168f,
                    width = 150f,
                    height = 20f,
                    text = "+24.8% vs last month",
                    fontSize = 12f,
                    textColorHex = "#10B981"
                )
            )
            "SEARCH_BAR" -> listOf(
                DesignElement(
                    name = "Search Bar Background",
                    type = ElementType.RECTANGLE,
                    x = 20f,
                    y = 40f,
                    width = aw - 40f,
                    height = 48f,
                    fillColorHex = "#1E293B",
                    cornerRadius = 24f,
                    strokeColorHex = "#334155",
                    strokeWidth = 1f
                ),
                DesignElement(
                    name = "Search Icon",
                    type = ElementType.ICON,
                    x = 34f,
                    y = 52f,
                    width = 24f,
                    height = 24f,
                    iconName = "search",
                    fillColorHex = "#94A3B8"
                ),
                DesignElement(
                    name = "Search Placeholder",
                    type = ElementType.TEXT,
                    x = 68f,
                    y = 54f,
                    width = aw - 110f,
                    height = 20f,
                    text = "Search designs, kits, layers...",
                    fontSize = 13f,
                    textColorHex = "#64748B"
                )
            )
            "SETTINGS_ROW" -> listOf(
                DesignElement(
                    name = "Settings Row Container",
                    type = ElementType.RECTANGLE,
                    x = 20f,
                    y = 80f,
                    width = aw - 40f,
                    height = 60f,
                    fillColorHex = "#1E293B",
                    cornerRadius = 14f
                ),
                DesignElement(
                    name = "Setting Title",
                    type = ElementType.TEXT,
                    x = 36f,
                    y = 92f,
                    width = aw - 140f,
                    height = 20f,
                    text = "Push Notifications",
                    fontSize = 14f,
                    textColorHex = "#F8FAFC",
                    isBold = true
                ),
                DesignElement(
                    name = "Setting Subtitle",
                    type = ElementType.TEXT,
                    x = 36f,
                    y = 114f,
                    width = aw - 140f,
                    height = 16f,
                    text = "Receive alerts for project changes",
                    fontSize = 11f,
                    textColorHex = "#94A3B8"
                ),
                DesignElement(
                    name = "Settings Switch",
                    type = ElementType.SWITCH,
                    x = aw - 84f,
                    y = 94f,
                    width = 48f,
                    height = 28f,
                    isChecked = true,
                    fillColorHex = "#6366F1"
                )
            )
            else -> emptyList()
        }

        if (newElements.isNotEmpty()) {
            mutateProject(pushToHistory = true) { p ->
                p.updateActiveArtboard(activeArtboard.copy(elements = activeArtboard.elements + newElements))
            }
            selectElement(newElements.first().id, false)
        }
    }

    // Precision Nudge & Quick Styling for Mobile
    fun nudgeSelected(dx: Float, dy: Float) {
        val sel = _uiState.value.selectedElementIds
        if (sel.isEmpty()) return
        val deltas = sel.associateWith { Pair(dx, dy) }
        moveElements(deltas, commit = true)
    }

    fun setQuickColorForSelected(hexColor: String) {
        val proj = _uiState.value.project ?: return
        val activeArtboard = proj.getActiveArtboard()
        val selIds = _uiState.value.selectedElementIds
        if (selIds.isEmpty()) return
        mutateProject(pushToHistory = true) { p ->
            val updated = activeArtboard.elements.map { elem ->
                if (selIds.contains(elem.id)) {
                    if (elem.type == ElementType.TEXT) {
                        elem.copy(textColorHex = hexColor)
                    } else {
                        elem.copy(fillColorHex = hexColor)
                    }
                } else elem
            }
            p.updateActiveArtboard(activeArtboard.copy(elements = updated))
        }
    }

    fun setQuickGradientForSelected(startHex: String, endHex: String) {
        val proj = _uiState.value.project ?: return
        val activeArtboard = proj.getActiveArtboard()
        val selIds = _uiState.value.selectedElementIds
        if (selIds.isEmpty()) return
        mutateProject(pushToHistory = true) { p ->
            val updated = activeArtboard.elements.map { elem ->
                if (selIds.contains(elem.id)) {
                    elem.copy(
                        hasGradient = true,
                        gradientStartHex = startHex,
                        gradientEndHex = endHex
                    )
                } else elem
            }
            p.updateActiveArtboard(activeArtboard.copy(elements = updated))
        }
    }

    fun setQuickShadowForSelected(elevation: Float) {
        val proj = _uiState.value.project ?: return
        val activeArtboard = proj.getActiveArtboard()
        val selIds = _uiState.value.selectedElementIds
        if (selIds.isEmpty()) return
        mutateProject(pushToHistory = true) { p ->
            val updated = activeArtboard.elements.map { elem ->
                if (selIds.contains(elem.id)) {
                    elem.copy(
                        hasShadow = elevation > 0f,
                        shadowElevation = elevation
                    )
                } else elem
            }
            p.updateActiveArtboard(activeArtboard.copy(elements = updated))
        }
    }

    // Artboard Management
    fun switchArtboard(artboardId: String) {
        val proj = _uiState.value.project ?: return
        if (proj.artboards.any { it.id == artboardId }) {
            _uiState.value = _uiState.value.copy(
                project = proj.copy(activeArtboardId = artboardId),
                selectedElementIds = emptySet(),
                primarySelectedId = null,
                isPropertySheetOpen = false
            )
            triggerAutosave()
        }
    }

    fun addArtboard(name: String, width: Float = 360f, height: Float = 640f, bgColor: String = "#0F172A") {
        val newArtboard = Artboard(
            name = name.ifBlank { "Screen ${_uiState.value.project?.artboards?.size?.plus(1) ?: 1}" },
            width = width,
            height = height,
            backgroundColorHex = bgColor,
            isStartArtboard = (_uiState.value.project?.artboards?.isEmpty() == true)
        )

        mutateProject(pushToHistory = true) { p ->
            p.copy(
                artboards = p.artboards + newArtboard,
                activeArtboardId = newArtboard.id
            )
        }
        selectElement(null, false)
    }

    fun updateArtboardProperties(artboardId: String, name: String, bgColorHex: String, isScrollable: Boolean) {
        val proj = _uiState.value.project ?: return
        mutateProject(pushToHistory = true) { p ->
            val updatedList = p.artboards.map {
                if (it.id == artboardId) it.copy(name = name, backgroundColorHex = bgColorHex, isScrollable = isScrollable) else it
            }
            p.copy(artboards = updatedList)
        }
    }

    fun setStartArtboard(artboardId: String) {
        mutateProject(pushToHistory = true) { p ->
            val updatedList = p.artboards.map {
                it.copy(isStartArtboard = it.id == artboardId)
            }
            p.copy(artboards = updatedList)
        }
    }

    fun deleteArtboard(artboardId: String) {
        val proj = _uiState.value.project ?: return
        if (proj.artboards.size <= 1) return

        mutateProject(pushToHistory = true) { p ->
            val updatedList = p.artboards.filterNot { it.id == artboardId }
            val nextActive = if (p.activeArtboardId == artboardId) updatedList.first().id else p.activeArtboardId
            p.copy(artboards = updatedList, activeArtboardId = nextActive)
        }
        selectElement(null, false)
    }

    // Prototyping Connections
    fun connectElementToArtboard(
        sourceElementId: String,
        targetArtboardId: String,
        transition: TransitionType = TransitionType.INSTANT,
        isOverlay: Boolean = false
    ) {
        val proj = _uiState.value.project ?: return
        val activeArtboard = proj.getActiveArtboard()

        mutateProject(pushToHistory = true) { p ->
            val filtered = activeArtboard.connections.filterNot { it.sourceElementId == sourceElementId }
            val newConnection = PrototypeConnection(
                sourceElementId = sourceElementId,
                targetArtboardId = targetArtboardId,
                transition = transition,
                isOverlay = isOverlay
            )
            p.updateActiveArtboard(activeArtboard.copy(connections = filtered + newConnection))
        }
    }

    fun removeConnection(sourceElementId: String) {
        val proj = _uiState.value.project ?: return
        val activeArtboard = proj.getActiveArtboard()

        mutateProject(pushToHistory = true) { p ->
            val filtered = activeArtboard.connections.filterNot { it.sourceElementId == sourceElementId }
            p.updateActiveArtboard(activeArtboard.copy(connections = filtered))
        }
    }

    fun getBrokenConnections(): List<Pair<PrototypeConnection, String>> {
        val proj = _uiState.value.project ?: return emptyList()
        val allIds = proj.artboards.map { it.id }.toSet()
        val broken = mutableListOf<Pair<PrototypeConnection, String>>()
        for (ab in proj.artboards) {
            for (conn in ab.connections) {
                if (!allIds.contains(conn.targetArtboardId)) {
                    broken.add(Pair(conn, ab.name))
                }
            }
        }
        return broken
    }

    // Canvas Transformation
    fun updateCanvasTransform(scaleChange: Float, panChange: Offset) {
        val newScale = (_uiState.value.canvasScale * scaleChange).coerceIn(0.25f, 3.5f)
        val newOffset = _uiState.value.canvasOffset + panChange
        _uiState.value = _uiState.value.copy(
            canvasScale = newScale,
            canvasOffset = newOffset
        )
    }

    fun zoomIn() {
        val newScale = (_uiState.value.canvasScale * 1.25f).coerceAtMost(3.5f)
        _uiState.value = _uiState.value.copy(canvasScale = newScale)
    }

    fun zoomOut() {
        val newScale = (_uiState.value.canvasScale / 1.25f).coerceAtLeast(0.25f)
        _uiState.value = _uiState.value.copy(canvasScale = newScale)
    }

    fun resetZoom() {
        _uiState.value = _uiState.value.copy(
            canvasScale = 1.0f,
            canvasOffset = Offset.Zero
        )
    }

    fun fitToScreen(containerWidthPx: Float, containerHeightPx: Float, density: Float) {
        val artboard = _uiState.value.project?.getActiveArtboard() ?: return
        val artboardWidthPx = artboard.width * density
        val artboardHeightPx = artboard.height * density

        val paddingPx = 48f * density
        val availWidth = (containerWidthPx - paddingPx).coerceAtLeast(100f)
        val availHeight = (containerHeightPx - paddingPx).coerceAtLeast(100f)

        val scaleX = availWidth / artboardWidthPx
        val scaleY = availHeight / artboardHeightPx
        val targetScale = minOf(scaleX, scaleY).coerceIn(0.3f, 2.0f)

        _uiState.value = _uiState.value.copy(
            canvasScale = targetScale,
            canvasOffset = Offset.Zero
        )
    }

    // Undo / Redo
    fun undo() {
        val current = _uiState.value.project ?: return
        val previous = undoRedoManager.undo(current)
        if (previous != null) {
            _uiState.value = _uiState.value.copy(
                project = previous,
                canUndo = undoRedoManager.canUndo,
                canRedo = undoRedoManager.canRedo,
                saveStatus = SaveStatus.DIRTY
            )
            triggerAutosave()
        }
    }

    fun redo() {
        val current = _uiState.value.project ?: return
        val next = undoRedoManager.redo(current)
        if (next != null) {
            _uiState.value = _uiState.value.copy(
                project = next,
                canUndo = undoRedoManager.canUndo,
                canRedo = undoRedoManager.canRedo,
                saveStatus = SaveStatus.DIRTY
            )
            triggerAutosave()
        }
    }
}
