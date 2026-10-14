package com.example.ui.prototype

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Warning
import com.example.util.IconRegistry
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProjectRepository
import com.example.model.Artboard
import com.example.model.DesignElement
import com.example.model.ElementType
import com.example.model.Project
import com.example.model.TextAlignOption
import com.example.model.TransitionType
import com.example.ui.theme.ForgeAccentCyan
import com.example.ui.theme.ForgeArtboardCanvas
import com.example.ui.theme.ForgeBorder
import com.example.ui.theme.ForgeError
import com.example.ui.theme.ForgePrimary
import com.example.ui.theme.ForgeSurface
import com.example.ui.theme.ForgeSurfaceVariant
import com.example.ui.theme.ForgeTextMuted
import com.example.ui.theme.ForgeTextPrimary
import com.example.ui.theme.ForgeTextSecondary
import com.example.util.ArtboardPngExporter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PrototypePlayerScreen(
    projectId: String,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { ProjectRepository(context) }

    var project by remember { mutableStateOf<Project?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var currentArtboardId by remember { mutableStateOf("") }
    val backStack = remember { mutableStateListOf<String>() }
    var showHotspotHint by remember { mutableStateOf(false) }
    var activeTransition by remember { mutableStateOf(TransitionType.INSTANT) }

    LaunchedEffect(projectId) {
        val loaded = repository.loadProject(projectId)
        project = loaded
        isLoading = false
        if (loaded != null && loaded.artboards.isNotEmpty()) {
            val startId = loaded.artboards.find { it.isStartArtboard }?.id ?: loaded.artboards.first().id
            currentArtboardId = startId
            backStack.clear()
            backStack.add(startId)
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = ForgeSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ForgeBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.testTag("exit_prototype_btn")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Exit Player", tint = ForgeTextPrimary)
                        }

                        IconButton(
                            onClick = {
                                if (backStack.size > 1) {
                                    backStack.removeAt(backStack.lastIndex)
                                    activeTransition = TransitionType.DISSOLVE
                                    currentArtboardId = backStack.last()
                                }
                            },
                            enabled = backStack.size > 1,
                            modifier = Modifier.testTag("prototype_back_btn")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = if (backStack.size > 1) ForgeTextPrimary else ForgeTextMuted
                            )
                        }

                        IconButton(
                            onClick = {
                                project?.let { p ->
                                    val startId = p.artboards.find { it.isStartArtboard }?.id ?: p.artboards.first().id
                                    backStack.clear()
                                    backStack.add(startId)
                                    activeTransition = TransitionType.DISSOLVE
                                    currentArtboardId = startId
                                }
                            },
                            modifier = Modifier.testTag("prototype_restart_btn")
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = "Restart Prototype", tint = ForgeAccentCyan)
                        }
                    }

                    // Screen title badge
                    val currentArtboard = project?.artboards?.find { it.id == currentArtboardId }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ForgeSurfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PlayCircle, contentDescription = null, tint = ForgePrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentArtboard?.name ?: "Screen",
                                style = MaterialTheme.typography.labelMedium,
                                color = ForgeTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        },
        containerColor = ForgeArtboardCanvas
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = ForgePrimary)
            } else if (project == null || project!!.artboards.isEmpty()) {
                Text("No artboards in this prototype", color = ForgeTextMuted)
            } else {
                val currentArtboard = project!!.artboards.find { it.id == currentArtboardId }
                    ?: project!!.artboards.first()

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(currentArtboardId) {
                            detectTapGestures {
                                coroutineScope.launch {
                                    showHotspotHint = true
                                    delay(600)
                                    showHotspotHint = false
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val padding = 32f
                    val availWidth = (maxWidth.value - padding).coerceAtLeast(100f)
                    val availHeight = (maxHeight.value - padding).coerceAtLeast(100f)
                    val fitScale = minOf(
                        availWidth / currentArtboard.width,
                        availHeight / currentArtboard.height
                    ).coerceIn(0.4f, 1.3f)

                    AnimatedContent(
                        targetState = currentArtboard,
                        transitionSpec = {
                            when (activeTransition) {
                                TransitionType.SLIDE_LEFT -> {
                                    (slideInHorizontally(tween(300)) { it } + fadeIn()).togetherWith(
                                        slideOutHorizontally(tween(300)) { -it } + fadeOut()
                                    )
                                }
                                TransitionType.SLIDE_UP, TransitionType.BOTTOM_SHEET -> {
                                    (slideInVertically(tween(300)) { it } + fadeIn()).togetherWith(
                                        slideOutVertically(tween(300)) { -it } + fadeOut()
                                    )
                                }
                                TransitionType.DISSOLVE -> {
                                    fadeIn(tween(250)).togetherWith(fadeOut(tween(250)))
                                }
                                else -> {
                                    fadeIn(tween(80)).togetherWith(fadeOut(tween(80)))
                                }
                            }
                        },
                        label = "PrototypeScreenTransition"
                    ) { targetArtboard ->
                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = fitScale
                                    scaleY = fitScale
                                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                                }
                                .size(targetArtboard.width.dp, targetArtboard.height.dp)
                                .shadow(elevation = 20.dp, shape = RoundedCornerShape(24.dp))
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(ArtboardPngExporter.parseColorSafe(targetArtboard.backgroundColorHex, android.graphics.Color.WHITE)))
                                .border(2.dp, ForgeBorder, RoundedCornerShape(24.dp))
                                .testTag("prototype_screen_${targetArtboard.id}")
                        ) {
                            val maxElemBottom = targetArtboard.elements.maxOfOrNull { it.y + it.height } ?: targetArtboard.height
                            val isContentLonger = maxElemBottom > targetArtboard.height

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(
                                        if (isContentLonger && targetArtboard.isScrollable) {
                                            Modifier.verticalScroll(rememberScrollState())
                                        } else Modifier
                                    )
                            ) {
                                for (element in targetArtboard.elements) {
                                    if (!element.isVisible) continue
                                    val connection = targetArtboard.findConnectionForElement(element.id)
                                    val isDestinationValid = connection == null || project!!.artboards.any { it.id == connection.targetArtboardId }

                                    PrototypeElementView(
                                        element = element,
                                        isInteractive = connection != null && isDestinationValid,
                                        showHint = showHotspotHint && connection != null,
                                        onClick = {
                                            if (connection != null && isDestinationValid) {
                                                activeTransition = connection.transition
                                                backStack.add(connection.targetArtboardId)
                                                currentArtboardId = connection.targetArtboardId
                                            } else {
                                                coroutineScope.launch {
                                                    showHotspotHint = true
                                                    delay(500)
                                                    showHotspotHint = false
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrototypeElementView(
    element: DesignElement,
    isInteractive: Boolean,
    showHint: Boolean,
    onClick: () -> Unit
) {
    var localChecked by remember(element.id, element.isChecked) { mutableStateOf(element.isChecked) }

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

    val isTransparentBg = (element.type == ElementType.TEXT || element.type == ElementType.ICON) &&
            (element.fillColorHex.equals("#6366F1", true) || element.fillColorHex == "#00000000" || element.fillColorHex.uppercase().startsWith("#00"))

    Box(
        modifier = Modifier
            .offset(x = element.x.dp, y = element.y.dp)
            .size(element.width.dp, element.height.dp)
            .graphicsLayer {
                rotationZ = element.rotation
                transformOrigin = TransformOrigin(0.5f, 0.5f)
            }
            .then(if (element.shadowElevation > 0f) Modifier.shadow(element.shadowElevation.dp, shape) else Modifier)
            .clip(shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (element.type == ElementType.SWITCH || element.type == ElementType.CHECKBOX) {
                        localChecked = !localChecked
                    }
                    onClick()
                }
            )
            .background(if (isTransparentBg) Color.Transparent else fillColor)
            .then(
                if (element.strokeWidth > 0f) {
                    Modifier.border(element.strokeWidth.dp, strokeColor, shape)
                } else Modifier
            )
            .then(
                if (showHint) {
                    Modifier.border(2.5.dp, ForgeAccentCyan, shape)
                } else Modifier
            )
            .testTag("prototype_element_${element.id}"),
        contentAlignment = when (element.type) {
            ElementType.BUTTON -> Alignment.Center
            ElementType.ICON -> Alignment.Center
            ElementType.TEXT_FIELD -> Alignment.CenterStart
            else -> when (element.textAlign) {
                TextAlignOption.LEFT -> Alignment.CenterStart
                TextAlignOption.CENTER -> Alignment.Center
                TextAlignOption.RIGHT -> Alignment.CenterEnd
            }
        }
    ) {
        when (element.type) {
            ElementType.TEXT, ElementType.BUTTON -> {
                Text(
                    text = element.text,
                    color = textColor,
                    fontSize = element.fontSize.sp,
                    fontWeight = if (element.isBold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (element.isItalic) FontStyle.Italic else FontStyle.Normal,
                    textAlign = textAlign,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
            }
            ElementType.IMAGE_PLACEHOLDER -> {
                Icon(Icons.Default.Image, contentDescription = null, tint = textColor.copy(alpha = 0.7f), modifier = Modifier.size(24.dp))
            }
            ElementType.ICON -> {
                Icon(
                    imageVector = IconRegistry.getIcon(element.iconName),
                    contentDescription = element.name,
                    tint = textColor,
                    modifier = Modifier.size((minOf(element.width, element.height) * 0.65f).coerceAtLeast(16f).dp)
                )
            }
            ElementType.TEXT_FIELD -> {
                Text(
                    text = if (element.text.isNotEmpty()) element.text else element.placeholder,
                    color = if (element.text.isNotEmpty()) textColor else textColor.copy(alpha = 0.5f),
                    fontSize = element.fontSize.sp,
                    fontWeight = if (element.isBold) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
            ElementType.SWITCH -> {
                val trackColor = if (localChecked) fillColor else Color(0xFF374151)
                val thumbColor = if (localChecked) textColor else Color(0xFFD1D5DB)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(element.height.dp / 2f))
                        .background(trackColor)
                        .padding(3.dp),
                    contentAlignment = if (localChecked) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .size((element.height - 6f).coerceAtLeast(10f).dp)
                            .clip(CircleShape)
                            .background(thumbColor)
                    )
                }
            }
            ElementType.CHECKBOX -> {
                val boxBg = if (localChecked) fillColor else Color.Transparent
                val boxBorder = if (localChecked) fillColor else textColor.copy(alpha = 0.6f)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(boxBg)
                            .border(2.dp, boxBorder, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (localChecked) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = textColor, modifier = Modifier.size(14.dp))
                        }
                    }
                    if (element.text.isNotEmpty()) {
                        Text(
                            text = element.text,
                            color = textColor,
                            fontSize = element.fontSize.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
            else -> {}
        }
    }
}
