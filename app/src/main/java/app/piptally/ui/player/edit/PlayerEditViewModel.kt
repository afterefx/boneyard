package app.piptally.ui.player.edit

import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.piptally.domain.model.Player
import app.piptally.domain.repository.PlayerRepository
import app.piptally.domain.usecase.player.CreatePlayerResult
import app.piptally.domain.usecase.player.CreatePlayerUseCase
import app.piptally.domain.usecase.player.UpdatePlayerResult
import app.piptally.domain.usecase.player.UpdatePlayerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerEditViewModelImpl @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playerRepository: PlayerRepository,
    private val createPlayerUseCase: CreatePlayerUseCase,
    private val updatePlayerUseCase: UpdatePlayerUseCase
) : ViewModel() {

    // Route arg: -1 means new player
    private val playerId: Long = savedStateHandle.get<Long>("playerId") ?: -1L

    private val _uiState = MutableStateFlow(PlayerEditUiState())
    val uiState = _uiState.asStateFlow()

    // One-shot event to signal navigation back after save
    private val _savedEvent = MutableStateFlow(false)
    val savedEvent = _savedEvent.asStateFlow()

    init {
        if (playerId != -1L) {
            loadExistingPlayer(playerId)
        }
    }

    private fun loadExistingPlayer(id: Long) {
        viewModelScope.launch {
            val player = playerRepository.getPlayerById(id) ?: return@launch
            val colorIndex = PLAYER_COLOR_OPTIONS.indexOfFirst { color ->
                runCatching {
                    AndroidColor.parseColor(player.color)
                }.getOrDefault(0) == run {
                    val r = (color.red * 255).toInt()
                    val g = (color.green * 255).toInt()
                    val b = (color.blue * 255).toInt()
                    (0xFF shl 24) or (r shl 16) or (g shl 8) or b
                }
            }.coerceAtLeast(0)

            _uiState.update {
                it.copy(
                    playerId = id,
                    name = player.name,
                    selectedColorIndex = colorIndex,
                    selectedAvatarIndex = player.avatarIndex.coerceIn(0, AVATAR_EMOJI_OPTIONS.lastIndex)
                )
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onColorSelect(index: Int) {
        _uiState.update { it.copy(selectedColorIndex = index) }
    }

    fun onAvatarSelect(index: Int) {
        _uiState.update { it.copy(selectedAvatarIndex = index) }
    }

    fun onSave() {
        val state = _uiState.value
        val hexColor = colorToHex(PLAYER_COLOR_OPTIONS[state.selectedColorIndex])

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, nameError = null) }

            val player = Player(
                id = state.playerId ?: 0L,
                name = state.name,
                color = hexColor,
                avatarIndex = state.selectedAvatarIndex
            )

            val error: String? = if (state.isNewPlayer) {
                when (createPlayerUseCase(player)) {
                    is CreatePlayerResult.Success -> null
                    CreatePlayerResult.NameBlank -> "Name cannot be empty"
                    CreatePlayerResult.NameTaken -> "A player with this name already exists"
                }
            } else {
                when (updatePlayerUseCase(player)) {
                    UpdatePlayerResult.Success -> null
                    UpdatePlayerResult.NameBlank -> "Name cannot be empty"
                    UpdatePlayerResult.NameTaken -> "A player with this name already exists"
                }
            }

            if (error == null) {
                _savedEvent.value = true
            } else {
                _uiState.update { it.copy(isSaving = false, nameError = error) }
            }
        }
    }

    fun onSavedEventConsumed() {
        _savedEvent.value = false
    }

    private fun colorToHex(color: Color): String {
        val r = (color.red * 255).toInt()
        val g = (color.green * 255).toInt()
        val b = (color.blue * 255).toInt()
        return String.format("#%02X%02X%02X", r, g, b)
    }
}
