package app.boneyard.ui.game.setup

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DragHandle
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.boneyard.di.AppScope
import app.boneyard.ui.components.AvatarSize
import app.boneyard.ui.components.PlayerAvatar
import app.boneyard.ui.navigation.GameSetupScreen
import app.boneyard.ui.theme.BoneyardTheme
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

// ---------------------------------------------------------------------------
// UI State & Models
// ---------------------------------------------------------------------------

data class SelectablePlayer(
    val playerId: Long,
    val name: String,
    val color: Color,
    val avatarIndex: Int,
    val isSelected: Boolean = false,
)

data class GameSetupUiState(
    val availablePlayers: List<SelectablePlayer> = emptyList(),
    val selectedOrder: List<SelectablePlayer> = emptyList(),
    val firstShakerIndex: Int = 0,
    val currentStep: Int = 0, // 0 = select, 1 = order
    val isLoading: Boolean = false,
    val eventSink: (GameSetupEvent) -> Unit = {},
) : CircuitUiState

val GameSetupUiState.selectedCount: Int get() = availablePlayers.count { it.isSelected }
val GameSetupUiState.canProceed: Boolean get() = selectedCount == 4
val GameSetupUiState.canStartGame: Boolean get() = currentStep == 1 && selectedOrder.size == 4

sealed interface GameSetupEvent : CircuitUiEvent {
    data object Back : GameSetupEvent
    data object CreatePlayer : GameSetupEvent
    data class TogglePlayer(val playerId: Long) : GameSetupEvent
    data class ReorderPlayers(val fromIndex: Int, val toIndex: Int) : GameSetupEvent
    data class SetFirstShaker(val index: Int) : GameSetupEvent
    data class GoToStep(val step: Int) : GameSetupEvent
    data object StartGame : GameSetupEvent
    data class SaveTemplate(val name: String) : GameSetupEvent
}

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@CircuitInject(GameSetupScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSetupUi(state: GameSetupUiState, modifier: Modifier = Modifier) {
    GameSetupScreenContent(
        uiState = state,
        onBack = { state.eventSink(GameSetupEvent.Back) },
        onCreatePlayer = { state.eventSink(GameSetupEvent.CreatePlayer) },
        onTogglePlayer = { state.eventSink(GameSetupEvent.TogglePlayer(it)) },
        onReorderPlayers = { from, to -> state.eventSink(GameSetupEvent.ReorderPlayers(from, to)) },
        onFirstShakerChange = { state.eventSink(GameSetupEvent.SetFirstShaker(it)) },
        onStartGame = { state.eventSink(GameSetupEvent.StartGame) },
        onSaveTemplate = { name -> state.eventSink(GameSetupEvent.SaveTemplate(name)) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameSetupScreenContent(
    uiState: GameSetupUiState,
    onBack: () -> Unit,
    onCreatePlayer: () -> Unit,
    onTogglePlayer: (playerId: Long) -> Unit,
    onReorderPlayers: (fromIndex: Int, toIndex: Int) -> Unit,
    onFirstShakerChange: (index: Int) -> Unit,
    onStartGame: () -> Unit,
    onSaveTemplate: (name: String) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "New Game",
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
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            SetupStepIndicator(
                currentStep = uiState.currentStep,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )

            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                    } else {
                        slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                    }
                },
                label = "setup_step",
                modifier = Modifier.weight(1f),
            ) { step ->
                when (step) {
                    0 -> PlayerSelectionStep(
                        players = uiState.availablePlayers,
                        selectedCount = uiState.selectedCount,
                        onToggle = onTogglePlayer,
                        onCreatePlayer = onCreatePlayer,
                    )
                    1 -> PlayerOrderStep(
                        orderedPlayers = uiState.selectedOrder,
                        firstShakerIndex = uiState.firstShakerIndex,
                        onFirstShakerChange = onFirstShakerChange,
                        onReorderPlayers = { from, to -> onReorderPlayers(from, to) },
                        onSaveTemplate = { name ->
                            onSaveTemplate(name)
                            scope.launch { snackbarHostState.showSnackbar("Table saved!") }
                        },
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                Button(
                    onClick = onStartGame,
                    enabled = if (uiState.currentStep == 0) uiState.canProceed
                              else uiState.canStartGame,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = if (uiState.currentStep == 0) "Next" else "Start Game")
                }
            }
        }
    }
}

@Composable
private fun SetupStepIndicator(
    currentStep: Int,
    modifier: Modifier = Modifier,
) {
    val stepLabels = listOf("Select Players", "Set Order")

    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            stepLabels.forEachIndexed { index, label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (index == currentStep) FontWeight.Bold else FontWeight.Normal,
                    color = if (index == currentStep) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { (currentStep + 1).toFloat() / stepLabels.size.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            strokeCap = StrokeCap.Round,
        )
    }
}

// ---------------------------------------------------------------------------
// Step 1: Player selection
// ---------------------------------------------------------------------------

@Composable
private fun PlayerSelectionStep(
    players: List<SelectablePlayer>,
    selectedCount: Int,
    onToggle: (Long) -> Unit,
    onCreatePlayer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "$selectedCount/4 players selected",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (selectedCount == 4) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(items = players, key = { it.playerId }) { player ->
                SelectablePlayerCard(
                    player = player,
                    isDisabledByLimit = selectedCount >= 4 && !player.isSelected,
                    onToggle = { onToggle(player.playerId) },
                )
            }

            item {
                AddPlayerPlaceholderCard(onClick = onCreatePlayer)
            }
        }
    }
}

