package app.piptally.domain.usecase.player

import app.piptally.domain.model.Player
import app.piptally.domain.repository.PlayerRepository
import javax.inject.Inject

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
