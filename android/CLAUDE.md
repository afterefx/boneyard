# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository layout

This is a monorepo with two platform directories:
- `android/` — Android app (Kotlin/Compose), this is the primary working directory
- `ios/` — iOS app (recently added, sibling directory)

All commands below assume you are in `android/`.

## Build & run commands

```bash
# Build debug APK
./gradlew assembleDebug

# Run all unit tests
./gradlew test

# Run a single test class
./gradlew test --tests "app.boneyard.SomeTest"

# Install on connected device
./gradlew installDebug

# Check for lint issues
./gradlew lint
```

## Architecture

Clean Architecture with three layers inside `app/src/main/java/app/boneyard/`:

### Domain layer (`domain/`)
- `model/` — pure Kotlin data classes: `Game`, `Player`, `GamePlayer`, `GameWithPlayers`, `Round`, `RoundScore`, `PlayerStats`, `GameStatus`
- `repository/` — interfaces only; no Android imports
- `usecase/` — single-responsibility use cases; each is a class with an `invoke` operator

### Data layer (`data/`)
- `local/entity/` — Room `@Entity` classes (5 tables: `PlayerEntity`, `GameEntity`, `GamePlayerEntity`, `RoundEntity`, `RoundScoreEntity`)
- `local/dao/` — Room DAOs with Flow-returning queries
- `local/AppDatabase` — single `RoomDatabase`; DB file is named `boneyard.db`
- `mapper/` — extension functions `toDomain()` / `toEntity()` for converting between entity and domain models
- `repository/` — `*RepositoryImpl` classes; `SettingsRepositoryImpl` stores theme preference in SharedPreferences

### DI layer (`di/`)
- **Metro** (`dev.zacsweers.metro`) replaces Hilt
- `AppScope.kt` — scope marker object
- `AppGraph.kt` — `@DependencyGraph` interface exposing `settingsRepository` and `circuit`; factory created in `BoneyardApp`
- `DatabaseBindings.kt` — `@BindingContainer` providing `AppDatabase` and all DAOs
- Repository impls use `@ContributesBinding(AppScope::class)` + `@SingleIn(AppScope::class)` — no separate bindings module needed

### UI layer (`ui/`)
- **Circuit** (`com.slack.circuit`) replaces ViewModel + Compose Navigation
- `navigation/Screens.kt` — all 8 `@Parcelize Screen` data objects/classes (route keys)
- Each feature folder has `*Screen.kt` (Compose UI with `@CircuitInject`) and `*Presenter.kt` (logic with `@AssistedInject` + `@AssistedFactory`)
- Presenter pattern: `@AssistedInject constructor(@Assisted navigator: Navigator, ...)` with an inner `@AssistedFactory @CircuitInject(FooScreen::class, AppScope::class) fun interface Factory`
- UiState implements `CircuitUiState` and carries `eventSink: (FooEvent) -> Unit`; sealed interface `FooEvent : CircuitUiEvent`
- Composable renamed from `FooScreen` to `FooUi`, annotated `@CircuitInject(FooScreen::class, AppScope::class)`, takes `(state: FooUiState, modifier: Modifier = Modifier)`
- `components/` — shared composables (`PlayerAvatar`, `DominoTile`, `ScoreEntryRow`, `ScoreboardTable`, `RoundIndicator`, `ConfirmDialog`, `EmptyState`)
- `theme/` — `BoneyardTheme` with three `AppTheme` variants: `SYSTEM`, `OLED`, `SLATE`

### Metro + Circuit integration notes
- Metro's annotation for assisted-inject constructors is `@AssistedInject` (not `@Inject`) when `@Assisted` params are present; regular constructors use `@Inject`
- All annotations live in `dev.zacsweers.metro.*` (from `runtime-jvm` artifact, NOT `metro-common` or `metro-runtime`)
- Circuit KSP codegen uses `circuit.codegen.mode = "metro"` to generate Metro multibinding factories from `@CircuitInject`
- `rememberSaveableNavStack` is at `com.slack.circuit.foundation.navstack.rememberSaveableNavStack` in Circuit 0.34.0

## Game rules encoded in the app

Key game logic lives in `util/GameConstants.kt`:
- 14 rounds per game; spinner sequence: `[6, 5, 4, 3, 2, 1, 0, 0, 1, 2, 3, 4, 5, 6]`
- Shaker rotates: `roundIndex % playerCount`
- **Lower score wins** — the `GameWithPlayers.leader` is `minByOrNull { it.totalScore }`
- Per-round scores are stored in `RoundScoreEntity`; cumulative totals are maintained in `GamePlayerEntity.totalScore`

## Key dependencies (from `gradle/libs.versions.toml`)

| Library | Version |
|---|---|
| AGP | 9.2.1 |
| Kotlin | 2.3.21 |
| Compose BOM | 2024.12.01 |
| Metro | 1.2.1 |
| Circuit | 0.34.0 |
| Room | 2.8.4 |
| KSP | 2.3.9 |
| MockK | 1.13.13 |

minSdk = 26, compileSdk = 36, targetSdk = 36, JVM target = 17. KSP2 is required (`ksp.useKSP2=true`).

## Known incomplete areas

`StatsRepositoryImpl` returns placeholder zeros for all stats (`gamesPlayed`, `gamesWon`, etc.). The implementation comment notes that suspend queries inside a `combine` flow require a different approach.
