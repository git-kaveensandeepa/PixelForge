package com.example.model

import java.util.UUID

enum class ElementType {
    RECTANGLE,
    CIRCLE,
    LINE,
    TEXT,
    BUTTON,
    IMAGE_PLACEHOLDER,
    ICON,
    TEXT_FIELD,
    SWITCH,
    CHECKBOX
}

enum class TextAlignOption {
    LEFT,
    CENTER,
    RIGHT
}

enum class TransitionType(val label: String) {
    INSTANT("Instant"),
    DISSOLVE("Dissolve"),
    SLIDE_LEFT("Slide Left"),
    SLIDE_UP("Slide Up"),
    BOTTOM_SHEET("Bottom Sheet Overlay")
}

data class DesignToken(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String, // "COLOR", "TYPOGRAPHY", "SPACING"
    val value: String
)

data class ComponentMaster(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val element: DesignElement
)

data class DesignElement(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Element",
    val type: ElementType = ElementType.RECTANGLE,
    val x: Float = 40f,
    val y: Float = 80f,
    val width: Float = 140f,
    val height: Float = 60f,
    val rotation: Float = 0f, // 0 - 360 degrees
    val fillColorHex: String = "#6366F1",
    val cornerRadius: Float = 8f,
    val strokeColorHex: String = "#00000000",
    val strokeWidth: Float = 0f,
    val opacity: Float = 1f,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val groupId: String? = null,
    // Gradients & Shadows
    val hasGradient: Boolean = false,
    val gradientStartHex: String? = null,
    val gradientEndHex: String? = null,
    val gradientAngle: Float = 0f,
    val hasShadow: Boolean = false,
    val shadowElevation: Float = 0f,
    // Icon specific
    val iconName: String = "home",
    // Interactive Form Controls
    val isChecked: Boolean = false,
    val placeholder: String = "Enter text...",
    // Component linkages
    val isComponentInstance: Boolean = false,
    val componentMasterId: String? = null,
    // Text and Button specific properties
    val text: String = "Label",
    val fontSize: Float = 16f,
    val textColorHex: String = "#FFFFFF",
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val textAlign: TextAlignOption = TextAlignOption.LEFT,
    val lineHeightMultiplier: Float = 1.2f
) {
    fun copyWithNewId(offset: Float = 16f): DesignElement {
        return copy(
            id = UUID.randomUUID().toString(),
            name = if (name.endsWith("Copy")) "$name 2" else "$name Copy",
            x = x + offset,
            y = y + offset
        )
    }
}

data class PrototypeConnection(
    val id: String = UUID.randomUUID().toString(),
    val sourceElementId: String,
    val targetArtboardId: String,
    val trigger: String = "TAP",
    val transition: TransitionType = TransitionType.INSTANT,
    val isOverlay: Boolean = false
)

data class Artboard(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Screen 1",
    val width: Float = 360f,
    val height: Float = 640f,
    val backgroundColorHex: String = "#FFFFFF",
    val elements: List<DesignElement> = emptyList(),
    val connections: List<PrototypeConnection> = emptyList(),
    val isStartArtboard: Boolean = false,
    val isScrollable: Boolean = true,
    val isDarkMode: Boolean = false
) {
    fun findElement(elementId: String): DesignElement? {
        return elements.find { it.id == elementId }
    }

    fun findConnectionForElement(elementId: String): PrototypeConnection? {
        return connections.find { it.sourceElementId == elementId }
    }
}

data class ProjectSettings(
    val showGrid: Boolean = true,
    val gridSize: Float = 16f,
    val snapToGrid: Boolean = false,
    val snapToElements: Boolean = true
)

data class Project(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Untitled Project",
    val version: Int = 2,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val artboards: List<Artboard> = listOf(Artboard(name = "Home Screen", isStartArtboard = true)),
    val activeArtboardId: String = artboards.firstOrNull()?.id ?: "",
    val settings: ProjectSettings = ProjectSettings(),
    val tokens: List<DesignToken> = defaultTokens(),
    val components: List<ComponentMaster> = emptyList()
) {
    fun getActiveArtboard(): Artboard {
        return artboards.find { it.id == activeArtboardId }
            ?: artboards.firstOrNull()
            ?: Artboard(name = "Main Screen", isStartArtboard = true)
    }

    fun updateActiveArtboard(updated: Artboard): Project {
        val updatedArtboards = artboards.map {
            if (it.id == updated.id) updated else it
        }
        return copy(artboards = updatedArtboards, updatedAt = System.currentTimeMillis())
    }

    companion object {
        fun defaultTokens(): List<DesignToken> = listOf(
            DesignToken(name = "color-primary", category = "COLOR", value = "#6366F1"),
            DesignToken(name = "color-primary-variant", category = "COLOR", value = "#4F46E5"),
            DesignToken(name = "color-accent-cyan", category = "COLOR", value = "#06B6D4"),
            DesignToken(name = "color-surface", category = "COLOR", value = "#161720"),
            DesignToken(name = "color-background", category = "COLOR", value = "#0F1015"),
            DesignToken(name = "color-text-main", category = "COLOR", value = "#F3F4F6"),
            DesignToken(name = "color-text-sub", category = "COLOR", value = "#9CA3AF"),
            DesignToken(name = "spacing-sm", category = "SPACING", value = "8dp"),
            DesignToken(name = "spacing-md", category = "SPACING", value = "16dp"),
            DesignToken(name = "spacing-lg", category = "SPACING", value = "24dp"),
            DesignToken(name = "radius-sm", category = "SPACING", value = "8dp"),
            DesignToken(name = "radius-md", category = "SPACING", value = "16dp")
        )
    }
}
