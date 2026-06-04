package com.example.siuliuren.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.siuliuren.entity.DivinationRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface DivinationDao {
    @Query("SELECT * FROM divination_records WHERE date = :date ORDER BY timestamp DESC")
    fun getRecordsByDate(date: String): Flow<List<DivinationRecord>>

    @Insert
    suspend fun insert(record: DivinationRecord)

    @Delete
    suspend fun delete(record: DivinationRecord)
}
