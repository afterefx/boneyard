package app.boneyard.ui.player.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.boneyard.di.AppScope
import app.boneyard.ui.components.ConfirmDialog
import app.boneyard.ui.components.AvatarSize
import app.boneyard.ui.components.EmptyState
import app.boneyard.ui.components.PlayerAvatar
import app.boneyard.ui.navigation.PlayerListScreen
import app.boneyard.ui.theme.BoneyardTheme
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// UI State
// ---------------------------------------------------------------------------

data class PlayerListItem(
    val playerId: Long,
    val name: String,
    val color: Color,
    val avatarIndex: Int,
    val gamesPlayed: Int,
    val gamesWon: Int,
)

data class PlayerListUiState(
    val players: List<PlayerListItem> = emptyList(),
    val isLoading: Boolean = false,
    val eventSink: (PlayerListEvent) -> Unit = {},
) : CircuitUiState

sealed interface PlayerListEvent : CircuitUiEvent {
    data object Back : PlayerListEvent
    data object AddPlayer : PlayerListEvent
    data class EditPlayer(val playerId: Long) : PlayerListEvent
    data class PlayerProfile(val playerId: Long) : PlayerListEvent
    data class DeletePlayer(val playerId: Long) : PlayerListEvent
}

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@CircuitInject(PlayerListScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerListUi(state: PlayerListUiState, modifier: Modifier = Modifier) {
    PlayerListScreenContent(
        uiState = state,
        onBack = { state.eventSink(PlayerListEvent.Back) },
        onAddPlayer = { state.eventSink(PlayerListEvent.AddPlayer) },
        onEditPlayer = { state.eventSink(PlayerListEvent.EditPlayer(it)) },
        onDeletePlayer = { state.eventSink(PlayerListEvent.DeletePlayer(it)) },
        onPlayerProfile = { state.eventSink(PlayerListEvent.PlayerProfile(it)) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerListScreenContent(
    uiState: PlayerListUiState,
    onBack: () -> Unit,
    onAddPlayer: () -> Unit,
    onEditPlayer: (playerId: Long) -> Unit,
    onDeletePlayer: (playerId: Long) -> Unit,
    onPlayerProfile: (playerId: Long) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Players",
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPlayer,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add new player",
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Loading state
            AnimatedVisibility(
                visible = uiState.isLoading,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center),
            ) {
                CircularProgressIndicator()
            }

            if (!uiState.isLoading) {
                if (uiState.players.isEmpty()) {
                    EmptyState(
                        icon = Icons.Outlined.People,
                        title = "No Players Yet",
                        subtitle = "Add players to get started. You need at least 2 to play.",
                        actionLabel = "Add Player",
                        onAction = onAddPlayer,
                        modifier = Modifier.align(Alignment.Center),
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 88.dp,
                        ),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(
                            items = uiState.players,
                            key = { it.playerId },
                        ) { player ->
                            SwipeablePlayerCard(
                                player = player,
                                onDelete = { onDeletePlayer(player.playerId) },
                                onEdit = { onEditPlayer(player.playerId) },
                                onTap = { onPlayerProfile(player.playerId) },
                                modifier = Modifier.padding(vertical = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Player card with swipe-to-delete
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeablePlayerCard(
    player: PlayerListItem,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showConfirm by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                showConfirm = true
                false
            } else {
                false
            }
        },
    )

    if (showConfirm) {
        ConfirmDialog(
            title = "Delete Player?",
            message = "Are you sure you want to delete ${player.name}? This will permanently remove their profile. Historical game records will retain their score data.",
            confirmText = "Delete",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                showConfirm = false
                onDelete()
            },
            onDismiss = {
                showConfirm = false
                scope.launch { dismissState.reset() }
            }
        )
    }

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
                    contentDescription = "Delete ${player.name}",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
        content = {
            PlayerCard(
                player = player,
                onTap = onTap,
                onEdit = onEdit,
            )
        },
    )
}

@Composable
private fun PlayerCard(
    player: PlayerListItem,
    onTap: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onTap,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
        ) {
            PlayerAvatar(
                name = player.name,
                color = player.color,
                avatarIndex = player.avatarIndex,
                size = AvatarSize.Medium,
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${player.gamesPlayed} games · ${player.gamesWon} wins",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Edit shortcut — avoids requiring users to navigate to profile just to edit
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit ${player.name}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

private val previewPlayers = listOf(
    PlayerListItem(1L, "Alice", Color(0xFF1E88E5), 0, 12, 5),
    PlayerListItem(2L, "Bob", Color(0xFF43A047), 1, 8, 2),
    PlayerListItem(3L, "Carol", Color(0xFFE53935), -1, 15, 7),
    PlayerListItem(4L, "Dave", Color(0xFF8E24AA), 3, 4, 1),
)

@Preview(name = "PlayerListScreen populated — Light", showBackground = true, device = "id:pixel_6")
@Composable
private fun PlayerListPopulatedLightPreview() {
    BoneyardTheme(darkTheme = false) {
        PlayerListScreenContent(
            uiState = PlayerListUiState(players = previewPlayers),
            onBack = {}, onAddPlayer = {}, onEditPlayer = {},
            onDeletePlayer = {}, onPlayerProfile = {},
        )
    }
}

@Preview(name = "PlayerListScreen empty — Dark", showBackground = true, device = "id:pixel_6")
@Composable
private fun PlayerListEmptyDarkPreview() {
    BoneyardTheme(darkTheme = true) {
        PlayerListScreenContent(
            uiState = PlayerListUiState(),
            onBack = {}, onAddPlayer = {}, onEditPlayer = {},
            onDeletePlayer = {}, onPlayerProfile = {},
        )
    }
}
