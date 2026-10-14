package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Brush
import com.example.util.IconRegistry
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Artboard
import com.example.model.DesignElement
import com.example.model.ElementType
import com.example.model.TextAlignOption
import com.example.ui.theme.ForgeAccentCyan
import com.example.ui.theme.ForgeArtboardCanvas
import com.example.ui.theme.ForgeBorder
import com.example.ui.theme.ForgePrimary
import com.example.util.ArtboardPngExporter
import kotlin.math.abs
import kotlin.math.roundToInt

data class SnapGuideLine(
    val isVertical: Boolean,
    val positionDp: Float
)

@Composable
fun CanvasView(
    artboard: Artboard,
    allArtboards: List<Artboard>,
    selectedElementIds: Set<String>,
    primarySelectedId: String?,
    mode: EditorMode,
    scale: Float,
    offset: Offset,
    showGrid: Boolean,
    snapToGrid: Boolean,
    snapToElements: Boolean,
    onTransform: (scaleChange: Float, panChange: Offset) -> Unit,
    onSelectElement: (id: String?, isMultiToggle: Boolean) -> Unit,
    onMoveElements: (deltas: Map<String, Pair<Float, Float>>, commit: Boolean) -> Unit,
    onResizeElement: (id: String, x: Float, y: Float, w: Float, h: Float, commit: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val activeGuides = remember { mutableStateListOf<SnapGuideLine>() }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        onTransform(zoomChange, panChange)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(ForgeArtboardCanvas)
            .transformable(state = transformableState)
            .pointerInput(Unit) {
                detectTapGestures {
                    onSelectElement(null, false)
                    activeGuides.clear()
                }
            }
            .testTag("canvas_viewport"),
        contentAlignment = Alignment.Center
    ) {
        // Artboard container
        Box(
            modifier = Modifier
                .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                }
                .size(artboard.width.dp, artboard.height.dp)
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(2.dp))
                .clip(RoundedCornerShape(2.dp))
                .background(Color(ArtboardPngExporter.parseColorSafe(artboard.backgroundColorHex, android.graphics.Color.WHITE)))
                .border(1.dp, ForgeBorder, RoundedCornerShape(2.dp))
                .drawBehind {
                    if (showGrid) {
                        drawSubtleGrid()
                    }
                    // Draw snap guide lines
                    val guideColor = Color(0xFF06B6D4)
                    for (guide in activeGuides) {
                        if (guide.isVertical) {
                            val xPx = guide.positionDp.dp.toPx()
                            drawLine(
                                color = guideColor,
                                start = Offset(xPx, 0f),
                                end = Offset(xPx, size.height),
                                strokeWidth = 1.2.dp.toPx()
                            )
                        } else {
                            val yPx = guide.positionDp.dp.toPx()
                            drawLine(
                                color = guideColor,
                                start = Offset(0f, yPx),
                                end = Offset(size.width, yPx),
                                strokeWidth = 1.2.dp.toPx()
                            )
                        }
                    }
                }
                .pointerInput(artboard.id) {
                    detectTapGestures {
                        onSelectElement(null, false)
                        activeGuides.clear()
                    }
                }
                .testTag("artboard_container")
        ) {
            // Render Elements in layer order
            for (element in artboard.elements) {
                if (!element.isVisible) continue
                val isSelected = selectedElementIds.contains(element.id)
                val isPrimary = element.id == primarySelectedId
                val connection = artboard.findConnectionForElement(element.id)
                val targetArtboard = if (connection != null) {
                    allArtboards.find { it.id == connection.targetArtboardId }
                } else null

                ElementRenderer(
                    element = element,
                    isSelected = isSelected,
                    isPrimary = isPrimary,
                    isMultiSelectMode = selectedElementIds.size > 1,
                    mode = mode,
                    targetScreenName = targetArtboard?.name,
                    canvasScale = scale,
                    onSelect = { isMultiToggle ->
                        onSelectElement(element.id, isMultiToggle)
                    },
                    onMove = { dx, dy, commit ->
                        // Calculate snapping for primary dragged element
                        var snapDx = dx
                        var snapDy = dy

                        if (!commit && (snapToGrid || snapToElements)) {
                            activeGuides.clear()
                            val curX = element.x + dx
                            val curY = element.y + dy
                            val curCenterX = curX + element.width / 2f
                            val curCenterY = curY + element.height / 2f
                            val artboardCenterX = artboard.width / 2f
                            val artboardCenterY = artboard.height / 2f

                            // Snap to Artboard Center
                            val threshold = 6f
                            if (abs(curCenterX - artboardCenterX) < threshold) {
                                snapDx = (artboardCenterX - element.width / 2f) - element.x
                                activeGuides.add(SnapGuideLine(isVertical = true, positionDp = artboardCenterX))
                            }
                            if (abs(curCenterY - artboardCenterY) < threshold) {
                                snapDy = (artboardCenterY - element.height / 2f) - element.y
                                activeGuides.add(SnapGuideLine(isVertical = false, positionDp = artboardCenterY))
                            }
                        } else if (commit) {
                            activeGuides.clear()
                        }

                        // Move all selected elements together
                        val targets = if (selectedElementIds.contains(element.id)) selectedElementIds else setOf(element.id)
                        val deltaMap = targets.associateWith { Pair(snapDx, snapDy) }
                        onMoveElements(deltaMap, commit)
                    },
                    onResize = { nx, ny, nw, nh, commit ->
                        onResizeElement(element.id, nx, ny, nw, nh, commit)
                    }
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSubtleGrid() {
    val dotColor = Color(0x22888899)
    val step = 16.dp.toPx()
    var x = step
    while (x < size.width) {
        var y = step
        while (y < size.height) {
            drawCircle(
                color = dotColor,
                radius = 1.2.dp.toPx(),
                center = Offset(x, y)
            )
            y += step
        }
        x += step
    }
}

@Composable
private fun ElementRenderer(
    element: DesignElement,
    isSelected: Boolean,
    isPrimary: Boolean,
    isMultiSelectMode: Boolean,
    mode: EditorMode,
    targetScreenName: String?,
    canvasScale: Float,
    onSelect: (isMultiToggle: Boolean) -> Unit,
    onMove: (dx: Float, dy: Float, commit: Boolean) -> Unit,
    onResize: (newX: Float, newY: Float, newW: Float, newH: Float, commit: Boolean) -> Unit
) {
    val density = LocalDensity.current

    val fillColor = Color(ArtboardPngExporter.parseColorSafe(element.fillColorHex, android.graphics.Color.GRAY))
        .copy(alpha = element.opacity.coerceIn(0f, 1f))
    val strokeColor = Color(ArtboardPngExporter.parseColorSafe(element.strokeColorHex, android.graphics.Color.TRANSPARENT))
    val textColor = Color(ArtboardPngExporter.parseColorSafe(element.textColorHex, android.graphics.Color.BLACK))

    val textAlign = when (element.textAlign) {
        TextAlignOption.LEFT -> TextAlign.Left
        TextAlignOption.CENTER -> TextAlign.Center
        TextAlignOption.RIGHT -> TextAlign.Right
    }

    val shape = when (element.type) {
        ElementType.CIRCLE -> CircleShape
        else -> RoundedCornerShape(element.cornerRadius.dp)
    }

    Box(
        modifier = Modifier
            .offset(x = element.x.dp, y = element.y.dp)
            .size(element.width.dp, element.height.dp)
            .graphicsLayer {
                rotationZ = element.rotation
                transformOrigin = TransformOrigin(0.5f, 0.5f)
            }
            .testTag("element_${element.id}")
            .pointerInput(element.id, canvasScale) {
                detectTapGestures(
                    onDoubleTap = {
                        // Double tap to multi-select toggle
                        onSelect(true)
                    },
                    onTap = {
                        onSelect(false)
                    }
                )
            }
            .pointerInput(element.id, canvasScale) {
                detectDragGestures(
                    onDragStart = {
                        if (!isSelected) onSelect(false)
                    },
                    onDragEnd = {
                        onMove(0f, 0f, true)
                    },
                    onDragCancel = {
                        onMove(0f, 0f, true)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val dxDp = (dragAmount.x / density.density) / canvasScale
                        val dyDp = (dragAmount.y / density.density) / canvasScale
                        onMove(dxDp, dyDp, false)
                    }
                )
            }
    ) {
        val gradientBrush = if (element.hasGradient) {
            Brush.linearGradient(
                listOf(
                    Color(ArtboardPngExporter.parseColorSafe(element.gradientStartHex ?: element.fillColorHex, android.graphics.Color.GRAY)),
                    Color(ArtboardPngExporter.parseColorSafe(element.gradientEndHex ?: element.fillColorHex, android.graphics.Color.GRAY))
                )
            )
        } else null

        // Render content based on ElementType
        when (element.type) {
            ElementType.RECTANGLE -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (element.shadowElevation > 0f) Modifier.shadow(element.shadowElevation.dp, shape) else Modifier)
                        .clip(shape)
                        .then(if (gradientBrush != null) Modifier.background(gradientBrush) else Modifier.background(fillColor))
                        .then(
                            if (element.strokeWidth > 0f) {
                                Modifier.border(element.strokeWidth.dp, strokeColor, shape)
                            } else Modifier
                        )
                )
            }
            ElementType.CIRCLE -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (element.shadowElevation > 0f) Modifier.shadow(element.shadowElevation.dp, CircleShape) else Modifier)
                        .clip(CircleShape)
                        .then(if (gradientBrush != null) Modifier.background(gradientBrush) else Modifier.background(fillColor))
                        .then(
                            if (element.strokeWidth > 0f) {
                                Modifier.border(element.strokeWidth.dp, strokeColor, CircleShape)
                            } else Modifier
                        )
                )
            }
            ElementType.LINE -> {
                val lineCol = if (element.strokeWidth > 0f) strokeColor else fillColor
                val thickness = if (element.strokeWidth > 0f) element.strokeWidth.dp else element.height.dp.coerceAtLeast(2.dp)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(thickness)
                            .background(lineCol)
                    )
                }
            }
            ElementType.TEXT -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(shape)
                        .then(
                            if (element.strokeWidth > 0f) {
                                Modifier.border(element.strokeWidth.dp, strokeColor, shape)
                            } else Modifier
                        ),
                    contentAlignment = when (element.textAlign) {
                        TextAlignOption.LEFT -> Alignment.CenterStart
                        TextAlignOption.CENTER -> Alignment.Center
                        TextAlignOption.RIGHT -> Alignment.CenterEnd
                    }
                ) {
                    Text(
                        text = element.text,
                        color = textColor,
                        fontSize = element.fontSize.sp,
                        fontWeight = if (element.isBold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (element.isItalic) FontStyle.Italic else FontStyle.Normal,
                        textAlign = textAlign,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
            ElementType.BUTTON -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (element.shadowElevation > 0f) Modifier.shadow(element.shadowElevation.dp, shape) else Modifier)
                        .clip(shape)
                        .then(if (gradientBrush != null) Modifier.background(gradientBrush) else Modifier.background(fillColor))
                        .then(
                            if (element.strokeWidth > 0f) {
                                Modifier.border(element.strokeWidth.dp, strokeColor, shape)
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = element.text,
                        color = textColor,
                        fontSize = element.fontSize.sp,
                        fontWeight = if (element.isBold) FontWeight.Bold else FontWeight.SemiBold,
                        fontStyle = if (element.isItalic) FontStyle.Italic else FontStyle.Normal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }
            }
            ElementType.IMAGE_PLACEHOLDER -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (element.shadowElevation > 0f) Modifier.shadow(element.shadowElevation.dp, shape) else Modifier)
                        .clip(shape)
                        .background(fillColor)
                        .then(
                            if (element.strokeWidth > 0f) {
                                Modifier.border(element.strokeWidth.dp, strokeColor, shape)
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Image Placeholder",
                        tint = textColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            ElementType.ICON -> {
                val iconVector = IconRegistry.getIcon(element.iconName)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (element.shadowElevation > 0f) Modifier.shadow(element.shadowElevation.dp, shape) else Modifier)
                        .clip(shape)
                        .then(
                            if (element.fillColorHex != "#00000000" && !element.fillColorHex.uppercase().startsWith("#00")) {
                                if (gradientBrush != null) Modifier.background(gradientBrush) else Modifier.background(fillColor)
                            } else Modifier
                        )
                        .then(
                            if (element.strokeWidth > 0f) {
                                Modifier.border(element.strokeWidth.dp, strokeColor, shape)
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = element.name,
                        tint = textColor,
                        modifier = Modifier.size((minOf(element.width, element.height) * 0.65f).coerceAtLeast(16f).dp)
                    )
                }
            }
            ElementType.TEXT_FIELD -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (element.shadowElevation > 0f) Modifier.shadow(element.shadowElevation.dp, shape) else Modifier)
                        .clip(shape)
                        .background(fillColor)
                        .then(
                            Modifier.border(
                                (if (element.strokeWidth > 0f) element.strokeWidth else 1.5f).dp,
                                if (element.strokeWidth > 0f) strokeColor else textColor.copy(alpha = 0.35f),
                                shape
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = if (element.text.isNotEmpty()) element.text else element.placeholder,
                        color = if (element.text.isNotEmpty()) textColor else textColor.copy(alpha = 0.5f),
                        fontSize = element.fontSize.sp,
                        fontWeight = if (element.isBold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (element.text.isEmpty()) FontStyle.Italic else FontStyle.Normal
                    )
                }
            }
            ElementType.SWITCH -> {
                val isSwitched = element.isChecked
                val trackColor = if (isSwitched) fillColor else Color(0xFF374151)
                val thumbColor = if (isSwitched) textColor else Color(0xFFD1D5DB)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(element.height.dp / 2f))
                        .background(trackColor)
                        .padding(4.dp),
                    contentAlignment = if (isSwitched) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .size((element.height - 8f).coerceAtLeast(10f).dp)
                            .clip(CircleShape)
                            .background(thumbColor)
                    )
                }
            }
            ElementType.CHECKBOX -> {
                val isChecked = element.isChecked
                val boxBg = if (isChecked) fillColor else Color.Transparent
                val boxBorder = if (isChecked) fillColor else textColor.copy(alpha = 0.6f)
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(element.cornerRadius.coerceAtLeast(4f).dp))
                            .background(boxBg)
                            .border(2.dp, boxBorder, RoundedCornerShape(element.cornerRadius.coerceAtLeast(4f).dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isChecked) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    if (element.text.isNotEmpty()) {
                        Text(
                            text = element.text,
                            color = textColor,
                            fontSize = element.fontSize.sp,
                            fontWeight = if (element.isBold) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        // Prototype link indicator badge
        if (targetScreenName != null && (mode == EditorMode.PROTOTYPE || isSelected)) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = ForgeAccentCyan,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-12).dp)
            ) {
                Text(
                    text = "→ $targetScreenName",
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Selection outline & handles
        if (isSelected) {
            val outlineColor = if (isMultiSelectMode && !isPrimary) ForgeAccentCyan else ForgePrimary
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(if (isPrimary) 1.8.dp else 1.2.dp, outlineColor)
            )

            // Only show corner resize handles on primary element
            if (isPrimary && !isMultiSelectMode) {
                SelectionOverlay(
                    element = element,
                    canvasScale = canvasScale,
                    onResize = onResize
                )
            }
        }
    }
}

