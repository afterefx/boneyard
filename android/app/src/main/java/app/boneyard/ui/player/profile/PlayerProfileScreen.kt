package app.boneyard.ui.player.profile

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.boneyard.di.AppScope
import app.boneyard.ui.components.AvatarSize
import app.boneyard.ui.components.EmptyState
import app.boneyard.ui.components.PlayerAvatar
import app.boneyard.ui.navigation.PlayerProfileScreen
import app.boneyard.ui.theme.BoneyardTheme
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

// ---------------------------------------------------------------------------
// UI State
// ---------------------------------------------------------------------------

data class RecentGameRow(
    val gameId: Long,
    val date: String,
    val opponents: String,
    val score: Int,
    val isWin: Boolean,
)

data class PlayerProfileUiState(
    val playerId: Long = 0L,
    val name: String = "",
    val color: Color = Color(0xFF1E88E5),
    val avatarIndex: Int = 0,
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val winPercent: Int = 0,
    val avgScore: Int = 0,
    val bestScore: Int = 0,
    val worstScore: Int = 0,
    val roundsWon: Int = 0,
    val recentGames: List<RecentGameRow> = emptyList(),
    val isLoading: Boolean = false,
    val eventSink: (PlayerProfileEvent) -> Unit = {},
) : CircuitUiState

sealed interface PlayerProfileEvent : CircuitUiEvent {
    data object Back : PlayerProfileEvent
    data object Edit : PlayerProfileEvent
}

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@CircuitInject(PlayerProfileScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProfileUi(state: PlayerProfileUiState, modifier: Modifier = Modifier) {
    PlayerProfileScreenContent(
        uiState = state,
        onBack = { state.eventSink(PlayerProfileEvent.Back) },
        onEdit = { state.eventSink(PlayerProfileEvent.Edit) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerProfileScreenContent(
    uiState: PlayerProfileUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.name.ifBlank { "Player Profile" },
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
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit player",
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
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 32.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // Avatar header
                    item {
                        PlayerProfileHeader(
                            name = uiState.name,
                            color = uiState.color,
                            avatarIndex = uiState.avatarIndex,
                        )
                    }

                    // Stats grid
                    item {
                        Text(
                            text = "Statistics",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 20.dp, top = 4.dp, bottom = 10.dp),
                        )
                        StatsGrid(uiState = uiState)
                    }

                    // Recent games section
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Recent Games",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 20.dp, bottom = 10.dp),
                        )
                    }

                    if (uiState.recentGames.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Outlined.SportsEsports,
                                title = "No Games Yet",
                                subtitle = "This player hasn't completed any games.",
                            )
                        }
                    } else {
                        items(
                            items = uiState.recentGames,
                            key = { it.gameId },
                        ) { game ->
                            RecentGameRowItem(
                                game = game,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                        }
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
private fun PlayerProfileHeader(
    name: String,
    color: Color,
    avatarIndex: Int,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 28.dp),
    ) {
        PlayerAvatar(
            name = name,
            color = color,
            avatarIndex = avatarIndex,
            size = AvatarSize.XLarge,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(6.dp))
        // Color badge
        Box(
            modifier = Modifier
                .size(width = 48.dp, height = 8.dp)
                .background(color = color, shape = RoundedCornerShape(4.dp)),
        )
    }
}

@Composable
private fun StatsGrid(uiState: PlayerProfileUiState) {
    // 2-column grid using a simple Row/Column layout for 7 stats
    val stats = listOf(
        Triple(Icons.Outlined.SportsEsports, "Games Played", "${uiState.gamesPlayed}"),
        Triple(Icons.Outlined.EmojiEvents, "Games Won", "${uiState.gamesWon}"),
        Triple(Icons.Filled.EmojiEvents, "Win %", "${uiState.winPercent}%"),
        Triple(Icons.Outlined.Leaderboard, "Avg Score", "${uiState.avgScore}"),
        Triple(Icons.AutoMirrored.Outlined.TrendingDown, "Best Score", "${uiState.bestScore}"),
        Triple(Icons.AutoMirrored.Outlined.TrendingUp, "Worst Score", "${uiState.worstScore}"),
        Triple(Icons.Outlined.Repeat, "Rounds Won", "${uiState.roundsWon}"),
    )

    // Chunk into rows of 2 for a 2-column grid
    val rows = stats.chunked(2)

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        rows.forEach { rowStats ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                rowStats.forEach { (icon, label, value) ->
                    StatCard(
                        icon = icon,
                        label = label,
                        value = value,
                        modifier = Modifier.weight(1f),
                    )
                }
                // If odd number of stats, fill remaining space
                if (rowStats.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RecentGameRowItem(
    game: RecentGameRow,
    modifier: Modifier = Modifier,
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Opponents
                Text(
                    text = game.opponents,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )

                // Score
                Text(
                    text = "${game.score} pts",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Win/loss badge — read-only, not interactive.
                FilterChip(
                    selected = game.isWin,
                    onClick = {},
                    label = {
                        Text(
                            text = if (game.isWin) "Win" else "Loss",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                    leadingIcon = if (game.isWin) {
                        {
                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    } else null,
                    modifier = Modifier.semantics(mergeDescendants = false) {
                        contentDescription = if (game.isWin) "Result: Win" else "Result: Loss"
                    },
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            // Date
            Text(
                text = game.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

private val previewUiState = PlayerProfileUiState(
    playerId = 1L,
    name = "Alice",
    color = Color(0xFF1E88E5),
    avatarIndex = 0,
    gamesPlayed = 12,
    gamesWon = 5,
    winPercent = 42,
    avgScore = 68,
    bestScore = 32,
    worstScore = 120,
    roundsWon = 28,
    recentGames = listOf(
        RecentGameRow(1L, "Feb 18, 2026", "Bob · Carol · Dave", 42, isWin = true),
        RecentGameRow(2L, "Feb 15, 2026", "Bob · Carol · Dave", 78, isWin = false),
        RecentGameRow(3L, "Feb 10, 2026", "Bob · Carol · Dave", 55, isWin = true),
        RecentGameRow(4L, "Feb 5, 2026", "Bob · Carol · Dave", 91, isWin = false),
        RecentGameRow(5L, "Jan 30, 2026", "Bob · Carol · Dave", 64, isWin = false),
    ),
)

@Preview(name = "PlayerProfileScreen — Light", showBackground = true, device = "id:pixel_6")
@Composable
private fun PlayerProfileLightPreview() {
    BoneyardTheme(darkTheme = false) {
        PlayerProfileScreenContent(
            uiState = previewUiState,
            onBack = {},
            onEdit = {},
        )
    }
}

@Preview(name = "PlayerProfileScreen — Dark", showBackground = true, device = "id:pixel_6")
@Composable
private fun PlayerProfileDarkPreview() {
    BoneyardTheme(darkTheme = true) {
        PlayerProfileScreenContent(
            uiState = previewUiState,
            onBack = {},
            onEdit = {},
        )
    }
}
