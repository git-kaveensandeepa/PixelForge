package com.example.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.ForgeBorder
import com.example.ui.theme.ForgeBorderLight
import com.example.ui.theme.ForgePrimary
import com.example.ui.theme.ForgeSurface
import com.example.ui.theme.ForgeSurfaceVariant
import com.example.ui.theme.ForgeTextPrimary
import com.example.ui.theme.ForgeTextSecondary

@Composable
fun TemplateSelectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (templateType: String, projectName: String) -> Unit
) {
    var selectedTemplate by remember { mutableStateOf("BLANK") }
    var projectName by remember { mutableStateOf("Mobile Prototype") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, ForgeBorder, RoundedCornerShape(20.dp))
                .testTag("template_selection_dialog"),
            color = ForgeSurface
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "New UI Project",
                    style = MaterialTheme.typography.titleLarge,
                    color = ForgeTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Choose an artboard preset or starter flow",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForgeTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    label = { Text("Project Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("project_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForgePrimary,
                        unfocusedBorderColor = ForgeBorder,
                        focusedLabelColor = ForgePrimary,
                        unfocusedLabelColor = ForgeTextSecondary,
                        focusedTextColor = ForgeTextPrimary,
                        unfocusedTextColor = ForgeTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Templates",
                    style = MaterialTheme.typography.labelLarge,
                    color = ForgeTextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                TemplateOptionCard(
                    title = "Blank Mobile Screen",
                    subtitle = "1 screen (360 x 640 dp), starter elements",
                    icon = Icons.Default.PhoneAndroid,
                    isSelected = selectedTemplate == "BLANK",
                    onClick = {
                        selectedTemplate = "BLANK"
                        if (projectName == "Login & Onboarding Flow" || projectName == "Shop Catalog & Detail") {
                            projectName = "Mobile Prototype"
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                TemplateOptionCard(
                    title = "Login & Auth Flow",
                    subtitle = "2 screens with interactive prototyping links",
                    icon = Icons.Default.Lock,
                    isSelected = selectedTemplate == "AUTH",
                    onClick = {
                        selectedTemplate = "AUTH"
                        projectName = "Login & Onboarding Flow"
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                TemplateOptionCard(
                    title = "E-Commerce Catalog",
                    subtitle = "Catalog & Product details with buy CTA",
                    icon = Icons.Default.ShoppingBag,
                    isSelected = selectedTemplate == "ECOMMERCE",
                    onClick = {
                        selectedTemplate = "ECOMMERCE"
                        projectName = "Shop Catalog & Detail"
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_template_button")
                    ) {
                        Text("Cancel", color = ForgeTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(selectedTemplate, projectName)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForgePrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("create_project_confirm_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Create Project")
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) ForgePrimary else ForgeBorder
    val bgColor = if (isSelected) ForgeSurfaceVariant else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = if (isSelected) ForgePrimary else ForgeSurfaceVariant,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else ForgeTextSecondary,
                modifier = Modifier
                    .padding(8.dp)
                    .size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) ForgePrimary else ForgeTextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = ForgeTextSecondary
            )
        }
    }
}
