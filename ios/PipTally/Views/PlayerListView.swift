import SwiftUI
import SwiftData

struct PlayerListView: View {
    @Environment(\.modelContext) private var modelContext
    @Query(sort: \Player.name) private var players: [Player]
    
    @State private var showingAddPlayerSheet = false
    @State private var playerToDelete: Player? = nil
    @State private var showingDeleteAlert = false
    
    var body: some View {
        Group {
            if players.isEmpty {
                VStack(spacing: 12) {
                    Image(systemName: "person.3.fill")
                        .font(.system(size: 48))
                        .foregroundColor(.secondary.opacity(0.5))
                        .padding(.bottom, 8)
                    
                    Text("No Players Yet")
                        .font(.system(size: 17, weight: .semibold))
                        .foregroundColor(.secondary)
                    
                    Text("Create player profiles to start tracking matches.")
                        .font(.system(size: 13))
                        .foregroundColor(.secondary.opacity(0.7))
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                    
                    Button(action: {
                        showingAddPlayerSheet = true
                    }) {
                        Text("Add First Player")
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
                List {
                    ForEach(players) { player in
                        NavigationLink(destination: PlayerProfileView(player: player)) {
                            HStack(spacing: 12) {
                                PlayerAvatar(
                                    name: player.name,
                                    colorHex: player.colorHex,
                                    avatarIndex: player.avatarIndex,
                                    size: .medium
                                )
                                
                                VStack(alignment: .leading, spacing: 4) {
                                    Text(player.name)
                                        .font(.system(size: 17, weight: .medium))
                                        .foregroundColor(.primary)
                                    
                                    Text("Joined \(player.createdAt, style: .date)")
                                        .font(.system(size: 12))
                                        .foregroundColor(.secondary)
                                }
                            }
                            .padding(.vertical, 4)
                        }
                        .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                            Button(role: .destructive) {
                                playerToDelete = player
                                showingDeleteAlert = true
                            } label: {
                                Label("Delete", systemImage: "trash")
                            }
                        }
                    }
                }
            }
        }
        .navigationTitle("Players")
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button(action: {
                    showingAddPlayerSheet = true
                }) {
                    Image(systemName: "plus")
                }
            }
        }
        .sheet(isPresented: $showingAddPlayerSheet) {
            PlayerEditView()
        }
        .alert("Delete Player?", isPresented: $showingDeleteAlert, presenting: playerToDelete) { player in
            Button("Delete", role: .destructive) {
                deletePlayer(player)
            }
            Button("Cancel", role: .cancel) {}
        } message: { player in
            Text("Are you sure you want to delete \(player.name)? This will delete their player profile and all their stats. This action cannot be undone.")
        }
    }
    
    private func deletePlayer(_ player: Player) {
        modelContext.delete(player)
        try? modelContext.save()
    }
}
