package app.boneyard.domain.usecase.player

import app.boneyard.domain.model.Player
import app.boneyard.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import dev.zacsweers.metro.Inject

class GetAllPlayersUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) {
    operator fun invoke(): Flow<List<Player>> = playerRepository.getAllPlayers()
}
