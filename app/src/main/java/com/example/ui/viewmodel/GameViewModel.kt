package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.GameHistory
import com.example.data.model.LeaderboardEntry
import com.example.data.model.UserProfile
import com.example.data.repository.GameRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.sqrt

enum class AppTab {
    PLAY, LEADERBOARD, PROFILE
}

enum class GamePlayState {
    NOT_STARTED, IN_PROGRESS, COMPLETED
}

data class GridCell(
    val value: Int,
    val isCorrect: Boolean = false,
    val isError: Boolean = false
)

enum class GameMode(val displayName: String, val description: String) {
    CRECIENTES("Creciente", "Ordena todos los números de menor a mayor"),
    DECRECIENTES("Decreciente", "Ordena todos los números de mayor a menor"),
    PRIMOS_CRECIENTES("Primos Creciente", "Ordena solo números primos de menor a mayor"),
    PRIMOS_DECRECIENTES("Primos Decreciente", "Ordena solo números primos de mayor a menor"),
    PARES_CRECIENTES("Pares Creciente", "Ordena solo números pares de menor a mayor"),
    PARES_DECRECIENTES("Pares Decreciente", "Ordena solo números pares de mayor a menor"),
    IMPARES_CRECIENTES("Impares Creciente", "Ordena solo números impares de menor a mayor"),
    IMPARES_DECRECIENTES("Impares Decreciente", "Ordena solo números impares de mayor a menor")
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository

    // Tabs
    var activeTab by mutableStateOf(AppTab.PLAY)
        private set

    // Selected profile ID in UI
    private val _activeProfileId = MutableStateFlow<Long?>(null)
    val activeProfileId: StateFlow<Long?> = _activeProfileId.asStateFlow()

    // Flow for profiles
    val allProfiles: StateFlow<List<UserProfile>>

    // Flow for active profile
    val activeProfile: StateFlow<UserProfile?> = _activeProfileId
        .flatMapLatest { id ->
            if (id == null) flowOf<UserProfile?>(null)
            else flow {
                emit(repository.getProfileById(id))
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Flow for histories
    val activeProfileHistory: StateFlow<List<GameHistory>> = _activeProfileId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getHistoryForProfile(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Leaderboard filters
    var lbSelectedMode by mutableStateOf(GameMode.CRECIENTES)
        private set
    var lbSelectedGridSize by mutableStateOf(3)
        private set

    // Flow for Leaderboard
    private val _leaderboardFilter = MutableStateFlow(Pair(GameMode.CRECIENTES.displayName, 3))
    val leaderboardList: StateFlow<List<LeaderboardEntry>> = _leaderboardFilter
        .flatMapLatest { filter ->
            repository.getLeaderboardByFilter(filter.first, filter.second)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- GAME IN-PLAY STATES ---
    var gamePlayState by mutableStateOf(GamePlayState.NOT_STARTED)
        private set
    var selectedGridSize by mutableStateOf(3) // ranges from 3 to 12
    var selectedGameMode by mutableStateOf(GameMode.CRECIENTES)

    var gridCells by mutableStateOf<List<GridCell>>(emptyList())
        private set
    var currentScore by mutableStateOf(0)
        private set
    var durationSeconds by mutableStateOf(0)
        private set
    var errorsCount by mutableStateOf(0)
        private set
    var targetValueIndex by mutableStateOf(0) // Index we are looking for in the sorted target list
        private set

    private var targetSortedValuesList = listOf<Int>()
    private var timerJob: Job? = null

    init {
        val database = AppDatabase.getDatabase(application)
        repository = GameRepository(database.gameDao())

        allProfiles = repository.allProfiles
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

        // Ensure there is at least a default profile
        viewModelScope.launch {
            repository.checkAndSeedLeaderboard()
            
            // Wait for DB to load profiles
            val profiles = repository.allProfiles.first()
            if (profiles.isEmpty()) {
                val defaultId = repository.insertProfile(
                    UserProfile(name = "Jugador 1", avatarColorHex = "#2196F3")
                )
                _activeProfileId.value = defaultId
            } else {
                if (_activeProfileId.value == null) {
                    _activeProfileId.value = profiles.first().id
                }
            }
        }
    }

    fun switchTab(tab: AppTab) {
        activeTab = tab
    }

    fun selectProfile(id: Long) {
        _activeProfileId.value = id
    }

    fun createProfile(name: String, colorHex: String) {
        viewModelScope.launch {
            val trimmedName = name.trim()
            if (trimmedName.isNotEmpty()) {
                val newId = repository.insertProfile(
                    UserProfile(name = trimmedName, avatarColorHex = colorHex)
                )
                _activeProfileId.value = newId
            }
        }
    }

    fun removeProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
            // If deleted active, reset
            if (_activeProfileId.value == profile.id) {
                val rem = allProfiles.value.filter { it.id != profile.id }
                if (rem.isNotEmpty()) {
                    _activeProfileId.value = rem.first().id
                } else {
                    _activeProfileId.value = null
                    // Will recreate inside check triggered above if empty
                    val defaultId = repository.insertProfile(
                        UserProfile(name = "Jugador 1", avatarColorHex = "#2196F3")
                    )
                    _activeProfileId.value = defaultId
                }
            }
        }
    }

    fun updateLeaderboardFilter(mode: GameMode, gridSize: Int) {
        lbSelectedMode = mode
        lbSelectedGridSize = gridSize
        _leaderboardFilter.value = Pair(mode.displayName, gridSize)
    }

    // --- GAME ENGINE ---

    fun startGame() {
        // Cancel first if running
        timerJob?.cancel()

        // Initialize state
        currentScore = 0
        durationSeconds = 0
        errorsCount = 0
        targetValueIndex = 0
        gamePlayState = GamePlayState.IN_PROGRESS

        // Generate values
        val countNeeded = selectedGridSize * selectedGridSize
        val numbers = generateNumbers(selectedGameMode, countNeeded)
        
        // Define target sequence
        targetSortedValuesList = when (selectedGameMode) {
            GameMode.CRECIENTES, GameMode.PRIMOS_CRECIENTES, GameMode.PARES_CRECIENTES, GameMode.IMPARES_CRECIENTES -> {
                numbers.sorted()
            }
            GameMode.DECRECIENTES, GameMode.PRIMOS_DECRECIENTES, GameMode.PARES_DECRECIENTES, GameMode.IMPARES_DECRECIENTES -> {
                numbers.sortedDescending()
            }
        }

        // Shuffle cells for grid UI
        gridCells = numbers.shuffled().map { GridCell(value = it) }

        // Start Clock Tick Job
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                durationSeconds++
                // Penalize 10 points per elapsed second
                currentScore -= 10
            }
        }
    }

