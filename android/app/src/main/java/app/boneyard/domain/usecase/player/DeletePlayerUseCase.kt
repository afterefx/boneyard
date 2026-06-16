package app.boneyard.domain.usecase.player

import app.boneyard.domain.model.Player
import app.boneyard.domain.repository.PlayerRepository
import dev.zacsweers.metro.Inject

class DeletePlayerUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) {
    suspend operator fun invoke(player: Player) = playerRepository.deletePlayer(player)
}