@Composable
private fun BoxScope.SelectionOverlay(
    element: DesignElement,
    canvasScale: Float,
    onResize: (newX: Float, newY: Float, newW: Float, newH: Float, commit: Boolean) -> Unit
) {
    val handleSize = 14.dp

    // Bottom-Right (SE)
    ResizeHandle(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .offset(x = 6.dp, y = 6.dp),
        handleSize = handleSize,
        onDrag = { dx, dy, commit ->
            val scaleComp = (1f / canvasScale)
            val newW = (element.width + dx * scaleComp).coerceAtLeast(16f)
            val newH = (element.height + dy * scaleComp).coerceAtLeast(16f)
            onResize(element.x, element.y, newW, newH, commit)
        }
    )

    // Top-Left (NW)
    ResizeHandle(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = (-6).dp, y = (-6).dp),
        handleSize = handleSize,
        onDrag = { dx, dy, commit ->
            val scaleComp = (1f / canvasScale)
            val deltaX = dx * scaleComp
            val deltaY = dy * scaleComp
            val newW = (element.width - deltaX).coerceAtLeast(16f)
            val newH = (element.height - deltaY).coerceAtLeast(16f)
            val newX = element.x + (element.width - newW)
            val newY = element.y + (element.height - newH)
            onResize(newX, newY, newW, newH, commit)
        }
    )

    // Top-Right (NE)
    ResizeHandle(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = 6.dp, y = (-6).dp),
        handleSize = handleSize,
        onDrag = { dx, dy, commit ->
            val scaleComp = (1f / canvasScale)
            val deltaX = dx * scaleComp
            val deltaY = dy * scaleComp
            val newW = (element.width + deltaX).coerceAtLeast(16f)
            val newH = (element.height - deltaY).coerceAtLeast(16f)
            val newY = element.y + (element.height - newH)
            onResize(element.x, newY, newW, newH, commit)
        }
    )

    // Bottom-Left (SW)
    ResizeHandle(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .offset(x = (-6).dp, y = 6.dp),
        handleSize = handleSize,
        onDrag = { dx, dy, commit ->
            val scaleComp = (1f / canvasScale)
            val deltaX = dx * scaleComp
            val deltaY = dy * scaleComp
            val newW = (element.width - deltaX).coerceAtLeast(16f)
            val newH = (element.height + deltaY).coerceAtLeast(16f)
            val newX = element.x + (element.width - newW)
            onResize(newX, element.y, newW, newH, commit)
        }
    )
}

@Composable
private fun ResizeHandle(
    modifier: Modifier,
    handleSize: androidx.compose.ui.unit.Dp,
    onDrag: (dxDp: Float, dyDp: Float, commit: Boolean) -> Unit
) {
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .size(handleSize)
            .background(Color.White, RoundedCornerShape(3.dp))
            .border(1.5.dp, ForgePrimary, RoundedCornerShape(3.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = { onDrag(0f, 0f, true) },
                    onDragCancel = { onDrag(0f, 0f, true) },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val dxDp = dragAmount.x / density.density
                        val dyDp = dragAmount.y / density.density
                        onDrag(dxDp, dyDp, false)
                    }
                )
            }
    )
}
