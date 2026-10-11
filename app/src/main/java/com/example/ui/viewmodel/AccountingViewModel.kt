package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AccountingTotals
import com.example.data.model.CalculationRole
import com.example.data.model.ColumnType
import com.example.data.model.RowHeightLevel
import com.example.data.model.SiteColumnEntity
import com.example.data.model.SiteEntity
import com.example.data.model.SiteRowEntity
import com.example.data.repository.AccountingRepository
import com.example.util.AppLanguage
import com.example.util.Localization
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AccountingRepository(AppDatabase.getDatabase(application))

    private val _language = MutableStateFlow(AppLanguage.BN)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    val allSites: StateFlow<List<SiteEntity>> = repository.getAllSites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeSiteId = MutableStateFlow<Long?>(null)
    val activeSiteId: StateFlow<Long?> = _activeSiteId.asStateFlow()

    private val _activeSite = MutableStateFlow<SiteEntity?>(null)
    val activeSite: StateFlow<SiteEntity?> = _activeSite.asStateFlow()

    private val _columns = MutableStateFlow<List<SiteColumnEntity>>(emptyList())
    val columns: StateFlow<List<SiteColumnEntity>> = _columns.asStateFlow()

    private val _rows = MutableStateFlow<List<SiteRowEntity>>(emptyList())
    val rows: StateFlow<List<SiteRowEntity>> = _rows.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedSampleSitesIfEmpty()
        }

        // Listen to active site changes
        viewModelScope.launch {
            _activeSiteId.collect { siteId ->
                if (siteId != null) {
                    launch {
                        repository.getSiteById(siteId).collect { site ->
                            _activeSite.value = site
                        }
                    }
                    launch {
                        repository.getColumnsForSite(siteId).collect { colList ->
                            _columns.value = colList
                        }
                    }
                    launch {
                        repository.getRowsForSite(siteId).collect { rowList ->
                            _rows.value = rowList
                        }
                    }
                } else {
                    _activeSite.value = null
                    _columns.value = emptyList()
                    _rows.value = emptyList()
                }
            }
        }
    }

    // Financial totals automatically calculated from site rows
    val totals: StateFlow<AccountingTotals> = combine(
        _activeSite,
        _columns,
        _rows
    ) { site, cols, rws ->
        if (site == null || cols.isEmpty()) {
            AccountingTotals()
        } else {
            repository.calculateTotals(site, cols, rws)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AccountingTotals())

    fun toggleLanguage() {
        _language.value = if (_language.value == AppLanguage.EN) AppLanguage.BN else AppLanguage.EN
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
    }

    fun selectSite(siteId: Long?) {
        _activeSiteId.value = siteId
    }

    fun createSite(name: String, location: String, clientName: String, notes: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.createSite(name, location, clientName, notes)
            selectSite(id)
            onCreated(id)
        }
    }

    fun updateSite(site: SiteEntity) {
        viewModelScope.launch {
            repository.updateSite(site)
        }
    }

    fun deleteSite(siteId: Long) {
        viewModelScope.launch {
            if (_activeSiteId.value == siteId) {
                _activeSiteId.value = null
            }
            repository.deleteSite(siteId)
        }
    }

    fun addRow(cellValues: Map<String, String>) {
        val siteId = _activeSiteId.value ?: return
        viewModelScope.launch {
            repository.addRow(siteId, cellValues)
        }
    }

    fun updateRow(row: SiteRowEntity, cellValues: Map<String, String>) {
        viewModelScope.launch {
            repository.updateRow(row, cellValues)
        }
    }

    fun updateCell(row: SiteRowEntity, columnKey: String, newValue: String) {
        viewModelScope.launch {
            val map = repository.jsonToMap(row.cellsJson).toMutableMap()
            map[columnKey] = newValue
            repository.updateRow(row, map)
        }
    }

    fun deleteRow(rowId: Long) {
        viewModelScope.launch {
            repository.deleteRow(rowId)
        }
    }

    fun addColumn(
        titleEn: String,
        titleBn: String,
        type: ColumnType,
        widthDp: Int = 130,
        role: CalculationRole = CalculationRole.NONE,
        options: String = ""
    ) {
        val siteId = _activeSiteId.value ?: return
        viewModelScope.launch {
            repository.addColumn(siteId, titleEn, titleBn, type, widthDp, role, options)
        }
    }

    fun updateColumn(column: SiteColumnEntity) {
        viewModelScope.launch {
            repository.updateColumn(column)
        }
    }

    fun deleteColumn(column: SiteColumnEntity) {
        viewModelScope.launch {
            repository.deleteColumn(column)
        }
    }

    fun moveColumn(index: Int, direction: Int) {
        val current = _columns.value.toMutableList()
        val targetIndex = index + direction
        if (targetIndex in current.indices) {
            val item = current.removeAt(index)
            current.add(targetIndex, item)
            viewModelScope.launch {
                repository.reorderColumns(current)
            }
        }
    }

    fun setZoomPercent(percent: Int) {
        val site = _activeSite.value ?: return
        val clamped = percent.coerceIn(65, 180)
        viewModelScope.launch {
            repository.updateZoom(site.id, clamped)
        }
    }

    fun zoomIn() {
        val site = _activeSite.value ?: return
        val newZoom = (site.zoomPercent + 15).coerceAtMost(180)
        viewModelScope.launch {
            repository.updateZoom(site.id, newZoom)
        }
    }

    fun zoomOut() {
        val site = _activeSite.value ?: return
        val newZoom = (site.zoomPercent - 15).coerceAtLeast(65)
        viewModelScope.launch {
            repository.updateZoom(site.id, newZoom)
        }
    }

    fun resetZoom() {
        val site = _activeSite.value ?: return
        viewModelScope.launch {
            repository.updateZoom(site.id, 100)
        }
    }

    fun setRowHeight(level: RowHeightLevel) {
        val site = _activeSite.value ?: return
        viewModelScope.launch {
            repository.updateRowHeight(site.id, level)
        }
    }

    fun toggleIncludeSalaryInExpenses() {
        val site = _activeSite.value ?: return
        viewModelScope.launch {
            repository.updateSalaryRule(site.id, !site.includeSalaryInExpenses)
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun exportCsv(context: Context) {
        val site = _activeSite.value ?: return
        val cols = _columns.value
        val rws = _rows.value
        val useBengali = _language.value == AppLanguage.BN
        val csv = repository.exportToCsv(cols, rws, useBengali)

        // Share via Intent & Copy to Clipboard
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Site Accounting CSV", csv)
        clipboard.setPrimaryClip(clip)

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, csv)
            putExtra(Intent.EXTRA_SUBJECT, "${site.name} Accounting.csv")
            type = "text/csv"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            val chooser = Intent.createChooser(sendIntent, "Share / Save CSV").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (_: Exception) {}

        _statusMessage.value = Localization.get("backup_copied", _language.value)
    }

    fun exportFullBackup(context: Context) {
        viewModelScope.launch {
            val json = repository.exportAllToJson()
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Sujon Contractor Backup JSON", json)
            clipboard.setPrimaryClip(clip)

            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, json)
                putExtra(Intent.EXTRA_SUBJECT, "Sujon_Contractor_Backup.json")
                type = "application/json"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                val chooser = Intent.createChooser(sendIntent, "Share / Save Backup JSON").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            } catch (_: Exception) {}

            _statusMessage.value = Localization.get("backup_copied", _language.value)
        }
    }

    fun restoreBackup(jsonString: String, onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.importFromJson(jsonString)
            if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                val msg = if (_language.value == AppLanguage.BN) {
                    "$count টি প্রজেক্ট সাইট সফলভাবে রিস্টোর করা হয়েছে!"
                } else {
                    "Successfully restored $count project sites!"
                }
                _statusMessage.value = msg
                onDone(true, msg)
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "Import failed"
                _statusMessage.value = "Error: $err"
                onDone(false, err)
            }
        }
    }

    fun seedDemoData() {
        viewModelScope.launch {
            repository.seedSampleData()
            _statusMessage.value = Localization.get("sample_loaded", _language.value)
        }
    }
}
