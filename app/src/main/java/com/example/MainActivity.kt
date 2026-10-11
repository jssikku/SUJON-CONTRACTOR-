package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ImportBackupDialog
import com.example.ui.components.TopNavBar
import com.example.ui.screens.SiteDetailScreen
import com.example.ui.screens.SiteListScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AccountingViewModel
import com.example.util.Localization
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: AccountingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: AccountingViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val language by viewModel.language.collectAsStateWithLifecycle()
    val sites by viewModel.allSites.collectAsStateWithLifecycle()
    val activeSiteId by viewModel.activeSiteId.collectAsStateWithLifecycle()
    val activeSite by viewModel.activeSite.collectAsStateWithLifecycle()
    val columns by viewModel.columns.collectAsStateWithLifecycle()
    val rows by viewModel.rows.collectAsStateWithLifecycle()
    val totals by viewModel.totals.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    var showImportDialog by remember { mutableStateOf(false) }

    // Status message feedback
    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(msg)
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        topBar = {
            TopNavBar(
                title = if (activeSite != null) activeSite!!.name else Localization.get("app_title", language),
                subtitle = if (activeSite != null) {
                    if (activeSite!!.location.isNotBlank()) activeSite!!.location else Localization.get("table_view", language)
                } else {
                    Localization.get("app_subtitle", language)
                },
                language = language,
                canNavigateBack = activeSiteId != null,
                onNavigateBack = { viewModel.selectSite(null) },
                onToggleLanguage = { viewModel.toggleLanguage() },
                onExportBackup = { viewModel.exportFullBackup(context) },
                onImportBackup = { showImportDialog = true },
                onSeedDemo = { viewModel.seedDemoData() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (activeSiteId == null || activeSite == null) {
                SiteListScreen(
                    sites = sites,
                    language = language,
                    onOpenSite = { viewModel.selectSite(it) },
                    onCreateSite = { name, loc, client, notes ->
                        viewModel.createSite(name, loc, client, notes) { newId ->
                            viewModel.selectSite(newId)
                        }
                    },
                    onUpdateSite = { viewModel.updateSite(it) },
                    onDeleteSite = { viewModel.deleteSite(it) },
                    onSeedDemo = { viewModel.seedDemoData() }
                )
            } else {
                SiteDetailScreen(
                    site = activeSite!!,
                    columns = columns,
                    rows = rows,
                    totals = totals,
                    language = language,
                    onBack = { viewModel.selectSite(null) },
                    onAddRow = { viewModel.addRow(it) },
                    onUpdateRow = { row, vals -> viewModel.updateRow(row, vals) },
                    onDeleteRow = { viewModel.deleteRow(it) },
                    onUpdateCell = { row, colKey, newVal -> viewModel.updateCell(row, colKey, newVal) },
                    onAddColumn = { titleEn, titleBn, type, width, role, options ->
                        viewModel.addColumn(titleEn, titleBn, type, width, role, options)
                    },
                    onUpdateColumn = { viewModel.updateColumn(it) },
                    onDeleteColumn = { viewModel.deleteColumn(it) },
                    onMoveColumn = { index, dir -> viewModel.moveColumn(index, dir) },
                    onZoomIn = { viewModel.zoomIn() },
                    onZoomOut = { viewModel.zoomOut() },
                    onResetZoom = { viewModel.resetZoom() },
                    onSetZoom = { viewModel.setZoomPercent(it) },
                    onSetRowHeight = { viewModel.setRowHeight(it) },
                    onToggleIncludeSalary = { viewModel.toggleIncludeSalaryInExpenses() },
                    onExportCsv = { viewModel.exportCsv(context) }
                )
            }
        }
    }

    if (showImportDialog) {
        ImportBackupDialog(
            language = language,
            onDismiss = { showImportDialog = false },
            onRestore = { json ->
                viewModel.restoreBackup(json) { success, _ ->
                    showImportDialog = false
                }
            }
        )
    }
}
