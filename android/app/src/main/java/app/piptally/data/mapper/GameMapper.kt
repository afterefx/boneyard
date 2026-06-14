package app.piptally.data.mapper

import app.piptally.data.local.entity.GameEntity
import app.piptally.data.local.entity.GamePlayerEntity
import app.piptally.domain.model.Game
import app.piptally.domain.model.GamePlayer
import app.piptally.domain.model.GameStatus
import app.piptally.domain.model.Player

fun GameEntity.toDomain(): Game = Game(
    id = id,
    status = GameStatus.fromString(status),
    currentRoundIndex = currentRoundIndex,
    createdAt = createdAt,
    completedAt = completedAt,
    winnerPlayerId = winnerPlayerId
)

fun Game.toEntity(): GameEntity = GameEntity(
    id = id,
    status = status.name,
    currentRoundIndex = currentRoundIndex,
    createdAt = createdAt,
    completedAt = completedAt,
    winnerPlayerId = winnerPlayerId
)

fun GamePlayerEntity.toDomain(player: Player): GamePlayer = GamePlayer(
    gameId = gameId,
    player = player,
    seatPosition = seatPosition,
    totalScore = totalScore,
    isWinner = isWinner
)
