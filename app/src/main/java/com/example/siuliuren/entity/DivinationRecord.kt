package com.example.siuliuren.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "divination_records")
data class DivinationRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // yyyy-MM-dd
    val question: String,
    val result: String,
    val process: String,
    val timestamp: Long = System.currentTimeMillis()
)
