package app.piptally.domain.usecase.round

import app.piptally.domain.model.Round
import app.piptally.domain.repository.RoundRepository
import javax.inject.Inject

class GetCurrentRoundUseCase @Inject constructor(
    private val roundRepository: RoundRepository
) {
    suspend operator fun invoke(gameId: Long): Round? =
        roundRepository.getLatestRound(gameId)
}
