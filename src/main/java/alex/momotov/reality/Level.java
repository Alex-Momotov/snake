package alex.momotov.reality;

import alex.momotov.reality.objects.Species;
import alex.momotov.reality.objects.Wall;

import java.util.Random;

/** The shape of each world, and how much harder it gets. */
public final class Level {

    private static final String[] NAMES = {
            "THE GARDEN", "THE PILLARS", "THE CROSSROADS",
            "THE COMBS", "THE RUINS", "THE LABYRINTH"};

    private Level() {
    }

    public static String name(int level) {
        return NAMES[Math.floorMod(level - 1, NAMES.length)] + " " + numeral(level);
    }

    /** How many treats must be eaten before the way out appears. */
    public static int quota(int level) {
        return 4 + level;
    }

    public static int mobCount(int level) {
        return Math.min(1 + level, 7);
    }

    /** Hostile creatures get more likely the deeper you go. */
    public static Species inhabitant(int level, int roll) {
        if (roll % 5 == 0)
            return Species.MOUSE;
        if (roll % 11 == 0)
            return Species.HERMIT;
        if (level >= 4 && roll % 3 == 0)
            return Species.WRAITH;
        return (level >= 2 && roll % 2 == 0) ? Species.WASP : Species.SPIDER;
    }

    public static void build(Board board, int level, Random random) {
        border(board);
        switch (Math.floorMod(level - 1, NAMES.length)) {
            case 0:  break;                          // open ground to learn on
            case 1:  pillars(board); break;
            case 2:  crossroads(board); break;
            case 3:  combs(board); break;
            case 4:  ruins(board, random); break;
            default: labyrinth(board); break;
        }
    }

    private static void border(Board board) {
        for (int c = 0; c < board.cols(); c++) {
            board.add(new Pos(0, c), new Wall());
            board.add(new Pos(board.rows() - 1, c), new Wall());
        }
        for (int r = 0; r < board.rows(); r++) {
            board.add(new Pos(r, 0), new Wall());
            board.add(new Pos(r, board.cols() - 1), new Wall());
        }
    }

    private static void pillars(Board board) {
        int rowA = board.rows() / 4, rowB = board.rows() - board.rows() / 4 - 1;
        int colA = board.cols() / 4, colB = board.cols() - board.cols() / 4 - 1;
        for (int r : new int[]{rowA, rowB}) {
            for (int c : new int[]{colA, colB})
                block(board, r - 1, c - 1, 3, 3);
        }
    }

    private static void crossroads(Board board) {
        int midRow = board.rows() / 2, midCol = board.cols() / 2;
        int gap = 3;
        for (int c = 2; c < board.cols() - 2; c++) {
            if (Math.abs(c - midCol) > gap)
                board.add(new Pos(midRow, c), new Wall());
        }
        for (int r = 2; r < board.rows() - 2; r++) {
            if (Math.abs(r - midRow) > gap)
                board.add(new Pos(r, midCol), new Wall());
        }
    }

    private static void combs(Board board) {
        for (int c = 3; c < board.cols() - 3; c += 4) {
            boolean fromTop = (c / 4) % 2 == 0;
            int length = board.rows() / 2;
            for (int i = 0; i < length; i++) {
                int r = fromTop ? 1 + i : board.rows() - 2 - i;
                board.add(new Pos(r, c), new Wall());
            }
        }
    }

    private static void ruins(Board board, Random random) {
        int chunks = Math.max(4, board.rows() * board.cols() / 90);
        for (int i = 0; i < chunks; i++) {
            int r = 2 + random.nextInt(Math.max(1, board.rows() - 5));
            int c = 2 + random.nextInt(Math.max(1, board.cols() - 5));
            block(board, r, c, 1 + random.nextInt(2), 1 + random.nextInt(3));
        }
    }

    private static void labyrinth(Board board) {
        for (int r = 2; r < board.rows() - 2; r += 3) {
            for (int c = 2; c < board.cols() - 2; c++) {
                boolean doorway = (c + r) % 7 < 2;
                if (!doorway)
                    board.add(new Pos(r, c), new Wall());
            }
        }
    }

    private static void block(Board board, int row, int col, int height, int width) {
        for (int r = row; r < row + height; r++) {
            for (int c = col; c < col + width; c++) {
                Pos pos = new Pos(r, c);
                if (board.inBounds(pos) && r > 0 && c > 0 && r < board.rows() - 1 && c < board.cols() - 1)
                    board.add(pos, new Wall());
            }
        }
    }

    private static String numeral(int level) {
        String[] numerals = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return level <= numerals.length ? numerals[level - 1] : String.valueOf(level);
    }
}
