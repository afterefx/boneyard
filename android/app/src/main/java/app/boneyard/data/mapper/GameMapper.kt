package app.boneyard.data.mapper

import app.boneyard.data.local.entity.GameEntity
import app.boneyard.data.local.entity.GamePlayerEntity
import app.boneyard.domain.model.Game
import app.boneyard.domain.model.GamePlayer
import app.boneyard.domain.model.GameStatus
import app.boneyard.domain.model.Player

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
