package app.piptally.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object PlayerList : Screen("player_list")
    object PlayerEdit : Screen("player_edit/{playerId}") {
        fun createRoute(playerId: Long = -1L) = "player_edit/$playerId"
    }
    object PlayerProfile : Screen("player_profile/{playerId}") {
        fun createRoute(playerId: Long) = "player_profile/$playerId"
    }
    object GameSetup : Screen("game_setup")
    object ActiveGame : Screen("active_game/{gameId}") {
        fun createRoute(gameId: Long) = "active_game/$gameId"
    }
    object GameSummary : Screen("game_summary/{gameId}") {
        fun createRoute(gameId: Long) = "game_summary/$gameId"
    }
    object GameHistory : Screen("game_history")
}
