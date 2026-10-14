package com.example.data

import com.example.model.Artboard
import com.example.model.ComponentMaster
import com.example.model.DesignElement
import com.example.model.DesignToken
import com.example.model.ElementType
import com.example.model.Project
import com.example.model.ProjectSettings
import com.example.model.PrototypeConnection
import com.example.model.TextAlignOption
import com.example.model.TransitionType
import org.json.JSONArray
import org.json.JSONObject

object ProjectSerializer {

    const val CURRENT_VERSION = 2

    fun serializeProject(project: Project): String {
        val root = JSONObject().apply {
            put("version", CURRENT_VERSION)
            put("id", project.id)
            put("name", project.name)
            put("createdAt", project.createdAt)
            put("updatedAt", project.updatedAt)
            put("activeArtboardId", project.activeArtboardId)

            // Settings
            put("settings", JSONObject().apply {
                put("showGrid", project.settings.showGrid)
                put("gridSize", project.settings.gridSize.toDouble())
                put("snapToGrid", project.settings.snapToGrid)
                put("snapToElements", project.settings.snapToElements)
            })

            // Design Tokens
            val tokensArray = JSONArray()
            project.tokens.forEach { token ->
                tokensArray.put(JSONObject().apply {
                    put("id", token.id)
                    put("name", token.name)
                    put("category", token.category)
                    put("value", token.value)
                })
            }
            put("tokens", tokensArray)

            // Component Masters
            val componentsArray = JSONArray()
            project.components.forEach { comp ->
                componentsArray.put(JSONObject().apply {
                    put("id", comp.id)
                    put("name", comp.name)
                    put("description", comp.description)
                    put("element", serializeElement(comp.element))
                })
            }
            put("components", componentsArray)

            // Artboards
            val artboardsArray = JSONArray()
            project.artboards.forEach { artboard ->
                artboardsArray.put(serializeArtboard(artboard))
            }
            put("artboards", artboardsArray)
        }
        return root.toString(2)
    }

    private fun serializeArtboard(artboard: Artboard): JSONObject {
        return JSONObject().apply {
            put("id", artboard.id)
            put("name", artboard.name)
            put("width", artboard.width.toDouble())
            put("height", artboard.height.toDouble())
            put("backgroundColorHex", artboard.backgroundColorHex)
            put("isStartArtboard", artboard.isStartArtboard)
            put("isScrollable", artboard.isScrollable)

            val elementsArray = JSONArray()
            artboard.elements.forEach { element ->
                elementsArray.put(serializeElement(element))
            }
            put("elements", elementsArray)

            val connectionsArray = JSONArray()
            artboard.connections.forEach { connection ->
                connectionsArray.put(serializeConnection(connection))
            }
            put("connections", connectionsArray)
        }
    }

    private fun serializeElement(element: DesignElement): JSONObject {
        return JSONObject().apply {
            put("id", element.id)
            put("name", element.name)
            put("type", element.type.name)
            put("x", element.x.toDouble())
            put("y", element.y.toDouble())
            put("width", element.width.toDouble())
            put("height", element.height.toDouble())
            put("rotation", element.rotation.toDouble())
            put("fillColorHex", element.fillColorHex)
            put("cornerRadius", element.cornerRadius.toDouble())
            put("strokeColorHex", element.strokeColorHex)
            put("strokeWidth", element.strokeWidth.toDouble())
            put("opacity", element.opacity.toDouble())
            put("isVisible", element.isVisible)
            put("isLocked", element.isLocked)
            if (element.groupId != null) put("groupId", element.groupId)
            put("isComponentInstance", element.isComponentInstance)
            if (element.componentMasterId != null) put("componentMasterId", element.componentMasterId)
            if (element.gradientStartHex != null) put("gradientStartHex", element.gradientStartHex)
            if (element.gradientEndHex != null) put("gradientEndHex", element.gradientEndHex)
            put("gradientAngle", element.gradientAngle.toDouble())
            put("shadowElevation", element.shadowElevation.toDouble())
            put("iconName", element.iconName)
            put("isChecked", element.isChecked)
            put("placeholder", element.placeholder)
            put("text", element.text)
            put("fontSize", element.fontSize.toDouble())
            put("textColorHex", element.textColorHex)
            put("isBold", element.isBold)
            put("isItalic", element.isItalic)
            put("textAlign", element.textAlign.name)
            put("lineHeightMultiplier", element.lineHeightMultiplier.toDouble())
        }
    }

