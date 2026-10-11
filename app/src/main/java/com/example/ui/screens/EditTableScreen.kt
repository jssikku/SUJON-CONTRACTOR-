package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalculationRole
import com.example.data.model.ColumnType
import com.example.data.model.SiteColumnEntity
import com.example.ui.theme.ContractorBackground
import com.example.ui.theme.ContractorGreen
import com.example.ui.theme.ContractorGreenDeep
import com.example.ui.theme.ContractorMintContainer
import com.example.ui.theme.ElectricGoldLight
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ShinyHeaderGradient
import com.example.util.AppLanguage
import com.example.util.Localization

@Composable
fun EditTableScreen(
    columns: List<SiteColumnEntity>,
    language: AppLanguage,
    onAddColumn: (String, String, ColumnType, Int, CalculationRole, String) -> Unit,
    onUpdateColumn: (SiteColumnEntity) -> Unit,
    onDeleteColumn: (SiteColumnEntity) -> Unit,
    onMoveColumn: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var columnToEdit by remember { mutableStateOf<SiteColumnEntity?>(null) }
    var columnToDelete by remember { mutableStateOf<SiteColumnEntity?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize().background(ContractorBackground).testTag("edit_table_scroll")
    ) {
        // Banner (Shining Green Tint)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFFF0FDF4),
                                    Color(0xFFDCFCE7),
                                    Color(0xFFD1FAE5)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = Localization.get("edit_table_view", language),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = ContractorGreenDeep
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (language == AppLanguage.BN)
                                    "টেবিলের কলাম যোগ, সম্পাদনা, প্রস্থ পরিবর্তন বা ক্রম সাজান।"
                                else
                                    "Add, edit, adjust width, or reorder table columns.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF334155)
                            )
                        }

                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(ShinyHeaderGradient)
                                .testTag("add_column_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = ElectricGoldLight, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    Localization.get("add_column", language),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Columns List
        itemsIndexed(columns, key = { _, col -> col.id }) { index, col ->
            ColumnItemCard(
                column = col,
                index = index,
                totalCount = columns.size,
                language = language,
                onMoveUp = { onMoveColumn(index, -1) },
                onMoveDown = { onMoveColumn(index, 1) },
                onEdit = { columnToEdit = col },
                onDelete = { columnToDelete = col }
            )
        }
    }

    // Add Column Dialog
    if (showAddDialog) {
        ColumnEditorDialog(
            title = Localization.get("add_column", language),
            language = language,
            onDismiss = { showAddDialog = false },
            onConfirm = { titleEn, titleBn, type, width, role, options ->
                onAddColumn(titleEn, titleBn, type, width, role, options)
                showAddDialog = false
            }
        )
    }

    // Edit Column Dialog
    columnToEdit?.let { col ->
        ColumnEditorDialog(
            title = Localization.get("edit_column", language),
            initialTitleEn = col.titleEn,
            initialTitleBn = col.titleBn,
            initialType = ColumnType.valueOf(col.type),
            initialWidthDp = col.widthDp,
            initialRole = CalculationRole.valueOf(col.calculationRole),
            initialOptions = col.optionsJson,
            language = language,
            onDismiss = { columnToEdit = null },
            onConfirm = { titleEn, titleBn, type, width, role, options ->
                onUpdateColumn(
                    col.copy(
                        titleEn = titleEn,
                        titleBn = titleBn,
                        type = type.name,
                        widthDp = width,
                        calculationRole = role.name,
                        optionsJson = options
                    )
                )
                columnToEdit = null
            }
        )
    }

    // Delete Column Warning Dialog
    columnToDelete?.let { col ->
        val colTitle = if (language == AppLanguage.BN) col.titleBn else col.titleEn
        AlertDialog(
            onDismissRequest = { columnToDelete = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = ExpenseRed) },
            title = { Text(Localization.get("delete_column", language), color = Color(0xFF0F172A), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = Localization.get("delete_column_warn", language),
                        color = ExpenseRed,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = colTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF0F172A)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteColumn(col)
                        columnToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    modifier = Modifier.testTag("confirm_delete_column_button")
                ) {
                    Text(Localization.get("delete", language))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { columnToDelete = null }) {
                    Text(Localization.get("cancel", language))
                }
            }
        )
    }
}

