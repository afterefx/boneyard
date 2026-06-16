package app.boneyard.domain.model

data class Round(
    val id: Long = 0,
    val gameId: Long,
    val roundIndex: Int,
    val spinnerValue: Int,
    val shakerPlayerId: Long,
    val completedAt: Long? = null
) {
    val isCompleted: Boolean get() = completedAt != null
}
