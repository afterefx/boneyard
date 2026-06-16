package app.boneyard.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.boneyard.di.AppScope
import app.boneyard.ui.components.AvatarSize
import app.boneyard.ui.components.EmptyState
import app.boneyard.ui.components.PlayerAvatar
import app.boneyard.ui.navigation.GameHistoryScreen
import app.boneyard.ui.theme.BoneyardTheme
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

// ---------------------------------------------------------------------------
// UI State & Models
// ---------------------------------------------------------------------------

data class GameHistoryItem(
    val gameId: Long,
    val date: String,
    val isAbandoned: Boolean,
    val roundProgress: String, // e.g. "Round 5 of 14"
    val winnerName: String,
    val winnerAvatarIndex: Int,
    val winnerColor: Color,
    val playerNames: String, // player names joined by " · "
    val winnerScore: Int,
    val playerScores: List<Pair<String, Int>>,  // name to total score
)

data class GameHistoryUiState(
    val games: List<GameHistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val eventSink: (GameHistoryEvent) -> Unit = {},
) : CircuitUiState

sealed interface GameHistoryEvent : CircuitUiEvent {
    data object Back : GameHistoryEvent
    data class GameTapped(val gameId: Long) : GameHistoryEvent
    data class DeleteGame(val gameId: Long) : GameHistoryEvent
}

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@CircuitInject(GameHistoryScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameHistoryUi(state: GameHistoryUiState, modifier: Modifier = Modifier) {
    GameHistoryScreenContent(
        uiState = state,
        onBack = { state.eventSink(GameHistoryEvent.Back) },
        onGameTapped = { state.eventSink(GameHistoryEvent.GameTapped(it)) },
        onDeleteGame = { state.eventSink(GameHistoryEvent.DeleteGame(it)) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameHistoryScreenContent(
    uiState: GameHistoryUiState,
    onBack: () -> Unit,
    onGameTapped: (gameId: Long) -> Unit,
    onDeleteGame: (gameId: Long) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Game History",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
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

            if (!uiState.isLoading) {
                if (uiState.games.isEmpty()) {
                    EmptyState(
                        icon = Icons.Outlined.History,
                        title = "No Game History",
                        subtitle = "Completed games will appear here. Go play a game!",
                        modifier = Modifier.align(Alignment.Center),
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(
                            items = uiState.games,
                            key = { it.gameId },
                        ) { game ->
                            SwipeableGameHistoryCard(
                                game = game,
                                onTap = { onGameTapped(game.gameId) },
                                onDelete = { onDeleteGame(game.gameId) },
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Swipeable Wrapper & History card
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableGameHistoryCard(
    game: GameHistoryItem,
    onTap: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.medium,
                    )
                    .padding(horizontal = 20.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete game",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
        content = {
            GameHistoryCard(
                game = game,
                onTap = onTap,
            )
        },
    )
}

private val TrophyGold = Color(0xFFFFC107)

@Composable
private fun GameHistoryCard(
    game: GameHistoryItem,
    onTap: () -> Unit,
) {
    Card(
        onClick = onTap,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            // Top row: date + status badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (game.isAbandoned) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Incomplete",
                        tint = Color(0xFFFB8C00), // Orange
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Incomplete · ${game.roundProgress}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFB8C00),
                    )
                } else {
                    PlayerAvatar(
                        name = game.winnerName,
                        color = game.winnerColor,
                        avatarIndex = game.winnerAvatarIndex,
                        size = AvatarSize.Small,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Winner",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = game.winnerColor,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${game.winnerScore} pts)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = game.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Player names text
            Text(
                text = game.playerNames,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (!game.isAbandoned) {
                Spacer(modifier = Modifier.height(8.dp))
                // Player score summary row (only for completed games)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    game.playerScores.forEach { (name, score) ->
                        PlayerScoreChip(
                            name = name,
                            score = score,
                            isWinner = name == game.winnerName,
                            winnerColor = game.winnerColor,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerScoreChip(
    name: String,
    score: Int,
    isWinner: Boolean,
    winnerColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Text(
            text = name.take(6),
            style = MaterialTheme.typography.labelSmall,
            color = if (isWinner) winnerColor else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            fontWeight = if (isWinner) FontWeight.SemiBold else FontWeight.Normal,
        )
        Text(
            text = "$score",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
            color = if (isWinner) winnerColor else MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

private val previewGames = listOf(
    GameHistoryItem(
        gameId = 1L, date = "Feb 18, 2026", isAbandoned = false, roundProgress = "Round 14 of 14",
        winnerName = "Alice", winnerAvatarIndex = 0, winnerColor = Color(0xFF1E88E5),
        playerNames = "Alice · Bob · Carol · Dave", winnerScore = 42,
        playerScores = listOf("Alice" to 42, "Bob" to 67, "Carol" to 89, "Dave" to 104),
    ),
    GameHistoryItem(
        gameId = 2L, date = "Feb 15, 2026", isAbandoned = true, roundProgress = "Round 5 of 14",
        winnerName = "", winnerAvatarIndex = 0, winnerColor = Color.Gray,
        playerNames = "Alice · Bob · Carol · Dave", winnerScore = 0,
        playerScores = emptyList(),
    ),
    GameHistoryItem(
        gameId = 3L, date = "Feb 10, 2026", isAbandoned = false, roundProgress = "Round 14 of 14",
        winnerName = "Carol", winnerAvatarIndex = 3, winnerColor = Color(0xFFE53935),
        playerNames = "Alice · Bob · Carol · Dave", winnerScore = 72,
        playerScores = listOf("Alice" to 110, "Bob" to 88, "Carol" to 72, "Dave" to 95),
    ),
)

@Preview(name = "GameHistoryScreen populated — Light", showBackground = true, device = "id:pixel_6")
@Composable
private fun GameHistoryPopulatedLightPreview() {
    BoneyardTheme(darkTheme = false) {
        GameHistoryScreenContent(
            uiState = GameHistoryUiState(games = previewGames),
            onBack = {},
            onGameTapped = {},
            onDeleteGame = {},
        )
    }
}

@Preview(name = "GameHistoryScreen empty — Dark", showBackground = true, device = "id:pixel_6")
@Composable
private fun GameHistoryEmptyDarkPreview() {
    BoneyardTheme(darkTheme = true) {
        GameHistoryScreenContent(
            uiState = GameHistoryUiState(),
            onBack = {},
            onGameTapped = {},
            onDeleteGame = {},
        )
    }
}
