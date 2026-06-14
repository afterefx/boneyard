import SwiftUI
import SwiftData

@main
struct PipTallyApp: App {
    var body: some Scene {
        WindowGroup {
            HomeView()
        }
        .modelContainer(for: [
            Player.self,
            Game.self,
            GamePlayer.self,
            Round.self,
            RoundScore.self
        ])
    }
}
