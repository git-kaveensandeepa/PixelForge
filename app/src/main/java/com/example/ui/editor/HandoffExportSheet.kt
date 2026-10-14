package com.example.ui.editor

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Html
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Artboard
import com.example.model.DesignElement
import com.example.model.Project
import com.example.ui.theme.ForgeAccentCyan
import com.example.ui.theme.ForgeBorder
import com.example.ui.theme.ForgePrimary
import com.example.ui.theme.ForgeSurface
import com.example.ui.theme.ForgeSurfaceVariant
import com.example.ui.theme.ForgeTextMuted
import com.example.ui.theme.ForgeTextPrimary
import com.example.ui.theme.ForgeTextSecondary
import com.example.util.ArtboardPngExporter
import com.example.util.CodeExportGenerator
import com.example.util.ShareHelper
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandoffExportSheet(
    project: Project,
    activeArtboard: Artboard,
    selectedElement: DesignElement?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var selectedTab by remember { mutableIntStateOf(0) }

    val composeCode = remember(activeArtboard) {
        CodeExportGenerator.generateJetpackComposeCode(activeArtboard)
    }

    val svgCode = remember(activeArtboard) {
        CodeExportGenerator.generateSvg(activeArtboard)
    }

    val htmlCode = remember(activeArtboard) {
        CodeExportGenerator.generateHtmlCss(activeArtboard)
    }

    val specText = remember(selectedElement, activeArtboard) {
        if (selectedElement != null) {
            CodeExportGenerator.generateDesignSpec(selectedElement, activeArtboard)
        } else {
            "Select an element on canvas to inspect exact dimensions, CSS box model, and layout specifications."
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
                .testTag("handoff_export_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Export & Dev Handoff",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ForgeTextPrimary
                    )
                    Text(
                        text = "Production assets, Compose code & design specs",
                        style = MaterialTheme.typography.bodySmall,
                        color = ForgeTextSecondary
                    )
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
                    text = { Text("Assets", fontSize = 11.sp, color = if (selectedTab == 0) ForgePrimary else ForgeTextSecondary) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Compose", fontSize = 11.sp, color = if (selectedTab == 1) ForgePrimary else ForgeTextSecondary) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("SVG / Web", fontSize = 11.sp, color = if (selectedTab == 2) ForgePrimary else ForgeTextSecondary) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Inspect", fontSize = 11.sp, color = if (selectedTab == 3) ForgePrimary else ForgeTextSecondary) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    // Export Assets
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ExportOptionRow(
                            title = "Export Current Artboard as PNG",
                            subtitle = "High-resolution 2x bitmap for presentation",
                            icon = Icons.Default.Image,
                            onAction = {
                                coroutineScope.launch {
                                    val file = ArtboardPngExporter.exportArtboardToPng(context, activeArtboard)
                                    ShareHelper.shareImage(context, file, "Share ${activeArtboard.name} PNG")
                                }
                            }
                        )

                        ExportOptionRow(
                            title = "Export Artboard as Vector SVG",
                            subtitle = "Scalable vector graphics XML format",
                            icon = Icons.Default.PictureAsPdf,
                            onAction = {
                                coroutineScope.launch {
                                    val exportDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
                                    val file = File(exportDir, "pixelforge_${activeArtboard.name.replace(" ", "_")}.svg")
                                    FileOutputStream(file).use { it.write(svgCode.toByteArray()) }
                                    ShareHelper.shareImage(context, file, "Share ${activeArtboard.name} SVG")
                                }
                            }
                        )

                        ExportOptionRow(
                            title = "Export Full Project JSON",
                            subtitle = "Complete version 2 document with artboards and links",
                            icon = Icons.Default.Share,
                            onAction = {
                                coroutineScope.launch {
                                    ShareHelper.shareProjectJson(context, project)
                                }
                            }
                        )
                    }
                }
                1 -> {
                    // Jetpack Compose Code View
                    CodeViewer(
                        code = composeCode,
                        onCopy = {
                            copyToClipboard(context, composeCode, "Compose code copied!")
                        }
                    )
                }
                2 -> {
                    // SVG and HTML/CSS Code View
                    CodeViewer(
                        code = htmlCode,
                        onCopy = {
                            copyToClipboard(context, htmlCode, "HTML/CSS code copied!")
                        }
                    )
                }
                3 -> {
                    // Inspect Specs
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ForgeSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .border(1.dp, ForgeBorder, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp).verticalScroll(rememberScrollState())) {
                            Text(
                                text = specText,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = ForgeTextPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ExportOptionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onAction: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ForgeSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ForgeBorder, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ForgePrimary.copy(alpha = 0.2f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = ForgePrimary, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ForgeTextPrimary)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = ForgeTextMuted)
            }
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = ForgePrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Export", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun CodeViewer(code: String, onCopy: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = onCopy,
                colors = ButtonDefaults.buttonColors(containerColor = ForgePrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy Code", fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = ForgeSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, ForgeBorder, RoundedCornerShape(12.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = code,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = ForgeTextPrimary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String, toastMsg: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("PixelForge Code", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
}
