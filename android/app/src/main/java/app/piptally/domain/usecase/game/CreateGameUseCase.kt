package app.piptally.domain.usecase.game

import app.piptally.data.local.dao.GamePlayerDao
import app.piptally.data.local.entity.GamePlayerEntity
import app.piptally.domain.model.Game
import app.piptally.domain.model.GameStatus
import app.piptally.domain.model.Player
import app.piptally.domain.repository.GameRepository
import javax.inject.Inject

class CreateGameUseCase @Inject constructor(
    private val gameRepository: GameRepository,
    private val gamePlayerDao: GamePlayerDao
) {
    suspend operator fun invoke(players: List<Player>): Long {
        require(players.size in 2..8) { "A game requires 2 to 8 players" }

        val gameId = gameRepository.createGame(
            Game(
                status = GameStatus.ACTIVE,
                currentRoundIndex = 0,
                createdAt = System.currentTimeMillis()
            )
        )

        val gamePlayers = players.mapIndexed { index, player ->
            GamePlayerEntity(
                gameId = gameId,
                playerId = player.id,
                seatPosition = index,
                totalScore = 0,
                isWinner = false
            )
        }
        gamePlayerDao.insertGamePlayers(gamePlayers)

        return gameId
    }
}
