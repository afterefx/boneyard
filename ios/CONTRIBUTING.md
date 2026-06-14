# 🤝 Contributing Guide — PipTally

This guide walks you through the development lifecycle, code styling conventions, project configuration guidelines, and terminal-based build verification workflows.

---

## 🛠️ Developer Requirements
* **Operating System**: macOS 14.0+ (Sonoma) or newer.
* **Developer Tools**: Xcode 15.0+ installed with active command line tools (`xcode-select`).
* **Compilers**: Swift 5.9+ compiler (`swiftc`) and Xcode Build toolchain (`xcodebuild`).

---

## ⚙️ Project File Generation (`generate_project.py`)

Because this project is built in a modern, lightweight environment, we do not commit bloated Xcode binary metadata files (`.xcodeproj/project.pbxproj`) directly. Instead, we use an automated Python generator script:

```bash
python3 generate_project.py
```

### Key Script Rules
* **No Manual Edits**: Never edit `project.pbxproj` manually; it is overwritten every time `generate_project.py` runs. If you add or delete source files, update the `swift_files` array inside `generate_project.py` first.
* **18-Character Hashed UUIDs**: The generator uses stable 18-character hex-padded UUID hashes (`index:016X` with `AA`, `BB`, `CC`, `DD`, `FF` prefixes) to ensure that generated files align perfectly with hardcoded PBXGroup hierarchies.

---

## 🔄 Development Cycle

To contribute a bug fix, feature, or UI polish, follow this step-by-step sequence:

```text
 1. MODIFY CODE  ──▶  2. VERIFY SYNTAX (swiftc)  ──▶  3. REGENERATE PROJECT
                                                            │
 5. LIVE RUN     ◀──  4. TARGET COMPILE (xcodebuild) ◀──────┘
```

### Step 1: Make Surgical Edits
Write precise code modifications matching the established design aesthetics and architectural patterns.

### Step 2: Run Host Compiler Check
Before regenerating metadata, run the quick direct Swift compiler diagnostic command on the macOS SDK to check for syntax, schemas, or controllers syntax errors:
```bash
swiftc -o /dev/null -sdk $(xcrun --show-sdk-path -sdk macosx) PipTally/PipTallyApp.swift PipTally/Utils/GameConstants.swift PipTally/Utils/ThemeColors.swift PipTally/Models/DominoModels.swift PipTally/Views/Components/PlayerAvatar.swift PipTally/Views/Components/DominoTile.swift PipTally/Views/Components/ScoreboardTable.swift PipTally/Views/Components/ScoreEntryRow.swift PipTally/Views/HomeView.swift PipTally/Views/PlayerListView.swift PipTally/Views/PlayerEditView.swift PipTally/Views/PlayerProfileView.swift PipTally/Views/GameSetupView.swift PipTally/Views/ActiveGameView.swift PipTally/Views/GameSummaryView.swift PipTally/Views/GameHistoryView.swift PipTally/Views/SettingsView.swift
```

### Step 3: Sync Xcode Project References
If files were added or renamed, update the files list in `generate_project.py` and run it:
```bash
python3 generate_project.py
```

### Step 4: Compile Simulator Target
Build the iOS simulator package to confirm everything compiles clean under the platform target:
```bash
xcodebuild -project PipTally.xcodeproj -scheme PipTally -destination "platform=iOS Simulator,name=iPhone 17 Pro" -configuration Debug build
```

### Step 5: Test on Simulator
Boot, deploy, and launch the app inside the active Simulator:
```bash
open -a Simulator
xcrun simctl install booted build/Build/Products/Debug-iphonesimulator/PipTally.app
xcrun simctl launch booted app.piptally
```

---

## 🎨 Coding Style & Portability Rules

To keep the codebase cleanly compilable across terminal test hosts and active iOS platforms:

1. **Leverage Theme Abstractions**:
   - Never reference platform-exclusive UIKit system colors directly (e.g., `Color(.secondarySystemGroupedBackground)`). Instead, use the adaptive theme color extensions declared in `ThemeColors.swift` (`Color.appBackground`, `Color.appSurface`, `Color.appRowBackground`, `Color.appGray`).
2. **Conditional View Modifiers**:
   - Wrap platform-exclusive iOS view modifiers (such as `.keyboardType()`, `.textInputAutocapitalization()`) inside conditional compiler checks:
     ```swift
     #if os(iOS)
     .textInputAutocapitalization(.words)
     #endif
     ```
3. **Cross-Platform Toolbars**:
   - Use cross-platform toolbar placements like `.primaryAction`, `.confirmationAction`, or `.cancellationAction` instead of iOS-only options like `.navigationBarTrailing` or `.navigationBarLeading`.
4. **Non-Deprecated Bindings**:
   - Use the modern double-parameter `.onChange(of: { oldValue, newValue in ... })` bindings in SwiftUI to maintain compiler compatibility with modern iOS 17 / macOS 14+ targets.
5. **SF Symbols over Emojis**:
   - Always use Apple's native vector-scaled **SF Symbols** (`Image(systemName:)`) for icons and UI avatars rather than raw Unicode emojis, preventing glyph rendering issues in simulator runtimes.
