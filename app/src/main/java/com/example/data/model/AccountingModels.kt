package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ColumnType(val title: String) {
    TEXT("Text"),
    DATE("Date"),
    NUMBER("Number"),
    MONEY("Money"),
    CATEGORY("Category")
}

enum class RowHeightLevel(val heightDp: Int) {
    SMALL(38),
    MEDIUM(50),
    LARGE(64)
}

enum class CalculationRole {
    NONE,
    LABOR_SALARY,
    TECHNICIAN_SALARY,
    TRADER_SALARY,
    MATERIAL_EXPENSE,
    OTHER_EXPENSE,
    INCOME,
    SITE_RECEIVED
}

@Entity(tableName = "sites")
data class SiteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val location: String = "",
    val clientName: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val zoomPercent: Int = 100,
    val rowHeightLevel: String = RowHeightLevel.MEDIUM.name,
    val includeSalaryInExpenses: Boolean = true
)

@Entity(
    tableName = "site_columns",
    foreignKeys = [
        ForeignKey(
            entity = SiteEntity::class,
            parentColumns = ["id"],
            childColumns = ["siteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("siteId"), Index(value = ["siteId", "key"], unique = true)]
)
data class SiteColumnEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val siteId: Long,
    val key: String,
    val titleEn: String,
    val titleBn: String,
    val type: String = ColumnType.TEXT.name,
    val orderIndex: Int = 0,
    val widthDp: Int = 120,
    val calculationRole: String = CalculationRole.NONE.name,
    val optionsJson: String = ""
)

@Entity(
    tableName = "site_rows",
    foreignKeys = [
        ForeignKey(
            entity = SiteEntity::class,
            parentColumns = ["id"],
            childColumns = ["siteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("siteId"), Index("siteId", "rowIndex")]
)
data class SiteRowEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val siteId: Long,
    val rowIndex: Int,
    val cellsJson: String, // JSON map of column key -> cell string value
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class AccountingTotals(
    val totalLaborSalary: Double = 0.0,
    val totalTechnicianSalary: Double = 0.0,
    val totalTraderSalary: Double = 0.0,
    val totalMaterialExpense: Double = 0.0,
    val totalOtherExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalSiteReceived: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val remainingBalance: Double = 0.0,
    val totalWorkingDays: Int = 0,
    val totalRecords: Int = 0
)

data class SiteWithDetails(
    val site: SiteEntity,
    val columns: List<SiteColumnEntity>,
    val rows: List<SiteRowEntity>
)
