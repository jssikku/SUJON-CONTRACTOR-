package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SiteColumnEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ColumnDao {
    @Query("SELECT * FROM site_columns WHERE siteId = :siteId ORDER BY orderIndex ASC")
    fun getColumnsForSite(siteId: Long): Flow<List<SiteColumnEntity>>

    @Query("SELECT * FROM site_columns WHERE siteId = :siteId ORDER BY orderIndex ASC")
    suspend fun getColumnsForSiteSync(siteId: Long): List<SiteColumnEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColumn(column: SiteColumnEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColumns(columns: List<SiteColumnEntity>)

    @Update
    suspend fun updateColumn(column: SiteColumnEntity)

    @Delete
    suspend fun deleteColumn(column: SiteColumnEntity)

    @Query("DELETE FROM site_columns WHERE id = :id")
    suspend fun deleteColumnById(id: Long)
}
