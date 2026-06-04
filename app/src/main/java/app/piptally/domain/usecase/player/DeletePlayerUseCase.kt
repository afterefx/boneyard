package app.piptally.domain.usecase.player

import app.piptally.domain.model.Player
import app.piptally.domain.repository.PlayerRepository
import javax.inject.Inject

class DeletePlayerUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) {
    suspend operator fun invoke(player: Player) = playerRepository.deletePlayer(player)
}
