package app.boneyard.domain.usecase.game

import app.boneyard.domain.model.Game
import app.boneyard.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import dev.zacsweers.metro.Inject

class GetActiveGamesUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    operator fun invoke(): Flow<List<Game>> = gameRepository.getActiveGames()
}
