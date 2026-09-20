package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.SkinDao
import com.example.data.local.dao.SleepDao
import com.example.data.local.entity.SkinAnalysisEntity
import com.example.data.local.entity.SleepClassifyEntity
import com.example.data.local.entity.SleepSessionEntity

/**
 * SomnaGlow アプリケーションのローカルデータベース
 * オフラインファースト設計に基づく Room データベース
 */
@Database(
    entities = [
        SleepSessionEntity::class,
        SleepClassifyEntity::class,
        SkinAnalysisEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SomnaGlowDatabase : RoomDatabase() {

    abstract fun sleepDao(): SleepDao
    abstract fun skinDao(): SkinDao

    companion object {
        @Volatile
        private var INSTANCE: SomnaGlowDatabase? = null

        fun getInstance(context: Context): SomnaGlowDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SomnaGlowDatabase::class.java,
                    "somnaglow_health.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
