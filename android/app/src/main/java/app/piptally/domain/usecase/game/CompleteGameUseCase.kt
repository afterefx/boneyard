package app.piptally.domain.usecase.game

import app.piptally.domain.repository.GameRepository
import javax.inject.Inject

class CompleteGameUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(gameId: Long, winnerPlayerId: Long) =
        gameRepository.completeGame(gameId, winnerPlayerId)
}
