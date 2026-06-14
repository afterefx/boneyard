import SwiftUI
import SwiftData

struct PlayerProfileView: View {
    @Environment(\.modelContext) private var modelContext
    let player: Player
    
    @Query(sort: \Game.createdAt, order: .reverse) private var allGames: [Game]
    @State private var showingEditSheet = false
    
    var body: some View {
        let stats = calculateStats()
        let playerGames = getCompletedGamesForPlayer()
        
        ScrollView {
            VStack(spacing: 24) {
                // Profile Header
                VStack(spacing: 12) {
                    PlayerAvatar(
                        name: player.name,
                        colorHex: player.colorHex,
                        avatarIndex: player.avatarIndex,
                        size: .xlarge
                    )
                    
                    Text(player.name)
                        .font(.system(size: 24, weight: .bold))
                        .foregroundColor(.primary)
                    
                    Button(action: {
                        showingEditSheet = true
                    }) {
                        Label("Edit Profile", systemImage: "pencil")
                            .font(.system(size: 15, weight: .semibold))
                            .foregroundColor(.accentColor)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 8)
                            .background(Color.accentColor.opacity(0.15))
                            .cornerRadius(12)
                    }
                }
                .padding(.top, 24)
                
                // Statistics Grid
                VStack(alignment: .leading, spacing: 12) {
                    Text("Lifetime Stats")
                        .font(.system(size: 17, weight: .semibold))
                        .foregroundColor(.primary)
                        .padding(.horizontal, 8)
                    
                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                        StatCard(title: "Games Played", value: "\(stats.played)", icon: "gamecontroller")
                        StatCard(title: "Games Won", value: "\(stats.won)", icon: "trophy")
                        StatCard(title: "Win Rate", value: String(format: "%.1f%%", stats.winRate * 100), icon: "percent")
                        StatCard(title: "Avg. Score", value: stats.played > 0 ? String(format: "%.1f", stats.avgScore) : "N/A", icon: "chart.bar")
                        StatCard(title: "Best Score", value: stats.played > 0 ? "\(stats.bestScore)" : "N/A", icon: "arrow.down.circle")
                    }
                }
                .padding(.horizontal, 16)
                
                // Recent Games Section
                VStack(alignment: .leading, spacing: 12) {
                    Text("Recent Games")
                        .font(.system(size: 17, weight: .semibold))
                        .foregroundColor(.primary)
                        .padding(.horizontal, 24)
                    
                    if playerGames.isEmpty {
                        VStack(spacing: 8) {
                            Image(systemName: "calendar.badge.exclamationmark")
                                .font(.system(size: 32))
                                .foregroundColor(.secondary.opacity(0.5))
                                .padding(.bottom, 4)
                            
                            Text("No Games Played")
                                .font(.system(size: 14, weight: .semibold))
                                .foregroundColor(.secondary)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 32)
                        .background(Color.appSurface)
                        .cornerRadius(16)
                        .padding(.horizontal, 16)
                    } else {
                        ForEach(playerGames) { gameRecord in
                            HStack {
                                VStack(alignment: .leading, spacing: 4) {
                                    Text(gameRecord.opponentNames)
                                        .font(.system(size: 15, weight: .medium))
                                        .foregroundColor(.primary)
                                    
                                    Text(gameRecord.date, style: .date)
                                        .font(.system(size: 12))
                                        .foregroundColor(.secondary)
                                }
                                
                                Spacer()
                                
                                VStack(alignment: .trailing, spacing: 4) {
                                    Text("\(gameRecord.playerScore) pts")
                                        .font(.system(size: 15, weight: .bold))
                                        .foregroundColor(.primary)
                                    
                                    if gameRecord.isWinner {
                                        Text("Won")
                                            .font(.system(size: 12, weight: .semibold))
                                            .foregroundColor(.green)
                                    } else {
                                        Text("Lost")
                                            .font(.system(size: 12))
                                            .foregroundColor(.secondary)
                                    }
                                }
                            }
                            .padding(.horizontal, 16)
                            .padding(.vertical, 14)
                            .background(Color.appSurface)
                            .cornerRadius(12)
                            .shadow(color: Color.black.opacity(0.01), radius: 3, x: 0, y: 1)
                            .padding(.horizontal, 16)
                        }
                    }
                }
            }
            .padding(.bottom, 32)
        }
        .background(Color.appBackground)
        .navigationTitle(player.name)
        .appNavigationBarTitleDisplayMode()
        .sheet(isPresented: $showingEditSheet) {
            PlayerEditView(playerToEdit: player)
        }
    }
    
    // Stats calculation logic
    private struct ComputedStats {
        let played: Int
        let won: Int
        let winRate: Double
        let avgScore: Double
        let bestScore: Int
    }
    
    private func calculateStats() -> ComputedStats {
        let completedGames = allGames.filter { $0.status == .completed }
        var playedCount = 0
        var wonCount = 0
        var totalScoreSum = 0
        var bestScoreValue = Int.max
        
        for game in completedGames {
            if let gamePlayer = game.gamePlayers.first(where: { $0.player?.id == player.id }) {
                playedCount += 1
                totalScoreSum += gamePlayer.totalScore
                if gamePlayer.totalScore < bestScoreValue {
                    bestScoreValue = gamePlayer.totalScore
                }
                
                if game.winnerPlayerId == player.id {
                    wonCount += 1
                }
            }
        }
        
        let winRateVal = playedCount > 0 ? Double(wonCount) / Double(playedCount) : 0.0
        let avgScoreVal = playedCount > 0 ? Double(totalScoreSum) / Double(playedCount) : 0.0
        
        return ComputedStats(
            played: playedCount,
            won: wonCount,
            winRate: winRateVal,
            avgScore: avgScoreVal,
            bestScore: bestScoreValue == Int.max ? 0 : bestScoreValue
        )
    }
    
    private struct PlayerGameRecord: Identifiable {
        var id: UUID { gameId }
        let gameId: UUID
        let opponentNames: String
        let playerScore: Int
        let isWinner: Bool
        let date: Date
    }
    
    private func getCompletedGamesForPlayer() -> [PlayerGameRecord] {
        let completedGames = allGames.filter { $0.status == .completed }
        var records: [PlayerGameRecord] = []
        
        for game in completedGames {
            if let gamePlayer = game.gamePlayers.first(where: { $0.player?.id == player.id }) {
                let opponents = game.gamePlayers
                    .sorted(by: { $0.seatPosition < $1.seatPosition })
                    .filter { $0.player?.id != player.id }
                    .compactMap { $0.player?.name }
                
                records.append(
                    PlayerGameRecord(
                        gameId: game.id,
                        opponentNames: opponents.joined(separator: " · "),
                        playerScore: gamePlayer.totalScore,
                        isWinner: game.winnerPlayerId == player.id,
                        date: game.completedAt ?? game.createdAt
                    )
                )
            }
        }
        
        return records
    }
}

struct StatCard: View {
    let title: String
    let value: String
    let icon: String
    
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 20))
                .foregroundColor(.accentColor)
                .frame(width: 32, height: 32)
                .background(Color.accentColor.opacity(0.15))
                .clipShape(RoundedRectangle(cornerRadius: 8))
            
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.system(size: 12))
                    .foregroundColor(.secondary)
                
                Text(value)
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.primary)
            }
            Spacer()
        }
        .padding(14)
        .background(Color.appSurface)
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.02), radius: 3, x: 0, y: 1)
    }
}
