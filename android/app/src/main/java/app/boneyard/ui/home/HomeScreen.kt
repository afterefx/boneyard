package app.boneyard.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.boneyard.di.AppScope
import app.boneyard.domain.repository.AppTheme
import app.boneyard.ui.components.AvatarSize
import app.boneyard.ui.components.ConfirmDialog
import app.boneyard.ui.components.DominoTile
import app.boneyard.ui.components.EmptyState
import app.boneyard.ui.components.PlayerAvatar
import app.boneyard.ui.navigation.HomeScreen
import app.boneyard.ui.theme.BoneyardTheme
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// UI State
// ---------------------------------------------------------------------------

data class ActiveGameSummary(
    val gameId: Long,
    val playerNames: List<String>,
    val currentRound: Int,
    val totalRounds: Int = 14,
)

data class TemplateSummary(
    val templateId: Long,
    val name: String,
    val players: List<PlayerInfo>,
) {
    data class PlayerInfo(val name: String, val color: Color, val avatarIndex: Int)
}

data class HomeUiState(
    val activePausedGames: List<ActiveGameSummary> = emptyList(),
    val templates: List<TemplateSummary> = emptyList(),
    val isLoading: Boolean = false,
    val activeTheme: AppTheme = AppTheme.SYSTEM,
    val eventSink: (HomeEvent) -> Unit = {},
) : CircuitUiState

