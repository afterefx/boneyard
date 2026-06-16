package app.boneyard.domain.usecase.game

import app.boneyard.data.local.dao.GamePlayerDao
import app.boneyard.data.local.entity.GamePlayerEntity
import app.boneyard.domain.model.Game
import app.boneyard.domain.model.GameStatus
import app.boneyard.domain.model.Player
import app.boneyard.domain.repository.GameRepository
import dev.zacsweers.metro.Inject

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
