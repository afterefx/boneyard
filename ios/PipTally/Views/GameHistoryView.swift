import SwiftUI
import SwiftData

struct GameHistoryView: View {
    @Environment(\.modelContext) private var modelContext
    @Query(sort: \Game.createdAt, order: .reverse) private var games: [Game]
    
    var body: some View {
        let completedGames = games.filter { $0.status == .completed || $0.status == .abandoned }
        
        Group {
            if completedGames.isEmpty {
                VStack(spacing: 12) {
                    Image(systemName: "clock.arrow.circlepath")
                        .font(.system(size: 48))
                        .foregroundColor(.secondary.opacity(0.5))
                        .padding(.bottom, 8)
                    
                    Text("No Game History")
                        .font(.system(size: 17, weight: .semibold))
                        .foregroundColor(.secondary)
                    
                    Text("Completed games will appear here after they are finished.")
                        .font(.system(size: 13))
                        .foregroundColor(.secondary.opacity(0.7))
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(Color.appBackground)
            } else {
                List {
                    ForEach(completedGames) { game in
                        NavigationLink(destination: GameSummaryView(gameId: game.id)) {
                            HStack(alignment: .center, spacing: 12) {
                                // Match Avatar based on status
                                if game.status == .abandoned {
                                    Image(systemName: "xmark.octagon.fill")
                                        .font(.system(size: 20))
                                        .foregroundColor(.orange)
                                        .frame(width: 48, height: 48)
                                        .background(Color.orange.opacity(0.15))
                                        .clipShape(Circle())
                                } else if let winnerGp = game.gamePlayers.first(where: { $0.player?.id == game.winnerPlayerId }),
                                   let winner = winnerGp.player {
                                    PlayerAvatar(
                                        name: winner.name,
                                        colorHex: winner.colorHex,
                                        avatarIndex: winner.avatarIndex,
                                        size: .medium
                                    )
                                } else {
                                    Image(systemName: "trophy.fill")
                                        .font(.system(size: 20))
                                        .foregroundColor(.secondary)
                                        .frame(width: 48, height: 48)
                                        .background(Color.appGray)
                                        .clipShape(Circle())
                                }
                                
                                VStack(alignment: .leading, spacing: 4) {
                                    // Player names
                                    let names = game.gamePlayers
                                        .sorted(by: { $0.seatPosition < $1.seatPosition })
                                        .compactMap { $0.player?.name }
                                    
                                    Text(names.joined(separator: " · "))
                                        .font(.system(size: 15, weight: .medium))
                                        .foregroundColor(.primary)
                                        .lineLimit(1)
                                    
                                    let compDate = game.completedAt ?? game.createdAt
                                    let statusLabel = game.status == .abandoned ? "Abandoned" : "Finished"
                                    Text("\(statusLabel) \(compDate, style: .date)")
                                        .font(.system(size: 12))
                                        .foregroundColor(.secondary)
                                }
                                
                                Spacer()
                                
                                if game.status == .abandoned {
                                    VStack(alignment: .trailing, spacing: 2) {
                                        Text("Incomplete")
                                            .font(.system(size: 13, weight: .bold))
                                            .foregroundColor(.orange)
                                        
                                        Text("Round \(game.currentRoundIndex + 1)/14")
                                            .font(.system(size: 11))
                                            .foregroundColor(.secondary)
                                    }
                                } else if let winnerGp = game.gamePlayers.first(where: { $0.player?.id == game.winnerPlayerId }) {
                                    VStack(alignment: .trailing, spacing: 2) {
                                        Text("\(winnerGp.totalScore) pts")
                                            .font(.system(size: 15, weight: .bold))
                                            .foregroundColor(.accentColor)
                                        
                                        Text("Winner")
                                            .font(.system(size: 11, weight: .semibold))
                                            .foregroundColor(.green)
                                    }
                                }
                            }
                            .padding(.vertical, 4)
                        }
                    }
                    .onDelete(perform: deleteHistory)
                }
            }
        }
        .navigationTitle("History")
        .appNavigationBarTitleDisplayMode()
    }
    
    private func deleteHistory(at offsets: IndexSet) {
        let completedGames = games.filter { $0.status == .completed || $0.status == .abandoned }
        for index in offsets {
            let game = completedGames[index]
            modelContext.delete(game)
        }
        try? modelContext.save()
    }
}
