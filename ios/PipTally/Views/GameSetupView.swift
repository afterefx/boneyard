import SwiftUI
import SwiftData

struct GameSetupView: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss
    
    let onStartGame: (UUID) -> Void
    
    @Query(sort: \Player.name) private var allPlayers: [Player]
    
    @State private var currentStep = 0
    @State private var selectedPlayerIds: Set<UUID> = []
    @State private var selectedPlayers: [Player] = []
    @State private var firstShakerIndex = 0
    @State private var showingAddPlayerSheet = false
    
    var body: some View {
        VStack(spacing: 0) {
            // Step Indicator
            SetupStepIndicator(currentStep: currentStep)
                .padding(.horizontal, 24)
                .padding(.vertical, 16)
            
            Divider()
            
            if allPlayers.isEmpty {
                // Roster Empty State
                VStack(spacing: 12) {
                    Image(systemName: "person.3.fill")
                        .font(.system(size: 48))
                        .foregroundColor(.secondary.opacity(0.5))
                        .padding(.bottom, 8)
                    
                    Text("No Players Available")
                        .font(.system(size: 17, weight: .semibold))
                        .foregroundColor(.secondary)
                    
                    Text("You need at least 4 registered players to start a game.")
                        .font(.system(size: 13))
                        .foregroundColor(.secondary.opacity(0.7))
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                    
                    Button(action: {
                        showingAddPlayerSheet = true
                    }) {
                        Text("Create Player")
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(.white)
                            .padding(.horizontal, 20)
                            .padding(.vertical, 10)
                            .background(Color.accentColor)
                            .cornerRadius(20)
                    }
                    .padding(.top, 12)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(Color.appBackground)
            } else {
                if currentStep == 0 {
                    // Step 0: Player Selection
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Text("\(selectedPlayerIds.count)/4 players selected")
                                .font(.system(size: 15, weight: .medium))
                                .foregroundColor(selectedPlayerIds.count == 4 ? .accentColor : .secondary)
                            
                            Spacer()
                            
                            if allPlayers.count < 4 {
                                Button(action: {
                                    showingAddPlayerSheet = true
                                }) {
                                    HStack(spacing: 4) {
                                        Image(systemName: "person.badge.plus.fill")
                                        Text("Create Player")
                                    }
                                    .font(.system(size: 13, weight: .bold))
                                    .foregroundColor(.white)
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 6)
                                    .background(Color.accentColor)
                                    .cornerRadius(12)
                                }
                            }
                        }
                        .padding(.horizontal, 24)
                        .padding(.top, 12)
                        
                        ScrollView {
                            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                                ForEach(allPlayers) { player in
                                    let isSelected = selectedPlayerIds.contains(player.id)
                                    let isDisabledLimit = selectedPlayerIds.count >= 4 && !isSelected
                                    
                                    Button(action: {
                                        if isSelected {
                                            selectedPlayerIds.remove(player.id)
                                        } else if selectedPlayerIds.count < 4 {
                                            selectedPlayerIds.insert(player.id)
                                        }
                                        updateSelectedPlayersList()
                                    }) {
                                        VStack(spacing: 8) {
                                            ZStack(alignment: .topTrailing) {
                                                PlayerAvatar(
                                                    name: player.name,
                                                    colorHex: player.colorHex,
                                                    avatarIndex: player.avatarIndex,
                                                    size: .large
                                                )
                                                .opacity(isDisabledLimit ? 0.4 : 1.0)
                                                
                                                if isSelected {
                                                    Image(systemName: "checkmark.circle.fill")
                                                        .font(.system(size: 22))
                                                        .foregroundColor(.accentColor)
                                                        .background(Color.white.clipShape(Circle()))
                                                        .offset(x: 4, y: -4)
                                                }
                                            }
                                            
                                            Text(player.name)
                                                .font(.system(size: 15, weight: .medium))
                                                .foregroundColor(isDisabledLimit ? .secondary.opacity(0.4) : .primary)
                                        }
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, 16)
                                        .background(isSelected ? Color.accentColor.opacity(0.1) : Color.appSurface)
                                        .cornerRadius(12)
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 12)
                                                .stroke(isSelected ? Color.accentColor : Color.gray.opacity(0.3), lineWidth: isSelected ? 2 : 1)
                                        )
                                    }
                                    .disabled(isDisabledLimit)
                                    .buttonStyle(.plain)
                                }
                                
                                // Interactive "+" placeholder card when less than 4 players are available
                                if allPlayers.count < 4 {
                                    Button(action: {
                                        showingAddPlayerSheet = true
                                    }) {
                                        VStack(spacing: 8) {
                                            ZStack {
                                                Circle()
                                                    .fill(Color.accentColor.opacity(0.12))
                                                    .frame(width: 64, height: 64)
                                                
                                                Image(systemName: "plus")
                                                    .font(.system(size: 24, weight: .bold))
                                                    .foregroundColor(.accentColor)
                                            }
                                            
                                            Text("Add Player")
                                                .font(.system(size: 15, weight: .bold))
                                                .foregroundColor(.accentColor)
                                        }
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, 16)
                                        .background(Color.appSurface)
                                        .cornerRadius(12)
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 12)
                                                .stroke(Color.accentColor.opacity(0.4), style: StrokeStyle(lineWidth: 1.5, dash: [4]))
                                        )
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                            .padding(.horizontal, 16)
                            .padding(.vertical, 12)
                        }
                    }
                    .background(Color.appBackground)
                } else {
                    // Step 1: Reordering & Shaker Selection
                    ScrollView {
                        VStack(alignment: .leading, spacing: 20) {
                            Text("Arrange Seating Order")
                                .font(.system(size: 17, weight: .semibold))
                                .foregroundColor(.primary)
                                .padding(.horizontal, 20)
                                .padding(.top, 16)
                            
                            VStack(spacing: 8) {
                                ForEach(0..<selectedPlayers.count, id: \.self) { index in
                                    let player = selectedPlayers[index]
                                    
                                    HStack(spacing: 12) {
                                        // Seat Index
                                        Text("\(index + 1)")
                                            .font(.system(size: 15, weight: .bold))
                                            .foregroundColor(.accentColor)
                                            .frame(width: 28, height: 28)
                                            .background(Color.accentColor.opacity(0.15))
                                            .cornerRadius(6)
                                        
                                        PlayerAvatar(
                                            name: player.name,
                                            colorHex: player.colorHex,
                                            avatarIndex: player.avatarIndex,
                                            size: .small
                                        )
                                        
                                        Text(player.name)
                                            .font(.system(size: 17, weight: .medium))
                                            .foregroundColor(.primary)
                                        
                                        Spacer()
                                        
                                        // Simple interactive Reordering buttons
                                        HStack(spacing: 16) {
                                            Button(action: { movePlayerUp(index: index) }) {
                                                Image(systemName: "chevron.up")
                                                    .font(.system(size: 16, weight: .bold))
                                                    .foregroundColor(index == 0 ? .secondary.opacity(0.3) : .accentColor)
                                            }
                                            .disabled(index == 0)
                                            
                                            Button(action: { movePlayerDown(index: index) }) {
                                                Image(systemName: "chevron.down")
                                                    .font(.system(size: 16, weight: .bold))
                                                    .foregroundColor(index == selectedPlayers.count - 1 ? .secondary.opacity(0.3) : .accentColor)
                                            }
                                            .disabled(index == selectedPlayers.count - 1)
                                        }
                                        .buttonStyle(.plain)
                                    }
                                    .padding(.horizontal, 16)
                                    .padding(.vertical, 12)
                                    .background(Color.appSurface)
                                    .cornerRadius(12)
                                    .shadow(color: Color.black.opacity(0.01), radius: 2, x: 0, y: 1)
                                    .padding(.horizontal, 16)
                                }
                            }
                            
                            Divider()
                                .padding(.horizontal, 16)
                            
                            // Shaker Selector
                            VStack(alignment: .leading, spacing: 8) {
                                Text("Select First Shaker")
                                    .font(.system(size: 17, weight: .semibold))
                                    .foregroundColor(.primary)
                                
                                Picker("First Shaker", selection: $firstShakerIndex) {
                                    ForEach(0..<selectedPlayers.count, id: \.self) { idx in
                                        Text(selectedPlayers[idx].name).tag(idx)
                                    }
                                }
                                .pickerStyle(.segmented)
                            }
                            .padding(.horizontal, 20)
                            .padding(.bottom, 24)
                        }
                    }
                    .background(Color.appBackground)
                }
                
                // Bottom Button
                VStack {
                    Divider()
                    Button(action: {
                        if currentStep == 0 {
                            currentStep = 1
                        } else {
                            startGame()
                        }
                    }) {
                        Text(currentStep == 0 ? "Next" : "Start Game")
                            .font(.system(size: 17, weight: .bold))
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(currentStep == 0 ? (selectedPlayerIds.count == 4 ? Color.accentColor : Color.gray) : Color.accentColor)
                            .cornerRadius(12)
                    }
                    .disabled(currentStep == 0 && selectedPlayerIds.count != 4)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 16)
                    .background(Color.appRowBackground)
                }
            }
        }
        .navigationTitle("New Game")
        .appNavigationBarTitleDisplayMode()
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
        }
        .sheet(isPresented: $showingAddPlayerSheet) {
            PlayerEditView()
        }
    }
    
    private func updateSelectedPlayersList() {
        // Sync selected list keeping ordering as consistent as possible
        selectedPlayers = allPlayers.filter { selectedPlayerIds.contains($0.id) }
        firstShakerIndex = 0
    }
    
    private func movePlayerUp(index: Int) {
        guard index > 0 else { return }
        selectedPlayers.swapAt(index, index - 1)
        firstShakerIndex = 0
    }
    
    private func movePlayerDown(index: Int) {
        guard index < selectedPlayers.count - 1 else { return }
        selectedPlayers.swapAt(index, index + 1)
        firstShakerIndex = 0
    }
    
    private func startGame() {
        guard selectedPlayers.count == 4 else { return }
        
        // Rotate the ordered list so the chosen starting shaker is at seat index 0
        var finalOrderedList = selectedPlayers
        if firstShakerIndex > 0 && firstShakerIndex < selectedPlayers.count {
            let prefix = selectedPlayers[firstShakerIndex..<selectedPlayers.count]
            let suffix = selectedPlayers[0..<firstShakerIndex]
            finalOrderedList = Array(prefix) + Array(suffix)
        }
        
        // SwiftData insertions
        let newGame = Game(status: .active)
        modelContext.insert(newGame)
        
        // Add game players
        for (idx, player) in finalOrderedList.enumerated() {
            let gp = GamePlayer(player: player, seatPosition: idx, totalScore: 0)
            gp.game = newGame
            newGame.gamePlayers.append(gp)
            modelContext.insert(gp)
        }
        
        try? modelContext.save()
        onStartGame(newGame.id)
    }
}

struct SetupStepIndicator: View {
    let currentStep: Int
    
    var body: some View {
        VStack(spacing: 8) {
            HStack {
                Text("Select Players")
                    .font(.system(size: 13, weight: currentStep == 0 ? .bold : .regular))
                    .foregroundColor(currentStep == 0 ? .accentColor : .secondary)
                
                Spacer()
                
                Text("Set Order")
                    .font(.system(size: 13, weight: currentStep == 1 ? .bold : .regular))
                    .foregroundColor(currentStep == 1 ? .accentColor : .secondary)
            }
            
            ProgressView(value: Double(currentStep + 1), total: 2.0)
                .tint(.accentColor)
        }
    }
}
