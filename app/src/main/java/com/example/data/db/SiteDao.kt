package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SiteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SiteDao {
    @Query("SELECT * FROM sites ORDER BY id DESC")
    fun getAllSites(): Flow<List<SiteEntity>>

    @Query("SELECT * FROM sites WHERE id = :id")
    fun getSiteById(id: Long): Flow<SiteEntity?>

    @Query("SELECT * FROM sites WHERE id = :id")
    suspend fun getSiteByIdSync(id: Long): SiteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSite(site: SiteEntity): Long

    @Update
    suspend fun updateSite(site: SiteEntity)

    @Delete
    suspend fun deleteSite(site: SiteEntity)

    @Query("DELETE FROM sites WHERE id = :id")
    suspend fun deleteSiteById(id: Long)
}
