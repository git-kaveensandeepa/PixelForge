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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ElementType
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsertElementSheet(
    onDismiss: () -> Unit,
    onSelectType: (ElementType) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ForgeSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(ForgeBorderLight)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Insert Component",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ForgeTextPrimary
                    )
                    Text(
                        text = "Select a vector shape, control, or typography block",
                        style = MaterialTheme.typography.bodySmall,
                        color = ForgeTextSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = ForgeTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = ForgeBorder)
            Spacer(modifier = Modifier.height(16.dp))

            // Section: Basic Vector Shapes
            Text(
                text = "BASIC VECTOR SHAPES",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = ForgeTextMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            InsertItemRow(
                title = "Rectangle",
                subtitle = "Custom rounded corners, borders & fill gradients",
                icon = Icons.Default.CropSquare,
                tint = ForgePrimaryLight,
                onClick = { onSelectType(ElementType.RECTANGLE); onDismiss() }
            )
            InsertItemRow(
                title = "Circle / Oval",
                subtitle = "Rounded avatar, badges & circular containers",
                icon = Icons.Default.Circle,
                tint = ForgeAccentCyan,
                onClick = { onSelectType(ElementType.CIRCLE); onDismiss() }
            )
            InsertItemRow(
                title = "Horizontal Line",
                subtitle = "Subtle hairline divider for list and card separation",
                icon = Icons.Default.HorizontalRule,
                tint = ForgeTextSecondary,
                onClick = { onSelectType(ElementType.LINE); onDismiss() }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "INTERACTIVE MOBILE CONTROLS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = ForgeTextMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            InsertItemRow(
                title = "Button",
                subtitle = "Primary CTA or secondary actionable button",
                icon = Icons.Default.SmartButton,
                tint = ForgePrimary,
                onClick = { onSelectType(ElementType.BUTTON); onDismiss() }
            )
            InsertItemRow(
                title = "Text Input Field",
                subtitle = "Outlined text box with placeholder and label",
                icon = Icons.Default.Edit,
                tint = ForgeAccentCyan,
                onClick = { onSelectType(ElementType.TEXT_FIELD); onDismiss() }
            )
            InsertItemRow(
                title = "Switch Toggle",
                subtitle = "Interactive mobile setting switch with on/off states",
                icon = Icons.Default.ToggleOn,
                tint = ForgePrimaryLight,
                onClick = { onSelectType(ElementType.SWITCH); onDismiss() }
            )
            InsertItemRow(
                title = "Checkbox Option",
                subtitle = "Selectable check box with customizable label",
                icon = Icons.Default.CropSquare,
                tint = ForgeTextPrimary,
                onClick = { onSelectType(ElementType.CHECKBOX); onDismiss() }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "TYPOGRAPHY & MEDIA",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = ForgeTextMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            InsertItemRow(
                title = "Text Label / Heading",
                subtitle = "Configurable font size, bold, italic, and alignment",
                icon = Icons.Default.TextFields,
                tint = ForgeTextPrimary,
                onClick = { onSelectType(ElementType.TEXT); onDismiss() }
            )
            InsertItemRow(
                title = "Vector Icon",
                subtitle = "25+ local Material vector icons for mobile interfaces",
                icon = Icons.Default.Star,
                tint = ForgeAccentCyan,
                onClick = { onSelectType(ElementType.ICON); onDismiss() }
            )
            InsertItemRow(
                title = "Image Placeholder",
                subtitle = "Hero banner or photo slot container",
                icon = Icons.Default.Image,
                tint = ForgePrimaryLight,
                onClick = { onSelectType(ElementType.IMAGE_PLACEHOLDER); onDismiss() }
            )
        }
    }
}

@Composable
private fun InsertItemRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ForgeSurfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, ForgeBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("insert_item_${title.lowercase().replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = tint.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ForgeTextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ForgeTextSecondary,
                    fontSize = 11.sp
                )
            }

            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = ForgePrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
