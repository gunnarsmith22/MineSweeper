/** Preset board configurations shared by the console and GUI front ends. */
public enum Difficulty {
    BEGINNER("Beginner", 9, 9, 10),
    INTERMEDIATE("Intermediate", 16, 16, 40),
    EXPERT("Expert", 16, 30, 99);

    public final String label;
    public final int rows;
    public final int cols;
    public final int mines;

    Difficulty(String label, int rows, int cols, int mines) {
        this.label = label;
        this.rows = rows;
        this.cols = cols;
        this.mines = mines;
    }
}
