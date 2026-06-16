package app.boneyard.domain.usecase.round

import app.boneyard.data.local.dao.GamePlayerDao
import app.boneyard.domain.model.Round
import app.boneyard.domain.model.RoundScore
import app.boneyard.domain.repository.GameRepository
import app.boneyard.domain.repository.RoundRepository
import app.boneyard.domain.repository.RoundScoreRepository
import app.boneyard.util.GameConstants
import dev.zacsweers.metro.Inject

/**
 * Submits scores for the current round and advances the game to the next round.
 * If all rounds are complete (14 total), determines the winner and completes the game.
 *
 * @return true if the game is now complete, false if there are more rounds
 */
class SubmitRoundScoresUseCase @Inject constructor(
    private val roundRepository: RoundRepository,
    private val roundScoreRepository: RoundScoreRepository,
    private val gameRepository: GameRepository,
    private val gamePlayerDao: GamePlayerDao
) {
    suspend operator fun invoke(
        gameId: Long,
        currentRoundIndex: Int,
        scores: Map<Long, Int>  // playerId -> score for this round
    ): Boolean {
        val players = gamePlayerDao.getPlayersForGameOnce(gameId)
            .sortedBy { it.seatPosition }

        val shakerIndex = GameConstants.getShakerIndex(currentRoundIndex, players.size)
        val shakerPlayerId = players[shakerIndex].playerId
        val spinnerValue = GameConstants.ROUND_SPINNER_SEQUENCE[currentRoundIndex]

        // Insert the round record
        val roundId = roundRepository.createRound(
            Round(
                gameId = gameId,
                roundIndex = currentRoundIndex,
                spinnerValue = spinnerValue,
                shakerPlayerId = shakerPlayerId,
                completedAt = System.currentTimeMillis()
            )
        )

        // Insert individual scores
        val roundScores = scores.map { (playerId, score) ->
            RoundScore(roundId = roundId, playerId = playerId, score = score)
        }
        roundScoreRepository.saveScores(roundScores)

        // Update cumulative totals for each player
        scores.forEach { (playerId, score) ->
            gamePlayerDao.addToScore(gameId, playerId, score)
        }

        val nextRoundIndex = currentRoundIndex + 1
        val isGameComplete = nextRoundIndex >= GameConstants.ROUND_SPINNER_SEQUENCE.size

        if (isGameComplete) {
            // The winner is the player with the lowest total score
            val updatedPlayers = gamePlayerDao.getPlayersForGameOnce(gameId)
            val winnerPlayerId = updatedPlayers.minByOrNull { it.totalScore }?.playerId
                ?: players.first().playerId
            gameRepository.completeGame(gameId, winnerPlayerId)
        } else {
            gameRepository.updateCurrentRound(gameId, nextRoundIndex)
        }

        return isGameComplete
    }
}
