package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.AppSettings
import com.example.data.local.SettingsManager
import com.example.ui.theme.GeminiAccent
import com.example.ui.theme.GeminiBg
import com.example.ui.theme.GeminiBorder
import com.example.ui.theme.GeminiSubtext
import com.example.ui.theme.GeminiSurface
import com.example.ui.theme.GeminiText

@Composable
fun SettingsDialog(
    currentSettings: AppSettings,
    onDismiss: () -> Unit,
    onSave: (openRouterKey: String, geminiKey: String, model: String, systemPrompt: String) -> Unit
) {
    var openRouterKey by remember { mutableStateOf(currentSettings.openRouterApiKey) }
    var geminiKey by remember { mutableStateOf(currentSettings.geminiApiKey) }
    var modelName by remember { mutableStateOf(currentSettings.model) }
    var showOpenRouterKey by remember { mutableStateOf(false) }
    var showGeminiKey by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, GeminiBorder, RoundedCornerShape(20.dp)),
            color = GeminiSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GeminiText
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close settings",
                            tint = GeminiSubtext
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // OpenRouter API Key Field
                Text(
                    text = "OpenRouter API Key",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = GeminiText
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = openRouterKey,
                    onValueChange = { openRouterKey = it },
                    placeholder = { Text("sk-or-v1-...", color = GeminiSubtext) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_input"),
                    visualTransformation = if (showOpenRouterKey) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showOpenRouterKey = !showOpenRouterKey }) {
                            Icon(
                                imageVector = if (showOpenRouterKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showOpenRouterKey) "Hide key" else "Show key",
                                tint = GeminiSubtext
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GeminiBg,
                        unfocusedContainerColor = GeminiBg,
                        focusedBorderColor = GeminiAccent,
                        unfocusedBorderColor = GeminiBorder,
                        focusedTextColor = GeminiText,
                        unfocusedTextColor = GeminiText
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Model Name Field
                Text(
                    text = "Model Name",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = GeminiText
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = modelName,
                    onValueChange = { modelName = it },
                    placeholder = { Text("google/gemini-2.5-pro", color = GeminiSubtext) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("model_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GeminiBg,
                        unfocusedContainerColor = GeminiBg,
                        focusedBorderColor = GeminiAccent,
                        unfocusedBorderColor = GeminiBorder,
                        focusedTextColor = GeminiText,
                        unfocusedTextColor = GeminiText
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                Text(
                    text = "Must match model IDs (e.g. google/gemini-2.5-pro, openai/gpt-4o)",
                    fontSize = 11.5.sp,
                    color = GeminiSubtext,
                    modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Model Selector Chips
                Text(
                    text = "Quick Presets:",
                    fontSize = 12.sp,
                    color = GeminiSubtext,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SettingsManager.POPULAR_MODELS.forEach { modelPreset ->
                        val isSelected = modelName.equals(modelPreset, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) GeminiAccent else GeminiBg)
                                .border(1.dp, if (isSelected) GeminiAccent else GeminiBorder, RoundedCornerShape(8.dp))
                                .clickable { modelName = modelPreset }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = modelPreset.substringAfter("/"),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) GeminiBg else GeminiText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Optional Direct Gemini API Key
                Text(
                    text = "Google Gemini API Key (Optional fallback)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = GeminiSubtext
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = geminiKey,
                    onValueChange = { geminiKey = it },
                    placeholder = { Text("AIzaSy...", color = GeminiSubtext) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showGeminiKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showGeminiKey = !showGeminiKey }) {
                            Icon(
                                imageVector = if (showGeminiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = GeminiSubtext
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GeminiBg,
                        unfocusedContainerColor = GeminiBg,
                        focusedBorderColor = GeminiAccent,
                        unfocusedBorderColor = GeminiBorder,
                        focusedTextColor = GeminiText,
                        unfocusedTextColor = GeminiText
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GeminiText),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("cancel_settings_btn")
                    ) {
                        Text("Cancel", fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            onSave(openRouterKey, geminiKey, modelName, currentSettings.systemPrompt)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_settings_btn")
                    ) {
                        Text("Save Changes", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
