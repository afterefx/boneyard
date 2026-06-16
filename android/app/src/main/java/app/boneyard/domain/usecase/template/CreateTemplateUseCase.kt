package app.boneyard.domain.usecase.template

import app.boneyard.domain.model.Player
import app.boneyard.domain.repository.TemplateRepository
import dev.zacsweers.metro.Inject

class CreateTemplateUseCase @Inject constructor(
    private val templateRepository: TemplateRepository,
) {
    suspend operator fun invoke(name: String, players: List<Player>): Long =
        templateRepository.createTemplate(name, players)
}
