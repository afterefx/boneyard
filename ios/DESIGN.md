# 🎨 UI/UX Design Guide — PipTally

This document outlines the visual identity, typography system, component specifications, and Apple Human Interface Guidelines (HIG) compliance principles for PipTally.

---

## 🎨 Palette & Color Archetypes

The app integrates custom adaptive dark-mode colors alongside harmonious player profile color lists:

### Core App Colors
* **DominoBlack (`#1A1A1A`)**: Used as the primary bone-black background for drawn domino tiles.
* **DominoBone (`#F5F0E8`)**: High-contrast warm ivory cream used to render vector domino pips, mimicking real bone-carved tiles.
* **TrophyGold (`#FFC107`)**: Premium warm amber used to accent round winners (stars and trophies).
* **AppBackground (Adaptive)**: Translates to iOS `.systemGroupedBackground` (clean off-white in light mode, deep carbon in dark mode).
* **AppSurface (Adaptive)**: Translates to iOS `.secondarySystemGroupedBackground` (pure white card decks in light mode, dark gray in dark mode).

### Roster Profile Harmony Colors
Players are assigned highly vibrant, contrasting pastel hex values designed to stand out against standard system cards:
* 🔴 Red: `#E53935`
* 🔵 Blue: `#1E88E5`
* 🟢 Green: `#43A047`
* 🟠 Orange: `#FB8C00`
* 🟣 Purple: `#8E24AA`
* 🌐 Cyan: `#00ACC1`
* 💗 Pink: `#E91E63`
* 🟤 Brown: `#6D4C41`

---

## 📐 Vector Canvas Domino Tiles (`DominoTile.swift`)

Domino tiles are drawn programmatically using SwiftUI's **`Canvas`** API instead of raster images. This achieves razor-sharp rendering at any screen resolution or device scaling factor:

```text
       VERTICAL TILE                   HORIZONTAL TILE
     ┌───────────────┐               ┌───────────────┬───────────────┐
     │  ●    o    ●  │               │  ●    o    ●  │  ●         ●  │
     │               │               │               │               │
     │  ●    o    ●  │               │  ●    o    ●  │  ●         ●  │
     ├───────────────┤               └───────────────┴───────────────┘
     │   Divider     │
     ├───────────────┤
     │  ●         ●  │               * Horizontal tiles are used when pips
     │               │                 alignment makes sense (e.g. Double-6
     │  ●         ●  │                 in landscape grid lists).
     └───────────────┘
```

### Rendering Rules
1. **Bone Outline**: The frame is drawn as a rounded rectangle with `DominoBlack` fill and a very subtle inner drop-shadow.
2. **Modular Grid Partitioning**: The canvas space is split in half by a custom horizontal/vertical bone divider.
3. **Pip Mathematics**: Pips (dots) are drawn as circles. Dot positions are computed based on standard canonical fractions (`1/4`, `1/2`, `3/4`) of the partition dimensions.
4. **Adaptive Rotation**: The view evaluates the active spinner value. If `value == 0` (unplayed blank double), it renders a clean blank partition. If the value increases, the dots are dynamically laid out matching actual domino face coordinates.

---

## 📱 Human Interface Guidelines (HIG) Compliance

1. **Fluid Navigation Stack**:
   - Matches iOS navigation patterns using a clean `NavigationStack` container.
   - All headers, edit flows, setup steps, and summaries transit smoothly with system animations.
2. **Sheet Modals**:
   - Modifying or registering players (`PlayerEditView`) launches via standard sheets with native corner rounding, and dismisses using intuitive Swipe-to-Dismiss gestures or clean navigation cancel actions.
3. **High-Contrast Typography**:
   - Uses Apple's standard system font family (`San Francisco`).
   - Standardizes font sizes (Header Title, Body, Captions) and weights (`.bold` for numbers/standings, `.medium` for profiles) to maintain visual hierarchy.
4. **Adaptive Text Contrasts**:
   - Incorporates a custom luminance calculator (`isDark(colorHex:)`) in `PlayerAvatar.swift` using the standard formula:
     $$\text{Luminance} = 0.299R + 0.587G + 0.114B$$
   - If the background color's calculated luminance is less than `0.5`, avatar text automatically renders in solid white. If it's a bright color, it transitions to a dark gray (`#1A1A1A`) to ensure perfect readability.
5. **No Clutter & Resiliency**:
   - Dynamic seating adjustments, infinite chevrons, and unplayed indicators maintain clear boundaries.
   - Inputs filter out non-numeric characters and cap entries at 3 digits to avoid field overflow.
