package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountingTotals
import com.example.data.model.ColumnType
import com.example.data.model.RowHeightLevel
import com.example.data.model.SiteColumnEntity
import com.example.data.model.SiteEntity
import com.example.data.model.SiteRowEntity
import com.example.ui.theme.ContractorBackground
import com.example.ui.theme.ContractorBorder
import com.example.ui.theme.ContractorGreen
import com.example.ui.theme.ContractorMintContainer
import com.example.ui.theme.ElectricGoldLight
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.ShinyGoldBadgeGradient
import com.example.ui.theme.ShinyHeaderGradient
import com.example.util.AppLanguage
import com.example.util.Localization
import org.json.JSONObject

@Composable
fun TableView(
    site: SiteEntity,
    columns: List<SiteColumnEntity>,
    rows: List<SiteRowEntity>,
    language: AppLanguage,
    onAddRowClick: () -> Unit,
    onEditRowClick: (SiteRowEntity) -> Unit,
    onDeleteRowClick: (Long) -> Unit,
    onUpdateCell: (SiteRowEntity, String, String) -> Unit,
    onZoomIn: () -> Unit = {},
    onZoomOut: () -> Unit = {},
    onResetZoom: () -> Unit = {},
    onSetZoom: (Int) -> Unit = {},
    onSetRowHeight: (RowHeightLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    var localZoomPercent by remember(site.zoomPercent) { mutableStateOf(site.zoomPercent.toFloat()) }
    val zoomFactor = (localZoomPercent / 100f).coerceIn(0.65f, 2.0f)

    val transformState = rememberTransformableState { zoomChange, _, _ ->
        if (zoomChange != 1f) {
            val updated = (localZoomPercent * zoomChange).coerceIn(65f, 180f)
            localZoomPercent = updated
            val newInt = updated.toInt()
            if (kotlin.math.abs(newInt - site.zoomPercent) >= 2) {
                onSetZoom(newInt)
            }
        }
    }

    val baseHeight = when (site.rowHeightLevel) {
        RowHeightLevel.SMALL.name -> RowHeightLevel.SMALL.heightDp
        RowHeightLevel.LARGE.name -> RowHeightLevel.LARGE.heightDp
        else -> RowHeightLevel.MEDIUM.heightDp
    }
    val effectiveRowHeight = (baseHeight * zoomFactor).dp
    val headerHeight = (44 * zoomFactor).coerceAtLeast(36f).dp
    val fontSize = (13 * zoomFactor).coerceIn(9f, 20f).sp
    val indexColWidth = (46 * zoomFactor).coerceAtLeast(36f).dp

    val horizontalScrollState = rememberScrollState()

    var quickEditCell by remember { mutableStateOf<QuickEditData?>(null) }
    var rowToDelete by remember { mutableStateOf<SiteRowEntity?>(null) }

    Column(modifier = modifier.fillMaxSize().background(ContractorBackground)) {
        // Bright & Shining Toolbar (No zoom text/buttons; zooming functions automatically via pinch gesture)
        TableControlToolbar(
            currentRowHeight = site.rowHeightLevel,
            language = language,
            onSetRowHeight = onSetRowHeight,
            onAddRow = onAddRowClick
        )

        if (rows.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(ShinyGoldBadgeGradient)
                                .border(1.5.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TableChart,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = Localization.get("empty_table", language),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = Localization.get("empty_table_sub", language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onAddRowClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(ShinyHeaderGradient)
                                .testTag("empty_add_row_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = ElectricGoldLight)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    Localization.get("add_row", language),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Spreadsheet Table Area (Pinch-to-zoom operates automatically in the background)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .transformable(state = transformState)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(horizontalScrollState)
                ) {
                    Column {
                        // 1. Table Header Row (Shining Green Gradient)
                        Row(
                            modifier = Modifier
                                .height(headerHeight)
                                .background(ShinyHeaderGradient)
                                .border(0.5.dp, Color(0xFF065F46)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Row Number Header (#)
                            Box(
                                modifier = Modifier
                                    .width(indexColWidth)
                                    .fillMaxHeight()
                                    .border(0.5.dp, Color.White.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = Localization.get("row_number", language),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontSize = fontSize,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White
                                )
                            }

                            // Dynamic Columns Headers
                            for (col in columns) {
                                val colWidth = (col.widthDp * zoomFactor).coerceAtLeast(60f).dp
                                Box(
                                    modifier = Modifier
                                        .width(colWidth)
                                        .fillMaxHeight()
                                        .border(0.5.dp, Color.White.copy(alpha = 0.25f))
                                        .padding(horizontal = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val colTitle = if (language == AppLanguage.BN) col.titleBn else col.titleEn
                                    Text(
                                        text = colTitle,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontSize = fontSize,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // Actions Header
                            Box(
                                modifier = Modifier
                                    .width((70 * zoomFactor).coerceAtLeast(60f).dp)
                                    .fillMaxHeight()
                                    .border(0.5.dp, Color.White.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "•••",
                                    color = Color.White,
                                    fontSize = fontSize,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // 2. Bright & Shining Table Rows
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .testTag("spreadsheet_rows_list")
                        ) {
                            itemsIndexed(rows, key = { _, r -> r.id }) { index, row ->
                                val rowMap = remember(row.cellsJson) { parseCells(row.cellsJson) }
                                val isAlternate = index % 2 == 1
                                val rowBg = if (isAlternate) Color(0xFFF7FCF9) else Color.White

                                Row(
                                    modifier = Modifier
                                        .height(effectiveRowHeight)
                                        .background(rowBg)
                                        .border(0.5.dp, ContractorBorder),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Row Index Cell
                                    Box(
                                        modifier = Modifier
                                            .width(indexColWidth)
                                            .fillMaxHeight()
                                            .border(0.5.dp, ContractorBorder)
                                            .background(Color(0xFFE8F5EE))
                                            .clickable { onEditRowClick(row) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = Localization.formatNumber(index + 1, language),
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = fontSize,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = ContractorGreen
                                        )
                                    }

                                    // Columns Cells
                                    for (col in columns) {
                                        val colWidth = (col.widthDp * zoomFactor).coerceAtLeast(60f).dp
                                        val cellValue = rowMap[col.key] ?: ""

                                        Box(
                                            modifier = Modifier
                                                .width(colWidth)
                                                .fillMaxHeight()
                                                .border(0.5.dp, ContractorBorder)
                                                .clickable {
                                                    quickEditCell = QuickEditData(
                                                        row = row,
                                                        column = col,
                                                        currentValue = cellValue
                                                    )
                                                }
                                                .padding(horizontal = (6 * zoomFactor).coerceAtLeast(3f).dp),
                                            contentAlignment = when (col.type) {
                                                ColumnType.MONEY.name, ColumnType.NUMBER.name -> Alignment.CenterEnd
                                                ColumnType.DATE.name, ColumnType.CATEGORY.name -> Alignment.Center
                                                else -> Alignment.CenterStart
                                            }
                                        ) {
                                            TableCellContent(
                                                value = cellValue,
                                                column = col,
                                                fontSize = fontSize,
                                                language = language
                                            )
                                        }
                                    }

                                    // Action Cell (Edit & Delete Row)
                                    Row(
                                        modifier = Modifier
                                            .width((70 * zoomFactor).coerceAtLeast(60f).dp)
                                            .fillMaxHeight()
                                            .border(0.5.dp, ContractorBorder),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { onEditRowClick(row) },
                                            modifier = Modifier.size((28 * zoomFactor).coerceAtLeast(24f).dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Row",
                                                tint = ContractorGreen,
                                                modifier = Modifier.size((16 * zoomFactor).coerceAtLeast(14f).dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { rowToDelete = row },
                                            modifier = Modifier.size((28 * zoomFactor).coerceAtLeast(24f).dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Row",
                                                tint = ExpenseRed,
                                                modifier = Modifier.size((16 * zoomFactor).coerceAtLeast(14f).dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom "Add Row" button bar inside table scroll view
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Button(
                                onClick = onAddRowClick,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ShinyHeaderGradient)
                                    .testTag("table_bottom_add_row_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = ElectricGoldLight, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        Localization.get("add_row", language),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Quick Edit Cell Dialog
    quickEditCell?.let { editData ->
        QuickEditCellDialog(
            data = editData,
            language = language,
            onDismiss = { quickEditCell = null },
            onSave = { newVal ->
                onUpdateCell(editData.row, editData.column.key, newVal)
                quickEditCell = null
            }
        )
    }

    // Confirm Row Delete Dialog
    rowToDelete?.let { row ->
        AlertDialog(
            onDismissRequest = { rowToDelete = null },
            title = { Text(Localization.get("delete_row", language), fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
            text = { Text(Localization.get("delete_row_confirm", language)) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteRowClick(row.id)
                        rowToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    modifier = Modifier.testTag("confirm_delete_row_button")
                ) {
                    Text(Localization.get("delete", language))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { rowToDelete = null }) {
                    Text(Localization.get("cancel", language))
                }
            }
        )
    }
}

@Composable
fun TableCellContent(
    value: String,
    column: SiteColumnEntity,
    fontSize: androidx.compose.ui.unit.TextUnit,
    language: AppLanguage
) {
    if (value.isBlank()) {
        Text(
            text = "-",
            color = Color.LightGray,
            fontSize = fontSize
        )
        return
    }

    when (column.type) {
        ColumnType.MONEY.name -> {
            val num = value.toDoubleOrNull()
            if (num != null) {
                val isIncome = column.key == "col_income" || column.key == "col_received"
                val textColor = if (isIncome) IncomeGreen else ExpenseRed
                Text(
                    text = Localization.formatCurrency(num, language),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold
                    ),
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(text = value, fontSize = fontSize, maxLines = 1)
            }
        }
        ColumnType.CATEGORY.name -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFE0F2FE))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = (fontSize.value * 0.9f).sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF0369A1),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        ColumnType.NUMBER.name -> {
            val numStr = if (language == AppLanguage.BN) Localization.toBanglaDigits(value) else value
            Text(
                text = numStr,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        else -> {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = fontSize),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TableControlToolbar(
    currentRowHeight: String,
    language: AppLanguage,
    onSetRowHeight: (RowHeightLevel) -> Unit,
    onAddRow: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Row Height Selector (S / M / L)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = Localization.get("row_height", language),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.width(8.dp))

                RowHeightPill(
                    label = if (language == AppLanguage.BN) "ছোট" else "S",
                    selected = currentRowHeight == RowHeightLevel.SMALL.name,
                    onClick = { onSetRowHeight(RowHeightLevel.SMALL) }
                )
                Spacer(modifier = Modifier.width(4.dp))
                RowHeightPill(
                    label = if (language == AppLanguage.BN) "মাঝারি" else "M",
                    selected = currentRowHeight == RowHeightLevel.MEDIUM.name,
                    onClick = { onSetRowHeight(RowHeightLevel.MEDIUM) }
                )
                Spacer(modifier = Modifier.width(4.dp))
                RowHeightPill(
                    label = if (language == AppLanguage.BN) "বড়" else "L",
                    selected = currentRowHeight == RowHeightLevel.LARGE.name,
                    onClick = { onSetRowHeight(RowHeightLevel.LARGE) }
                )
            }

            // Add Row Button
            FilledTonalButton(
                onClick = onAddRow,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = ContractorMintContainer,
                    contentColor = ContractorGreen
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp).testTag("toolbar_add_row_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = Localization.get("add_row", language),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun RowHeightPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) ContractorGreen else Color(0xFFE2E8F0))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (selected) Color.White else Color(0xFF475569)
        )
    }
}

data class QuickEditData(
    val row: SiteRowEntity,
    val column: SiteColumnEntity,
    val currentValue: String
)

@Composable
fun QuickEditCellDialog(
    data: QuickEditData,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var textValue by remember { mutableStateOf(data.currentValue) }
    val colTitle = if (language == AppLanguage.BN) data.column.titleBn else data.column.titleEn

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "${Localization.get("quick_edit_cell", language)} ($colTitle)", color = Color(0xFF0F172A)) },
        text = {
            Column {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    label = { Text(colTitle) },
                    singleLine = data.column.type != ColumnType.TEXT.name,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("quick_edit_cell_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(textValue.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = ContractorGreen),
                modifier = Modifier.testTag("save_quick_edit_cell_button")
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

fun parseCells(jsonStr: String): Map<String, String> {
    if (jsonStr.isBlank()) return emptyMap()
    val map = mutableMapOf<String, String>()
    try {
        val json = JSONObject(jsonStr)
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            map[key] = json.optString(key, "")
        }
    } catch (_: Exception) {}
    return map
}
