package com.example.siuliuren.repository

import com.example.siuliuren.dao.DivinationDao
import com.example.siuliuren.entity.DivinationRecord
import kotlinx.coroutines.flow.Flow

class DivinationRepository(private val divinationDao: DivinationDao) {

    fun getRecordsByDate(date: String): Flow<List<DivinationRecord>> {
        return divinationDao.getRecordsByDate(date)
    }

    suspend fun insert(record: DivinationRecord) {
        divinationDao.insert(record)
    }

    suspend fun delete(record: DivinationRecord) {
        divinationDao.delete(record)
    }
}
