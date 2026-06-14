package app.piptally.domain.usecase.game

import app.piptally.domain.model.Game
import app.piptally.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveGamesUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    operator fun invoke(): Flow<List<Game>> = gameRepository.getActiveGames()
}
