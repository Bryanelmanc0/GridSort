package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.GameHistory
import com.example.data.model.LeaderboardEntry
import com.example.data.model.UserProfile
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {
    val viewModel: GameViewModel = viewModel()
    val activeTab = viewModel.activeTab
    
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val profiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    
    // Dialog control for profile creation
    var showCreateProfileDialog by remember { mutableStateOf(false) }
    var showSwitchProfileDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_root_scaffold"),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GridSort Challenge",
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    // Quick profile switch button
                    activeProfile?.let { profile ->
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(android.graphics.Color.parseColor(profile.avatarColorHex)).copy(alpha = 0.2f))
                                .border(1.dp, Color(android.graphics.Color.parseColor(profile.avatarColorHex)), RoundedCornerShape(8.dp))
                                .clickable { showSwitchProfileDialog = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(profile.avatarColorHex)))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = profile.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 80.dp)
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            // Respect notched screen safe areas
            if (viewModel.gamePlayState != GamePlayState.IN_PROGRESS) {
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = activeTab == AppTab.PLAY,
                        onClick = { viewModel.switchTab(AppTab.PLAY) },
                        icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Play") },
                        label = { Text("Jugar") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("nav_item_play")
                    )
                    NavigationBarItem(
                        selected = activeTab == AppTab.LEADERBOARD,
                        onClick = { viewModel.switchTab(AppTab.LEADERBOARD) },
                        icon = { Icon(Icons.Default.Star, contentDescription = "Clasificación") },
                        label = { Text("Tablas") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("nav_item_leaderboard")
                    )
                    NavigationBarItem(
                        selected = activeTab == AppTab.PROFILE,
                        onClick = { viewModel.switchTab(AppTab.PROFILE) },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Mi Perfil") },
                        label = { Text("Mi Perfil") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("nav_item_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            Color(0xFF07090F)
                        )
                    )
                )
        ) {
            when (activeTab) {
                AppTab.PLAY -> PlayTabScreen(viewModel, onCreateProfileClick = { showCreateProfileDialog = true })
                AppTab.LEADERBOARD -> LeaderboardTabScreen(viewModel)
                AppTab.PROFILE -> ProfileTabScreen(
                    viewModel,
                    onCreateProfileClick = { showCreateProfileDialog = true },
                    onSwitchProfileClick = { showSwitchProfileDialog = true }
                )
            }
        }
    }

    // Modal dialog for Switching Profile
    if (showSwitchProfileDialog) {
        SwitchProfileDialog(
            profiles = profiles,
            activeProfile = activeProfile,
            onSelect = {
                viewModel.selectProfile(it)
                showSwitchProfileDialog = false
            },
            onDelete = {
                viewModel.removeProfile(it)
            },
            onDismiss = { showSwitchProfileDialog = false }
        )
    }

    // Modal dialog for Creating Profile
    if (showCreateProfileDialog) {
        CreateProfileDialog(
            onConfirm = { name, color ->
                viewModel.createProfile(name, color)
                showCreateProfileDialog = false
            },
            onDismiss = { showCreateProfileDialog = false }
        )
    }
}

// ==================== SCREEN: PLAY TAB ====================
@Composable
fun PlayTabScreen(viewModel: GameViewModel, onCreateProfileClick: () -> Unit) {
    AnimatedContent(
        targetState = viewModel.gamePlayState,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "gameStateAnim"
    ) { status ->
        when (status) {
            GamePlayState.NOT_STARTED -> GameConfigDashboard(viewModel, onCreateProfileClick)
            GamePlayState.IN_PROGRESS -> GameBoardPlayingScreen(viewModel)
            GamePlayState.COMPLETED -> GameVictoryCelebrationScreen(viewModel)
        }
    }
}

