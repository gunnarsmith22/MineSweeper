# Minesweeper (Java)

A playable Minesweeper built with Java, object-oriented design, and core data structures. It ships with a Swing GUI and a console version that share the same game engine.

<!-- Add a screenshot or GIF after you run it: save it as docs/screenshot.png and uncomment the line below.
![Minesweeper screenshot](docs/screenshot.png)
-->

## Features

- Swing GUI with colored numbers, a mine counter, a timer, and a reset button
- Console version for terminal play
- Three difficulty levels (Beginner 9x9, Intermediate 16x16, Expert 16x30)
- Random mine placement with a guaranteed safe first click
- Automatic flood-fill expansion of empty regions
- Win/loss detection, with mines revealed and wrong flags marked on a loss
- JUnit 5 test suite covering the core game logic

## Requirements

- JDK 17 or newer
- Maven (optional, only needed to run the tests)

## How to Run

**GUI**
```bash
mkdir -p out
javac -d out src/main/java/*.java
java -cp out MinesweeperGUI
```

**Console**
```bash
java -cp out Minesweeper
```

**Tests**
```bash
mvn test
```

## How to Play

**GUI:** left click reveals a cell, right click places or removes a flag. Click the face button or use the Game menu to start a new game or change difficulty.

**Console:**

| Command | Action |
|---------|--------|
| `r <row> <col>` | Reveal a cell (e.g. `r 3 4`) |
| `f <row> <col>` | Flag or unflag a cell (e.g. `f 0 2`) |
| `q` | Quit |

Console symbols: `#` hidden, `F` flag, `.` empty, `1-8` adjacent mine count, `*` mine.

Reveal every cell that doesn't contain a mine to win.

## Project Structure

```
src/
├── main/java/
│   ├── Cell.java            # One square: mine, revealed, flagged, adjacent count
│   ├── Board.java           # Game engine: grid, mine placement, flood fill, win detection
│   ├── Difficulty.java      # Board presets shared by both front ends
│   ├── MinesweeperGUI.java  # Swing front end
│   └── Minesweeper.java     # Console front end
└── test/java/
    └── BoardTest.java       # JUnit 5 tests for Board
```

## Design Notes

- **Separation of concerns:** `Board` and `Cell` contain all game rules and know nothing about the UI. The GUI and console front ends are thin layers that call into the same engine.
- **2D array (`Cell[][]`):** stores the grid for O(1) access by row and column.
- **`Deque` flood fill:** revealing an empty cell expands outward iteratively with an `ArrayDeque`, which avoids recursion depth issues on large boards.
- **`List` + shuffle:** mine positions are chosen by shuffling all eligible cells, which guarantees exactly N unique mines and excludes the first-click safe zone.
- **Enums:** `Difficulty` and the console's `State` keep configuration and game state type-safe.
- **Testability:** `Board` accepts a seeded `Random`, so tests can reproduce boards deterministically.

## Possible Improvements

- Chording (reveal neighbors of a satisfied number)
- High-score tracking
- Custom board sizes
- Solver or hint feature
