package app.boneyard.domain.usecase.round

import app.boneyard.data.local.dao.GamePlayerDao
import app.boneyard.domain.repository.GameRepository
import app.boneyard.domain.repository.RoundRepository
import app.boneyard.domain.repository.RoundScoreRepository
import dev.zacsweers.metro.Inject

class UndoLastRoundUseCase @Inject constructor(
    private val roundRepository: RoundRepository,
    private val roundScoreRepository: RoundScoreRepository,
    private val gameRepository: GameRepository,
    private val gamePlayerDao: GamePlayerDao
) {
    /**
     * Removes the last completed round and reverses the score changes.
     * @return true if undo was successful, false if there's nothing to undo
     */
    suspend operator fun invoke(gameId: Long): Boolean {
        val latestRound = roundRepository.getLatestRound(gameId) ?: return false

        // Reverse the scores for each player
        val scores = roundScoreRepository.getScoresForRoundOnce(latestRound.id)
        scores.forEach { roundScore ->
            gamePlayerDao.addToScore(gameId, roundScore.playerId, -roundScore.score)
        }

        // Delete scores then the round
        roundScoreRepository.deleteScoresForRound(latestRound.id)
        roundRepository.deleteRound(latestRound.id)

        // Step back the game's current round index
        val prevRoundIndex = latestRound.roundIndex
        gameRepository.updateCurrentRound(gameId, prevRoundIndex)

        return true
    }
}
