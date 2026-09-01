# Jeweled — Color-Blind Friendly Bejeweled Clone (Java 25)

A match-3 gem-swap game in pure Java Swing. Designed to be playable by
people with color vision deficiencies **without relying on color alone**.

## Run

```sh
./run.sh
```

Or manually:

```sh
javac -d out src/com/jeweled/*.java
java -cp out com.jeweled.Game
```

## How to play

- **Tap two adjacent gems** to swap them (or tap one, then its neighbor).
- Form a row/column of **3+ matching gems** to clear them and score.
- Matches **cascade**: gems above fall down, new gems refill from the top,
  and further matches are resolved automatically.
- The game ends when no legal moves remain — click the board to reshuffle.

Scoring awards `cleared × 10 × combo`, so chains earn bonus points.

## Color-blind friendly design

Two independent visual channels identify every gem, so it works for
protanopia, deuteranopia, and tritanopia:

1. **Color** — a palette of six hues chosen to stay distinct across common
   color vision deficiencies (red, yellow, blue, green, purple, orange).
2. **Shape** — each color also has a unique symbol:
   - Red    = filled circle (berry)
   - Yellow = star
   - Blue   = diamond (rhombus)
   - Green  = triangle
   - Purple = square
   - Orange = hexagon

A **legend in the header** maps each shape to its name, so players can learn
the mapping at a glance.

## Files

| File | Purpose |
|------|---------|
| `src/com/jeweled/Game.java` | Entry point / Swing frame |
| `src/com/jeweled/GamePanel.java` | Rendering, input, header legend |
| `src/com/jeweled/Board.java` | Grid model, matching, gravity, cascades, scoring |
| `src/com/jeweled/GemType.java` | Gem colors + color-blind shape symbols |