    private fun serializeConnection(connection: PrototypeConnection): JSONObject {
        return JSONObject().apply {
            put("id", connection.id)
            put("sourceElementId", connection.sourceElementId)
            put("targetArtboardId", connection.targetArtboardId)
            put("trigger", connection.trigger)
            put("transition", connection.transition.name)
            put("isOverlay", connection.isOverlay)
        }
    }

    fun deserializeProject(jsonString: String): Project {
        val root = JSONObject(jsonString)
        val version = root.optInt("version", 1)
        val id = root.optString("id", java.util.UUID.randomUUID().toString())
        val name = root.optString("name", "Imported Project")
        val createdAt = root.optLong("createdAt", System.currentTimeMillis())
        val updatedAt = root.optLong("updatedAt", System.currentTimeMillis())
        val activeArtboardId = root.optString("activeArtboardId", "")

        val settingsObj = root.optJSONObject("settings")
        val settings = if (settingsObj != null) {
            ProjectSettings(
                showGrid = settingsObj.optBoolean("showGrid", true),
                gridSize = settingsObj.optDouble("gridSize", 16.0).toFloat(),
                snapToGrid = settingsObj.optBoolean("snapToGrid", false),
                snapToElements = settingsObj.optBoolean("snapToElements", true)
            )
        } else {
            ProjectSettings()
        }

        // Tokens with migration fallback
        val tokens = mutableListOf<DesignToken>()
        val tokensArray = root.optJSONArray("tokens")
        if (tokensArray != null) {
            for (i in 0 until tokensArray.length()) {
                val tObj = tokensArray.getJSONObject(i)
                tokens.add(
                    DesignToken(
                        id = tObj.optString("id", java.util.UUID.randomUUID().toString()),
                        name = tObj.optString("name", "token"),
                        category = tObj.optString("category", "COLOR"),
                        value = tObj.optString("value", "#FFFFFF")
                    )
                )
            }
        }
        val finalTokens = if (tokens.isEmpty()) Project.defaultTokens() else tokens

        // Component Masters
        val components = mutableListOf<ComponentMaster>()
        val componentsArray = root.optJSONArray("components")
        if (componentsArray != null) {
            for (i in 0 until componentsArray.length()) {
                val cObj = componentsArray.getJSONObject(i)
                val elemObj = cObj.optJSONObject("element")
                if (elemObj != null) {
                    components.add(
                        ComponentMaster(
                            id = cObj.optString("id", java.util.UUID.randomUUID().toString()),
                            name = cObj.optString("name", "Component"),
                            description = cObj.optString("description", ""),
                            element = deserializeElement(elemObj)
                        )
                    )
                }
            }
        }

        val artboardsArray = root.optJSONArray("artboards") ?: JSONArray()
        val artboards = mutableListOf<Artboard>()
        for (i in 0 until artboardsArray.length()) {
            val artboardObj = artboardsArray.getJSONObject(i)
            artboards.add(deserializeArtboard(artboardObj))
        }

        val finalActiveId = if (activeArtboardId.isNotEmpty() && artboards.any { it.id == activeArtboardId }) {
            activeArtboardId
        } else {
            artboards.firstOrNull()?.id ?: ""
        }

        return Project(
            id = id,
            name = name,
            version = CURRENT_VERSION,
            createdAt = createdAt,
            updatedAt = updatedAt,
            artboards = if (artboards.isEmpty()) listOf(Artboard(name = "Screen 1", isStartArtboard = true)) else artboards,
            activeArtboardId = finalActiveId,
            settings = settings,
            tokens = finalTokens,
            components = components
        )
    }

    private fun deserializeArtboard(obj: JSONObject): Artboard {
        val id = obj.optString("id", java.util.UUID.randomUUID().toString())
        val name = obj.optString("name", "Screen")
        val width = obj.optDouble("width", 360.0).toFloat()
        val height = obj.optDouble("height", 640.0).toFloat()
        val backgroundColorHex = obj.optString("backgroundColorHex", "#FFFFFF")
        val isStartArtboard = obj.optBoolean("isStartArtboard", false)
        val isScrollable = obj.optBoolean("isScrollable", true)

        val elements = mutableListOf<DesignElement>()
        val elementsArray = obj.optJSONArray("elements") ?: JSONArray()
        for (i in 0 until elementsArray.length()) {
            elements.add(deserializeElement(elementsArray.getJSONObject(i)))
        }

        val connections = mutableListOf<PrototypeConnection>()
        val connectionsArray = obj.optJSONArray("connections") ?: JSONArray()
        for (i in 0 until connectionsArray.length()) {
            connections.add(deserializeConnection(connectionsArray.getJSONObject(i)))
        }

        return Artboard(
            id = id,
            name = name,
            width = width,
            height = height,
            backgroundColorHex = backgroundColorHex,
            elements = elements,
            connections = connections,
            isStartArtboard = isStartArtboard,
            isScrollable = isScrollable
        )
    }

