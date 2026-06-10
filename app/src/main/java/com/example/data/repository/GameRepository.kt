package com.example.data.repository

import com.example.data.dao.GameDao
import com.example.data.model.GameHistory
import com.example.data.model.LeaderboardEntry
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class GameRepository(private val gameDao: GameDao) {

    val allProfiles: Flow<List<UserProfile>> = gameDao.getAllProfiles()

    fun getHistoryForProfile(profileId: Long): Flow<List<GameHistory>> {
        return gameDao.getHistoryForProfile(profileId)
    }

    val globalLeaderboard: Flow<List<LeaderboardEntry>> = gameDao.getGlobalLeaderboard()

    fun getLeaderboardByFilter(modeName: String, gridSize: Int): Flow<List<LeaderboardEntry>> {
        return gameDao.getLeaderboardByFilter(modeName, gridSize)
    }

    suspend fun getProfileById(id: Long): UserProfile? {
        return gameDao.getProfileById(id)
    }

    suspend fun insertProfile(profile: UserProfile): Long {
        return gameDao.insertProfile(profile)
    }

    suspend fun deleteProfile(profile: UserProfile) {
        gameDao.deleteProfile(profile)
    }

    suspend fun insertHistory(history: GameHistory): Long {
        return gameDao.insertHistory(history)
    }

    suspend fun clearHistoryForProfile(profileId: Long) {
        gameDao.clearHistoryForProfile(profileId)
    }

    suspend fun insertLeaderboardEntry(entry: LeaderboardEntry): Long {
        return gameDao.insertLeaderboardEntry(entry)
    }

    suspend fun checkAndSeedLeaderboard() {
        val current = gameDao.getGlobalLeaderboard().first()
        if (current.isEmpty()) {
            val defaultLeaders = listOf(
                LeaderboardEntry(name = "EinsteinS", avatarColorHex = "#FF3D00", score = 15000, modeName = "Primos Crecientes", gridSize = 3, isLocal = false),
                LeaderboardEntry(name = "EulerX", avatarColorHex = "#AA00FF", score = 12000, modeName = "Primos Decrecientes", gridSize = 3, isLocal = false),
                LeaderboardEntry(name = "EulerX", avatarColorHex = "#AA00FF", score = 38500, modeName = "Primos Decrecientes", gridSize = 4, isLocal = false),
                LeaderboardEntry(name = "SpeedRunner99", avatarColorHex = "#FF3D00", score = 8800, modeName = "Crecientes", gridSize = 3, isLocal = false),
                LeaderboardEntry(name = "SpeedRunner99", avatarColorHex = "#FF3D00", score = 17500, modeName = "Crecientes", gridSize = 4, isLocal = false),
                LeaderboardEntry(name = "SpeedRunner99", avatarColorHex = "#FF3D00", score = 39000, modeName = "Crecientes", gridSize = 5, isLocal = false),
                LeaderboardEntry(name = "MathWhiz", avatarColorHex = "#3D5AFE", score = 8500, modeName = "Crecientes", gridSize = 3, isLocal = false),
                LeaderboardEntry(name = "GridMaster", avatarColorHex = "#00E676", score = 18000, modeName = "Pares Crecientes", gridSize = 4, isLocal = false),
                LeaderboardEntry(name = "GridMaster", avatarColorHex = "#00E676", score = 34000, modeName = "Pares Decrecientes", gridSize = 5, isLocal = false),
                LeaderboardEntry(name = "AlphaSort", avatarColorHex = "#FFEB3B", score = 56000, modeName = "Impares Crecientes", gridSize = 6, isLocal = false),
                LeaderboardEntry(name = "ChronoSorted", avatarColorHex = "#AA00FF", score = 98000, modeName = "Crecientes", gridSize = 8, isLocal = false),
                LeaderboardEntry(name = "PrimeHacker", avatarColorHex = "#00B0FF", score = 52000, modeName = "Primos Crecientes", gridSize = 5, isLocal = false),
                LeaderboardEntry(name = "FastFingers", avatarColorHex = "#FF1744", score = 142000, modeName = "Decrecientes", gridSize = 10, isLocal = false),
                LeaderboardEntry(name = "QuantumMind", avatarColorHex = "#00E676", score = 210000, modeName = "Impares Decrecientes", gridSize = 11, isLocal = false),
                LeaderboardEntry(name = "SortGod", avatarColorHex = "#E040FB", score = 345000, modeName = "Crecientes", gridSize = 12, isLocal = false)
            )
            for (leader in defaultLeaders) {
                gameDao.insertLeaderboardEntry(leader)
            }
        }
    }
}
