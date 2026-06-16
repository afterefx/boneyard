package app.boneyard.domain.usecase.game

import app.boneyard.domain.model.GameStatus
import app.boneyard.domain.repository.GameRepository
import dev.zacsweers.metro.Inject

class ResumeGameUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(gameId: Long) =
        gameRepository.updateStatus(gameId, GameStatus.ACTIVE)
}
