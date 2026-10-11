package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.CalculationRole
import com.example.data.model.ColumnType
import com.example.data.model.SiteEntity
import com.example.data.model.SiteRowEntity
import com.example.data.repository.AccountingRepository
import com.example.util.AppLanguage
import com.example.util.Localization
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: AccountingRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = AccountingRepository(db)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Sujon Contractor", appName)
    }

    @Test
    fun `create site and verify 9 default columns`() = runBlocking {
        val siteId = repository.createSite(
            name = "Test Site",
            location = "Dhaka",
            clientName = "Owner",
            notes = "Notes"
        )
        val columns = repository.getColumnsForSite(siteId).first()
        assertEquals(9, columns.size)
        assertEquals("col_date", columns[0].key)
        assertEquals("col_details", columns[1].key)
        assertEquals("col_category", columns[2].key)
        assertEquals("col_quantity", columns[3].key)
        assertEquals("col_salary", columns[4].key)
        assertEquals("col_income", columns[5].key)
        assertEquals("col_expense", columns[6].key)
        assertEquals("col_received", columns[7].key)
        assertEquals("col_note", columns[8].key)
    }

    @Test
    fun `accounting totals calculate accurately and avoid double-counting`() = runBlocking {
        val siteId = repository.createSite("Accounting Test Site")
        val site = db.siteDao().getSiteByIdSync(siteId)!!
        val columns = repository.getColumnsForSite(siteId).first()

        // Row 1: Income / Client Payment of 50,000
        repository.addRow(
            siteId,
            mapOf(
                "col_date" to "2026-10-10",
                "col_details" to "Advance Received",
                "col_category" to "Client Payment",
                "col_income" to "50000",
                "col_received" to "50000"
            )
        )

        // Row 2: Labor Daily Salary 1,600
        repository.addRow(
            siteId,
            mapOf(
                "col_date" to "2026-10-10",
                "col_details" to "2 Helpers",
                "col_category" to "Labor",
                "col_salary" to "1600"
            )
        )

        // Row 3: Technician Daily Salary 1,200
        repository.addRow(
            siteId,
            mapOf(
                "col_date" to "2026-10-10",
                "col_details" to "Technician",
                "col_category" to "Technician",
                "col_salary" to "1200"
            )
        )

        // Row 4: Material Expense 15,000
        repository.addRow(
            siteId,
            mapOf(
                "col_date" to "2026-10-11",
                "col_details" to "Cables",
                "col_category" to "Material",
                "col_expense" to "15000"
            )
        )

        val rows = repository.getRowsForSite(siteId).first()
        val totals = repository.calculateTotals(site, columns, rows)

        assertEquals(1600.0, totals.totalLaborSalary, 0.01)
        assertEquals(1200.0, totals.totalTechnicianSalary, 0.01)
        assertEquals(15000.0, totals.totalMaterialExpense, 0.01)
        assertEquals(50000.0, totals.totalIncome, 0.01)
        assertEquals(50000.0, totals.totalSiteReceived, 0.01)

        // Total expenses with includeSalaryInExpenses = true:
        // (1600 + 1200) + 15000 = 17800
        assertEquals(17800.0, totals.totalExpenses, 0.01)

        // Total received = 50000 + 50000 = 100000
        // Remaining balance = 100000 - 17800 = 82200
        assertEquals(82200.0, totals.remainingBalance, 0.01)

        // Working days count (unique dates with worker entries: "2026-10-10")
        assertEquals(1, totals.totalWorkingDays)
    }

    @Test
    fun `csv export includes UTF-8 BOM and headers`() = runBlocking {
        val siteId = repository.createSite("CSV Test")
        val columns = repository.getColumnsForSite(siteId).first()
        val rows = repository.getRowsForSite(siteId).first()

        val csv = repository.exportToCsv(columns, rows, useBengaliTitles = false)
        assertTrue(csv.startsWith("\uFEFF"))
        assertTrue(csv.contains("Date,Name / Details,Category"))
    }

    @Test
    fun `currency formatting supports English and Bengali numerals`() {
        val formattedEn = Localization.formatCurrency(12500.0, AppLanguage.EN)
        assertEquals("৳ 12,500.00", formattedEn)

        val formattedBn = Localization.formatCurrency(12500.0, AppLanguage.BN)
        assertEquals("৳ ১২,৫০০.০০", formattedBn)
    }
}
