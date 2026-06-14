import SwiftUI
import SwiftData

struct HomeView: View {
    @Environment(\.modelContext) private var modelContext
    @Query(sort: \Game.createdAt, order: .reverse) private var games: [Game]
    @Query(sort: \Player.name) private var players: [Player]
    
    @AppStorage("appTheme") private var appTheme = 0
    @State private var showSettings = false
    @State private var showingSetup = false
    @State private var pendingGameId: UUID? = nil
    // HomeView owns the NavigationStack so we can push ActiveGameView explicitly
    // onto navPath — eliminating any ambiguity about which stack receives the push.
    @State private var navPath = NavigationPath()
    @Environment(\.colorScheme) private var colorScheme
    
    private var isDarkMode: Bool {
        let theme = AppTheme(rawValue: appTheme) ?? .system
        switch theme {
        case .system:
            return colorScheme == .dark
        case .pureBlack, .deepGray:
            return true
        }
    }
    
    var body: some View {
        NavigationStack(path: $navPath) {
        ScrollView {
            VStack(spacing: 24) {
                // Header - App Branding
                VStack(spacing: 12) {
                    if isDarkMode {
                        DominoTile(
                            topValue: 2,
                            bottomValue: 1,
                            tileWidth: 48,
                            isVertical: true,
                            backgroundColor: Color(hex: "#F5F0E8"),
                            pipColor: Color(hex: "#1A1A1A"),
                            dividerColor: Color(hex: "#D0C9C0")
                        )
                        .rotationEffect(.degrees(15))
                        .shadow(color: .black.opacity(0.35), radius: 6, x: 3, y: 5)
                        .padding(.bottom, 16)
                    } else {
                        DominoTile(topValue: 2, bottomValue: 1, tileWidth: 48, isVertical: true)
                            .rotationEffect(.degrees(15))
                            .shadow(color: .black.opacity(0.35), radius: 6, x: 3, y: 5)
                            .padding(.bottom, 16)
                    }
                    
                    Text("PipTally")
                        .font(.system(size: 28, weight: .bold))
                        .foregroundColor(.primary)
                    
                    Text("Domino Scorer")
                        .font(.system(size: 15))
                        .foregroundColor(.secondary)
                }
                .padding(.top, 40)
                .padding(.bottom, 24)
                
                // Primary CTA Card
                VStack(alignment: .leading, spacing: 12) {
                    HStack(alignment: .center) {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Ready to play?")
                                .font(.system(size: 17, weight: .semibold))
                                .foregroundColor(.primary)
                            
                            Text("Start a new 14-round game for 4 players.")
                                .font(.system(size: 13))
                                .foregroundColor(.secondary)
                        }
                        
                        Button(action: {
                            showingSetup = true
                        }) {
                            HStack(spacing: 6) {
                                Image(systemName: "dice.fill")
                                    .font(.system(size: 14))
                                Text("New Game")
                                    .font(.system(size: 15, weight: .bold))
                            }
                            .foregroundColor(.white)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 10)
                            .background(Color.accentColor)
                            .cornerRadius(20)
                        }
                    }
                    .padding(20)
                }
                .background(Color.appSurface)
                .cornerRadius(16)
                .shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 2)
                .padding(.horizontal, 16)
                
                // Secondary Actions Row
                HStack(spacing: 12) {
                    NavigationLink(destination: PlayerListView()) {
                        HStack(spacing: 6) {
                            Image(systemName: "person.2.fill")
                            Text("Players")
                        }
                        .font(.system(size: 15, weight: .semibold))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(Color.clear)
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color.accentColor, lineWidth: 1)
                        )
                    }
                    
