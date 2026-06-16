package app.boneyard.ui.game.active

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.boneyard.di.AppScope
import app.boneyard.ui.components.AvatarSize
import app.boneyard.ui.components.ConfirmDialog
import app.boneyard.ui.components.DominoTile
import app.boneyard.ui.components.PlayerAvatar
import app.boneyard.ui.components.RoundIndicator
import app.boneyard.ui.components.ScoreboardEntry
import app.boneyard.ui.components.ScoreboardTable
import app.boneyard.ui.components.ScoreEntryRow
import app.boneyard.ui.navigation.ActiveGameScreen
import app.boneyard.ui.theme.BoneyardTheme
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

private val ShakerGold = Color(0xFFFFC107)

// ---------------------------------------------------------------------------
// UI State & Models
// ---------------------------------------------------------------------------

data class PlayerScoreEntry(
    val playerId: Long,
    val name: String,
    val color: Color,
    val avatarIndex: Int,
    val scoreText: String = "",
    val isWinner: Boolean = false,
    val showError: Boolean = false,
    val totalScore: Int = 0,
)

data class ActiveGameUiState(
    val gameId: Long = 0L,
    val currentRoundIndex: Int = 0,      // 0-based (0–13)
    val completedRounds: Int = 0,
    val viewingRoundIndex: Int = 0,      // displayed round
    val viewingRoundScores: Map<Long, Int> = emptyMap(),
    val spinnerValue: Int = 6,
    val shakerName: String = "",
    val shakerAvatarIndex: Int = 0,
    val shakerColor: Color = Color(0xFF1E88E5),
    val players: List<PlayerScoreEntry> = emptyList(),
    val scoreboardEntries: List<ScoreboardEntry> = emptyList(),
    val isLoading: Boolean = false,
    val isScoreboardExpanded: Boolean = true,
    val isEditingHistory: Boolean = false,
    val eventSink: (ActiveGameEvent) -> Unit = {},
) : CircuitUiState

