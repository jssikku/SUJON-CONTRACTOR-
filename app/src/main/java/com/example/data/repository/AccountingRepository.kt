package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.model.AccountingTotals
import com.example.data.model.CalculationRole
import com.example.data.model.ColumnType
import com.example.data.model.RowHeightLevel
import com.example.data.model.SiteColumnEntity
import com.example.data.model.SiteEntity
import com.example.data.model.SiteRowEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AccountingRepository(private val database: AppDatabase) {

    private val siteDao = database.siteDao()
    private val columnDao = database.columnDao()
    private val rowDao = database.rowDao()

    fun getAllSites(): Flow<List<SiteEntity>> = siteDao.getAllSites()

    fun getSiteById(siteId: Long): Flow<SiteEntity?> = siteDao.getSiteById(siteId)

    fun getColumnsForSite(siteId: Long): Flow<List<SiteColumnEntity>> = columnDao.getColumnsForSite(siteId)

    fun getRowsForSite(siteId: Long): Flow<List<SiteRowEntity>> = rowDao.getRowsForSite(siteId)

    suspend fun createSite(
        name: String,
        location: String = "",
        clientName: String = "",
        notes: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val site = SiteEntity(
            name = name,
            location = location,
            clientName = clientName,
            notes = notes
        )
        val siteId = siteDao.insertSite(site)
        createDefaultColumnsForSite(siteId)
        siteId
    }

    suspend fun updateSite(site: SiteEntity) = withContext(Dispatchers.IO) {
        siteDao.updateSite(site)
    }

    suspend fun deleteSite(siteId: Long) = withContext(Dispatchers.IO) {
        siteDao.deleteSiteById(siteId)
    }

    suspend fun createDefaultColumnsForSite(siteId: Long) = withContext(Dispatchers.IO) {
        val defaultColumns = listOf(
            SiteColumnEntity(
                siteId = siteId,
                key = "col_date",
                titleEn = "Date",
                titleBn = "তারিখ",
                type = ColumnType.DATE.name,
                orderIndex = 0,
                widthDp = 115,
                calculationRole = CalculationRole.NONE.name
            ),
            SiteColumnEntity(
                siteId = siteId,
                key = "col_details",
                titleEn = "Name / Details",
                titleBn = "নাম / কাজের বিবরণ",
                type = ColumnType.TEXT.name,
                orderIndex = 1,
                widthDp = 160,
                calculationRole = CalculationRole.NONE.name
            ),
            SiteColumnEntity(
                siteId = siteId,
                key = "col_category",
                titleEn = "Category",
                titleBn = "ক্যাটাগরি",
                type = ColumnType.CATEGORY.name,
                orderIndex = 2,
                widthDp = 140,
                calculationRole = CalculationRole.NONE.name,
                optionsJson = "Labor,Technician,Trader,Material,Site Advance,Client Payment,Food & Transport,Other"
            ),
            SiteColumnEntity(
                siteId = siteId,
                key = "col_quantity",
                titleEn = "Quantity / Work Details",
                titleBn = "পরিমাণ / বিবরণ",
                type = ColumnType.TEXT.name,
                orderIndex = 3,
                widthDp = 140,
                calculationRole = CalculationRole.NONE.name
            ),
            SiteColumnEntity(
                siteId = siteId,
                key = "col_salary",
                titleEn = "Daily Salary",
                titleBn = "দৈনিক বেতন",
                type = ColumnType.MONEY.name,
                orderIndex = 4,
                widthDp = 120,
                calculationRole = CalculationRole.LABOR_SALARY.name
            ),
            SiteColumnEntity(
                siteId = siteId,
                key = "col_income",
                titleEn = "Income",
                titleBn = "জমা / আয়",
                type = ColumnType.MONEY.name,
                orderIndex = 5,
                widthDp = 120,
                calculationRole = CalculationRole.INCOME.name
            ),
            SiteColumnEntity(
                siteId = siteId,
                key = "col_expense",
                titleEn = "Expense",
                titleBn = "খরচ",
                type = ColumnType.MONEY.name,
                orderIndex = 6,
                widthDp = 120,
                calculationRole = CalculationRole.OTHER_EXPENSE.name
            ),
            SiteColumnEntity(
                siteId = siteId,
                key = "col_received",
                titleEn = "Received from Site",
                titleBn = "সাইট থেকে গ্রহণ",
                type = ColumnType.MONEY.name,
                orderIndex = 7,
                widthDp = 135,
                calculationRole = CalculationRole.SITE_RECEIVED.name
            ),
            SiteColumnEntity(
                siteId = siteId,
                key = "col_note",
                titleEn = "Note",
                titleBn = "মন্তব্য",
                type = ColumnType.TEXT.name,
                orderIndex = 8,
                widthDp = 150,
                calculationRole = CalculationRole.NONE.name
            )
        )
        columnDao.insertColumns(defaultColumns)
    }

    suspend fun addColumn(
        siteId: Long,
        titleEn: String,
        titleBn: String,
        type: ColumnType,
        widthDp: Int = 130,
        calculationRole: CalculationRole = CalculationRole.NONE,
        options: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val currentColumns = columnDao.getColumnsForSiteSync(siteId)
        val nextOrder = (currentColumns.maxOfOrNull { it.orderIndex } ?: -1) + 1
        val uniqueKey = "col_" + System.currentTimeMillis()
        val column = SiteColumnEntity(
            siteId = siteId,
            key = uniqueKey,
            titleEn = titleEn.ifBlank { "Column ${nextOrder + 1}" },
            titleBn = titleBn.ifBlank { titleEn },
            type = type.name,
            orderIndex = nextOrder,
            widthDp = widthDp,
            calculationRole = calculationRole.name,
            optionsJson = options
        )
        columnDao.insertColumn(column)
    }

    suspend fun updateColumn(column: SiteColumnEntity) = withContext(Dispatchers.IO) {
        columnDao.updateColumn(column)
    }

    suspend fun deleteColumn(column: SiteColumnEntity) = withContext(Dispatchers.IO) {
        // Remove this column's key from all existing rows
        val rows = rowDao.getRowsForSiteSync(column.siteId)
        for (row in rows) {
            val cellMap = jsonToMap(row.cellsJson).toMutableMap()
            if (cellMap.containsKey(column.key)) {
                cellMap.remove(column.key)
                rowDao.updateRow(row.copy(cellsJson = mapToJson(cellMap), updatedAt = System.currentTimeMillis()))
            }
        }
        columnDao.deleteColumn(column)
    }

    suspend fun reorderColumns(columns: List<SiteColumnEntity>) = withContext(Dispatchers.IO) {
        columns.forEachIndexed { index, col ->
            columnDao.updateColumn(col.copy(orderIndex = index))
        }
    }

    suspend fun addRow(siteId: Long, cellValues: Map<String, String>): Long = withContext(Dispatchers.IO) {
        val currentRows = rowDao.getRowsForSiteSync(siteId)
        val nextIndex = (currentRows.maxOfOrNull { it.rowIndex } ?: -1) + 1
        val row = SiteRowEntity(
            siteId = siteId,
            rowIndex = nextIndex,
            cellsJson = mapToJson(cellValues),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        rowDao.insertRow(row)
    }

    suspend fun updateRow(row: SiteRowEntity, newCells: Map<String, String>) = withContext(Dispatchers.IO) {
        val updated = row.copy(
            cellsJson = mapToJson(newCells),
            updatedAt = System.currentTimeMillis()
        )
        rowDao.updateRow(updated)
    }

    suspend fun updateSingleCell(rowId: Long, columnKey: String, newValue: String) = withContext(Dispatchers.IO) {
        val rows = database.openHelper.readableDatabase
        // Alternatively get and update through DAO
    }

    suspend fun deleteRow(rowId: Long) = withContext(Dispatchers.IO) {
        rowDao.deleteRowById(rowId)
    }

    suspend fun updateZoom(siteId: Long, zoomPercent: Int) = withContext(Dispatchers.IO) {
        val site = siteDao.getSiteByIdSync(siteId) ?: return@withContext
        siteDao.updateSite(site.copy(zoomPercent = zoomPercent.coerceIn(60, 200)))
    }

    suspend fun updateRowHeight(siteId: Long, level: RowHeightLevel) = withContext(Dispatchers.IO) {
        val site = siteDao.getSiteByIdSync(siteId) ?: return@withContext
        siteDao.updateSite(site.copy(rowHeightLevel = level.name))
    }

    suspend fun updateSalaryRule(siteId: Long, includeInExpenses: Boolean) = withContext(Dispatchers.IO) {
        val site = siteDao.getSiteByIdSync(siteId) ?: return@withContext
        siteDao.updateSite(site.copy(includeSalaryInExpenses = includeInExpenses))
    }

    // Helper functions for JSON Map conversions
    fun mapToJson(map: Map<String, String>): String {
        val json = JSONObject()
        for ((k, v) in map) {
            json.put(k, v)
        }
        return json.toString()
    }

    fun jsonToMap(jsonStr: String): Map<String, String> {
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

    // Accounting calculation
    fun calculateTotals(
        site: SiteEntity,
        columns: List<SiteColumnEntity>,
        rows: List<SiteRowEntity>
    ): AccountingTotals {
        var totalLaborSalary = 0.0
        var totalTechSalary = 0.0
        var totalTraderSalary = 0.0
        var totalMaterialExpense = 0.0
        var totalOtherExpense = 0.0
        var totalIncome = 0.0
        var totalSiteReceived = 0.0

        val workingDaysSet = mutableSetOf<String>()

        val columnRoleMap = columns.associate { it.key to it.calculationRole }
        val categoryColKey = columns.firstOrNull { it.key == "col_category" || it.type == ColumnType.CATEGORY.name }?.key
        val dateColKey = columns.firstOrNull { it.key == "col_date" || it.type == ColumnType.DATE.name }?.key
        val salaryColKey = columns.firstOrNull { it.key == "col_salary" }?.key
        val expenseColKey = columns.firstOrNull { it.key == "col_expense" }?.key

        for (row in rows) {
            val cells = jsonToMap(row.cellsJson)
            val dateVal = dateColKey?.let { cells[it] } ?: ""
            val categoryVal = (categoryColKey?.let { cells[it] } ?: "").lowercase(Locale.ROOT)

            var rowHasWorkerActivity = false

            // 1. Process by column explicit calculation role
            for (col in columns) {
                val valueStr = cells[col.key]?.trim() ?: ""
                val amount = valueStr.toDoubleOrNull() ?: 0.0
                if (amount <= 0.0) continue

                when (col.calculationRole) {
                    CalculationRole.LABOR_SALARY.name -> {
                        // In default setup, check category if present to split between Labor, Tech, Trader
                        if (col.key == salaryColKey && categoryVal.isNotBlank()) {
                            if (categoryVal.contains("tech") || categoryVal.contains("টেকনিশিয়ান")) {
                                totalTechSalary += amount
                            } else if (categoryVal.contains("trader") || categoryVal.contains("ব্যবসায়ী") || categoryVal.contains("সাপ্লায়ার")) {
                                totalTraderSalary += amount
                            } else {
                                totalLaborSalary += amount
                            }
                        } else {
                            totalLaborSalary += amount
                        }
                        rowHasWorkerActivity = true
                    }
                    CalculationRole.TECHNICIAN_SALARY.name -> {
                        totalTechSalary += amount
                        rowHasWorkerActivity = true
                    }
                    CalculationRole.TRADER_SALARY.name -> {
                        totalTraderSalary += amount
                        rowHasWorkerActivity = true
                    }
                    CalculationRole.MATERIAL_EXPENSE.name -> {
                        totalMaterialExpense += amount
                    }
                    CalculationRole.OTHER_EXPENSE.name -> {
                        // Check if categorized as material
                        if (col.key == expenseColKey && (categoryVal.contains("material") || categoryVal.contains("মালামাল"))) {
                            totalMaterialExpense += amount
                        } else {
                            totalOtherExpense += amount
                        }
                    }
                    CalculationRole.INCOME.name -> {
                        totalIncome += amount
                    }
                    CalculationRole.SITE_RECEIVED.name -> {
                        totalSiteReceived += amount
                    }
                    CalculationRole.NONE.name -> {
                        // no-op
                    }
                }
            }

            if (rowHasWorkerActivity && dateVal.isNotBlank()) {
                workingDaysSet.add(dateVal)
            }
        }

        val totalSalaries = totalLaborSalary + totalTechSalary + totalTraderSalary
        val totalExpenses = if (site.includeSalaryInExpenses) {
            totalSalaries + totalMaterialExpense + totalOtherExpense
        } else {
            totalMaterialExpense + totalOtherExpense
        }

        val totalReceived = totalIncome + totalSiteReceived
        val remainingBalance = totalReceived - totalExpenses

        return AccountingTotals(
            totalLaborSalary = totalLaborSalary,
            totalTechnicianSalary = totalTechSalary,
            totalTraderSalary = totalTraderSalary,
            totalMaterialExpense = totalMaterialExpense,
            totalOtherExpense = totalOtherExpense,
            totalIncome = totalIncome,
            totalSiteReceived = totalSiteReceived,
            totalExpenses = totalExpenses,
            remainingBalance = remainingBalance,
            totalWorkingDays = workingDaysSet.size,
            totalRecords = rows.size
        )
    }

    // CSV generation with UTF-8 BOM
    fun exportToCsv(
        columns: List<SiteColumnEntity>,
        rows: List<SiteRowEntity>,
        useBengaliTitles: Boolean
    ): String {
        val sb = StringBuilder()
        sb.append('\uFEFF') // UTF-8 BOM for Excel

        // Headers
        val headers = columns.map { if (useBengaliTitles) it.titleBn else it.titleEn }
        sb.append(headers.joinToString(",") { escapeCsv(it) })
        sb.append("\r\n")

        // Rows
        for (row in rows) {
            val cells = jsonToMap(row.cellsJson)
            val line = columns.map { col ->
                escapeCsv(cells[col.key] ?: "")
            }
            sb.append(line.joinToString(","))
            sb.append("\r\n")
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        var str = value.replace("\"", "\"\"")
        if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
            str = "\"$str\""
        }
        return str
    }

    // Full JSON backup
    suspend fun exportAllToJson(): String = withContext(Dispatchers.IO) {
        val sites = database.openHelper.readableDatabase
        val allSites = siteDao.getAllSites()
        // Synchronously fetch all
        val root = JSONObject()
        root.put("app", "SUJON_CONTRACTOR")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val sitesArray = JSONArray()
        val siteList = siteDao.getAllSites() // will convert to direct query
        // Let's query sync
        val siteEntities = mutableListOf<SiteEntity>()
        val cursor = database.openHelper.readableDatabase.query("SELECT * FROM sites")
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val loc = cursor.getString(cursor.getColumnIndexOrThrow("location"))
            val client = cursor.getString(cursor.getColumnIndexOrThrow("clientName"))
            val notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"))
            val created = cursor.getLong(cursor.getColumnIndexOrThrow("createdAt"))
            val zoom = cursor.getInt(cursor.getColumnIndexOrThrow("zoomPercent"))
            val rowH = cursor.getString(cursor.getColumnIndexOrThrow("rowHeightLevel"))
            val incSalary = cursor.getInt(cursor.getColumnIndexOrThrow("includeSalaryInExpenses")) == 1
            siteEntities.add(
                SiteEntity(
                    id = id,
                    name = name,
                    location = loc,
                    clientName = client,
                    notes = notes,
                    createdAt = created,
                    zoomPercent = zoom,
                    rowHeightLevel = rowH,
                    includeSalaryInExpenses = incSalary
                )
            )
        }
        cursor.close()

        for (site in siteEntities) {
            val siteObj = JSONObject()
            siteObj.put("id", site.id)
            siteObj.put("name", site.name)
            siteObj.put("location", site.location)
            siteObj.put("clientName", site.clientName)
            siteObj.put("notes", site.notes)
            siteObj.put("createdAt", site.createdAt)
            siteObj.put("zoomPercent", site.zoomPercent)
            siteObj.put("rowHeightLevel", site.rowHeightLevel)
            siteObj.put("includeSalaryInExpenses", site.includeSalaryInExpenses)

            val cols = columnDao.getColumnsForSiteSync(site.id)
            val colsArray = JSONArray()
            for (col in cols) {
                val colObj = JSONObject()
                colObj.put("key", col.key)
                colObj.put("titleEn", col.titleEn)
                colObj.put("titleBn", col.titleBn)
                colObj.put("type", col.type)
                colObj.put("orderIndex", col.orderIndex)
                colObj.put("widthDp", col.widthDp)
                colObj.put("calculationRole", col.calculationRole)
                colObj.put("optionsJson", col.optionsJson)
                colsArray.put(colObj)
            }
            siteObj.put("columns", colsArray)

            val rows = rowDao.getRowsForSiteSync(site.id)
            val rowsArray = JSONArray()
            for (row in rows) {
                val rowObj = JSONObject()
                rowObj.put("rowIndex", row.rowIndex)
                rowObj.put("cellsJson", row.cellsJson)
                rowsArray.put(rowObj)
            }
            siteObj.put("rows", rowsArray)

            sitesArray.put(siteObj)
        }

        root.put("sites", sitesArray)
        root.toString(2)
    }

    // Restore from JSON backup
    suspend fun importFromJson(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            val sitesArray = root.optJSONArray("sites") ?: return@withContext Result.failure(Exception("Invalid backup JSON format"))
            var importedCount = 0

            for (i in 0 until sitesArray.length()) {
                val siteObj = sitesArray.getJSONObject(i)
                val newSite = SiteEntity(
                    name = siteObj.getString("name"),
                    location = siteObj.optString("location", ""),
                    clientName = siteObj.optString("clientName", ""),
                    notes = siteObj.optString("notes", ""),
                    zoomPercent = siteObj.optInt("zoomPercent", 100),
                    rowHeightLevel = siteObj.optString("rowHeightLevel", "MEDIUM"),
                    includeSalaryInExpenses = siteObj.optBoolean("includeSalaryInExpenses", true)
                )
                val newSiteId = siteDao.insertSite(newSite)

                val colsArray = siteObj.optJSONArray("columns")
                if (colsArray != null && colsArray.length() > 0) {
                    val colsList = mutableListOf<SiteColumnEntity>()
                    for (j in 0 until colsArray.length()) {
                        val colObj = colsArray.getJSONObject(j)
                        colsList.add(
                            SiteColumnEntity(
                                siteId = newSiteId,
                                key = colObj.getString("key"),
                                titleEn = colObj.getString("titleEn"),
                                titleBn = colObj.optString("titleBn", colObj.getString("titleEn")),
                                type = colObj.optString("type", ColumnType.TEXT.name),
                                orderIndex = colObj.optInt("orderIndex", j),
                                widthDp = colObj.optInt("widthDp", 120),
                                calculationRole = colObj.optString("calculationRole", CalculationRole.NONE.name),
                                optionsJson = colObj.optString("optionsJson", "")
                            )
                        )
                    }
                    columnDao.insertColumns(colsList)
                } else {
                    createDefaultColumnsForSite(newSiteId)
                }

                val rowsArray = siteObj.optJSONArray("rows")
                if (rowsArray != null && rowsArray.length() > 0) {
                    val rowsList = mutableListOf<SiteRowEntity>()
                    for (k in 0 until rowsArray.length()) {
                        val rowObj = rowsArray.getJSONObject(k)
                        rowsList.add(
                            SiteRowEntity(
                                siteId = newSiteId,
                                rowIndex = rowObj.optInt("rowIndex", k),
                                cellsJson = rowObj.getString("cellsJson")
                            )
                        )
                    }
                    rowDao.insertRows(rowsList)
                }
                importedCount++
            }
            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Demo / Realistic sample data seeding for electrical contractor
    suspend fun seedSampleSitesIfEmpty() = withContext(Dispatchers.IO) {
        val cursor = database.openHelper.readableDatabase.query("SELECT COUNT(*) FROM sites")
        var count = 0
        if (cursor.moveToNext()) {
            count = cursor.getInt(0)
        }
        cursor.close()

        if (count == 0) {
            seedSampleData()
        }
    }

    suspend fun seedSampleData() = withContext(Dispatchers.IO) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = sdf.format(Date())
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val yesterday = sdf.format(cal.time)
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val dayBefore = sdf.format(cal.time)

        // Site 1: Dhanmondi Commercial Tower
        val site1Id = createSite(
            name = "Dhanmondi Commercial Tower (ধানমন্ডি কমার্শিয়াল টাওয়ার)",
            location = "Road 27, Dhanmondi, Dhaka",
            clientName = "Engr. Monirul Islam (Green Real Estate)",
            notes = "14-Story Building Main Electrical Substation & Cable Riser Setup"
        )

        val site1Rows = listOf(
            mapOf(
                "col_date" to dayBefore,
                "col_details" to "Advance Received from Client (সাইট অগ্রিম)",
                "col_category" to "Client Payment",
                "col_quantity" to "1st Stage Token Advance",
                "col_salary" to "",
                "col_income" to "150000",
                "col_expense" to "",
                "col_received" to "150000",
                "col_note" to "Bank transfer by owner"
            ),
            mapOf(
                "col_date" to dayBefore,
                "col_details" to "Main Switchgear & MCCB Breaker Purchase (মেইন সুইচগিয়ার)",
                "col_category" to "Material",
                "col_quantity" to "400A MCCB 4-Pole Schneider",
                "col_salary" to "",
                "col_income" to "",
                "col_expense" to "68500",
                "col_received" to "",
                "col_note" to "Purchased from Stadium Market"
            ),
            mapOf(
                "col_date" to yesterday,
                "col_details" to "Technician Kabir (কেব্‌ল লেয়িং টেকনিশিয়ান)",
                "col_category" to "Technician",
                "col_quantity" to "1 Full Day Work",
                "col_salary" to "1200",
                "col_income" to "",
                "col_expense" to "1200",
                "col_received" to "",
                "col_note" to "Riser cable laying complete"
            ),
            mapOf(
                "col_date" to yesterday,
                "col_details" to "Labor Helal & Rasel (শ্রমিক হেলাল ও রাসেল)",
                "col_category" to "Labor",
                "col_quantity" to "2 Persons",
                "col_salary" to "1600",
                "col_income" to "",
                "col_expense" to "1600",
                "col_received" to "",
                "col_note" to "800 Tk per person daily salary"
            ),
            mapOf(
                "col_date" to yesterday,
                "col_details" to "PVC Conduit Pipe & Cable Ties (কনডুইট পাইপ)",
                "col_category" to "Material",
                "col_quantity" to "30 Pcs 1-inch pipe & 5 pkt tie",
                "col_salary" to "",
                "col_income" to "",
                "col_expense" to "4200",
                "col_received" to "",
                "col_note" to "Local hardware shop"
            ),
            mapOf(
                "col_date" to today,
                "col_details" to "Substation Earthing Contractor Subrata (আর্থিং ঠিকাদার)",
                "col_category" to "Trader",
                "col_quantity" to "Chemical Earth Boring 100ft",
                "col_salary" to "8500",
                "col_income" to "",
                "col_expense" to "8500",
                "col_received" to "",
                "col_note" to "Chemical earthing with copper plate"
            ),
            mapOf(
                "col_date" to today,
                "col_details" to "Emergency Site Running Cash (সাইট থেকে নগদ গ্রহণ)",
                "col_category" to "Site Advance",
                "col_quantity" to "Petty Cash",
                "col_salary" to "",
                "col_income" to "20000",
                "col_expense" to "",
                "col_received" to "20000",
                "col_note" to "Handed over by site engineer"
            ),
            mapOf(
                "col_date" to today,
                "col_details" to "Site Worker Lunch & Transport (দুপুরের খাবার ও যাতায়াত)",
                "col_category" to "Food & Transport",
                "col_quantity" to "4 workers lunch + van fare",
                "col_salary" to "",
                "col_income" to "",
                "col_expense" to "1150",
                "col_received" to "",
                "col_note" to "Daily site food expense"
            )
        )

        for (rowMap in site1Rows) {
            addRow(site1Id, rowMap)
        }

        // Site 2: Gulshan Residence Substation Project
        val site2Id = createSite(
            name = "Gulshan Duplex Residence (গুলশান ডুপ্লেক্স হাউজ)",
            location = "Gulshan-2, Dhaka",
            clientName = "Dr. Tariq Ahmed",
            notes = "Solar Inverter, DB Dressing and Concealed Light Wiring"
        )

        val site2Rows = listOf(
            mapOf(
                "col_date" to yesterday,
                "col_details" to "Project Initial Advance (প্রজেক্ট অগ্রিম গ্রহণ)",
                "col_category" to "Client Payment",
                "col_quantity" to "Cheque Clearance",
                "col_salary" to "",
                "col_income" to "80000",
                "col_expense" to "",
                "col_received" to "80000",
                "col_note" to "50% upfront payment"
            ),
            mapOf(
                "col_date" to yesterday,
                "col_details" to "BRB Multi-strand Flame Retardant Wire (বিআরবি ক্যাবল)",
                "col_category" to "Material",
                "col_quantity" to "3 Coils 2.5 RM, 2 Coils 4.0 RM",
                "col_salary" to "",
                "col_income" to "",
                "col_expense" to "26400",
                "col_received" to "",
                "col_note" to "Original BRB red, black, yellow"
            ),
            mapOf(
                "col_date" to today,
                "col_details" to "Technician Selim (সুইচ বোর্ড ওয়্যারিং)",
                "col_category" to "Technician",
                "col_quantity" to "1 Day Work",
                "col_salary" to "1100",
                "col_income" to "",
                "col_expense" to "1100",
                "col_received" to "",
                "col_note" to "Master bedroom gang switches dressed"
            )
        )

        for (rowMap in site2Rows) {
            addRow(site2Id, rowMap)
        }
    }
}