                    NavigationLink(destination: GameHistoryView()) {
                        HStack(spacing: 6) {
                            Image(systemName: "clock.arrow.circlepath")
                            Text("History")
                        }
                        .font(.system(size: 15, weight: .semibold))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(Color.clear)
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color.accentColor, lineWidth: 1)
                        )
                    }
                }
                .padding(.horizontal, 16)
                
                // Active Games Section
                VStack(alignment: .leading, spacing: 12) {
                    HStack(alignment: .center) {
                        Text("Active Games")
                            .font(.system(size: 17, weight: .semibold))
                            .foregroundColor(.primary)
                        
                        let activeCount = activeGames.count
                        if activeCount > 0 {
                            Text("\(activeCount)")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(.accentColor)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 2)
                                .background(Color.accentColor.opacity(0.15))
                                .cornerRadius(8)
                        }
                    }
                    .padding(.horizontal, 24)
                    .padding(.top, 16)
                    
                    let activePausedGames = activeGames
                    if !activePausedGames.isEmpty {
                        ForEach(activePausedGames) { game in
                            HStack {
                                VStack(alignment: .leading, spacing: 4) {
                                    // Player names
                                    let names = game.gamePlayers
                                        .sorted(by: { $0.seatPosition < $1.seatPosition })
                                        .compactMap { $0.player?.name }
                                    
                                    Text(names.joined(separator: " · "))
                                        .font(.system(size: 15, weight: .medium))
                                        .foregroundColor(.primary)
                                    
                                    Text("Round \(game.currentRoundIndex + 1) of 14")
                                        .font(.system(size: 13))
                                        .foregroundColor(.secondary)
                                }
                                
                                Spacer()
                                
                                Button(action: { navPath.append(game.id) }) {
                                    Text("Resume")
                                        .font(.system(size: 15, weight: .semibold))
                                        .foregroundColor(.primary)
                                        .padding(.horizontal, 16)
                                        .padding(.vertical, 6)
                                        .background(Color.accentColor.opacity(0.15))
                                        .cornerRadius(12)
                                }
                                .buttonStyle(.plain)
                            }
                            .padding(.horizontal, 16)
                            .padding(.vertical, 14)
                            .background(Color.appSurface)
                            .cornerRadius(12)
                            .shadow(color: Color.black.opacity(0.02), radius: 3, x: 0, y: 1)
                            .padding(.horizontal, 16)
                            .contextMenu {
                                Button(action: {
                                    abandonGame(game)
                                }) {
                                    Label("Abandon Game", systemImage: "xmark.octagon.fill")
                                }
                                
                                Button(role: .destructive, action: {
                                    deleteGame(game)
                                }) {
                                    Label("Delete (As If Never Happened)", systemImage: "trash.fill")
                                }
                            }
                        }
                    } else {
                        // Empty State
                        VStack(spacing: 8) {
                            Image(systemName: "gamecontroller")
                                .font(.system(size: 40))
                                .foregroundColor(.secondary.opacity(0.5))
                                .padding(.bottom, 4)
                            
                            Text("No Active Games")
                                .font(.system(size: 15, weight: .semibold))
                                .foregroundColor(.secondary)
                            
                            Text("Tap \"New Game\" above to start tracking scores.")
                                .font(.system(size: 13))
                                .foregroundColor(.secondary.opacity(0.7))
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 32)
                        .background(Color.appSurface.opacity(0.5))
                        .cornerRadius(16)
                        .padding(.horizontal, 16)
                    }
                }
            }
            .padding(.bottom, 32)
        }
        .background(Color.appBackground)
        .navigationTitle("Home")
        .appNavigationBarTitleDisplayMode()
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button(action: {
                    showSettings = true
                }) {
                    Image(systemName: "gearshape.fill")
                        .font(.system(size: 16, weight: .semibold))
                }
            }
        }
        .sheet(isPresented: $showSettings) {
            NavigationStack {
                SettingsView()
            }
            .applyThemeColorScheme(appTheme)
        }
        .sheet(isPresented: $showingSetup, onDismiss: {
            // Push onto navPath only after the sheet is fully dismissed.
            // Because HomeView owns the NavigationStack, navPath.append() is
            // unambiguously targeting the root stack — not the sheet's stack.
            if let gameId = pendingGameId {
                navPath.append(gameId)
                pendingGameId = nil
            }
        }) {
            NavigationStack {
                GameSetupView(onStartGame: { gameId in
                    pendingGameId = gameId
                    showingSetup = false
                })
            }
        }
        .navigationDestination(for: UUID.self) { gameId in
            ActiveGameView(gameId: gameId)
        }
        .applyThemeColorScheme(appTheme)
        .onAppear {
            if CommandLine.arguments.contains("-runThemeTests") {
                runThemeTests()
            }
        }
        } // end NavigationStack
    }
    
    private var activeGames: [Game] {
        games.filter { $0.status == .active || $0.status == .paused }
    }
    
    private func abandonGame(_ game: Game) {
        game.status = .abandoned
        try? modelContext.save()
    }
    
    private func deleteGame(_ game: Game) {
        modelContext.delete(game)
        try? modelContext.save()
    }
    
    private func runThemeTests() {
        Task {
            print("🚀 Starting Simulator Functional Theme Tests...")
            
            func writeResult(status: String, message: String) {
                let fileManager = FileManager.default
                guard let documentsURL = fileManager.urls(for: .documentDirectory, in: .userDomainMask).first else { return }
                let fileURL = documentsURL.appendingPathComponent("theme_test_results.json")
                
                let result: [String: String] = ["status": status, "message": message]
                if let data = try? JSONSerialization.data(withJSONObject: result, options: .prettyPrinted) {
                    try? data.write(to: fileURL)
                }
            }
            
            do {
                // 1. Open settings sheet
                await MainActor.run {
                    showSettings = true
                }
                try await Task.sleep(nanoseconds: 1_000_000_000) // Wait 1s for presentation
                
                guard showSettings else {
                    writeResult(status: "FAILURE", message: "Settings sheet failed to open")
                    return
                }
                
                // 2. Cycle through themes
                let themes: [AppTheme] = [.system, .pureBlack, .deepGray, .system]
                for theme in themes {
                    await MainActor.run {
                        appTheme = theme.rawValue
                    }
                    try await Task.sleep(nanoseconds: 1_000_000_000) // Wait 1s for transition
                    
                    guard showSettings else {
                        writeResult(status: "FAILURE", message: "Settings sheet auto-closed when switching to \(theme.displayName)")
                        return
                    }
                    
                    // Verify colors update
                    let bg = Color.appBackground
                    let surface = Color.appSurface
                    
                    switch theme {
                    case .system:
                        if bg == nil {
                            writeResult(status: "FAILURE", message: "System background is nil")
                            return
                        }
                    case .pureBlack:
                        if bg != Color.black || surface != Color(hex: "#121212") {
                            writeResult(status: "FAILURE", message: "Pure Black color mismatch: bg=\(bg), surface=\(surface)")
                            return
                        }
                    case .deepGray:
                        if bg != Color(hex: "#121212") || surface != Color(hex: "#1E1E1E") {
                            writeResult(status: "FAILURE", message: "Deep Gray color mismatch: bg=\(bg), surface=\(surface)")
                            return
                        }
                    }
                }
                
                writeResult(status: "SUCCESS", message: "Theme switches successfully retained and colors updated correctly.")
                try await Task.sleep(nanoseconds: 1_000_000_000)
                exit(0)
            } catch {
                writeResult(status: "FAILURE", message: "Test execution threw error: \(error.localizedDescription)")
            }
        }
    }
}
