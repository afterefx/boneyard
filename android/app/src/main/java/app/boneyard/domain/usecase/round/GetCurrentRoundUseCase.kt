package app.boneyard.domain.usecase.round

import app.boneyard.domain.model.Round
import app.boneyard.domain.repository.RoundRepository
import dev.zacsweers.metro.Inject

class GetCurrentRoundUseCase @Inject constructor(
    private val roundRepository: RoundRepository
) {
    suspend operator fun invoke(gameId: Long): Round? =
        roundRepository.getLatestRound(gameId)
}
