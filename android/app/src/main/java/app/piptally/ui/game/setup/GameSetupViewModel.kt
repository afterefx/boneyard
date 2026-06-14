package app.piptally.ui.game.setup

import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.piptally.domain.usecase.game.CreateGameUseCase
import app.piptally.domain.usecase.player.GetAllPlayersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameSetupViewModelImpl @Inject constructor(
    private val getAllPlayersUseCase: GetAllPlayersUseCase,
    private val createGameUseCase: CreateGameUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameSetupUiState())
    val uiState = _uiState.asStateFlow()

    // One-shot event: gameId when game is created
    private val _gameCreated = MutableStateFlow<Long?>(null)
    val gameCreated = _gameCreated.asStateFlow()

    init {
        viewModelScope.launch {
            getAllPlayersUseCase().collect { players ->
                _uiState.update { current ->
                    val selectedIds = current.availablePlayers
                        .filter { it.isSelected }
                        .map { it.playerId }
                        .toSet()

                    current.copy(
                        availablePlayers = players.map { player ->
                            SelectablePlayer(
                                playerId = player.id,
                                name = player.name,
                                color = runCatching {
                                    Color(AndroidColor.parseColor(player.color))
                                }.getOrDefault(Color(0xFF1E88E5)),
                                avatarIndex = player.avatarIndex,
                                isSelected = player.id in selectedIds
                            )
                        },
                        isLoading = false
                    )
                }
            }
        }
    }

    fun togglePlayer(playerId: Long) {
        _uiState.update { current ->
            val selected = current.availablePlayers.count { it.isSelected }
            val target = current.availablePlayers.find { it.playerId == playerId } ?: return@update current

            // Enforce selection limit of exactly 4 players
            if (!target.isSelected && selected >= 4) return@update current

            val updated = current.availablePlayers.map { p ->
                if (p.playerId == playerId) p.copy(isSelected = !p.isSelected) else p
            }
            val selectedOrder = updated.filter { it.isSelected }
            
            // Default first shaker to index 0
            current.copy(
                availablePlayers = updated,
                selectedOrder = selectedOrder,
                firstShakerIndex = 0
            )
        }
    }

    fun reorderPlayers(fromIndex: Int, toIndex: Int) {
        _uiState.update { current ->
            val mutable = current.selectedOrder.toMutableList()
            if (fromIndex in mutable.indices && toIndex in mutable.indices) {
                val item = mutable.removeAt(fromIndex)
                mutable.add(toIndex, item)
            }
            
            // Maintain first shaker mapping on reorder
            current.copy(
                selectedOrder = mutable,
                firstShakerIndex = 0 // reset shaker back to seat index 0 to align with PRD
            )
        }
    }

    fun setFirstShaker(index: Int) {
        _uiState.update { current ->
            if (index !in current.selectedOrder.indices) return@update current
            
            // PRD: When first shaker is selected, seating order is rotated so the chosen shaker occupies seat index 0
            val list = current.selectedOrder
            val rotated = list.subList(index, list.size) + list.subList(0, index)
            
            current.copy(
                selectedOrder = rotated,
                firstShakerIndex = 0
            )
        }
    }

    fun goToStep(step: Int) {
        _uiState.update { it.copy(currentStep = step.coerceIn(0, 1)) }
    }

    fun startGame() {
        val state = _uiState.value
        val orderedPlayers = state.selectedOrder
        if (orderedPlayers.size != 4) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val domainPlayers = orderedPlayers.map { sp ->
                    val r = (sp.color.red * 255).toInt()
                    val g = (sp.color.green * 255).toInt()
                    val b = (sp.color.blue * 255).toInt()
                    val hexColor = String.format("#%02X%02X%02X", r, g, b)
                    app.piptally.domain.model.Player(
                        id = sp.playerId,
                        name = sp.name,
                        color = hexColor,
                        avatarIndex = sp.avatarIndex
                    )
                }
                val gameId = createGameUseCase(domainPlayers)
                _gameCreated.value = gameId
            }.onFailure {
                _uiState.update { s -> s.copy(isLoading = false) }
            }
        }
    }

    fun onGameCreatedConsumed() {
        _gameCreated.value = null
    }
}
