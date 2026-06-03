package com.domino.scoretracker.domain.usecase.round

import com.domino.scoretracker.data.local.dao.GamePlayerDao
import com.domino.scoretracker.data.local.entity.GamePlayerEntity
import com.domino.scoretracker.domain.repository.GameRepository
import com.domino.scoretracker.domain.repository.RoundRepository
import com.domino.scoretracker.domain.repository.RoundScoreRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SubmitRoundScoresUseCaseTest {

    private lateinit var roundRepository: RoundRepository
    private lateinit var roundScoreRepository: RoundScoreRepository
    private lateinit var gameRepository: GameRepository
    private lateinit var gamePlayerDao: GamePlayerDao
    private lateinit var useCase: SubmitRoundScoresUseCase

    @Before
    fun setup() {
        roundRepository = mockk()
        roundScoreRepository = mockk(relaxed = true)
        gameRepository = mockk(relaxed = true)
        gamePlayerDao = mockk(relaxed = true)
        useCase = SubmitRoundScoresUseCase(
            roundRepository,
            roundScoreRepository,
            gameRepository,
            gamePlayerDao
        )
    }

    @Test
    fun `invoke should create round, save scores, and update current round when game not complete`() = runBlocking {
        // Given
        val gameId = 1L
        val currentRoundIndex = 0
        val scores = mapOf(101L to 5, 102L to 10)
        val players = listOf(
            createMockGamePlayerEntity(gameId, 101L, 0),
            createMockGamePlayerEntity(gameId, 102L, 1)
        )

        coEvery { gamePlayerDao.getPlayersForGameOnce(gameId) } returns players
        coEvery { roundRepository.createRound(any()) } returns 50L

        // When
        val isComplete = useCase(gameId, currentRoundIndex, scores)

        // Then
        assertFalse(isComplete)
        coVerify { roundRepository.createRound(match { it.roundIndex == currentRoundIndex }) }
        coVerify { roundScoreRepository.saveScores(any()) }
        coVerify { gameRepository.updateCurrentRound(gameId, currentRoundIndex + 1) }
    }

    @Test
    fun `invoke should complete game when it is the last round`() = runBlocking {
        // Given
        val gameId = 1L
        val currentRoundIndex = 13 // Last round (0-13)
        val scores = mapOf(101L to 5, 102L to 10)
        val players = listOf(
            createMockGamePlayerEntity(gameId, 101L, 0, totalScore = 50),
            createMockGamePlayerEntity(gameId, 102L, 1, totalScore = 40)
        )

        coEvery { gamePlayerDao.getPlayersForGameOnce(gameId) } returns players
        coEvery { roundRepository.createRound(any()) } returns 50L

        // When
        val isComplete = useCase(gameId, currentRoundIndex, scores)

        // Then
        assertTrue(isComplete)
        coVerify { gameRepository.completeGame(gameId, 102L) } // Player 102 has lowest score
    }

    private fun createMockGamePlayerEntity(gameId: Long, playerId: Long, position: Int, totalScore: Int = 0): GamePlayerEntity {
        return GamePlayerEntity(
            gameId = gameId,
            playerId = playerId,
            seatPosition = position,
            totalScore = totalScore
        )
    }
}
