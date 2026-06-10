package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val avatarColorHex: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_history")
data class GameHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val modeName: String,
    val gridSize: Int,
    val score: Int,
    val durationSeconds: Int,
    val errorCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "leaderboard")
data class LeaderboardEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val avatarColorHex: String,
    val score: Int,
    val modeName: String,
    val gridSize: Int,
    val isLocal: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
