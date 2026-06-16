package app.boneyard.ui.player.edit

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
import app.boneyard.domain.repository.PlayerRepository
import app.boneyard.domain.usecase.player.CreatePlayerResult
import app.boneyard.domain.usecase.player.CreatePlayerUseCase
import app.boneyard.domain.usecase.player.UpdatePlayerResult
import app.boneyard.domain.usecase.player.UpdatePlayerUseCase
import app.boneyard.ui.navigation.PlayerEditScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import kotlinx.coroutines.launch
import dev.zacsweers.metro.AssistedInject

class PlayerEditPresenter @AssistedInject constructor(
    @Assisted private val screen: PlayerEditScreen,
    @Assisted private val navigator: Navigator,
    private val playerRepository: PlayerRepository,
    private val createPlayerUseCase: CreatePlayerUseCase,
    private val updatePlayerUseCase: UpdatePlayerUseCase,
) : Presenter<PlayerEditUiState> {

    @CircuitInject(PlayerEditScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(screen: PlayerEditScreen, navigator: Navigator): PlayerEditPresenter
    }

    @Composable
    override fun present(): PlayerEditUiState {
        val scope = rememberCoroutineScope()
        val playerId = screen.playerId
        var state by remember { mutableStateOf(PlayerEditUiState()) }

        LaunchedEffect(playerId) {
            if (playerId != -1L) {
                val player = playerRepository.getPlayerById(playerId) ?: return@LaunchedEffect
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
                state = state.copy(
                    playerId = playerId,
                    name = player.name,
                    selectedColorIndex = colorIndex,
                    selectedAvatarIndex = player.avatarIndex.coerceIn(0, AVATAR_EMOJI_OPTIONS.lastIndex),
                )
            }
        }

        return state.copy(
            eventSink = { event ->
                when (event) {
                    PlayerEditEvent.Back -> navigator.pop()
                    is PlayerEditEvent.NameChange -> state = state.copy(name = event.name, nameError = null)
                    is PlayerEditEvent.ColorSelect -> state = state.copy(selectedColorIndex = event.index)
                    is PlayerEditEvent.AvatarSelect -> state = state.copy(selectedAvatarIndex = event.index)
                    PlayerEditEvent.Save -> {
                        val current = state
                        val hexColor = colorToHex(PLAYER_COLOR_OPTIONS[current.selectedColorIndex])
                        scope.launch {
                            state = state.copy(isSaving = true, nameError = null)
                            val player = Player(
                                id = current.playerId ?: 0L,
                                name = current.name,
                                color = hexColor,
                                avatarIndex = current.selectedAvatarIndex,
                            )
                            val error: String? = if (current.isNewPlayer) {
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
                                navigator.pop()
                            } else {
                                state = state.copy(isSaving = false, nameError = error)
                            }
                        }
                    }
                }
            }
        )
    }

    private fun colorToHex(color: Color): String {
        val r = (color.red * 255).toInt()
        val g = (color.green * 255).toInt()
        val b = (color.blue * 255).toInt()
        return String.format("#%02X%02X%02X", r, g, b)
    }
}
