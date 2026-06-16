package app.boneyard.domain.usecase.player

import app.boneyard.domain.model.Player
import app.boneyard.domain.repository.PlayerRepository
import dev.zacsweers.metro.Inject

sealed class UpdatePlayerResult {
    object Success : UpdatePlayerResult()
    object NameBlank : UpdatePlayerResult()
    object NameTaken : UpdatePlayerResult()
}

class UpdatePlayerUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) {
    suspend operator fun invoke(player: Player): UpdatePlayerResult {
        val trimmedName = player.name.trim()
        if (trimmedName.isBlank()) return UpdatePlayerResult.NameBlank
        if (playerRepository.isNameTaken(trimmedName, excludeId = player.id)) return UpdatePlayerResult.NameTaken

        playerRepository.updatePlayer(player.copy(name = trimmedName))
        return UpdatePlayerResult.Success
    }
}
