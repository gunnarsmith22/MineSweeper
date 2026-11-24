import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * The Minesweeper grid. Owns all game logic: mine placement,
 * adjacent-mine counting, flood-fill reveal, and win/loss detection.
 *
 * Data structures used:
 *  - Cell[][]        2D array for the grid
 *  - List<int[]>     candidate positions, shuffled for random mine placement
 *  - Deque<int[]>    iterative flood fill (avoids stack overflow on large boards)
 */
public class Board {
    private static final int[][] DIRECTIONS = {
        {-1, -1}, {-1, 0}, {-1, 1},
        { 0, -1},          { 0, 1},
        { 1, -1}, { 1, 0}, { 1, 1}
    };

    private final int rows;
    private final int cols;
    private final int mineCount;
    private final Cell[][] grid;
    private final Random random;

    private boolean minesPlaced = false;
    private int revealedCount = 0;
    private int flagCount = 0;

    public Board(int rows, int cols, int mineCount) {
        this(rows, cols, mineCount, new Random());
    }

    /** Accepts a Random so tests can use a fixed seed for reproducible boards. */
    public Board(int rows, int cols, int mineCount, Random random) {
        if (rows < 2 || cols < 2) {
            throw new IllegalArgumentException("Board must be at least 2x2.");
        }
        // Leave room for a 3x3 safe zone around the first click.
        if (mineCount < 1 || mineCount > rows * cols - 9) {
            throw new IllegalArgumentException("Invalid mine count for this board size.");
        }
        this.rows = rows;
        this.cols = cols;
        this.mineCount = mineCount;
        this.random = random;
        this.grid = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new Cell();
            }
        }
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getMineCount() { return mineCount; }
    public int getFlagsRemaining() { return mineCount - flagCount; }

    /** Returns the cell at (r, c). Used by front ends to draw the board. */
    public Cell getCell(int r, int c) {
        return grid[r][c];
    }

    public boolean inBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    /**
     * Reveals a cell. Mines are placed on the first reveal so the
     * first click (and its neighbors) is always safe.
     *
     * @return true if the player hit a mine
     */
    public boolean reveal(int r, int c) {
        Cell cell = grid[r][c];
        if (cell.isRevealed() || cell.isFlagged()) {
            return false;
        }
        if (!minesPlaced) {
            placeMines(r, c);
        }
        if (cell.isMine()) {
            cell.reveal();
            return true;
        }
        floodReveal(r, c);
        return false;
    }

    /** Places or removes a flag on a hidden cell. */
    public void toggleFlag(int r, int c) {
        Cell cell = grid[r][c];
        if (cell.isRevealed()) {
            return;
        }
        cell.toggleFlag();
        flagCount += cell.isFlagged() ? 1 : -1;
    }

    /** The game is won when every non-mine cell has been revealed. */
    public boolean isWon() {
        return revealedCount == rows * cols - mineCount;
    }

    // ---------- internal helpers ----------

    private void placeMines(int safeRow, int safeCol) {
        List<int[]> candidates = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                boolean nearFirstClick = Math.abs(r - safeRow) <= 1 && Math.abs(c - safeCol) <= 1;
                if (!nearFirstClick) {
                    candidates.add(new int[] {r, c});
                }
            }
        }
        Collections.shuffle(candidates, random);
        for (int i = 0; i < mineCount; i++) {
            int[] pos = candidates.get(i);
            grid[pos[0]][pos[1]].setMine(true);
        }
        computeAdjacentCounts();
        minesPlaced = true;
    }

    private void computeAdjacentCounts() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c].isMine()) {
                    continue;
                }
                int count = 0;
                for (int[] d : DIRECTIONS) {
                    int nr = r + d[0];
                    int nc = c + d[1];
                    if (inBounds(nr, nc) && grid[nr][nc].isMine()) {
                        count++;
                    }
                }
                grid[r][c].setAdjacentMines(count);
            }
        }
    }

    /** Reveals a cell and, if it has zero adjacent mines, expands outward. */
    private void floodReveal(int startR, int startC) {
        Deque<int[]> queue = new ArrayDeque<>();
        queue.push(new int[] {startR, startC});
        while (!queue.isEmpty()) {
            int[] pos = queue.pop();
            Cell cell = grid[pos[0]][pos[1]];
            if (cell.isRevealed() || cell.isFlagged() || cell.isMine()) {
                continue;
            }
            cell.reveal();
            revealedCount++;
            if (cell.getAdjacentMines() == 0) {
                for (int[] d : DIRECTIONS) {
                    int nr = pos[0] + d[0];
                    int nc = pos[1] + d[1];
                    if (inBounds(nr, nc)) {
                        queue.push(new int[] {nr, nc});
                    }
                }
            }
        }
    }

    /**
     * Builds a text view of the board.
     *
     * @param showMines if true, reveals all mines (used after the game ends)
     */
    public String render(boolean showMines) {
        StringBuilder sb = new StringBuilder();
        sb.append("    ");
        for (int c = 0; c < cols; c++) {
            sb.append(String.format("%3d", c));
        }
        sb.append("\n    ").append("---".repeat(cols)).append("\n");
        for (int r = 0; r < rows; r++) {
            sb.append(String.format("%3d|", r));
            for (int c = 0; c < cols; c++) {
                sb.append("  ").append(symbolFor(grid[r][c], showMines));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private char symbolFor(Cell cell, boolean showMines) {
        if (showMines && cell.isMine()) {
            return cell.isFlagged() ? 'F' : '*';
        }
        if (cell.isFlagged()) {
            return 'F';
        }
        if (!cell.isRevealed()) {
            return '#';
        }
        if (cell.isMine()) {
            return '*';
        }
        int n = cell.getAdjacentMines();
        return n == 0 ? '.' : (char) ('0' + n);
    }
}
