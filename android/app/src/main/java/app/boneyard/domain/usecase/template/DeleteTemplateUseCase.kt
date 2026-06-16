package app.boneyard.domain.usecase.template

import app.boneyard.domain.repository.TemplateRepository
import dev.zacsweers.metro.Inject

class DeleteTemplateUseCase @Inject constructor(
    private val templateRepository: TemplateRepository,
) {
    suspend operator fun invoke(templateId: Long) =
        templateRepository.deleteTemplate(templateId)
}
