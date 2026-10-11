package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.util.AppLanguage
import com.example.util.Localization

@Composable
fun ImportBackupDialog(
    language: AppLanguage,
    onDismiss: () -> Unit,
    onRestore: (String) -> Unit
) {
    var jsonInput by remember { mutableStateOf("") }
    var showConfirmWarning by remember { mutableStateOf(false) }

    if (showConfirmWarning) {
        AlertDialog(
            onDismissRequest = { showConfirmWarning = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text(Localization.get("confirm", language), fontWeight = FontWeight.Bold) },
            text = { Text(Localization.get("import_confirm", language)) },
            confirmButton = {
                Button(
                    onClick = {
                        onRestore(jsonInput.trim())
                        showConfirmWarning = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("confirm_restore_backup_button")
                ) {
                    Text(Localization.get("confirm", language))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmWarning = false }) {
                    Text(Localization.get("cancel", language))
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(Localization.get("import_json", language), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (language == AppLanguage.BN)
                            "পূর্বে ব্যাকআপ নেওয়া JSON টেক্সট নিচে পেস্ট করুন:"
                        else
                            "Paste your previously exported JSON backup text below:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = jsonInput,
                        onValueChange = { jsonInput = it },
                        label = { Text("Backup JSON Content") },
                        placeholder = { Text("{\"app\": \"SUJON_CONTRACTOR\", ...}") },
                        maxLines = 8,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("backup_json_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (jsonInput.isNotBlank()) {
                            showConfirmWarning = true
                        }
                    },
                    enabled = jsonInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("submit_restore_json_button")
                ) {
                    Text(Localization.get("confirm", language))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text(Localization.get("cancel", language))
                }
            }
        )
    }
}
