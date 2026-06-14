# 🎴 PipTally — Domino Scorer

A premium, native iOS score tracking application (Domino Scorer) for Domino matches, written entirely in **Swift**, **SwiftUI**, and persisted locally using **SwiftData**. 

Adhering strictly to Apple's modern **Human Interface Guidelines (HIG)**, the app delivers a fluid, dark-themed, and highly responsive user experience. It retains all the classic game rules, circular seating sequences, and dynamic 14-round double-spinner math from the original system.

---

## ✨ Features

* **🎨 Pixel-Perfect Vector Tiles (`DominoTile.swift`)**: Custom 3D-esque domino faces drawn programmatically on a SwiftUI Canvas, dynamically adjusting layout (horizontal or vertical) and pips relative to the active double-spinner value.
* **📈 Lifetime Metric Analytics (`PlayerProfileView.swift`)**: Detailed player statistics highlighting match histories, total wins, win rates, historical averages, and personal bests.
* **🔁 circular Seating Reordering (`GameSetupView.swift`)**: Interactive reordering controls allowing simple circular seating arrangements and first-round shaker selection.
* **🎯 real-Time Standings Sorted (`ScoreboardTable.swift`)**: Leaderboards automatically sorted by lowest score, highlighting the active leader with trophies and contrasting row styles.
* **🔄 Infinite Round Navigation**:
  - Navigate freely through all 14 rounds of a game (past, current, and upcoming).
  - Unplayed upcoming rounds display beautiful placeholder states.
  - A primary **"Return to Current Round"** button appears instantly at the bottom when viewing any non-active round.
* **✏️ Historical Round Editing**: A pencil icon on past rounds lets you enter edit mode, validate adjustments, and **automatically recalculate player totals** and standings.
* **💾 Local SwiftData Persistence**: Zero SQL boilerplate local database mapping relational schemas and handling cascading deletions natively.

---

## 📂 Project Structure

```text
ios-domino-score-tracker/
├── README.md                    # Project overview and quickstart
├── DESIGN.md                    # Color tokens, visual philosophy, HIG styling
├── ARCHITECTURE.md              # SwiftData relations, MVVM flow, transactions
├── CONTRIBUTING.md              # Xcode generator instructions, coding styles
├── generate_project.py          # Dynamic Xcode project generator script
├── PipTally/
│   ├── PipTallyApp.swift        # App entry point and ModelContainer initialization
│   ├── Utils/
│   │   └── GameConstants.swift  # Double-spinner math and circular seating calculations
│   │   └── ThemeColors.swift    # Core color palette & custom themes
│   ├── Models/
│   │   └── DominoModels.swift   # SwiftData relational database entities
│   └── Views/
│       ├── HomeView.swift           # Main card-based CTA dashboard
│       ├── PlayerListView.swift     # Roster deck with swipe actions
│       ├── PlayerEditView.swift     # Profile creation and edit sheets
│       ├── PlayerProfileView.swift  # Analytics graphs and matches log registry
│       ├── GameSetupView.swift      # Seat ordering, starting shaker selector
│       ├── ActiveGameView.swift     # Live tracking console and historical editor
│       ├── GameSummaryView.swift    # Confetti celebration banner and full rounds grid
│       ├── GameHistoryView.swift    # Completed matches registry log
│       ├── SettingsView.swift       # App preferences, reset, and theme picker
│       └── Components/
│           ├── PlayerAvatar.swift   # Vector SF Symbol circular avatar with contrasts
│           ├── DominoTile.swift     # Programmatically rendered Canvas domino tile
│           ├── ScoreboardTable.swift# Compact sortable standings table
│           └── ScoreEntryRow.swift  # Interactive text field rows with trophy toggles
```

---

## 🚀 Quickstart

### 🛠️ Prerequisites
* macOS 14.0+ (Sonoma)
* Xcode 15.0+
* Swift 5.9+
* iOS SDK 17.0+ (for local simulator target builds)

### 💻 Local Compilation & Deploy
1. Clone the repository and navigate to the project directory:
   ```bash
   cd ios-domino-score-tracker
   ```
2. **Generate the Xcode Project**:
   ```bash
   python3 generate_project.py
   ```
3. **Open in Xcode**:
   ```bash
   open PipTally.xcodeproj
   ```
4. Press **Cmd + R** inside Xcode to boot the active **iPhone Simulator** and deploy the app!

---

## 🧪 Terminal Verification

If you are working in a terminal-only environment or performing CI/CD checks, you can compile and verify the entire codebase against the host SDK:

```bash
swiftc -o /dev/null -sdk $(xcrun --show-sdk-path -sdk macosx) PipTally/PipTallyApp.swift PipTally/Utils/GameConstants.swift PipTally/Utils/ThemeColors.swift PipTally/Models/DominoModels.swift PipTally/Views/Components/PlayerAvatar.swift PipTally/Views/Components/DominoTile.swift PipTally/Views/Components/ScoreboardTable.swift PipTally/Views/Components/ScoreEntryRow.swift PipTally/Views/HomeView.swift PipTally/Views/PlayerListView.swift PipTally/Views/PlayerEditView.swift PipTally/Views/PlayerProfileView.swift PipTally/Views/GameSetupView.swift PipTally/Views/ActiveGameView.swift PipTally/Views/GameSummaryView.swift PipTally/Views/GameHistoryView.swift PipTally/Views/SettingsView.swift
```

*This compilation check will finish with exit code `0` and zero warnings.*
