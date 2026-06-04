package app.piptally.data.mapper

import app.piptally.data.local.entity.RoundEntity
import app.piptally.data.local.entity.RoundScoreEntity
import app.piptally.domain.model.Round
import app.piptally.domain.model.RoundScore

fun RoundEntity.toDomain(): Round = Round(
    id = id,
    gameId = gameId,
    roundIndex = roundIndex,
    spinnerValue = spinnerValue,
    shakerPlayerId = shakerPlayerId,
    completedAt = completedAt
)

fun Round.toEntity(): RoundEntity = RoundEntity(
    id = id,
    gameId = gameId,
    roundIndex = roundIndex,
    spinnerValue = spinnerValue,
    shakerPlayerId = shakerPlayerId,
    completedAt = completedAt
)

fun RoundScoreEntity.toDomain(): RoundScore = RoundScore(
    roundId = roundId,
    playerId = playerId,
    score = score
)

fun RoundScore.toEntity(): RoundScoreEntity = RoundScoreEntity(
    roundId = roundId,
    playerId = playerId,
    score = score
)
