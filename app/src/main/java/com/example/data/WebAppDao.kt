package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WebAppDao {
    @Query("SELECT * FROM web_app_profiles ORDER BY isBootDefault DESC, lastOpenedAt DESC")
    fun getAllProfiles(): Flow<List<WebAppProfile>>

    @Query("SELECT * FROM web_app_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Int): WebAppProfile?

    @Query("SELECT * FROM web_app_profiles WHERE isBootDefault = 1 LIMIT 1")
    suspend fun getBootDefaultProfile(): WebAppProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: WebAppProfile): Long

    @Update
    suspend fun updateProfile(profile: WebAppProfile)

    @Query("DELETE FROM web_app_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: Int)

    @Query("UPDATE web_app_profiles SET isBootDefault = 0")
    suspend fun clearAllBootDefaults()

    @Query("UPDATE web_app_profiles SET isBootDefault = CASE WHEN id = :id THEN 1 ELSE 0 END")
    suspend fun setBootDefaultProfile(id: Int)

    @Query("SELECT COUNT(*) FROM web_app_profiles")
    suspend fun getProfileCount(): Int
}
