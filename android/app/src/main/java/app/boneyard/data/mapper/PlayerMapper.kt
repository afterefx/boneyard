package app.boneyard.data.mapper

import app.boneyard.data.local.entity.PlayerEntity
import app.boneyard.domain.model.Player

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