    fun abandonGame() {
        timerJob?.cancel()
        gamePlayState = GamePlayState.NOT_STARTED
    }

    fun handleCellClick(cellIndex: Int) {
        if (gamePlayState != GamePlayState.IN_PROGRESS) return

        val cell = gridCells[cellIndex]
        if (cell.isCorrect) return // already selected correct

        val expectedValue = targetSortedValuesList[targetValueIndex]

        if (cell.value == expectedValue) {
            // Correct click!
            currentScore += 1000
            
            // Mark cell correct
            gridCells = gridCells.toMutableList().apply {
                this[cellIndex] = cell.copy(isCorrect = true, isError = false)
            }
            
            // Go to next target
            targetValueIndex++

            if (targetValueIndex == targetSortedValuesList.size) {
                // VICTORY!
                completeGame()
            }
        } else {
            // Error!
            currentScore -= 500
            errorsCount++
            
            // Temporary error indicator
            viewModelScope.launch {
                gridCells = gridCells.toMutableList().apply {
                    this[cellIndex] = cell.copy(isError = true)
                }
                delay(400)
                // Remove error indicator if not selected correctly in the meantime
                gridCells = gridCells.toMutableList().apply {
                    if (this[cellIndex].isError) {
                        this[cellIndex] = this[cellIndex].copy(isError = false)
                    }
                }
            }
        }
    }

    private fun completeGame() {
        timerJob?.cancel()
        gamePlayState = GamePlayState.COMPLETED

        val profile = activeProfile.value ?: return
        val finalScore = currentScore

        viewModelScope.launch {
            // Record game history locally
            repository.insertHistory(
                GameHistory(
                    profileId = profile.id,
                    modeName = selectedGameMode.displayName,
                    gridSize = selectedGridSize,
                    score = finalScore,
                    durationSeconds = durationSeconds,
                    errorCount = errorsCount
                )
            )

            // Submit score to leaderboards
            repository.insertLeaderboardEntry(
                LeaderboardEntry(
                    name = profile.name,
                    avatarColorHex = profile.avatarColorHex,
                    score = finalScore,
                    modeName = selectedGameMode.displayName,
                    gridSize = selectedGridSize,
                    isLocal = true
                )
            )
            
            // Update leaderboard filter trigger to let game show high-scores directly
            updateLeaderboardFilter(selectedGameMode, selectedGridSize)
        }
    }

    // --- MATHEMATICAL VALUE GENERATION ---

    private fun generateNumbers(mode: GameMode, count: Int): List<Int> {
        val list = mutableListOf<Int>()
        when (mode) {
            GameMode.CRECIENTES, GameMode.DECRECIENTES -> {
                // Generate unique random integers. Start from a random offset for fun.
                val start = (1..150).random()
                val set = mutableSetOf<Int>()
                while (set.size < count) {
                    set.add(start + (0..(count * 4)).random())
                }
                list.addAll(set)
            }
            GameMode.PRIMOS_CRECIENTES, GameMode.PRIMOS_DECRECIENTES -> {
                // Generate first premium prime sequence
                var current = 2
                while (list.size < count) {
                    if (isPrime(current)) {
                        list.add(current)
                    }
                    current++
                }
            }
            GameMode.PARES_CRECIENTES, GameMode.PARES_DECRECIENTES -> {
                // Even numbers
                val start = (1..50).random() * 2 // even start
                for (i in 0 until count) {
                    list.add(start + (i * 2))
                }
            }
            GameMode.IMPARES_CRECIENTES, GameMode.IMPARES_DECRECIENTES -> {
                // Odd numbers
                val start = (0..50).random() * 2 + 1 // odd start
                for (i in 0 until count) {
                    list.add(start + (i * 2))
                }
            }
        }
        return list
    }

    private fun isPrime(n: Int): Boolean {
        if (n < 2) return false
        val limit = sqrt(n.toDouble()).toInt()
        for (i in 2..limit) {
            if (n % i == 0) return false
        }
        return true
    }
}
