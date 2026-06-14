package app.piptally.domain.usecase.game

import app.piptally.domain.model.GameStatus
import app.piptally.domain.repository.GameRepository
import javax.inject.Inject

class PauseGameUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(gameId: Long) =
        gameRepository.updateStatus(gameId, GameStatus.PAUSED)
}
