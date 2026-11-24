import java.util.Scanner;

/**
 * Entry point and game loop. Handles user input and manages game state.
 *
 * Commands:
 *   r <row> <col>   reveal a cell
 *   f <row> <col>   flag / unflag a cell
 *   q               quit
 */
public class Minesweeper {

    private enum State { PLAYING, WON, LOST, QUIT }

    private final Scanner scanner = new Scanner(System.in);
    private Board board;
    private State state = State.PLAYING;

    public static void main(String[] args) {
        new Minesweeper().run();
    }

    private void run() {
        System.out.println("=== MINESWEEPER ===");
        Difficulty difficulty = chooseDifficulty();
        board = new Board(difficulty.rows, difficulty.cols, difficulty.mines);

        System.out.println("\nCommands: r <row> <col> = reveal | f <row> <col> = flag | q = quit");

        while (state == State.PLAYING) {
            System.out.println("\nFlags remaining: " + board.getFlagsRemaining());
            System.out.println(board.render(false));
            handleInput();
        }

        System.out.println();
        System.out.println(board.render(true));
        switch (state) {
            case WON -> System.out.println("You cleared the board. You win!");
            case LOST -> System.out.println("BOOM! You hit a mine. Game over.");
            default -> System.out.println("Thanks for playing.");
        }
    }

    private Difficulty chooseDifficulty() {
        while (true) {
            System.out.println("Choose difficulty:");
            System.out.println("  1) Beginner     (9x9, 10 mines)");
            System.out.println("  2) Intermediate (16x16, 40 mines)");
            System.out.println("  3) Expert       (16x30, 99 mines)");
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            switch (line) {
                case "1": return Difficulty.BEGINNER;
                case "2": return Difficulty.INTERMEDIATE;
                case "3": return Difficulty.EXPERT;
                default: System.out.println("Please enter 1, 2, or 3.\n");
            }
        }
    }

    private void handleInput() {
        System.out.print("> ");
        if (!scanner.hasNextLine()) {
            state = State.QUIT;
            return;
        }
        String[] parts = scanner.nextLine().trim().toLowerCase().split("\\s+");

        if (parts[0].equals("q")) {
            state = State.QUIT;
            return;
        }
        if (parts.length != 3 || !(parts[0].equals("r") || parts[0].equals("f"))) {
            System.out.println("Invalid command. Try: r 3 4  or  f 3 4  or  q");
            return;
        }

        int row, col;
        try {
            row = Integer.parseInt(parts[1]);
            col = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            System.out.println("Row and column must be numbers.");
            return;
        }
        if (!board.inBounds(row, col)) {
            System.out.println("That cell is off the board.");
            return;
        }

        if (parts[0].equals("f")) {
            board.toggleFlag(row, col);
        } else if (board.reveal(row, col)) {
            state = State.LOST;
        } else if (board.isWon()) {
            state = State.WON;
        }
    }
}
