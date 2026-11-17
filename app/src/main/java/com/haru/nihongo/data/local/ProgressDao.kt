package com.haru.nihongo.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "progress")
data class ProgressEntity(
    @PrimaryKey val date: String,
    val completedCount: Int,
    val listened: Boolean,
    val recorded: Boolean,
)

@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress ORDER BY date DESC LIMIT 7")
    fun recent(): Flow<List<ProgressEntity>>

    @Query("SELECT * FROM progress WHERE date = :date")
    suspend fun on(date: String): ProgressEntity?

    @Upsert
    suspend fun upsert(value: ProgressEntity)
}

@Database(entities = [ProgressEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun progressDao(): ProgressDao
}
