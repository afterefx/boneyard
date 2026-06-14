package app.piptally.domain.usecase.game

import app.piptally.data.local.dao.GamePlayerDao
import app.piptally.data.local.dao.PlayerDao
import app.piptally.data.mapper.toDomain
import app.piptally.domain.model.GameWithPlayers
import app.piptally.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

class GetGameDetailsUseCase @Inject constructor(
    private val gameRepository: GameRepository,
    private val gamePlayerDao: GamePlayerDao,
    private val playerDao: PlayerDao
) {
    operator fun invoke(gameId: Long): Flow<GameWithPlayers> {
        return combine(
            gameRepository.getGameById(gameId).filterNotNull(),
            gamePlayerDao.getPlayersForGame(gameId),
            playerDao.getAllPlayers()
        ) { game, gamePlayers, allPlayers ->
            val playerMap = allPlayers.associateBy { it.id }
            val domainGamePlayers = gamePlayers.mapNotNull { gp ->
                val playerEntity = playerMap[gp.playerId] ?: return@mapNotNull null
                gp.toDomain(playerEntity.toDomain())
            }
            GameWithPlayers(game = game, players = domainGamePlayers)
        }
    }
}
