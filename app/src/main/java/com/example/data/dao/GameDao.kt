package com.example.data.dao

import androidx.room.*
import com.example.data.model.GameHistory
import com.example.data.model.LeaderboardEntry
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    // --- User Profile ---
    @Query("SELECT * FROM user_profiles ORDER BY id DESC")
    fun getAllProfiles(): Flow<List<UserProfile>>

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile): Long

    @Delete
    suspend fun deleteProfile(profile: UserProfile)

    // --- Game History ---
    @Query("SELECT * FROM game_history WHERE profileId = :profileId ORDER BY timestamp DESC")
    fun getHistoryForProfile(profileId: Long): Flow<List<GameHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: GameHistory): Long

    @Query("DELETE FROM game_history WHERE profileId = :profileId")
    suspend fun clearHistoryForProfile(profileId: Long)

    // --- Leaderboard ---
    @Query("SELECT * FROM leaderboard ORDER BY score DESC LIMIT 100")
    fun getGlobalLeaderboard(): Flow<List<LeaderboardEntry>>

    @Query("SELECT * FROM leaderboard WHERE modeName = :modeName AND gridSize = :gridSize ORDER BY score DESC LIMIT 50")
    fun getLeaderboardByFilter(modeName: String, gridSize: Int): Flow<List<LeaderboardEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboardEntry(entry: LeaderboardEntry): Long

    @Query("DELETE FROM leaderboard WHERE isLocal = 0")
    suspend fun clearSeededLeaderboard()
}
