package app.boneyard.ui.game.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.boneyard.di.AppScope
import app.boneyard.ui.components.AvatarSize
import app.boneyard.ui.components.DominoTile
import app.boneyard.ui.components.PlayerAvatar
import app.boneyard.ui.navigation.GameSummaryScreen
import app.boneyard.ui.theme.BoneyardTheme
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

// ---------------------------------------------------------------------------
// UI State & Models
// ---------------------------------------------------------------------------

data class RoundSummaryRow(
    val roundIndex: Int,
    val spinnerValue: Int,
    val scores: List<Int>,
    val winnerSeatIndex: Int,
)

data class SummaryPlayer(
    val name: String,
    val color: Color,
    val avatarIndex: Int,
    val totalScore: Int,
    val isWinner: Boolean,
)

data class GameSummaryUiState(
    val gameId: Long = 0L,
    val gameDate: String = "",
    val players: List<SummaryPlayer> = emptyList(),
    val rounds: List<RoundSummaryRow> = emptyList(),
    val isLoading: Boolean = false,
    val isAbandoned: Boolean = false,
    val endedRoundIndex: Int = 0,
    val eventSink: (GameSummaryEvent) -> Unit = {},
) : CircuitUiState

val GameSummaryUiState.winner: SummaryPlayer? get() = players.firstOrNull { it.isWinner }
val GameSummaryUiState.winnerColumnIndex: Int get() = players.indexOfFirst { it.isWinner }

