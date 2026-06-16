package app.boneyard.domain.usecase.game

import app.boneyard.domain.repository.GameRepository
import dev.zacsweers.metro.Inject

class CompleteGameUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(gameId: Long, winnerPlayerId: Long) =
        gameRepository.completeGame(gameId, winnerPlayerId)
}
