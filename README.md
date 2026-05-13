# Borders Game
### A Geography Game by Lily Kassaei

**Borders** is an interactive 3D globe game where you guess countries one at a time. Every guess after your first must share a border with your previous guess. Navigate the world strategically, chain your guesses across continents, and try to name all 196 countries before making 10 mistakes.

---

## Gameplay

1. **Start anywhere** — type any country name to place your first guess on the globe.
2. **Chain your guesses** — each subsequent country must border one you've already guessed.
3. **You lose** if you make **10 invalid guesses** (countries that don't border any of your previous guesses).
4. **You win** if you successfully guess all **196 countries** before running out of attempts.
5. Type `clear` at any time to reset the board and start over.

A live **timer** and **guess counter** (e.g. `47/196`) are displayed on screen throughout the game.

---

## Features

- **3D Interactive Globe** — rendered in JavaFX with a draggable, rotatable globe. Click and drag to spin it freely.
- **Country Fill & Outline** — correctly guessed countries are drawn on the globe with a filled highlight and border outline, using a ray-casting algorithm and GeoJSON coordinate data.
- **Auto-Continent Camera** — the globe automatically rotates to center on the continent of your most recent guess.
- **Fuzzy Search / Typo Correction** — mistyped a country? The game uses a bigram index combined with Levenshtein distance to suggest the closest match. Type `yes` to confirm the suggestion.
- **Mistake Tracker** — up to 10 red X markers are drawn on screen, one per invalid guess.
- **Win/Lose Screen** — a full-screen result is shown when the game ends.

---

## Project Structure
src/
├── Borders.java       # Main application — JavaFX setup, game logic, rendering, input handling, fuzzy search
├── Country.java       # Country model — GeoJSON parsing, border loading, point-in-polygon logic
resources/
├── countries.geojson                  # GeoJSON geometry for all countries (used for drawing)
├── GEODATASOURCE-COUNTRY-BORDERS.CSV  # Border relationships between countries (used for neighbor validation)

---

## How It Works

### Rendering
Country shapes are sourced from a GeoJSON file parsed at startup into a cache (`polygonCache`). Each country is stored as one or more rings (to handle islands and non-contiguous territories). Countries are drawn on the globe surface as dense grids of small 3D spheres — one pass for the filled interior and one for the outline. Coordinates are converted from latitude/longitude to 3D XYZ using standard spherical projection.

### Border Validation
Neighbor relationships are loaded from a CSV file. When a country is guessed, its neighbors are read from the CSV and added to a running pool of valid next guesses. A guess is only accepted if it appears in that pool (or is the very first guess).

### Fuzzy Matching
When an unrecognized input is entered, the game:
1. Builds a **bigram index** over all known country names.
2. Finds **candidates** — words sharing bigrams with the input, plus words of similar length.
3. Runs **Levenshtein distance** on all candidates with early-exit pruning at a threshold of 2.
4. Returns the closest match sorted by edit distance, then alphabetically.

---

## Requirements

- Java 17+
- JavaFX SDK
- Maven (or your preferred build tool with JavaFX configured)

---

## Running the Game

```bash
mvn javafx:run
```

Or run `Borders.main()` directly from your IDE with JavaFX on the module path.

---

## Controls

| Action | Input |
|---|---|
| Guess a country | Type name + press Enter |
| Confirm a typo suggestion | Type `yes` + press Enter |
| Reset the board | Type `clear` + press Enter |
| Rotate the globe | Click and drag |