@Composable
fun GameConfigDashboard(viewModel: GameViewModel, onCreateProfileClick: () -> Unit) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Welcoming card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (activeProfile != null) {
                    Text(
                        text = "¡Hola, ${activeProfile?.name}!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Selecciona tu modo y tamaño de cuadrícula para comenzar.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "Bienvenido a GridSort",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onCreateProfileClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier.testTag("create_profile_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Crear Perfil para Guardar Progreso")
                    }
                }
            }
        }

        // Section: Game Mode Selector
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "MODO DE JUEGO",
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Large Horizontal scroll for modes
        val modes = GameMode.values()
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(modes) { mode ->
                val isSelected = viewModel.selectedGameMode == mode
                Card(
                    modifier = Modifier
                        .width(180.dp)
                        .height(110.dp)
                        .clickable { viewModel.selectedGameMode = mode },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = mode.displayName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = mode.description,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            lineHeight = 14.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Section: Grid Size Selectors
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "TAMAÑO DE LA CUADRÍCULA: ${viewModel.selectedGridSize}x${viewModel.selectedGridSize} (${viewModel.selectedGridSize * viewModel.selectedGridSize} números)",
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Flow grids selection chips from 3x3 to 12x12
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (size in 3..12) {
                val isSelected = viewModel.selectedGridSize == size
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectedGridSize = size },
                    label = { Text("${size}x${size}") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                        borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                    )
                )
            }
        }

        if (viewModel.selectedGridSize > 5) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Las cuadrículas mayores a 5x5 son interactivas y se pueden desplazar arrastrando de un lado a otro para facilitar el juego.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Large Play button
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.startGame() },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(60.dp)
                .testTag("start_game_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "COMENZAR DESAFÍO",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ==================== SCREEN: GAME BOARD ACTIVE ====================
@Composable
fun GameBoardPlayingScreen(viewModel: GameViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display header stat dashboard
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live score
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "PUNTUACIÓN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = "${viewModel.currentScore}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = if (viewModel.currentScore >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("current_score")
                )
            }

            // Game Timer
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "TIEMPO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = formatDuration(viewModel.durationSeconds),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Error count
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "ERRORES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = "${viewModel.errorsCount}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = if (viewModel.errorsCount == 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                )
            }
        }

        // Linear Progress bar completion
        val totalCellsCount = viewModel.selectedGridSize * viewModel.selectedGridSize
        val completedRatio = viewModel.targetValueIndex.toFloat() / totalCellsCount.toFloat()
        
        LinearProgressIndicator(
            progress = { completedRatio },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        // Navigation info showing expected setup
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Patrón: ${viewModel.selectedGameMode.displayName}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Completados: ${viewModel.targetValueIndex} de $totalCellsCount",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Show what value is next
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "SIGUIENTE: ${if (viewModel.targetValueIndex < totalCellsCount) viewModel.handleGetNextExpectedValue() else "¡Listo!"}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Render adaptive Grid
        if (viewModel.selectedGridSize <= 5) {
            // Adaptive statically placed grid inside viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                val cellPadding = 4.dp
                val gridSize = viewModel.selectedGridSize
                
                BoxWithConstraints(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                    val cellSize = maxWidth / gridSize
                    
                    Column {
                        for (r in 0 until gridSize) {
                            Row {
                                for (c in 0 until gridSize) {
                                    val cellIndex = r * gridSize + c
                                    if (cellIndex < viewModel.gridCells.size) {
                                        Box(
                                            modifier = Modifier
                                                .size(cellSize)
                                                .padding(cellPadding)
                                        ) {
                                            val cell = viewModel.gridCells[cellIndex]
                                            GridCellComp(
                                                cell = cell,
                                                onCellClick = { viewModel.handleCellClick(cellIndex) },
                                                modifier = Modifier.testTag("cell_${cellIndex}")
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Panning scrollable grid view for larger frames (up to 12)
            val scrollStateV = rememberScrollState()
            val scrollStateH = rememberScrollState()
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .background(Color(0xFF0C0E17)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollStateV)
                        .horizontalScroll(scrollStateH)
                        .padding(16.dp)
                ) {
                    val gridSize = viewModel.selectedGridSize
                    
                    for (r in 0 until gridSize) {
                        Row {
                            for (c in 0 until gridSize) {
                                val cellIndex = r * gridSize + c
                                if (cellIndex < viewModel.gridCells.size) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .padding(3.dp)
                                    ) {
                                        val cell = viewModel.gridCells[cellIndex]
                                        GridCellComp(
                                            cell = cell,
                                            onCellClick = { viewModel.handleCellClick(cellIndex) },
                                            modifier = Modifier.testTag("cell_scroll_${cellIndex}")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Back / Abandon button
        OutlinedButton(
            onClick = { viewModel.abandonGame() },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(48.dp)
                .testTag("abandon_game_button"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Abandonar Partida")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}

// Function helper to fetch the exact next value in sequence silently
fun GameViewModel.handleGetNextExpectedValue(): Int {
    val count = selectedGridSize * selectedGridSize
    if (this.targetValueIndex in 0 until count) {
        val numbers = when (selectedGameMode) {
            GameMode.CRECIENTES, GameMode.PRIMOS_CRECIENTES, GameMode.PARES_CRECIENTES, GameMode.IMPARES_CRECIENTES -> {
                gridCells.map { it.value }.sorted()
            }
            GameMode.DECRECIENTES, GameMode.PRIMOS_DECRECIENTES, GameMode.PARES_DECRECIENTES, GameMode.IMPARES_DECRECIENTES -> {
                gridCells.map { it.value }.sortedDescending()
            }
        }
        if (targetValueIndex < numbers.size) {
            return numbers[targetValueIndex]
        }
    }
    return 0
}

@Composable
fun GridCellComp(
    cell: GridCell,
    onCellClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        cell.isCorrect -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
        cell.isError -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
    }

    val borderColor = when {
        cell.isCorrect -> MaterialTheme.colorScheme.tertiary
        cell.isError -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    }

    val textColor = when {
        cell.isCorrect -> MaterialTheme.colorScheme.tertiary
        cell.isError -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = if (cell.isCorrect || cell.isError) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = !cell.isCorrect) { onCellClick() },
        contentAlignment = Alignment.Center
    ) {
        if (cell.isCorrect) {
            // Display checkmark + mini number
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${cell.value}",
                    fontSize = 11.sp,
                    color = textColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            Text(
                text = "${cell.value}",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// ==================== SCREEN: VICTORY COMPLETED ====================
@Composable
fun GameVictoryCelebrationScreen(viewModel: GameViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Victory Trophy",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(100.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "¡CUADRÍCULA COMPLETADA!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Has ordenado correctamente todos los números según la regla: ${viewModel.selectedGameMode.displayName} (${viewModel.selectedGridSize}x${viewModel.selectedGridSize})",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Stats summary cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Score
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("PUNTOS EXTRA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${viewModel.currentScore}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Duration
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("TIEMPO TOTAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatDuration(viewModel.durationSeconds),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Errors
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("FALLOS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${viewModel.errorsCount}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (viewModel.errorsCount == 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.abandonGame() /* returns to lobby */ },
            modifier = Modifier
                .fillMaxWidth(0.81f)
                .height(55.dp)
                .testTag("victory_continue_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("VOLVER AL MENÚ", fontSize = 15.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Navigate to Leaderboards right now to stack up scores
        TextButton(
            onClick = {
                viewModel.abandonGame()
                viewModel.switchTab(AppTab.LEADERBOARD)
            },
            modifier = Modifier.testTag("victory_go_leaderboard")
        ) {
            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Ver Tabla de Clasificación Global", textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
        }
    }
}


// ==================== SCREEN: LEADERBOARD TAB ====================
@Composable
fun LeaderboardTabScreen(viewModel: GameViewModel) {
    val scores by viewModel.leaderboardList.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Quick Title
        Text(
            text = "COMPETICIÓN GLOBAL",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Text(
            text = "Compara tus mejores puntuaciones con la comunidad mundial.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Row filters
        // Filter elements for Selecting Game Mode
        Text(
            text = "FILTRAR POR REGLA:",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (m in GameMode.values()) {
                val isSelected = viewModel.lbSelectedMode == m
                KeyFiltersChip(
                    text = m.displayName,
                    isSelected = isSelected,
                    onClick = { viewModel.updateLeaderboardFilter(m, viewModel.lbSelectedGridSize) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Size Filter
        Text(
            text = "FILTRAR POR CUADRÍCULA:",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (size in 3..12) {
                val isSelected = viewModel.lbSelectedGridSize == size
                KeyFiltersChip(
                    text = "${size}x${size}",
                    isSelected = isSelected,
                    onClick = { viewModel.updateLeaderboardFilter(viewModel.lbSelectedMode, size) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // High scores list table frame
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("leaderboard_table_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            if (scores.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Sin récords registrados",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "¡Completa una partida en esta configuración para ser el primero!",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(scores) { idx, scoreEntry ->
                        val rankNum = idx + 1
                        LeaderboardRowItem(rankNum, scoreEntry)
                    }
                }
            }
        }
    }
}

@Composable
fun KeyFiltersChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else Color.White.copy(alpha = 0.03f)
            )
            .border(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun LeaderboardRowItem(rank: Int, entry: LeaderboardEntry) {
    val highlightBg = if (entry.isLocal) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    } else {
        Color.Transparent
    }

    val highlightBorder = if (entry.isLocal) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
    } else {
        BorderStroke(0.dp, Color.Transparent)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (entry.isLocal) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent),
        border = if (entry.isLocal) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank counter bubble
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        when (rank) {
                            1 -> Color(0xFFFFD700) // Gold
                            2 -> Color(0xFFC0C0C0) // Silver
                            3 -> Color(0xFFCD7F32) // Bronze
                            else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = if (rank in 1..3) Color.Black else MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // User profile avatar indicator
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(entry.avatarColorHex)))
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Player name
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (entry.isLocal) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("TÚ", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(entry.timestamp)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            // Score readout
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${entry.score} pts",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${entry.modeName} (${entry.gridSize}x${entry.gridSize})",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}


// ==================== SCREEN: MY PROFILE TAB ====================
@Composable
fun ProfileTabScreen(
    viewModel: GameViewModel,
    onCreateProfileClick: () -> Unit,
    onSwitchProfileClick: () -> Unit
) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val rawHistories by viewModel.activeProfileHistory.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Active Profile dashboard panel
        Card(
            modifier = Modifier.fillMaxWidth().testTag("profile_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (activeProfile != null) {
                    val p = activeProfile!!
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Color Circle
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(p.avatarColorHex)))
                                .border(2.dp, Color.White, CircleShape)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = p.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Miembro desde: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(p.createdAt))}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        // Switch Profile Button
                        IconButton(onClick = onSwitchProfileClick) {
                            Icon(Icons.Default.Refresh, contentDescription = "Cambiar Cuenta", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Aggregate Stats display grid
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Matches played
                        ProfileInlineStatCell("Partidas", "${rawHistories.size}", MaterialTheme.colorScheme.onSurface)
                        
                        // Highest score
                        val maxScore = if (rawHistories.isNotEmpty()) rawHistories.maxOf { it.score } else 0
                        ProfileInlineStatCell("Récord Pts", "$maxScore", MaterialTheme.colorScheme.primary)

                        // Avg errors count
                        val avgErrors = if (rawHistories.isNotEmpty()) String.format(Locale.getDefault(), "%.1f", rawHistories.map { it.errorCount }.average()) else "0"
                        ProfileInlineStatCell("Err Promedio", avgErrors, MaterialTheme.colorScheme.error)
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No se ha seleccionado ningún perfil.", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onCreateProfileClick) {
                            Text("Crear Perfil Ahora")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // History Game logs header
        Text(
            text = "HISTORIAL DE PARTIDAS",
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Game History lists inside card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("history_list_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        ) {
            if (rawHistories.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(42.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Historial vacío",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Text(
                            text = "Tus partidas completadas se guardarán aquí de forma histórica.",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(rawHistories) { gameLog ->
                        HistoryRowItem(gameLog)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileInlineStatCell(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            color = valueColor
        )
    }
}

@Composable
fun HistoryRowItem(history: GameHistory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${history.modeName} (${history.gridSize}x${history.gridSize})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Tiempo: ${formatDuration(history.durationSeconds)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Errores: ${history.errorCount}",
                        fontSize = 11.sp,
                        color = if (history.errorCount == 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(history.timestamp)),
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            Text(
                text = "${if (history.score >= 0) "+" else ""}${history.score} pts",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = if (history.score >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}


// ==================== DIALOG: PROFILE CREATE ====================
@Composable
fun CreateProfileDialog(onConfirm: (String, String) -> Unit, onDismiss: () -> Unit) {
    var profileName by remember { mutableStateOf("") }
    
    // Available neon avatar colors
    val colorsList = listOf(
        "#2196F3", // Blue
        "#E91E63", // Pink
        "#4CAF50", // Green
        "#FF9800", // Orange
        "#9C27B0", // Purple
        "#00E5FF", // Neon Cyan
        "#E040FB", // Bright Orchid
        "#FFD700"  // Gold
    )
    var selectedColorHex by remember { mutableStateOf(colorsList[0]) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NUEVO PERFIL",
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it },
                    label = { Text("Nombre del Jugador") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("profile_name_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Selecciona color de Avatar:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                // Color picker elements list
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (colorHex in colorsList) {
                        val isSelected = selectedColorHex == colorHex
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(colorHex)))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = colorHex }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCELAR", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Button(
                        onClick = {
                            if (profileName.trim().isNotEmpty()) {
                                onConfirm(profileName, selectedColorHex)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("profile_confirm_btn")
                    ) {
                        Text("CREAR")
                    }
                }
            }
        }
    }
}


// ==================== DIALOG: PROFILE SWITCH ====================
@Composable
fun SwitchProfileDialog(
    profiles: List<UserProfile>,
    activeProfile: UserProfile?,
    onSelect: (Long) -> Unit,
    onDelete: (UserProfile) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "CAMBIAR DE PERFIL",
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.weight(1f, fill = false)) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(profiles) { p ->
                            val isActive = p.id == activeProfile?.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        else Color.White.copy(alpha = 0.02f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isActive) MaterialTheme.colorScheme.primary
                                        else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSelect(p.id) }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(android.graphics.Color.parseColor(p.avatarColorHex)))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = p.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (!isActive) {
                                    IconButton(
                                        onClick = { onDelete(p) },
                                        modifier = Modifier.size(32.dp).testTag("delete_profile_${p.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar de la lista",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("ACTIVO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("CERRAR", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        }
    }
}


// ==================== HELPER CONVERT FUNCTIONS ====================
// Timer format helper list
fun formatDuration(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", m, s)
}
