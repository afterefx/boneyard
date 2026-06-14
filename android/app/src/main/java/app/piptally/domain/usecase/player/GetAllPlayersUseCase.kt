package app.piptally.domain.usecase.player

import app.piptally.domain.model.Player
import app.piptally.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllPlayersUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) {
    operator fun invoke(): Flow<List<Player>> = playerRepository.getAllPlayers()
}
