package app.boneyard.ui.game.setup

import android.graphics.Color as AndroidColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import app.boneyard.di.AppScope
import app.boneyard.domain.model.Player
import app.boneyard.domain.usecase.game.CreateGameUseCase
import app.boneyard.domain.usecase.player.GetAllPlayersUseCase
import app.boneyard.domain.usecase.template.CreateTemplateUseCase
import app.boneyard.ui.navigation.ActiveGameScreen
import app.boneyard.ui.navigation.GameSetupScreen
import app.boneyard.ui.navigation.PlayerEditScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.launch

class GameSetupPresenter @AssistedInject constructor(
    @Assisted private val screen: GameSetupScreen,
    @Assisted private val navigator: Navigator,
    private val getAllPlayersUseCase: GetAllPlayersUseCase,
    private val createGameUseCase: CreateGameUseCase,
    private val createTemplateUseCase: CreateTemplateUseCase,
) : Presenter<GameSetupUiState> {

    @CircuitInject(GameSetupScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(screen: GameSetupScreen, navigator: Navigator): GameSetupPresenter
    }

    @Composable
    override fun present(): GameSetupUiState {
        val scope = rememberCoroutineScope()
        var state by remember { mutableStateOf(GameSetupUiState()) }
        var initialized by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            getAllPlayersUseCase().collect { players ->
                val availablePlayers = players.map { player ->
                    val preselected = !initialized && screen.preselectedPlayerIds.isNotEmpty()
                    SelectablePlayer(
                        playerId = player.id,
                        name = player.name,
                        color = runCatching {
                            Color(AndroidColor.parseColor(player.color))
                        }.getOrDefault(Color(0xFF1E88E5)),
                        avatarIndex = player.avatarIndex,
                        isSelected = if (preselected) player.id in screen.preselectedPlayerIds
                                     else state.availablePlayers.find { it.playerId == player.id }?.isSelected ?: false
                    )
                }
                val selectedOrder = if (!initialized && screen.preselectedPlayerIds.isNotEmpty()) {
                    screen.preselectedPlayerIds.mapNotNull { id -> availablePlayers.find { it.playerId == id } }
                } else {
                    state.selectedOrder
                }
                val currentStep = if (!initialized && screen.preselectedPlayerIds.isNotEmpty()) {
                    screen.initialStep
                } else {
                    state.currentStep
                }
                initialized = true
                state = state.copy(
                    availablePlayers = availablePlayers,
                    selectedOrder = selectedOrder,
                    currentStep = currentStep,
                    isLoading = false
                )
            }
        }

        return state.copy(
            eventSink = { event ->
                when (event) {
                    GameSetupEvent.Back -> navigator.pop()
                    GameSetupEvent.CreatePlayer -> navigator.goTo(PlayerEditScreen())
                    is GameSetupEvent.TogglePlayer -> {
                        val current = state
                        val selected = current.availablePlayers.count { it.isSelected }
                        val target = current.availablePlayers.find { it.playerId == event.playerId }
                            ?: return@copy
                        if (!target.isSelected && selected >= 4) return@copy
                        val updated = current.availablePlayers.map { p ->
                            if (p.playerId == event.playerId) p.copy(isSelected = !p.isSelected) else p
                        }
                        val selectedOrder = updated.filter { it.isSelected }
                        state = current.copy(
                            availablePlayers = updated,
                            selectedOrder = selectedOrder,
                            firstShakerIndex = 0
                        )
                    }
                    is GameSetupEvent.ReorderPlayers -> {
                        val current = state
                        val mutable = current.selectedOrder.toMutableList()
                        if (event.fromIndex in mutable.indices && event.toIndex in mutable.indices) {
                            val item = mutable.removeAt(event.fromIndex)
                            mutable.add(event.toIndex, item)
                        }
                        state = current.copy(selectedOrder = mutable, firstShakerIndex = 0)
                    }
                    is GameSetupEvent.SetFirstShaker -> {
                        val current = state
                        if (event.index !in current.selectedOrder.indices) return@copy
                        val list = current.selectedOrder
                        val rotated = list.subList(event.index, list.size) + list.subList(0, event.index)
                        state = current.copy(selectedOrder = rotated, firstShakerIndex = 0)
                    }
                    is GameSetupEvent.GoToStep -> {
                        state = state.copy(currentStep = event.step.coerceIn(0, 1))
                    }
                    GameSetupEvent.StartGame -> {
                        val current = state
                        if (current.currentStep == 0 && current.canProceed) {
                            state = current.copy(currentStep = 1)
                        } else if (current.currentStep == 1 && current.canStartGame) {
                            scope.launch {
                                state = state.copy(isLoading = true)
                                runCatching {
                                    val domainPlayers = current.selectedOrder.map { sp -> sp.toDomainPlayer() }
                                    val gameId = createGameUseCase(domainPlayers)
                                    navigator.pop()
                                    navigator.goTo(ActiveGameScreen(gameId))
                                }.onFailure {
                                    state = state.copy(isLoading = false)
                                }
                            }
                        }
                    }
                    is GameSetupEvent.SaveTemplate -> {
                        val players = state.selectedOrder.map { it.toDomainPlayer() }
                        scope.launch { runCatching { createTemplateUseCase(event.name, players) } }
                    }
                }
            }
        )
    }

    private fun SelectablePlayer.toDomainPlayer(): Player {
        val r = (color.red * 255).toInt()
        val g = (color.green * 255).toInt()
        val b = (color.blue * 255).toInt()
        return Player(
            id = playerId,
            name = name,
            color = String.format("#%02X%02X%02X", r, g, b),
            avatarIndex = avatarIndex,
        )
    }
}
