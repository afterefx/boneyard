package app.piptally.ui.player.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.piptally.ui.components.AvatarSize
import app.piptally.ui.components.PlayerAvatar
import app.piptally.ui.theme.PipTallyTheme

// ---------------------------------------------------------------------------
// UI State
// ---------------------------------------------------------------------------

val PLAYER_COLOR_OPTIONS = listOf(
    Color(0xFFE53935), // Red
    Color(0xFF1E88E5), // Blue
    Color(0xFF43A047), // Green
    Color(0xFFFB8C00), // Orange
    Color(0xFF8E24AA), // Purple
    Color(0xFF00ACC1), // Cyan
    Color(0xFFE91E63), // Pink  — must match PlayerColors[6] in Color.kt
    Color(0xFF6D4C41), // Brown
)

// Human-readable names paired with PLAYER_COLOR_OPTIONS, used for accessibility labels.
private val PLAYER_COLOR_NAMES = listOf(
    "Red", "Blue", "Green", "Orange", "Purple", "Cyan", "Pink", "Brown",
)

val AVATAR_EMOJI_OPTIONS = listOf("🎲", "🃏", "♟️", "🏆", "🎯", "⭐")

data class PlayerEditUiState(
    val playerId: Long? = null,
    val name: String = "",
    val nameError: String? = null,
    val selectedColorIndex: Int = 0,
    val selectedAvatarIndex: Int = 0,
    val isSaving: Boolean = false,
)

val PlayerEditUiState.isNewPlayer: Boolean get() = playerId == null
val PlayerEditUiState.selectedColor: Color get() = PLAYER_COLOR_OPTIONS[selectedColorIndex]

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerEditScreen(
    viewModel: PlayerEditViewModelImpl = hiltViewModel(),
    onBack: () -> Unit = {},
    onSaved: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val savedEvent by viewModel.savedEvent.collectAsState()

    LaunchedEffect(savedEvent) {
        if (savedEvent) {
            viewModel.onSavedEventConsumed()
            onSaved()
        }
    }

    PlayerEditScreenContent(
        uiState = uiState,
        onBack = onBack,
        onSave = { viewModel.onSave() },
        onNameChange = { viewModel.onNameChange(it) },
        onColorSelect = { viewModel.onColorSelect(it) },
        onAvatarSelect = { viewModel.onAvatarSelect(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerEditScreenContent(
    uiState: PlayerEditUiState,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onNameChange: (String) -> Unit,
    onColorSelect: (Int) -> Unit,
    onAvatarSelect: (Int) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isNewPlayer) "New Player" else "Edit Player",
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
                    TextButton(
                        onClick = onSave,
                        enabled = uiState.name.isNotBlank() && !uiState.isSaving,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Avatar preview (XLarge, centered)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                PlayerAvatar(
                    name = uiState.name.ifBlank { "?" },
                    color = uiState.selectedColor,
                    avatarIndex = uiState.selectedAvatarIndex,
                    size = AvatarSize.XLarge,
                )
            }

            // Name field
            SectionLabel(text = "Name")
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = { Text("Player name") },
                singleLine = true,
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
            )

            // Color selection
            SectionLabel(text = "Color")
            ColorPicker(
                colors = PLAYER_COLOR_OPTIONS,
                selectedIndex = uiState.selectedColorIndex,
                onSelect = onColorSelect,
            )

            // Avatar / emoji selection
            SectionLabel(text = "Avatar")
            AvatarPicker(
                emojis = AVATAR_EMOJI_OPTIONS,
                selectedIndex = uiState.selectedAvatarIndex,
                onSelect = onAvatarSelect,
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-composables
// ---------------------------------------------------------------------------

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorPicker(
    colors: List<Color>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        colors.forEachIndexed { index, color ->
            val isSelected = index == selectedIndex
            val colorName = PLAYER_COLOR_NAMES.getOrElse(index) { "Color ${index + 1}" }
            val a11yLabel = if (isSelected) "$colorName, selected" else colorName
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .semantics { contentDescription = a11yLabel }
                    .shadow(if (isSelected) 6.dp else 2.dp, CircleShape)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (isSelected) Modifier.border(3.dp, Color.White, CircleShape)
                        else Modifier
                    )
                    .clickable { onSelect(index) },
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null, // parent Box already announces selection state
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AvatarPicker(
    emojis: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.height(160.dp),
    ) {
        itemsIndexed(emojis) { index, emoji ->
            val isSelected = index == selectedIndex
            Card(
                onClick = { onSelect(index) },
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                     else MaterialTheme.colorScheme.surfaceVariant,
                ),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(
                    2.dp, MaterialTheme.colorScheme.primary
                ) else null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                ) {
                    Text(
                        text = emoji,
                        fontSize = 28.sp,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "PlayerEditScreen new — Light", showBackground = true, device = "id:pixel_6")
@Composable
private fun PlayerEditNewLightPreview() {
    PipTallyTheme(darkTheme = false) {
        PlayerEditScreenContent(
            uiState = PlayerEditUiState(name = "Alice", selectedColorIndex = 1, selectedAvatarIndex = 0),
            onBack = {}, onSave = {}, onNameChange = {},
            onColorSelect = {}, onAvatarSelect = {},
        )
    }
}

@Preview(name = "PlayerEditScreen edit — Dark", showBackground = true, device = "id:pixel_6")
@Composable
private fun PlayerEditExistingDarkPreview() {
    PipTallyTheme(darkTheme = true) {
        PlayerEditScreenContent(
            uiState = PlayerEditUiState(playerId = 1L, name = "Bob", selectedColorIndex = 3, selectedAvatarIndex = 3),
            onBack = {}, onSave = {}, onNameChange = {},
            onColorSelect = {}, onAvatarSelect = {},
        )
    }
}
