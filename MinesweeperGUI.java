import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.BevelBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Swing front end for Minesweeper.
 *
 * Left click reveals a cell, right click flags it. All game rules live in
 * {@link Board}; this class only draws the board and forwards input to it.
 */
public class MinesweeperGUI extends JFrame {

    private static final int CELL_SIZE = 30;
    private static final Color HIDDEN_COLOR = new Color(192, 192, 192);
    private static final Color REVEALED_COLOR = new Color(225, 225, 225);
    private static final Border RAISED_BORDER = BorderFactory.createBevelBorder(BevelBorder.RAISED);
    private static final Border FLAT_BORDER = BorderFactory.createLineBorder(Color.GRAY);

    // Index = number of adjacent mines (index 0 unused).
    private static final Color[] NUMBER_COLORS = {
        Color.BLACK,
        new Color(0, 0, 255),
        new Color(0, 128, 0),
        new Color(255, 0, 0),
        new Color(0, 0, 128),
        new Color(128, 0, 0),
        new Color(0, 128, 128),
        Color.BLACK,
        Color.GRAY
    };

    private final JPanel gridPanel = new JPanel();
    private final JLabel minesLabel = new JLabel();
    private final JLabel timeLabel = new JLabel();
    private final JButton resetButton = new JButton(":)");
    private final javax.swing.Timer timer;

    private Board board;
    private Difficulty difficulty = Difficulty.BEGINNER;
    private JButton[][] buttons;

    private int seconds;
    private boolean timerStarted;
    private boolean gameOver;
    private boolean won;

    public MinesweeperGUI() {
        super("Minesweeper");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        timer = new javax.swing.Timer(1000, e -> {
            seconds++;
            updateTimeLabel();
        });

        add(buildHeader(), BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);
        setJMenuBar(buildMenuBar());

        startNewGame(difficulty);
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ---------- UI construction ----------

    private JPanel buildHeader() {
        Font digitFont = new Font(Font.MONOSPACED, Font.BOLD, 20);
        minesLabel.setFont(digitFont);
        timeLabel.setFont(digitFont);

        resetButton.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(e -> startNewGame(difficulty));

        JPanel center = new JPanel();
        center.add(resetButton);

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        header.add(minesLabel, BorderLayout.WEST);
        header.add(center, BorderLayout.CENTER);
        header.add(timeLabel, BorderLayout.EAST);
        return header;
    }

    private JMenuBar buildMenuBar() {
        JMenu gameMenu = new JMenu("Game");

        JMenuItem newGame = new JMenuItem("New Game");
        newGame.addActionListener(e -> startNewGame(difficulty));
        gameMenu.add(newGame);
        gameMenu.addSeparator();

        for (Difficulty d : Difficulty.values()) {
            JMenuItem item = new JMenuItem(d.label);
            item.addActionListener(e -> startNewGame(d));
            gameMenu.add(item);
        }

        gameMenu.addSeparator();
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> System.exit(0));
        gameMenu.add(exit);

        JMenuBar bar = new JMenuBar();
        bar.add(gameMenu);
        return bar;
    }

    private JButton createCellButton(int r, int c) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!button.contains(e.getPoint())) {
                    return; // released outside the button: ignore
                }
                if (SwingUtilities.isRightMouseButton(e)) {
                    handleFlag(r, c);
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    handleReveal(r, c);
                }
            }
        });
        return button;
    }

    // ---------- game flow ----------

    private void startNewGame(Difficulty d) {
        timer.stop();
        seconds = 0;
        timerStarted = false;
        gameOver = false;
        won = false;
        difficulty = d;
        board = new Board(d.rows, d.cols, d.mines);

        gridPanel.removeAll();
        gridPanel.setLayout(new GridLayout(d.rows, d.cols));
        buttons = new JButton[d.rows][d.cols];
        for (int r = 0; r < d.rows; r++) {
            for (int c = 0; c < d.cols; c++) {
                buttons[r][c] = createCellButton(r, c);
                gridPanel.add(buttons[r][c]);
            }
        }

        resetButton.setText(":)");
        updateMinesLabel();
        updateTimeLabel();
        refreshAll();
        gridPanel.revalidate();
        gridPanel.repaint();
        pack();
    }

    private void handleReveal(int r, int c) {
        if (gameOver || board.getCell(r, c).isFlagged()) {
            return;
        }
        if (!timerStarted) {
            timerStarted = true;
            timer.start();
        }
        boolean hitMine = board.reveal(r, c);
        if (hitMine) {
            endGame(false);
        } else if (board.isWon()) {
            endGame(true);
        }
        refreshAll();
    }

    private void handleFlag(int r, int c) {
        if (gameOver) {
            return;
        }
        board.toggleFlag(r, c);
        updateMinesLabel();
        updateCell(r, c);
    }

    private void endGame(boolean playerWon) {
        gameOver = true;
        won = playerWon;
        timer.stop();
        resetButton.setText(playerWon ? "B)" : "X(");
        updateMinesLabel();
    }

    // ---------- drawing ----------

    private void refreshAll() {
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                updateCell(r, c);
            }
        }
    }

    private void updateCell(int r, int c) {
        Cell cell = board.getCell(r, c);
        JButton button = buttons[r][c];

        String text = "";
        Color fg = Color.BLACK;
        Color bg = HIDDEN_COLOR;
        boolean flat = false;

        if (cell.isRevealed() && cell.isMine()) {
            // The mine the player clicked.
            text = "*";
            bg = Color.RED;
            flat = true;
        } else if (cell.isRevealed()) {
            int n = cell.getAdjacentMines();
            bg = REVEALED_COLOR;
            flat = true;
            if (n > 0) {
                text = String.valueOf(n);
                fg = NUMBER_COLORS[n];
            }
        } else if (gameOver && !won && cell.isMine() && !cell.isFlagged()) {
            // Loss: show remaining mines.
            text = "*";
            bg = REVEALED_COLOR;
            flat = true;
        } else if (gameOver && !won && cell.isFlagged() && !cell.isMine()) {
            // Loss: mark incorrect flags.
            text = "X";
            fg = Color.RED;
            bg = REVEALED_COLOR;
            flat = true;
        } else if (cell.isFlagged() || (gameOver && won && cell.isMine())) {
            text = "F";
            fg = Color.RED;
        }

        button.setText(text);
        button.setForeground(fg);
        button.setBackground(bg);
        button.setBorder(flat ? FLAT_BORDER : RAISED_BORDER);
    }

    private void updateMinesLabel() {
        int remaining = (gameOver && won) ? 0 : board.getFlagsRemaining();
        minesLabel.setText(String.format("%03d", Math.max(remaining, 0)));
    }

    private void updateTimeLabel() {
        timeLabel.setText(String.format("%03d", Math.min(seconds, 999)));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Cross-platform look and feel so button colors render the same on every OS.
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fall back to the default look and feel.
            }
            new MinesweeperGUI();
        });
    }
}