sealed interface GameSummaryEvent : CircuitUiEvent {
    data object Done : GameSummaryEvent
}

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@CircuitInject(GameSummaryScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSummaryUi(state: GameSummaryUiState, modifier: Modifier = Modifier) {
    GameSummaryScreenContent(
        uiState = state,
        onDone = { state.eventSink(GameSummaryEvent.Done) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameSummaryScreenContent(
    uiState: GameSummaryUiState,
    onDone: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isAbandoned) "Game Incomplete" else "Game Summary",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            // Hero section: Winner for completed, or Warning card for abandoned
            item {
                if (uiState.isAbandoned) {
                    AbandonedHeroSection(
                        endedRoundNumber = uiState.endedRoundIndex + 1,
                        date = uiState.gameDate,
                    )
                } else {
                    WinnerHeroSection(
                        winner = uiState.winner,
                        gameDate = uiState.gameDate,
                    )
                }
            }

            // Section divider
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Round-by-Round Scores",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                )
            }

            // Full round table
            if (uiState.players.isNotEmpty()) {
                item {
                    FullScoreTable(
                        players = uiState.players,
                        rounds = uiState.rounds,
                        winnerColumnIndex = if (uiState.isAbandoned) -1 else uiState.winnerColumnIndex,
                    )
                }
            }

            // Done button
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Done")
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Hero sections
// ---------------------------------------------------------------------------

private val TrophyGold = Color(0xFFFFC107)
private val WinnerGreenTint = Color(0xFF43A047)

@Composable
private fun WinnerHeroSection(
    winner: SummaryPlayer?,
    gameDate: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
    ) {
        ConfettiRow()

        Spacer(modifier = Modifier.height(12.dp))

        Icon(
            imageVector = Icons.Filled.EmojiEvents,
            contentDescription = null,
            tint = TrophyGold,
            modifier = Modifier.size(40.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (winner != null) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .background(TrophyGold.copy(alpha = 0.25f), shape = androidx.compose.foundation.shape.CircleShape)
                )
                PlayerAvatar(
                    name = winner.name,
                    color = winner.color,
                    avatarIndex = winner.avatarIndex,
                    size = AvatarSize.XLarge,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = winner.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Wins with ${winner.totalScore} points",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            )
        }

        if (gameDate.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = gameDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        ConfettiRow(reversed = true)
    }
}

@Composable
private fun AbandonedHeroSection(
    endedRoundNumber: Int,
    date: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = Color(0xFFFB8C00), // Orange
            modifier = Modifier.size(48.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Game Abandoned",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "This game was ended in round $endedRoundNumber",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (date.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun ConfettiRow(reversed: Boolean = false) {
    val confettiColors = listOf(
        Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047),
        Color(0xFFFB8C00), Color(0xFF8E24AA), Color(0xFF00ACC1),
    )
    val colors = if (reversed) confettiColors.reversed() else confettiColors

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 32.dp),
    ) {
        colors.forEachIndexed { index, color ->
            val verticalOffset = if (index % 2 == 0) (-4).dp else 4.dp
            Box(
                modifier = Modifier
                    .offset(y = verticalOffset)
                    .size(width = 18.dp, height = 8.dp)
                    .background(
                        color = color.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(2.dp),
                    ),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Full round score table
// ---------------------------------------------------------------------------

@Composable
private fun FullScoreTable(
    players: List<SummaryPlayer>,
    rounds: List<RoundSummaryRow>,
    winnerColumnIndex: Int,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            TableHeaderRow(
                players = players,
                winnerColumnIndex = winnerColumnIndex,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            rounds.forEachIndexed { index, round ->
                RoundScoreRow(
                    round = round,
                    winnerColumnIndex = winnerColumnIndex,
                    playerCount = players.size,
                )
                if (index < rounds.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 2.dp,
            )
            TotalsRow(
                players = players,
                winnerColumnIndex = winnerColumnIndex,
            )
        }
    }
}

@Composable
private fun TableHeaderRow(
    players: List<SummaryPlayer>,
    winnerColumnIndex: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Box(modifier = Modifier.width(52.dp))

        players.forEachIndexed { index, player ->
            val isWinner = index == winnerColumnIndex
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (isWinner) Modifier.background(
                            WinnerGreenTint.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp),
                        ) else Modifier
                    )
                    .padding(4.dp),
            ) {
                PlayerAvatar(
                    name = player.name,
                    color = player.color,
                    avatarIndex = player.avatarIndex,
                    size = AvatarSize.Small,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = player.name.take(6),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isWinner) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun RoundScoreRow(
    round: RoundSummaryRow,
    winnerColumnIndex: Int,
    playerCount: Int,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(52.dp),
        ) {
            DominoTile(
                topValue = round.spinnerValue,
                bottomValue = round.spinnerValue,
                tileWidth = 22.dp,
            )
            Text(
                text = "R${round.roundIndex + 1}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        round.scores.forEachIndexed { playerIndex, score ->
            val isWinnerCol = playerIndex == winnerColumnIndex
            val isRoundWinner = playerIndex == round.winnerSeatIndex

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (isWinnerCol) Modifier.background(WinnerGreenTint.copy(alpha = 0.07f))
                        else Modifier
                    )
                    .padding(vertical = 4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isRoundWinner) FontWeight.Bold else FontWeight.Normal,
                        color = if (isWinnerCol) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface,
                    )
                    if (isRoundWinner) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Round winner",
                            tint = TrophyGold,
                            modifier = Modifier.size(10.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TotalsRow(
    players: List<SummaryPlayer>,
    winnerColumnIndex: Int,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 10.dp),
    ) {
        Text(
            text = "Total",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(52.dp),
        )

        players.forEachIndexed { index, player ->
            val isWinner = index == winnerColumnIndex
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (isWinner) Modifier.background(WinnerGreenTint.copy(alpha = 0.15f))
                        else Modifier
                    )
                    .padding(4.dp),
            ) {
                Text(
                    text = "${player.totalScore}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isWinner) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

private val previewPlayers = listOf(
    SummaryPlayer("Alice", Color(0xFF1E88E5), 0, 42, isWinner = true),
    SummaryPlayer("Bob", Color(0xFF43A047), 1, 67, isWinner = false),
    SummaryPlayer("Carol", Color(0xFFE53935), -1, 89, isWinner = false),
    SummaryPlayer("Dave", Color(0xFF8E24AA), 3, 104, isWinner = false),
)

private val previewRounds = listOf(
    RoundSummaryRow(0, 6, listOf(12, 24, 0, 18), winnerSeatIndex = 2),
    RoundSummaryRow(1, 5, listOf(0, 15, 30, 22), winnerSeatIndex = 0),
    RoundSummaryRow(2, 4, listOf(18, 0, 27, 14), winnerSeatIndex = 1),
    RoundSummaryRow(3, 3, listOf(12, 28, 32, 50), winnerSeatIndex = 0),
)

@Preview(name = "GameSummaryScreen — Light", showBackground = true, device = "id:pixel_6")
@Composable
private fun GameSummaryLightPreview() {
    BoneyardTheme(darkTheme = false) {
        GameSummaryScreenContent(
            uiState = GameSummaryUiState(
                gameDate = "Feb 18, 2026",
                players = previewPlayers,
                rounds = previewRounds,
            ),
            onDone = {},
        )
    }
}
