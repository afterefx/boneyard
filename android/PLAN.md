# Domino Score Tracker - Android App

## Context

Build an Android app from scratch to track scores for a 4-player dominoes game played off the spinner. The game has 14 rounds (double-6 down to double-0, then back up to double-6), with a rotating shaker role. Players score based on dots remaining in hand; lowest total wins. The app needs player profiles, game history, pause/resume, and stats.

## Tech Stack

- **Kotlin** + **Jetpack Compose** + **Material 3**
- **Room** for local database persistence
- **Hilt** for dependency injection
- **Navigation Compose** for screen routing
- **MVVM** architecture with Clean Architecture layering
- **Gradle Kotlin DSL** with version catalog (`libs.versions.toml`)
- **Package name:** `com.domino.scoretracker`

## Database Schema

### Entities

**PlayerEntity** - `id`, `name` (unique), `color` (hex string), `avatarIndex`, `createdAt`

**GameEntity** - `id`, `status` (IN_PROGRESS/PAUSED/COMPLETED), `currentRoundIndex` (0-13), `createdAt`, `completedAt?`, `winnerPlayerId?`

**GamePlayerEntity** (junction) - `id`, `gameId` (FK), `playerId` (FK), `playerOrder` (0-3), `shakerOrder` (0-3), `totalScore` (denormalized running total). Unique on `(gameId, playerId)`.

**RoundEntity** - `id`, `gameId` (FK), `roundIndex` (0-13), `spinnerValue`, `shakerPlayerId` (FK), `isCompleted`

**RoundScoreEntity** - `id`, `roundId` (FK), `playerId` (FK), `score`, `isWinner`

### Core Game Logic

```kotlin
val ROUND_SPINNER_SEQUENCE = listOf(6, 5, 4, 3, 2, 1, 0, 0, 1, 2, 3, 4, 5, 6)
fun getShakerIndex(roundIndex: Int) = roundIndex % 4
```

## Screens & Navigation

1. **Home** - Active/paused games list, quick actions (New Game, Players, History)
2. **Player List** - All players, FAB to add, tap for profile
3. **Player Edit** - Create/edit player (name, color, avatar)
4. **Player Profile** - Stats + game history for one player
5. **Game Setup** - Select 4 players, set order, choose first shaker
6. **Active Game** - Round-by-round scoring: spinner value, shaker indicator, score entry per player, winner toggle, running totals, submit/undo, pause
7. **Game Summary** - Full scoreboard with per-round breakdown, winner highlighted
8. **Game History** - Completed games list, tap for summary

## Player Stats

- Games played, games won, win percentage
- Average score, best (lowest) game score, worst (highest) game score
- Total rounds won

## Implementation Phases

### Phase 1: Project Scaffolding & Database
- Create Gradle wrapper, `settings.gradle.kts`, root `build.gradle.kts`, `gradle.properties`, `libs.versions.toml`
- Create `app/build.gradle.kts` with all dependencies (Compose BOM, Room, Hilt, Navigation)
- `AndroidManifest.xml`, `DominoTrackerApp.kt` (@HiltAndroidApp), `MainActivity.kt` (@AndroidEntryPoint)
- All 5 entity classes with Room annotations
- All 5 DAO interfaces with CRUD + key queries
- `AppDatabase.kt`, `DatabaseModule.kt` (Hilt)
- Theme files (Color.kt, Theme.kt, Type.kt)
- Verify: `./gradlew assembleDebug`

### Phase 2: Domain Layer & Player Management
- Domain models: `Player`, `GameStatus`, `PlayerStats`
- `PlayerRepository` interface + `PlayerRepositoryImpl` + mapper
- `RepositoryModule.kt` (Hilt bindings)
- Player use cases (GetAll, Create, Update, Delete)
- Navigation setup: `Screen.kt` sealed class, `NavGraph.kt`
- `PlayerListScreen` + `PlayerListViewModel` (list, delete with confirmation)
- `PlayerEditScreen` + `PlayerEditViewModel` (create/edit form with validation)
- Minimal `HomeScreen` + `HomeViewModel` with nav buttons

### Phase 3: Game Creation & Active Gameplay
- Domain models: `Game`, `GameWithPlayers`, `Round`, `RoundScore`
- `GameRepository` interface + impl + mappers
- Game use cases (Create, GetActive, GetDetails, Pause, Resume, Complete)
- Round use cases (SubmitRoundScores, GetCurrentRound, UndoLastRound)
- `GameSetupScreen` - select 4 players, set order, pick first shaker
- `ActiveGameScreen` - round header (spinner + shaker), score entry rows, winner toggle, running scoreboard, submit/undo, pause
- Reusable components: `PlayerAvatar`, `ScoreEntryRow`, `RoundIndicator`, `ScoreboardTable`
- Update `HomeScreen` with active game cards + resume

### Phase 4: Game Summary & History
- `GameSummaryScreen` - full 14-round scoreboard, winner highlighted
- `GameHistoryScreen` - completed games list
- Auto-transition from final round to summary
- Tie handling: co-winners (both players share the win)

### Phase 5: Player Stats & Profile
- `StatsRepository` interface + impl with aggregate queries
- `GetPlayerStatsUseCase`
- `PlayerProfileScreen` - stats cards + recent games list

### Phase 6: Polish
- Confirmation dialogs (delete player, end game early, undo)
- Empty states, input validation, back-navigation warnings
- Dark theme support via Material 3

## Project Structure

```
app/src/main/java/com/domino/scoretracker/
├── DominoTrackerApp.kt
├── MainActivity.kt
├── data/local/{AppDatabase, dao/*, entity/*}
├── data/repository/*Impl.kt
├── data/mapper/*.kt
├── domain/model/*.kt
├── domain/repository/*.kt (interfaces)
├── domain/usecase/{player,game,round,stats}/*.kt
├── di/{DatabaseModule, RepositoryModule}.kt
└── ui/{navigation, theme, components, home, player, game/{setup,active,summary}, history}
```

## Verification

- `./gradlew assembleDebug` compiles after each phase
- Manual testing on emulator/device after each phase
- Verify round sequence: double-6 through double-0 and back
- Verify shaker rotation cycles through all 4 players
- Verify score totals and winner determination
- Test pause/resume preserves full game state
- Test game history and player stats accuracy
