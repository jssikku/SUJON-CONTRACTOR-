package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountingTotals
import com.example.data.model.CalculationRole
import com.example.data.model.ColumnType
import com.example.data.model.RowHeightLevel
import com.example.data.model.SiteColumnEntity
import com.example.data.model.SiteEntity
import com.example.data.model.SiteRowEntity
import com.example.ui.components.RowEditorDialog
import com.example.ui.theme.ContractorBackground
import com.example.ui.theme.ContractorGreen
import com.example.ui.theme.ContractorGreenDeep
import com.example.ui.theme.ElectricGold
import com.example.util.AppLanguage
import com.example.util.Localization

@Composable
fun SiteDetailScreen(
    site: SiteEntity,
    columns: List<SiteColumnEntity>,
    rows: List<SiteRowEntity>,
    totals: AccountingTotals,
    language: AppLanguage,
    onBack: () -> Unit,
    onAddRow: (Map<String, String>) -> Unit,
    onUpdateRow: (SiteRowEntity, Map<String, String>) -> Unit,
    onDeleteRow: (Long) -> Unit,
    onUpdateCell: (SiteRowEntity, String, String) -> Unit,
    onAddColumn: (String, String, ColumnType, Int, CalculationRole, String) -> Unit,
    onUpdateColumn: (SiteColumnEntity) -> Unit,
    onDeleteColumn: (SiteColumnEntity) -> Unit,
    onMoveColumn: (Int, Int) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetZoom: () -> Unit,
    onSetZoom: (Int) -> Unit = {},
    onSetRowHeight: (RowHeightLevel) -> Unit,
    onToggleIncludeSalary: () -> Unit,
    onExportCsv: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddRowDialog by remember { mutableStateOf(false) }
    var rowToEdit by remember { mutableStateOf<SiteRowEntity?>(null) }

    val tabs = listOf(
        Pair(Localization.get("table_view", language), Icons.Default.TableChart),
        Pair(Localization.get("summary_view", language), Icons.Default.Calculate),
        Pair(Localization.get("edit_table_view", language), Icons.Default.ViewColumn)
    )

    Column(modifier = modifier.fillMaxSize().background(ContractorBackground)) {
        // Site Sub-Header (Bright & Shining Green Tint)
        SiteSubHeader(
            site = site,
            language = language,
            onExportCsv = onExportCsv
        )

        // Navigation Tabs (Table, Summary, Customize Columns)
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 12.dp,
            containerColor = Color.White,
            contentColor = ContractorGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = ContractorGreen,
                    height = 3.dp
                )
            },
            modifier = Modifier.fillMaxWidth().testTag("site_detail_tab_row")
        ) {
            tabs.forEachIndexed { index, (tabTitle, icon) ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = tabTitle,
                            fontWeight = if (selectedTabIndex == index) FontWeight.ExtraBold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (selectedTabIndex == index) ContractorGreenDeep else Color(0xFF475569)
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = tabTitle,
                            tint = if (selectedTabIndex == index) ContractorGreen else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.testTag("tab_$index")
                )
            }
        }

        // Active Tab Screen Content
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (selectedTabIndex) {
                0 -> {
                    TableView(
                        site = site,
                        columns = columns,
                        rows = rows,
                        language = language,
                        onAddRowClick = { showAddRowDialog = true },
                        onEditRowClick = { rowToEdit = it },
                        onDeleteRowClick = onDeleteRow,
                        onUpdateCell = onUpdateCell,
                        onZoomIn = onZoomIn,
                        onZoomOut = onZoomOut,
                        onResetZoom = onResetZoom,
                        onSetZoom = onSetZoom,
                        onSetRowHeight = onSetRowHeight
                    )
                }
                1 -> {
                    SummaryView(
                        site = site,
                        totals = totals,
                        language = language,
                        onToggleIncludeSalary = onToggleIncludeSalary
                    )
                }
                2 -> {
                    EditTableScreen(
                        columns = columns,
                        language = language,
                        onAddColumn = onAddColumn,
                        onUpdateColumn = onUpdateColumn,
                        onDeleteColumn = onDeleteColumn,
                        onMoveColumn = onMoveColumn
                    )
                }
            }
        }
    }

    // Add Row Dialog
    if (showAddRowDialog) {
        RowEditorDialog(
            title = Localization.get("add_row", language),
            columns = columns,
            language = language,
            onDismiss = { showAddRowDialog = false },
            onConfirm = { values ->
                onAddRow(values)
                showAddRowDialog = false
            }
        )
    }

    // Edit Row Dialog
    rowToEdit?.let { row ->
        val initialMap = remember(row.cellsJson) { parseCells(row.cellsJson) }
        RowEditorDialog(
            title = Localization.get("edit_row", language),
            columns = columns,
            initialValues = initialMap,
            language = language,
            onDismiss = { rowToEdit = null },
            onConfirm = { values ->
                onUpdateRow(row, values)
                rowToEdit = null
            }
        )
    }
}

@Composable
fun SiteSubHeader(
    site: SiteEntity,
    language: AppLanguage,
    onExportCsv: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFFE8FDF0),
                        Color(0xFFDCFCE7),
                        Color(0xFFF0FDF4)
                    )
                )
            )
            .border(BorderStroke(1.dp, Color(0xFFA7F3D0)))
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = site.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = ContractorGreenDeep,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (site.location.isNotBlank() || site.clientName.isNotBlank()) {
                val subText = listOf(site.location, site.clientName)
                    .filter { it.isNotBlank() }
                    .joinToString(" • ")
                Text(
                    text = subText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF334155),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        IconButton(
            onClick = onExportCsv,
            modifier = Modifier.size(36.dp).testTag("export_csv_btn")
        ) {
            Icon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = "Export CSV",
                tint = ContractorGreen,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