@Composable
fun ColumnItemCard(
    column: SiteColumnEntity,
    index: Int,
    totalCount: Int,
    language: AppLanguage,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("column_item_${column.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Reorder controls
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onMoveUp,
                    enabled = index > 0,
                    modifier = Modifier.size(28.dp).testTag("move_up_col_${column.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Move Up",
                        tint = if (index > 0) ContractorGreen else Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = Localization.formatNumber(index + 1, language),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = ContractorGreen
                )
                IconButton(
                    onClick = onMoveDown,
                    enabled = index < totalCount - 1,
                    modifier = Modifier.size(28.dp).testTag("move_down_col_${column.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Move Down",
                        tint = if (index < totalCount - 1) ContractorGreen else Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Column details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (language == AppLanguage.BN) column.titleBn else column.titleEn,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Type pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE0E7FF))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = column.type,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF3730A3)
                        )
                    }

                    // Width pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${column.widthDp}dp",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF475569)
                        )
                    }

                    // Role pill
                    if (column.calculationRole != CalculationRole.NONE.name) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ContractorMintContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = column.calculationRole.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ContractorGreenDeep
                            )
                        }
                    }
                }
            }

            // Edit & Delete Buttons
            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp).testTag("edit_col_btn_${column.id}")) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Column",
                        tint = ContractorGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp).testTag("delete_col_btn_${column.id}")) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Column",
                        tint = ExpenseRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnEditorDialog(
    title: String,
    initialTitleEn: String = "",
    initialTitleBn: String = "",
    initialType: ColumnType = ColumnType.TEXT,
    initialWidthDp: Int = 120,
    initialRole: CalculationRole = CalculationRole.NONE,
    initialOptions: String = "",
    language: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (String, String, ColumnType, Int, CalculationRole, String) -> Unit
) {
    var titleEn by remember { mutableStateOf(initialTitleEn) }
    var titleBn by remember { mutableStateOf(initialTitleBn) }
    var selectedType by remember { mutableStateOf(initialType) }
    var widthSlider by remember { mutableFloatStateOf(initialWidthDp.toFloat()) }
    var selectedRole by remember { mutableStateOf(initialRole) }
    var optionsText by remember { mutableStateOf(initialOptions) }

    var typeMenuExpanded by remember { mutableStateOf(false) }
    var roleMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = titleEn,
                    onValueChange = { titleEn = it },
                    label = { Text(Localization.get("column_title_en", language)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("col_title_en_input")
                )

                OutlinedTextField(
                    value = titleBn,
                    onValueChange = { titleBn = it },
                    label = { Text(Localization.get("column_title_bn", language)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("col_title_bn_input")
                )

                // Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = typeMenuExpanded,
                    onExpandedChange = { typeMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedType.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(Localization.get("column_type", language)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = typeMenuExpanded,
                        onDismissRequest = { typeMenuExpanded = false }
                    ) {
                        ColumnType.values().forEach { cType ->
                            DropdownMenuItem(
                                text = { Text(cType.name) },
                                onClick = {
                                    selectedType = cType
                                    typeMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Calculation Role Dropdown
                ExposedDropdownMenuBox(
                    expanded = roleMenuExpanded,
                    onExpandedChange = { roleMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedRole.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(Localization.get("column_role", language)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = roleMenuExpanded,
                        onDismissRequest = { roleMenuExpanded = false }
                    ) {
                        CalculationRole.values().forEach { rRole ->
                            DropdownMenuItem(
                                text = { Text(rRole.name.replace("_", " ")) },
                                onClick = {
                                    selectedRole = rRole
                                    roleMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Width Selector Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = Localization.get("column_width", language),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF334155)
                        )
                        Text(
                            text = "${widthSlider.toInt()} dp",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = ContractorGreen
                        )
                    }
                    Slider(
                        value = widthSlider,
                        onValueChange = { widthSlider = it },
                        valueRange = 80f..250f,
                        steps = 16
                    )
                }

                if (selectedType == ColumnType.CATEGORY) {
                    OutlinedTextField(
                        value = optionsText,
                        onValueChange = { optionsText = it },
                        label = { Text("Dropdown Options (comma-separated)") },
                        placeholder = { Text("Labor, Technician, Material, Other") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalEn = titleEn.ifBlank { "Column" }
                    val finalBn = titleBn.ifBlank { finalEn }
                    onConfirm(
                        finalEn,
                        finalBn,
                        selectedType,
                        widthSlider.toInt(),
                        selectedRole,
                        optionsText
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = ContractorGreen),
                modifier = Modifier.testTag("save_column_dialog_button")
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
