package app.piptally.data.mapper

import app.piptally.data.local.entity.PlayerEntity
import app.piptally.domain.model.Player

fun PlayerEntity.toDomain(): Player = Player(
    id = id,
    name = name,
    color = color,
    avatarIndex = avatarIndex,
    createdAt = createdAt
)

fun Player.toEntity(): PlayerEntity = PlayerEntity(
    id = id,
    name = name,
    color = color,
    avatarIndex = avatarIndex,
    createdAt = createdAt
)
