package app.piptally.domain.model

enum class GameStatus {
    ACTIVE,
    PAUSED,
    COMPLETED,
    ABANDONED;

    companion object {
        fun fromString(value: String): GameStatus = when (value.uppercase()) {
            "ACTIVE" -> ACTIVE
            "PAUSED" -> PAUSED
            "COMPLETED" -> COMPLETED
            "ABANDONED" -> ABANDONED
            else -> ACTIVE
        }
    }
}
