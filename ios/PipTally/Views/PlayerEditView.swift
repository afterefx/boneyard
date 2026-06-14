import SwiftUI
import SwiftData

struct PlayerEditView: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss
    
    var playerToEdit: Player?
    
    @Query(sort: \Player.name) private var existingPlayers: [Player]
    
    @State private var name: String = ""
    @State private var selectedColorHex: String = "#E53935"
    @State private var selectedAvatarIndex: Int = 0
    @State private var errorMessage: String? = nil
    
    private let playerColors = [
        "#E53935", // Red
        "#1E88E5", // Blue
        "#43A047", // Green
        "#FB8C00", // Orange
        "#8E24AA", // Purple
        "#00ACC1", // Cyan
        "#E91E63", // Pink
        "#6D4C41"  // Brown
    ]
    
    private let avatarSymbols = [
        "die.face.6.fill",      // Dice 🎲
        "suit.spade.fill",      // Joker 🃏
        "gamecontroller.fill",  // Chess ♟️
        "trophy.fill",          // Trophy 🏆
        "target",               // Target 🎯
        "star.fill"             // Star ⭐
    ]
    
    init(playerToEdit: Player? = nil) {
        self.playerToEdit = playerToEdit
        _name = State(initialValue: playerToEdit?.name ?? "")
        _selectedColorHex = State(initialValue: playerToEdit?.colorHex ?? "#E53935")
        _selectedAvatarIndex = State(initialValue: playerToEdit?.avatarIndex ?? 0)
    }
    
    var body: some View {
        NavigationStack {
            Form {
                Section(header: Text("Player Profile")) {
                    TextField("Enter Player Name", text: $name)
                        .autocorrectionDisabled()
                        #if os(iOS)
                        .textInputAutocapitalization(.words)
                        #endif
                    
                    if let errorMessage = errorMessage {
                        Text(errorMessage)
                            .font(.caption)
                            .foregroundColor(.red)
                    }
                }
                
                Section(header: Text("Select Avatar")) {
                    HStack(spacing: 12) {
                        ForEach(0..<avatarSymbols.count, id: \.self) { index in
                            Button(action: {
                                selectedAvatarIndex = index
                            }) {
                                Image(systemName: avatarSymbols[index])
                                    .font(.system(size: 20, weight: .bold))
                                    .foregroundColor(.primary)
                                    .frame(width: 44, height: 44)
                                    .background(selectedAvatarIndex == index ? Color.accentColor.opacity(0.2) : Color.appGray)
                                    .clipShape(Circle())
                                    .overlay(
                                        Circle()
                                            .stroke(selectedAvatarIndex == index ? Color.accentColor : Color.clear, lineWidth: 2)
                                    )
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.vertical, 8)
                }
                
                Section(header: Text("Select Color")) {
                    LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 4), spacing: 12) {
                        ForEach(playerColors, id: \.self) { colorHex in
                            let isSelected = selectedColorHex == colorHex
                            let parsedColor = Color(hex: colorHex) ?? .blue
                            
                            Button(action: {
                                selectedColorHex = colorHex
                            }) {
                                Circle()
                                    .fill(parsedColor)
                                    .frame(width: 44, height: 44)
                                    .overlay(
                                        Circle()
                                            .stroke(Color.white, lineWidth: isSelected ? 3 : 0)
                                            .shadow(radius: isSelected ? 3 : 0)
                                    )
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.vertical, 8)
                }
                
                Section {
                    // Preview
                    HStack {
                        Spacer()
                        VStack(spacing: 8) {
                            Text("Preview")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            PlayerAvatar(
                                name: name.isEmpty ? "P" : name,
                                colorHex: selectedColorHex,
                                avatarIndex: selectedAvatarIndex,
                                size: .large
                            )
                        }
                        Spacer()
                    }
                    .padding(.vertical, 8)
                }
            }
            .navigationTitle(playerToEdit == nil ? "New Player" : "Edit Player")
            .appNavigationBarTitleDisplayMode()
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
                
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        savePlayer()
                    }
                    .disabled(name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                }
            }
        }
    }
    
    private func savePlayer() {
        let trimmedName = name.trimmingCharacters(in: .whitespacesAndNewlines)
        
        if trimmedName.isEmpty {
            errorMessage = "Name is required"
            return
        }
        
        let isDuplicate = existingPlayers.contains { player in
            player.name.lowercased() == trimmedName.lowercased() && player.id != playerToEdit?.id
        }
        
        if isDuplicate {
            errorMessage = "A player with this name already exists"
            return
        }
        
        if let player = playerToEdit {
            player.name = trimmedName
            player.colorHex = selectedColorHex
            player.avatarIndex = selectedAvatarIndex
        } else {
            let newPlayer = Player(name: trimmedName, colorHex: selectedColorHex, avatarIndex: selectedAvatarIndex)
            modelContext.insert(newPlayer)
        }
        
        try? modelContext.save()
        dismiss()
    }
}