sealed interface HomeEvent : CircuitUiEvent {
    data object NewGame : HomeEvent
    data object Players : HomeEvent
    data object History : HomeEvent
    data class ResumeGame(val gameId: Long) : HomeEvent
    data class AbandonGame(val gameId: Long) : HomeEvent
    data class DeleteGame(val gameId: Long) : HomeEvent
    data class StartFromTemplate(val templateId: Long) : HomeEvent
    data class DeleteTemplate(val templateId: Long) : HomeEvent
    data class SetTheme(val theme: AppTheme) : HomeEvent
    data object ResetData : HomeEvent
}

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@CircuitInject(HomeScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeUi(state: HomeUiState, modifier: Modifier = Modifier) {
    var showSettings by remember { mutableStateOf(false) }
    var abandonGameConfirm by remember { mutableStateOf<ActiveGameSummary?>(null) }
    var deleteGameConfirm by remember { mutableStateOf<ActiveGameSummary?>(null) }
    var deleteTemplateConfirm by remember { mutableStateOf<TemplateSummary?>(null) }

    HomeScreenContent(
        uiState = state,
        onNewGame = { state.eventSink(HomeEvent.NewGame) },
        onPlayers = { state.eventSink(HomeEvent.Players) },
        onHistory = { state.eventSink(HomeEvent.History) },
        onResumeGame = { state.eventSink(HomeEvent.ResumeGame(it)) },
        onOpenSettings = { showSettings = true },
        onAbandonGame = { abandonGameConfirm = it },
        onDeleteGame = { deleteGameConfirm = it },
        onStartFromTemplate = { state.eventSink(HomeEvent.StartFromTemplate(it)) },
        onDeleteTemplate = { deleteTemplateConfirm = it },
    )

    if (showSettings) {
        SettingsBottomSheet(
            activeTheme = state.activeTheme,
            onThemeChange = { state.eventSink(HomeEvent.SetTheme(it)) },
            onResetAllData = { state.eventSink(HomeEvent.ResetData) },
            onDismiss = { showSettings = false }
        )
    }

    abandonGameConfirm?.let { game ->
        ConfirmDialog(
            title = "Abandon Game?",
            message = "This will move the game to history as incomplete. You won't be able to resume it.",
            confirmText = "Abandon",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                state.eventSink(HomeEvent.AbandonGame(game.gameId))
                abandonGameConfirm = null
            },
            onDismiss = { abandonGameConfirm = null }
        )
    }

    deleteGameConfirm?.let { game ->
        ConfirmDialog(
            title = "Delete Game permanently?",
            message = "This will permanently delete the game and all associated scores as if it never happened. This action cannot be undone.",
            confirmText = "Delete",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                state.eventSink(HomeEvent.DeleteGame(game.gameId))
                deleteGameConfirm = null
            },
            onDismiss = { deleteGameConfirm = null }
        )
    }

    deleteTemplateConfirm?.let { template ->
        ConfirmDialog(
            title = "Delete \"${template.name}\"?",
            message = "This table will be removed. Your game history is not affected.",
            confirmText = "Delete",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                state.eventSink(HomeEvent.DeleteTemplate(template.templateId))
                deleteTemplateConfirm = null
            },
            onDismiss = { deleteTemplateConfirm = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    onNewGame: () -> Unit,
    onPlayers: () -> Unit,
    onHistory: () -> Unit,
    onResumeGame: (gameId: Long) -> Unit,
    onOpenSettings: () -> Unit,
    onAbandonGame: (ActiveGameSummary) -> Unit,
    onDeleteGame: (ActiveGameSummary) -> Unit,
    onStartFromTemplate: (templateId: Long) -> Unit,
    onDeleteTemplate: (TemplateSummary) -> Unit,
) {
    val isDark = when (uiState.activeTheme) {
        AppTheme.OLED, AppTheme.SLATE -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Loading overlay
            AnimatedVisibility(
                visible = uiState.isLoading,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                ) {
                    DominoTile(
                        topValue = 6,
                        bottomValue = 3,
                        tileWidth = 64.dp,
                    )
                }
            }

            // Main content — shown even during load so layout doesn't jump
            LazyColumn(
                contentPadding = PaddingValues(bottom = 32.dp),
            ) {
                // Header — app branding
                item {
                    HomeHeader(isDark = isDark)
                }

                // Primary CTA
                item {
                    NewGameCard(onNewGame = onNewGame)
                }

                // Secondary actions
                item {
                    SecondaryActionsRow(
                        onPlayers = onPlayers,
                        onHistory = onHistory,
                    )
                }

                // Quick Start templates section
                if (uiState.templates.isNotEmpty()) {
                    item {
                        Text(
                            text = "Quick Start",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 10.dp),
                        )
                    }
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                        ) {
                            items(uiState.templates, key = { it.templateId }) { template ->
                                TemplateCard(
                                    template = template,
                                    onStart = { onStartFromTemplate(template.templateId) },
                                    onDelete = { onDeleteTemplate(template) },
                                )
                            }
                        }
                    }
                }

                // Active games section header
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 12.dp),
                    ) {
                        Text(
                            text = "Active Games",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (uiState.activePausedGames.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            // Pill badge — count needs a surface so it reads as a distinct element
                            Text(
                                text = "${uiState.activePausedGames.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(8.dp),
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                    }
                }

                // Active game cards
                if (uiState.activePausedGames.isNotEmpty()) {
                    items(
                        items = uiState.activePausedGames,
                        key = { it.gameId },
                    ) { game ->
                        ActiveGameCard(
                            game = game,
                            onResume = { onResumeGame(game.gameId) },
                            onAbandon = { onAbandonGame(game) },
                            onDelete = { onDeleteGame(game) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                } else if (!uiState.isLoading) {
                    item {
                        EmptyState(
                            icon = Icons.Outlined.SportsEsports,
                            title = "No Active Games",
                            subtitle = "Tap \"New Game\" above to start tracking scores.",
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-composables
// ---------------------------------------------------------------------------

@Composable
private fun HomeHeader(isDark: Boolean) {
    val dominoBg      = if (isDark) Color(0xFFF5F0E8) else Color(0xFF1A1A1A)
    val dominoPip     = if (isDark) Color(0xFF1A1A1A) else Color(0xFFF5F0E8)
    val dominoDivider = if (isDark) Color(0xFFD0C9C0) else Color(0xFF3A3A3A)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
    ) {
        DominoTile(
            topValue = 6,
            bottomValue = 3,
            tileWidth = 48.dp,
            backgroundColor = dominoBg,
            pipColor = dominoPip,
            dividerColor = dominoDivider,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .graphicsLayer {
                    rotationZ = 15f
                    shadowElevation = 24f
                    spotShadowColor = Color.Black.copy(alpha = 0.35f)
                    ambientShadowColor = Color.Black.copy(alpha = 0.15f)
                },
        )
        Text(
            text = "Boneyard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Domino Scorer",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NewGameCard(onNewGame: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(20.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ready to play?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Start a new 14-round game for 4 players.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = onNewGame,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Icon(
                    imageVector = Icons.Filled.Casino,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Game")
            }
        }
    }
}

@Composable
private fun SecondaryActionsRow(
    onPlayers: () -> Unit,
    onHistory: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        OutlinedButton(
            onClick = onPlayers,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                imageVector = Icons.Outlined.Group,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Players")
        }
        OutlinedButton(
            onClick = onHistory,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("History")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ActiveGameCard(
    game: ActiveGameSummary,
    onResume: () -> Unit,
    onAbandon: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardDefaults.shape)
                .combinedClickable(
                    onClick = onResume,
                    onLongClick = { showMenu = true }
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = game.playerNames.joinToString(" · "),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Round ${game.currentRound} of ${game.totalRounds}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                FilledTonalButton(onClick = onResume) {
                    Text("Resume")
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Abandon Game") },
                onClick = {
                    showMenu = false
                    onAbandon()
                }
            )
            DropdownMenuItem(
                text = { Text("Delete (As If Never Happened)") },
                onClick = {
                    showMenu = false
                    onDelete()
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TemplateCard(
    template: TemplateSummary,
    onStart: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .width(160.dp)
                .clip(CardDefaults.shape)
                .combinedClickable(
                    onClick = onStart,
                    onLongClick = { showMenu = true },
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    template.players.forEach { player ->
                        PlayerAvatar(
                            name = player.name,
                            color = player.color,
                            avatarIndex = player.avatarIndex,
                            size = AvatarSize.Small,
                        )
                    }
                }
                Text(
                    text = "Start Game →",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            DropdownMenuItem(
                text = { Text("Delete Table") },
                onClick = {
                    showMenu = false
                    onDelete()
                },
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Settings Bottom Sheet
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsBottomSheet(
    activeTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    onResetAllData: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showResetConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Theme Options
            Text(
                text = "Theme / Appearance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            ThemeOptionItem(
                theme = AppTheme.SYSTEM,
                title = "System Default",
                description = "Follows your device settings",
                bgColor1 = MaterialTheme.colorScheme.background,
                surfColor1 = MaterialTheme.colorScheme.surfaceVariant,
                selected = activeTheme == AppTheme.SYSTEM,
                onClick = { onThemeChange(AppTheme.SYSTEM) }
            )

            ThemeOptionItem(
                theme = AppTheme.OLED,
                title = "OLED Pure Black",
                description = "Perfect for OLED screens, saves battery",
                bgColor1 = Color(0xFF000000),
                surfColor1 = Color(0xFF121212),
                selected = activeTheme == AppTheme.OLED,
                onClick = { onThemeChange(AppTheme.OLED) }
            )

            ThemeOptionItem(
                theme = AppTheme.SLATE,
                title = "Slate Deep Gray",
                description = "A sleek, gorgeous dark mode in slate tones",
                bgColor1 = Color(0xFF121212),
                surfColor1 = Color(0xFF1E1E1E),
                selected = activeTheme == AppTheme.SLATE,
                onClick = { onThemeChange(AppTheme.SLATE) }
            )

            ThemeOptionItem(
                theme = AppTheme.LIGHT,
                title = "Always Light",
                description = "Forces light mode regardless of system setting",
                bgColor1 = Color(0xFFF5F5F5),
                surfColor1 = Color(0xFFFFFFFF),
                selected = activeTheme == AppTheme.LIGHT,
                onClick = { onThemeChange(AppTheme.LIGHT) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Live Preview Card
            Text(
                text = "Live Theme Preview",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            LiveThemePreviewCard()

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            // About Section
            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Boneyard digitizes scorekeeping for 4-player domino games, managing shaker rotations, spinners, and cumulative totals.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Version: 1.0.0 (Build 1)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            // Danger Zone
            Text(
                text = "Danger Zone",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { showResetConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error))
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset All Application Data")
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showResetConfirm) {
        ConfirmDialog(
            title = "Reset All Application Data?",
            message = "This is a destructive action and will permanently delete all players, active games, and completed game history. It cannot be undone.",
            confirmText = "Reset Everything",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                onResetAllData()
                showResetConfirm = false
                scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
            },
            onDismiss = { showResetConfirm = false }
        )
    }
}

@Composable
private fun ThemeOptionItem(
    theme: AppTheme,
    title: String,
    description: String,
    bgColor1: Color,
    surfColor1: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp)
    ) {
        // Theme preview circles
        Row(
            horizontalArrangement = Arrangement.spacedBy((-8).dp),
            modifier = Modifier.width(44.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .shadow(1.dp, CircleShape)
                    .background(bgColor1, CircleShape)
                    .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .shadow(1.dp, CircleShape)
                    .background(surfColor1, CircleShape)
                    .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun LiveThemePreviewCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Miniature Mock toolbar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(6.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), RoundedCornerShape(3.dp))
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            // Miniature game card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(5.dp)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f), RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(3.dp)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(1.5.dp))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(16.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "HomeScreen empty — Light", showBackground = true, device = "id:pixel_6")
@Composable
private fun HomeScreenEmptyLightPreview() {
    BoneyardTheme(darkTheme = false) {
        HomeScreenContent(
            uiState = HomeUiState(),
            onNewGame = {}, onPlayers = {}, onHistory = {}, onResumeGame = {},
            onOpenSettings = {}, onAbandonGame = {}, onDeleteGame = {},
            onStartFromTemplate = {}, onDeleteTemplate = {},
        )
    }
}