@Composable
private fun SelectablePlayerCard(
    player: SelectablePlayer,
    isDisabledByLimit: Boolean,
    onToggle: () -> Unit,
) {
    val borderColor = when {
        player.isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    }
    val cardA11yLabel = when {
        isDisabledByLimit -> "${player.name}, not available (4 players already selected)"
        player.isSelected -> "${player.name}, selected"
        else -> "${player.name}, tap to select"
    }

    Card(
        onClick = { if (!isDisabledByLimit) onToggle() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (player.isSelected) MaterialTheme.colorScheme.primaryContainer
                             else MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(
            width = if (player.isSelected) 2.dp else 1.dp,
            color = borderColor,
        ),
        modifier = Modifier.semantics { contentDescription = cardA11yLabel },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Box {
                PlayerAvatar(
                    name = player.name,
                    color = if (isDisabledByLimit) player.color.copy(alpha = 0.4f) else player.color,
                    avatarIndex = player.avatarIndex,
                    size = AvatarSize.Large,
                )
                if (player.isSelected) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(22.dp)
                            .background(Color.White, CircleShape)
                            .align(Alignment.TopEnd),
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = player.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (isDisabledByLimit) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun AddPlayerPlaceholderCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add Player",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Step 2: Player order
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerOrderStep(
    orderedPlayers: List<SelectablePlayer>,
    firstShakerIndex: Int,
    onFirstShakerChange: (Int) -> Unit,
    onReorderPlayers: (fromIndex: Int, toIndex: Int) -> Unit,
    onSaveTemplate: (name: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    var showTemplateDialog by remember { mutableStateOf(false) }
    var templateName by remember(orderedPlayers) {
        mutableStateOf(orderedPlayers.joinToString(", ") { it.name })
    }
    val firstShakerName = orderedPlayers.getOrNull(firstShakerIndex)?.name ?: ""
    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onReorderPlayers(from.index, to.index)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = "Set Seating & Play Order",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
        Text(
            text = "Hold and drag ≡ to reorder",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            modifier = Modifier.weight(1f),
        ) {
            itemsIndexed(orderedPlayers, key = { _, p -> p.playerId }) { index, player ->
                ReorderableItem(reorderState, player.playerId) { isDragging ->
                    OrderedPlayerRow(
                        position = index + 1,
                        player = player,
                        isDragging = isDragging,
                        dragHandleModifier = Modifier.draggableHandle(),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "First Shaker",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 6.dp),
        )

        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = it },
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = firstShakerName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select First Shaker") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false },
            ) {
                orderedPlayers.forEachIndexed { index, player ->
                    DropdownMenuItem(
                        text = { Text(player.name) },
                        onClick = {
                            onFirstShakerChange(index)
                            dropdownExpanded = false
                        },
                        leadingIcon = {
                            PlayerAvatar(
                                name = player.name,
                                color = player.color,
                                avatarIndex = player.avatarIndex,
                                size = AvatarSize.Small,
                            )
                        },
                    )
                }
            }
        }

        TextButton(
            onClick = { showTemplateDialog = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save Lineup as Table")
        }

        Spacer(modifier = Modifier.height(8.dp))
    }

    if (showTemplateDialog) {
        AlertDialog(
            onDismissRequest = { showTemplateDialog = false },
            title = { Text("Save Table") },
            text = {
                Column {
                    Text(
                        text = "Give this lineup a name so you can start a game with one tap.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = templateName,
                        onValueChange = { templateName = it },
                        label = { Text("Table name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (templateName.isNotBlank()) {
                            onSaveTemplate(templateName.trim())
                            showTemplateDialog = false
                        }
                    },
                    enabled = templateName.isNotBlank(),
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTemplateDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun OrderedPlayerRow(
    position: Int,
    player: SelectablePlayer,
    isDragging: Boolean,
    dragHandleModifier: Modifier = Modifier,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 6.dp else 1.dp,
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small,
                    ),
            ) {
                Text(
                    text = "$position",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            PlayerAvatar(
                name = player.name,
                color = player.color,
                avatarIndex = player.avatarIndex,
                size = AvatarSize.Small,
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = player.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )

            Icon(
                imageVector = Icons.Filled.DragHandle,
                contentDescription = "Drag to reorder",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = dragHandleModifier.size(24.dp),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

private val previewPlayers = listOf(
    SelectablePlayer(1L, "Alice", Color(0xFF1E88E5), 0, isSelected = true),
    SelectablePlayer(2L, "Bob", Color(0xFF43A047), 1, isSelected = true),
    SelectablePlayer(3L, "Carol", Color(0xFFE53935), -1, isSelected = false),
    SelectablePlayer(4L, "Dave", Color(0xFF8E24AA), 3, isSelected = true),
    SelectablePlayer(5L, "Eve", Color(0xFFFB8C00), 4, isSelected = true),
    SelectablePlayer(6L, "Frank", Color(0xFF00ACC1), 5, isSelected = false),
)

@Preview(name = "GameSetupScreen step1 — Light", showBackground = true, device = "id:pixel_6")
@Composable
private fun GameSetupStep1LightPreview() {
    BoneyardTheme(darkTheme = false) {
        GameSetupScreenContent(
            uiState = GameSetupUiState(
                availablePlayers = previewPlayers,
                currentStep = 0,
            ),
            onBack = {}, onCreatePlayer = {}, onTogglePlayer = {}, onReorderPlayers = { _, _ -> },
            onFirstShakerChange = {}, onStartGame = {}, onSaveTemplate = {},
        )
    }
}
