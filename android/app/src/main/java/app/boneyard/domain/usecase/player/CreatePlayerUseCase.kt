package app.boneyard.domain.usecase.player

import app.boneyard.domain.model.Player
import app.boneyard.domain.repository.PlayerRepository
import dev.zacsweers.metro.Inject

sealed class CreatePlayerResult {
    data class Success(val playerId: Long) : CreatePlayerResult()
    object NameBlank : CreatePlayerResult()
    object NameTaken : CreatePlayerResult()
}

class CreatePlayerUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) {
    suspend operator fun invoke(player: Player): CreatePlayerResult {
        val trimmedName = player.name.trim()
        if (trimmedName.isBlank()) return CreatePlayerResult.NameBlank
        if (playerRepository.isNameTaken(trimmedName)) return CreatePlayerResult.NameTaken

        val id = playerRepository.createPlayer(player.copy(name = trimmedName))
        return CreatePlayerResult.Success(id)
    }
}