    private fun deserializeElement(obj: JSONObject): DesignElement {
        val typeStr = obj.optString("type", ElementType.RECTANGLE.name)
        val type = try {
            ElementType.valueOf(typeStr)
        } catch (_: Exception) {
            ElementType.RECTANGLE
        }

        val alignStr = obj.optString("textAlign", TextAlignOption.LEFT.name)
        val align = try {
            TextAlignOption.valueOf(alignStr)
        } catch (_: Exception) {
            TextAlignOption.LEFT
        }

        val groupId = if (obj.has("groupId") && !obj.isNull("groupId")) obj.getString("groupId") else null
        val compId = if (obj.has("componentMasterId") && !obj.isNull("componentMasterId")) obj.getString("componentMasterId") else null
        val gradStart = if (obj.has("gradientStartHex") && !obj.isNull("gradientStartHex")) obj.getString("gradientStartHex") else null
        val gradEnd = if (obj.has("gradientEndHex") && !obj.isNull("gradientEndHex")) obj.getString("gradientEndHex") else null
        val gradAngle = obj.optDouble("gradientAngle", 0.0).toFloat()
        val shadow = obj.optDouble("shadowElevation", 0.0).toFloat()
        val iconName = obj.optString("iconName", "home")
        val isChecked = obj.optBoolean("isChecked", false)
        val placeholder = obj.optString("placeholder", "Enter text...")

        return DesignElement(
            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
            name = obj.optString("name", "Element"),
            type = type,
            x = obj.optDouble("x", 0.0).toFloat(),
            y = obj.optDouble("y", 0.0).toFloat(),
            width = obj.optDouble("width", 100.0).toFloat(),
            height = obj.optDouble("height", 50.0).toFloat(),
            rotation = obj.optDouble("rotation", 0.0).toFloat(),
            fillColorHex = obj.optString("fillColorHex", "#6366F1"),
            cornerRadius = obj.optDouble("cornerRadius", 8.0).toFloat(),
            strokeColorHex = obj.optString("strokeColorHex", "#00000000"),
            strokeWidth = obj.optDouble("strokeWidth", 0.0).toFloat(),
            opacity = obj.optDouble("opacity", 1.0).toFloat(),
            isVisible = obj.optBoolean("isVisible", true),
            isLocked = obj.optBoolean("isLocked", false),
            groupId = groupId,
            gradientStartHex = gradStart,
            gradientEndHex = gradEnd,
            gradientAngle = gradAngle,
            shadowElevation = shadow,
            iconName = iconName,
            isChecked = isChecked,
            placeholder = placeholder,
            isComponentInstance = obj.optBoolean("isComponentInstance", false),
            componentMasterId = compId,
            text = obj.optString("text", "Label"),
            fontSize = obj.optDouble("fontSize", 16.0).toFloat(),
            textColorHex = obj.optString("textColorHex", "#FFFFFF"),
            isBold = obj.optBoolean("isBold", false),
            isItalic = obj.optBoolean("isItalic", false),
            textAlign = align,
            lineHeightMultiplier = obj.optDouble("lineHeightMultiplier", 1.2).toFloat()
        )
    }

    private fun deserializeConnection(obj: JSONObject): PrototypeConnection {
        val transStr = obj.optString("transition", TransitionType.INSTANT.name)
        val trans = try {
            TransitionType.valueOf(transStr)
        } catch (_: Exception) {
            TransitionType.INSTANT
        }

        return PrototypeConnection(
            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
            sourceElementId = obj.optString("sourceElementId", ""),
            targetArtboardId = obj.optString("targetArtboardId", ""),
            trigger = obj.optString("trigger", "TAP"),
            transition = trans,
            isOverlay = obj.optBoolean("isOverlay", false)
        )
    }
}
