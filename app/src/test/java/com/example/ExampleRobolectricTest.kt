package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ProjectSerializer
import com.example.model.Artboard
import com.example.model.ComponentMaster
import com.example.model.DesignElement
import com.example.model.ElementType
import com.example.model.Project
import com.example.model.PrototypeConnection
import com.example.model.TransitionType
import com.example.ui.editor.UndoRedoManager
import com.example.util.CodeExportGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches PixelForge`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PixelForge", appName)
    }

    @Test
    fun `project serialization and deserialization preserves all elements and connections`() {
        val sourceElemId = "elem_btn_1"
        val targetScreenId = "artboard_screen_2"

        val originalProject = Project(
            name = "Test Prototype",
            version = 2,
            artboards = listOf(
                Artboard(
                    id = "artboard_screen_1",
                    name = "Home Screen",
                    width = 360f,
                    height = 640f,
                    backgroundColorHex = "#0F172A",
                    isStartArtboard = true,
                    elements = listOf(
                        DesignElement(
                            id = sourceElemId,
                            name = "Submit Button",
                            type = ElementType.BUTTON,
                            x = 20f,
                            y = 100f,
                            width = 200f,
                            height = 48f,
                            rotation = 15f,
                            fillColorHex = "#6366F1",
                            text = "Get Started",
                            fontSize = 16f,
                            isBold = true
                        ),
                        DesignElement(
                            name = "Divider",
                            type = ElementType.LINE,
                            x = 20f,
                            y = 180f,
                            width = 320f,
                            height = 2f
                        )
                    ),
                    connections = listOf(
                        PrototypeConnection(
                            sourceElementId = sourceElemId,
                            targetArtboardId = targetScreenId,
                            transition = TransitionType.SLIDE_LEFT
                        )
                    )
                ),
                Artboard(
                    id = targetScreenId,
                    name = "Detail Screen",
                    width = 360f,
                    height = 640f,
                    backgroundColorHex = "#FFFFFF",
                    isStartArtboard = false,
                    elements = listOf(
                        DesignElement(
                            name = "Header",
                            type = ElementType.TEXT,
                            x = 24f,
                            y = 40f,
                            width = 300f,
                            height = 36f,
                            text = "Details Screen"
                        ),
                        DesignElement(
                            name = "Hero Image",
                            type = ElementType.IMAGE_PLACEHOLDER,
                            x = 24f,
                            y = 90f,
                            width = 312f,
                            height = 140f
                        )
                    )
                )
            ),
            components = listOf(
                ComponentMaster(
                    name = "Primary Button Master",
                    element = DesignElement(name = "Master", type = ElementType.BUTTON, text = "CTA")
                )
            )
        )

        val json = ProjectSerializer.serializeProject(originalProject)
        assertTrue(json.contains("Test Prototype"))
        assertTrue(json.contains("Submit Button"))
        assertTrue(json.contains("SLIDE_LEFT"))
        assertTrue(json.contains("IMAGE_PLACEHOLDER"))
        assertTrue(json.contains("Primary Button Master"))

        val deserialized = ProjectSerializer.deserializeProject(json)
        assertEquals("Test Prototype", deserialized.name)
        assertEquals(2, deserialized.artboards.size)
        assertEquals(1, deserialized.components.size)
        assertEquals("Primary Button Master", deserialized.components.first().name)

        val screen1 = deserialized.artboards.first()
        assertEquals("Home Screen", screen1.name)
        assertTrue(screen1.isStartArtboard)
        assertEquals(2, screen1.elements.size)
        assertEquals("Submit Button", screen1.elements.first().name)
        assertEquals(ElementType.BUTTON, screen1.elements.first().type)
        assertEquals(15f, screen1.elements.first().rotation)
        assertEquals(ElementType.LINE, screen1.elements[1].type)

        assertEquals(1, screen1.connections.size)
        assertEquals(sourceElemId, screen1.connections.first().sourceElementId)
        assertEquals(targetScreenId, screen1.connections.first().targetArtboardId)
        assertEquals(TransitionType.SLIDE_LEFT, screen1.connections.first().transition)
    }

    @Test
    fun `version 1 legacy json safely migrates to version 2`() {
        val legacyV1Json = """
        {
          "version": 1,
          "id": "legacy_proj_1",
          "name": "Legacy V1 Project",
          "createdAt": 1700000000000,
          "updatedAt": 1700000000000,
          "activeArtboardId": "artboard_1",
          "artboards": [
            {
              "id": "artboard_1",
              "name": "Legacy Screen",
              "width": 360,
              "height": 640,
              "backgroundColorHex": "#FFFFFF",
              "isStartArtboard": true,
              "elements": [
                {
                  "id": "elem_legacy_1",
                  "name": "Legacy Box",
                  "type": "RECTANGLE",
                  "x": 10,
                  "y": 20,
                  "width": 100,
                  "height": 50,
                  "fillColorHex": "#6366F1"
                }
              ],
              "connections": []
            }
          ]
        }
        """.trimIndent()

        val migrated = ProjectSerializer.deserializeProject(legacyV1Json)
        assertEquals(2, migrated.version)
        assertEquals("Legacy V1 Project", migrated.name)
        assertTrue(migrated.tokens.isNotEmpty()) // Default tokens populated
        val elem = migrated.artboards.first().elements.first()
        assertEquals(0f, elem.rotation)
        assertEquals(ElementType.RECTANGLE, elem.type)
        assertEquals(10f, elem.x)
    }

    @Test
    fun `code generation produces valid Compose, SVG, and HTML`() {
        val artboard = Artboard(
            name = "Checkout Screen",
            width = 360f,
            height = 640f,
            backgroundColorHex = "#0F172A",
            elements = listOf(
                DesignElement(
                    name = "Checkout Button",
                    type = ElementType.BUTTON,
                    x = 24f,
                    y = 500f,
                    width = 312f,
                    height = 52f,
                    text = "Pay $49.00"
                ),
                DesignElement(
                    name = "Divider Line",
                    type = ElementType.LINE,
                    x = 24f,
                    y = 480f,
                    width = 312f,
                    height = 2f
                )
            )
        )

        val composeCode = CodeExportGenerator.generateJetpackComposeCode(artboard)
        assertTrue(composeCode.contains("fun CheckoutScreen()"))
        assertTrue(composeCode.contains("Pay $49.00"))
        assertTrue(composeCode.contains("HorizontalDivider"))

        val svgCode = CodeExportGenerator.generateSvg(artboard)
        assertTrue(svgCode.contains("<svg"))
        assertTrue(svgCode.contains("<line"))
        assertTrue(svgCode.contains("</svg>"))

        val htmlCode = CodeExportGenerator.generateHtmlCss(artboard)
        assertTrue(htmlCode.contains("<!DOCTYPE html>"))
        assertTrue(htmlCode.contains("class=\"artboard\""))
    }

    @Test
    fun `undo redo manager tracks history and restores state`() {
        val manager = UndoRedoManager(maxHistory = 10)
        assertFalse(manager.canUndo)
        assertFalse(manager.canRedo)

        val p1 = Project(name = "Version 1")
        val p2 = Project(name = "Version 2")
        val p3 = Project(name = "Version 3")

        manager.pushState(p1)
        assertTrue(manager.canUndo)
        assertFalse(manager.canRedo)

        manager.pushState(p2)
        assertEquals(p2, manager.undo(p3))
        assertTrue(manager.canRedo)

        val redone = manager.redo(p2)
        assertEquals(p3, redone)
    }
}
