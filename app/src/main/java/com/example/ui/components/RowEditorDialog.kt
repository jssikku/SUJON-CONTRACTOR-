package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.ColumnType
import com.example.data.model.SiteColumnEntity
import com.example.data.model.SiteRowEntity
import com.example.util.AppLanguage
import com.example.util.Localization
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RowEditorDialog(
    title: String,
    columns: List<SiteColumnEntity>,
    initialValues: Map<String, String> = emptyMap(),
    language: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (Map<String, String>) -> Unit
) {
    val cellValues = remember {
        mutableStateMapOf<String, String>().apply {
            putAll(initialValues)
            // Pre-fill today's date for Date column if empty
            val dateCol = columns.firstOrNull { it.key == "col_date" || it.type == ColumnType.DATE.name }
            if (dateCol != null && !containsKey(dateCol.key)) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                put(dateCol.key, sdf.format(Date()))
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(columns, key = { it.key }) { col ->
                    val colTitle = if (language == AppLanguage.BN) col.titleBn else col.titleEn
                    val currentValue = cellValues[col.key] ?: ""

                    when (col.type) {
                        ColumnType.DATE.name -> {
                            Column {
                                OutlinedTextField(
                                    value = currentValue,
                                    onValueChange = { cellValues[col.key] = it },
                                    label = { Text(colTitle) },
                                    placeholder = { Text("YYYY-MM-DD") },
                                    trailingIcon = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = {
                                                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                                cellValues[col.key] = sdf.format(Date())
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Default.Today,
                                                    contentDescription = "Today",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("row_input_${col.key}")
                                )
                            }
                        }
                        ColumnType.CATEGORY.name -> {
                            val optionsList = remember(col.optionsJson) {
                                if (col.optionsJson.isNotBlank()) {
                                    col.optionsJson.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                } else {
                                    listOf(
                                        "Labor", "Technician", "Trader", "Material",
                                        "Site Advance", "Client Payment", "Food & Transport", "Other"
                                    )
                                }
                            }
                            var menuExpanded by remember { mutableStateOf(false) }

                            ExposedDropdownMenuBox(
                                expanded = menuExpanded,
                                onExpandedChange = { menuExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = currentValue,
                                    onValueChange = { cellValues[col.key] = it },
                                    label = { Text(colTitle) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryEditable)
                                        .testTag("row_input_${col.key}")
                                )
                                ExposedDropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    optionsList.forEach { opt ->
                                        DropdownMenuItem(
                                            text = { Text(opt) },
                                            onClick = {
                                                cellValues[col.key] = opt
                                                menuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        ColumnType.MONEY.name -> {
                            OutlinedTextField(
                                value = currentValue,
                                onValueChange = { input ->
                                    // Allow numbers and decimal point only
                                    val filtered = input.filter { it.isDigit() || it == '.' }
                                    cellValues[col.key] = filtered
                                },
                                label = { Text(colTitle) },
                                leadingIcon = {
                                    Text(
                                        text = "৳",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(start = 12.dp)
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("row_input_${col.key}")
                            )
                        }
                        ColumnType.NUMBER.name -> {
                            OutlinedTextField(
                                value = currentValue,
                                onValueChange = { input ->
                                    val filtered = input.filter { it.isDigit() || it == '.' }
                                    cellValues[col.key] = filtered
                                },
                                label = { Text(colTitle) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("row_input_${col.key}")
                            )
                        }
                        else -> {
                            OutlinedTextField(
                                value = currentValue,
                                onValueChange = { cellValues[col.key] = it },
                                label = { Text(colTitle) },
                                maxLines = if (col.key == "col_note") 3 else 1,
                                modifier = Modifier.fillMaxWidth().testTag("row_input_${col.key}")
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(cellValues.toMap()) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("save_row_dialog_button")
            ) {
                Text(Localization.get("save", language), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(Localization.get("cancel", language))
            }
        }
    )
}
