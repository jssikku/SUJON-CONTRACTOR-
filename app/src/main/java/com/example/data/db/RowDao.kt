package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SiteRowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RowDao {
    @Query("SELECT * FROM site_rows WHERE siteId = :siteId ORDER BY rowIndex ASC")
    fun getRowsForSite(siteId: Long): Flow<List<SiteRowEntity>>

    @Query("SELECT * FROM site_rows WHERE siteId = :siteId ORDER BY rowIndex ASC")
    suspend fun getRowsForSiteSync(siteId: Long): List<SiteRowEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRow(row: SiteRowEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRows(rows: List<SiteRowEntity>)

    @Update
    suspend fun updateRow(row: SiteRowEntity)

    @Delete
    suspend fun deleteRow(row: SiteRowEntity)

    @Query("DELETE FROM site_rows WHERE id = :id")
    suspend fun deleteRowById(id: Long)

    @Query("DELETE FROM site_rows WHERE siteId = :siteId")
    suspend fun deleteAllRowsForSite(siteId: Long)
}