sealed interface ActiveGameEvent : CircuitUiEvent {
    data class ScoreChange(val playerId: Long, val text: String) : ActiveGameEvent
    data class WinnerToggle(val playerId: Long) : ActiveGameEvent
    data object SubmitRound : ActiveGameEvent
    data object UndoRound : ActiveGameEvent
    data object PauseGame : ActiveGameEvent
    data object ToggleScoreboard : ActiveGameEvent
    data class NavigateToRound(val roundIndex: Int) : ActiveGameEvent
    data object StartEditingHistory : ActiveGameEvent
    data object CancelEditingHistory : ActiveGameEvent
    data object SaveEditingHistory : ActiveGameEvent
}

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@CircuitInject(ActiveGameScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveGameUi(state: ActiveGameUiState, modifier: Modifier = Modifier) {
    ActiveGameScreenContent(
        uiState = state,
        onPause = { state.eventSink(ActiveGameEvent.PauseGame) },
        onScoreChange = { playerId, text -> state.eventSink(ActiveGameEvent.ScoreChange(playerId, text)) },
        onWinnerToggle = { playerId -> state.eventSink(ActiveGameEvent.WinnerToggle(playerId)) },
        onSubmitRound = { state.eventSink(ActiveGameEvent.SubmitRound) },
        onUndoRound = { state.eventSink(ActiveGameEvent.UndoRound) },
        onToggleScoreboard = { state.eventSink(ActiveGameEvent.ToggleScoreboard) },
        onNavigateRound = { state.eventSink(ActiveGameEvent.NavigateToRound(it)) },
        onEditHistory = { state.eventSink(ActiveGameEvent.StartEditingHistory) },
        onCancelEditHistory = { state.eventSink(ActiveGameEvent.CancelEditingHistory) },
        onSaveEditHistory = { state.eventSink(ActiveGameEvent.SaveEditingHistory) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveGameScreenContent(
    uiState: ActiveGameUiState,
    onPause: () -> Unit,
    onScoreChange: (playerId: Long, text: String) -> Unit,
    onWinnerToggle: (playerId: Long) -> Unit,
    onSubmitRound: () -> Unit,
    onUndoRound: () -> Unit,
    onToggleScoreboard: () -> Unit,
    onNavigateRound: (Int) -> Unit,
    onEditHistory: () -> Unit,
    onCancelEditHistory: () -> Unit,
    onSaveEditHistory: () -> Unit,
) {
    var showPauseDialog by remember { mutableStateOf(false) }
    val isViewingHistory = uiState.viewingRoundIndex < uiState.currentRoundIndex
    val isViewingFuture = uiState.viewingRoundIndex > uiState.currentRoundIndex
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    if (showPauseDialog) {
        ConfirmDialog(
            title = "Pause Game?",
            message = "Your progress is saved. Resume anytime from the home screen.",
            confirmText = "Pause",
            dismissText = "Keep Playing",
            onConfirm = {
                showPauseDialog = false
                onPause()
            },
            onDismiss = { showPauseDialog = false },
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Round ${uiState.viewingRoundIndex + 1}/14",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showPauseDialog = true },
                        enabled = !uiState.isEditingHistory
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Pause,
                            contentDescription = "Pause game",
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            ActiveGameBottomBar(
                onSubmitRound = onSubmitRound,
                onUndoRound = onUndoRound,
                canUndo = uiState.completedRounds > 0,
                isViewingHistory = isViewingHistory,
                isViewingFuture = isViewingFuture,
                isEditingHistory = uiState.isEditingHistory,
                onReturnToCurrent = { onNavigateRound(uiState.currentRoundIndex) },
                onCancelEdit = onCancelEditHistory,
                onSaveEdit = onSaveEditHistory,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            AnimatedVisibility(
                visible = uiState.isLoading,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center),
            ) {
                CircularProgressIndicator()
            }

            AnimatedVisibility(
                visible = !uiState.isLoading,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 16.dp),
                ) {
                    // Navigation disabled during editing
                    RoundNavigationRow(
                        viewingRoundIndex = uiState.viewingRoundIndex,
                        currentRoundIndex = uiState.currentRoundIndex,
                        onNavigate = onNavigateRound,
                        enabled = !uiState.isEditingHistory,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                    )

                    RoundHeaderCard(
                        spinnerValue = uiState.spinnerValue,
                        shakerName = uiState.shakerName,
                        shakerColor = uiState.shakerColor,
                        shakerAvatarIndex = uiState.shakerAvatarIndex,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )

                    RoundIndicator(
                        completedRounds = uiState.completedRounds,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isViewingFuture) {
                        // Future unplayed round placeholder
                        FutureRoundPlaceholder(
                            roundNumber = uiState.viewingRoundIndex + 1,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    } else if (isViewingHistory && !uiState.isEditingHistory) {
                        // Read-only scores for a completed round
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Round ${uiState.viewingRoundIndex + 1} Scores",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(onClick = onEditHistory) {
                                Text("Edit")
                            }
                        }
                        uiState.players.forEach { player ->
                            HistoryScoreRow(
                                playerName = player.name,
                                playerColor = player.color,
                                avatarIndex = player.avatarIndex,
                                score = uiState.viewingRoundScores[player.playerId] ?: 0,
                            )
                        }
                    } else {
                        // Live score entry for current round OR editing historical round
                        Text(
                            text = if (uiState.isEditingHistory) "Edit Round Scores" else "Enter Scores",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                        )
                        uiState.players.forEach { player ->
                            ScoreEntryRow(
                                playerName = player.name,
                                playerColor = player.color,
                                avatarIndex = player.avatarIndex,
                                scoreText = player.scoreText,
                                isWinner = player.isWinner,
                                showError = player.showError,
                                onScoreChange = { text -> onScoreChange(player.playerId, text) },
                                onWinnerToggle = { onWinnerToggle(player.playerId) },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))

                    CollapsibleScoreboard(
                        entries = uiState.scoreboardEntries,
                        isExpanded = uiState.isScoreboardExpanded,
                        onToggle = onToggleScoreboard,
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-composables
// ---------------------------------------------------------------------------

@Composable
private fun FutureRoundPlaceholder(
    roundNumber: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Round $roundNumber is Unplayed",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Complete the current round first before entering scores for this round.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RoundHeaderCard(
    spinnerValue: Int,
    shakerName: String,
    shakerColor: Color,
    shakerAvatarIndex: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.WorkspacePremium,
                        contentDescription = null,
                        tint = ShakerGold,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Shaker",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PlayerAvatar(
                        name = shakerName,
                        color = shakerColor,
                        avatarIndex = shakerAvatarIndex,
                        size = AvatarSize.Small,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = shakerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DominoTile(
                    topValue = spinnerValue,
                    bottomValue = spinnerValue,
                    tileWidth = 40.dp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Double-$spinnerValue",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CollapsibleScoreboard(
    entries: List<ScoreboardEntry>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    Column(modifier = Modifier.animateContentSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
        ) {
            Text(
                text = "Standings",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onToggle) {
                Text(text = if (isExpanded) "Hide" else "Show")
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            ScoreboardTable(
                entries = entries,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun ActiveGameBottomBar(
    onSubmitRound: () -> Unit,
    onUndoRound: () -> Unit,
    canUndo: Boolean,
    isViewingHistory: Boolean,
    isViewingFuture: Boolean,
    isEditingHistory: Boolean,
    onReturnToCurrent: () -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: () -> Unit,
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            if (isEditingHistory) {
                OutlinedButton(
                    onClick = onCancelEdit,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = onSaveEdit,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Save Changes")
                }
            } else if (isViewingFuture || isViewingHistory) {
                Button(
                    onClick = onReturnToCurrent,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Return to Current Round")
                }
            } else {
                TextButton(
                    onClick = onUndoRound,
                    enabled = canUndo,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo last round",
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Undo")
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = onSubmitRound,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Submit Round")
                }
            }
        }
    }
}

@Composable
private fun RoundNavigationRow(
    viewingRoundIndex: Int,
    currentRoundIndex: Int,
    onNavigate: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        IconButton(
            onClick = { onNavigate(viewingRoundIndex - 1) },
            enabled = enabled && viewingRoundIndex > 0,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous round",
            )
        }

        Text(
            text = "Round ${viewingRoundIndex + 1} of 14",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )

        IconButton(
            onClick = { onNavigate(viewingRoundIndex + 1) },
            enabled = enabled && viewingRoundIndex < 13,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next round",
            )
        }
    }
}

@Composable
private fun HistoryScoreRow(
    playerName: String,
    playerColor: Color,
    avatarIndex: Int,
    score: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        PlayerAvatar(
            name = playerName,
            color = playerColor,
            avatarIndex = avatarIndex,
            size = AvatarSize.Small,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = playerName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        if (score == 0) {
            Icon(
                imageVector = Icons.Outlined.EmojiEvents,
                contentDescription = "Round winner",
                tint = ShakerGold,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = "$score pts",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
