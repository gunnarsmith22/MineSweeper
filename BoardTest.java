import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardTest {

    private static int countMines(Board board) {
        int count = 0;
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                if (board.getCell(r, c).isMine()) {
                    count++;
                }
            }
        }
        return count;
    }

    private static int countRevealed(Board board) {
        int count = 0;
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                if (board.getCell(r, c).isRevealed()) {
                    count++;
                }
            }
        }
        return count;
    }

    @Test
    void placesExactNumberOfMines() {
        Board board = new Board(9, 9, 10, new Random(1));
        board.reveal(4, 4);
        assertEquals(10, countMines(board));
    }

    @Test
    void firstClickAndItsNeighborsAreAlwaysSafe() {
        int[][] clicks = {{0, 0}, {0, 8}, {8, 0}, {8, 8}, {4, 4}, {0, 4}};
        for (int seed = 0; seed < 50; seed++) {
            for (int[] click : clicks) {
                Board board = new Board(9, 9, 10, new Random(seed));
                assertFalse(board.reveal(click[0], click[1]), "first click hit a mine");
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        int r = click[0] + dr;
                        int c = click[1] + dc;
                        if (board.inBounds(r, c)) {
                            assertFalse(board.getCell(r, c).isMine());
                        }
                    }
                }
            }
        }
    }

    @Test
    void adjacentCountsMatchNeighboringMines() {
        Board board = new Board(9, 9, 10, new Random(5));
        board.reveal(4, 4);
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                if (board.getCell(r, c).isMine()) {
                    continue;
                }
                int expected = 0;
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if ((dr != 0 || dc != 0) && board.inBounds(r + dr, c + dc)
                                && board.getCell(r + dr, c + dc).isMine()) {
                            expected++;
                        }
                    }
                }
                assertEquals(expected, board.getCell(r, c).getAdjacentMines());
            }
        }
    }

    @Test
    void revealingEmptyCellFloodFillsNeighbors() {
        Board board = new Board(10, 10, 1, new Random(7));
        board.reveal(0, 0);
        assertEquals(0, board.getCell(0, 0).getAdjacentMines());
        assertTrue(countRevealed(board) > 1, "flood fill should reveal more than one cell");
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                if (board.getCell(r, c).isRevealed()) {
                    assertFalse(board.getCell(r, c).isMine(), "flood fill must never reveal a mine");
                }
            }
        }
    }

    @Test
    void revealingAMineReturnsTrue() {
        Board board = new Board(9, 9, 10, new Random(3));
        board.reveal(0, 0);
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                if (board.getCell(r, c).isMine()) {
                    assertTrue(board.reveal(r, c));
                    return;
                }
            }
        }
    }

    @Test
    void revealingEverySafeCellWinsTheGame() {
        Board board = new Board(9, 9, 10, new Random(3));
        assertFalse(board.isWon());
        board.reveal(0, 0);
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                if (!board.getCell(r, c).isMine()) {
                    board.reveal(r, c);
                }
            }
        }
        assertTrue(board.isWon());
    }

    @Test
    void flaggedCellsCannotBeRevealed() {
        Board board = new Board(9, 9, 10, new Random(2));
        board.toggleFlag(0, 0);
        assertEquals(9, board.getFlagsRemaining());
        assertFalse(board.reveal(0, 0));
        assertFalse(board.getCell(0, 0).isRevealed());

        board.toggleFlag(0, 0);
        assertEquals(10, board.getFlagsRemaining());
    }

    @Test
    void rejectsInvalidBoardConfigurations() {
        assertThrows(IllegalArgumentException.class, () -> new Board(1, 5, 1));
        assertThrows(IllegalArgumentException.class, () -> new Board(9, 9, 0));
        assertThrows(IllegalArgumentException.class, () -> new Board(3, 3, 1)); // no room for safe zone
    }
}
