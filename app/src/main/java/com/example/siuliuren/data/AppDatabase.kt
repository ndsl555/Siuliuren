package com.example.siuliuren.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.siuliuren.dao.DivinationDao
import com.example.siuliuren.entity.DivinationRecord

@Database(entities = [DivinationRecord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun divinationDao(): DivinationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "siuliuren_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
