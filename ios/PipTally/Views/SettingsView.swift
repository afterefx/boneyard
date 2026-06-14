import SwiftUI
import SwiftData

struct SettingsView: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss
    @AppStorage("appTheme") private var appTheme = 0
    
    @State private var showResetAlert = false
    @State private var resetSuccess = false
    
    var body: some View {
        Form {
            Section {
                VStack(alignment: .leading, spacing: 12) {
                    Text("Choose Theme")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.secondary)
                        .textCase(.uppercase)
                        .padding(.bottom, 2)
                    
                    ForEach(AppTheme.allCases) { theme in
                        Button(action: {
                            withAnimation(.spring(response: 0.35, dampingFraction: 0.7)) {
                                appTheme = theme.rawValue
                            }
                        }) {
                            HStack(spacing: 12) {
                                // Theme indicator color preview circles
                                HStack(spacing: -6) {
                                    Circle()
                                        .fill(themeColorPreview(for: theme, element: .background))
                                        .frame(width: 18, height: 18)
                                        .overlay(Circle().stroke(Color.primary.opacity(0.15), lineWidth: 1))
                                    
                                    Circle()
                                        .fill(themeColorPreview(for: theme, element: .surface))
                                        .frame(width: 18, height: 18)
                                        .overlay(Circle().stroke(Color.primary.opacity(0.15), lineWidth: 1))
                                }
                                
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(theme.displayName)
                                        .font(.system(size: 16, weight: .semibold))
                                        .foregroundColor(.primary)
                                    
                                    Text(theme.description)
                                        .font(.system(size: 11))
                                        .foregroundColor(.secondary)
                                        .multilineTextAlignment(.leading)
                                }
                                
                                Spacer()
                                
                                if appTheme == theme.rawValue {
                                    Image(systemName: "checkmark.circle.fill")
                                        .foregroundColor(.accentColor)
                                        .font(.system(size: 20))
                                } else {
                                    Circle()
                                        .stroke(Color.secondary.opacity(0.3), lineWidth: 1.5)
                                        .frame(width: 20, height: 20)
                                }
                            }
                            .padding(.vertical, 8)
                            .contentShape(Rectangle())
                        }
                        .buttonStyle(PlainButtonStyle())
                        
                        if theme.rawValue != AppTheme.allCases.last?.rawValue {
                            Divider()
                        }
                    }
                }
                .padding(.vertical, 4)
            } header: {
                Text("Appearance")
            }
            .listRowBackground(Color.appRowBackground)
            
            // Live Theme Preview Card Section
            Section {
                VStack(alignment: .leading, spacing: 10) {
                    Text("Theme Preview")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(.secondary)
                    
                    // Miniature mock card using active theme colors
                    VStack(spacing: 8) {
                        HStack {
                            PlayerAvatar(name: "Premium Player", colorHex: "#1E88E5", avatarIndex: 0, size: .small)
                            
                            VStack(alignment: .leading, spacing: 2) {
                                Text("Premium Player")
                                    .font(.system(size: 14, weight: .semibold))
                                    .foregroundColor(.primary)
                                Text("Scoreboard Standing Preview")
                                    .font(.system(size: 10))
                                    .foregroundColor(.secondary)
                            }
                            Spacer()
                            Text("0 pips")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(.accentColor)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 2)
                                .background(Color.accentColor.opacity(0.15))
                                .cornerRadius(6)
                        }
                        .padding(10)
                        .background(Color.appSurface)
                        .cornerRadius(8)
                        .overlay(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(Color.primary.opacity(0.05), lineWidth: 1)
                        )
                    }
                    .padding(8)
                    .background(Color.appBackground)
                    .cornerRadius(12)
                }
                .padding(.vertical, 6)
            } header: {
                Text("Live Style Preview")
            }
            .listRowBackground(Color.appRowBackground)
            
            // Info Section
            Section {
                VStack(alignment: .leading, spacing: 8) {
                    HStack {
                        Image(systemName: "info.circle.fill")
                            .foregroundColor(.accentColor)
                        Text("App Information")
                            .font(.system(size: 15, weight: .semibold))
                    }
                    
                    Text("This app keeps all your players, game history, and round scores saved directly and privately on your phone. It works entirely offline, so you can track your matches anywhere without needing an internet connection.")
                        .font(.system(size: 12))
                        .foregroundColor(.secondary)
                        .lineSpacing(2)
                }
                .padding(.vertical, 6)
                
                HStack {
                    Text("App Version")
                    Spacer()
                    Text("1.0.0 (Build 1)")
                        .foregroundColor(.secondary)
                        .font(.system(size: 14))
                }
            } header: {
                Text("About")
            }
            .listRowBackground(Color.appRowBackground)
            
            // Danger Zone
            Section {
                Button(role: .destructive, action: {
                    showResetAlert = true
                }) {
                    HStack {
                        Image(systemName: "trash.fill")
                        Text("Reset All Application Data")
                            .fontWeight(.semibold)
                    }
                    .frame(maxWidth: .infinity, alignment: .center)
                }
            } header: {
                Text("Danger Zone")
            }
            .listRowBackground(Color.appRowBackground)
        }
        .background(Color.appBackground)
        .scrollContentBackground(.hidden)
        .navigationTitle("Settings")
        .appNavigationBarTitleDisplayMode()
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button("Done") {
                    dismiss()
                }
                .fontWeight(.bold)
            }
        }
        .alert("Reset All Data?", isPresented: $showResetAlert) {
            Button("Cancel", role: .cancel) { }
            Button("Reset Everything", role: .destructive) {
                resetAllData()
            }
        } message: {
            Text("This will permanently delete all registered players, matches history, ongoing games, and round scores. This action cannot be undone.")
        }
        .alert("Database Reset", isPresented: $resetSuccess) {
            Button("OK", role: .cancel) { }
        } message: {
            Text("All application data has been successfully cleared.")
        }
        .applyThemeColorScheme(appTheme)
    }
    
    private enum PreviewElement {
        case background
        case surface
    }
    
    private func themeColorPreview(for theme: AppTheme, element: PreviewElement) -> Color {
        switch theme {
        case .system:
            return element == .background ? Color(white: 0.95) : Color.white
        case .pureBlack:
            return element == .background ? Color.black : (Color(hex: "#121212") ?? Color(white: 0.07))
        case .deepGray:
            return element == .background ? (Color(hex: "#121212") ?? Color(white: 0.07)) : (Color(hex: "#1E1E1E") ?? Color(white: 0.12))
        }
    }
    
    private func resetAllData() {
        do {
            // Delete all games
            try modelContext.delete(model: Game.self)
            // Delete all players
            try modelContext.delete(model: Player.self)
            // Delete all rounds
            try modelContext.delete(model: Round.self)
            // Delete all round scores
            try modelContext.delete(model: RoundScore.self)
            // Delete all game players
            try modelContext.delete(model: GamePlayer.self)
            
            try modelContext.save()
            resetSuccess = true
        } catch {
            print("Failed to reset database: \(error.localizedDescription)")
        }
    }
}
