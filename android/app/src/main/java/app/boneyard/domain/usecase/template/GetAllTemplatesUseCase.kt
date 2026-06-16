package app.boneyard.domain.usecase.template

import app.boneyard.domain.model.Template
import app.boneyard.domain.repository.TemplateRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

class GetAllTemplatesUseCase @Inject constructor(
    private val templateRepository: TemplateRepository,
) {
    operator fun invoke(): Flow<List<Template>> = templateRepository.getAllTemplates()
}
