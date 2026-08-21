package alex.momotov.ui;

import alex.momotov.Ink;
import alex.momotov.reality.Board;
import alex.momotov.reality.Pos;
import alex.momotov.reality.View;
import alex.momotov.reality.objects.Obj;

/**
 * Paints the board with ANSI cursor moves, repainting only the cells that changed.
 * Owns the caret position; every write states its own visible width so the caret
 * never drifts.
 */
public class TerminalView implements View {

    public static final int CELL_WIDTH = 3;        // every cell renders as " x "

    private static final String EMPTY = Ink.paint(Ink.COAL, ".");
    private static final String HIDE_CURSOR = "\u001b[?25l";
    private static final String SHOW_CURSOR = "\u001b[?25h";

    private int caretRow;
    private int caretCol;

    @Override
    public void begin(Board board, int tick) {
        System.out.print(HIDE_CURSOR);
        for (int r = 0; r < board.rows(); r++) {
            if (r > 0)
                newLine();
            for (int c = 0; c < board.cols(); c++)
                write(paint(board, new Pos(r, c), tick), CELL_WIDTH);
        }
        newLine();                                  // the status line lives here
        moveTo(0, 0);
        System.out.flush();
        board.drainDirty();                         // the whole board is now up to date
    }

    @Override
    public void draw(Board board, int tick) {
        for (Pos pos : board.drainDirty()) {
            moveTo(pos.row, pos.col * CELL_WIDTH);
            write(paint(board, pos, tick), CELL_WIDTH);
        }
        repaintAnimated(board, tick);
        System.out.flush();
    }

    @Override
    public void hud(Board board, String text) {
        int width = board.cols() * CELL_WIDTH;
        String padded = text.length() > width ? text.substring(0, width) : text;
        StringBuilder line = new StringBuilder(padded);
        while (line.length() < width)
            line.append(' ');
        moveTo(board.rows(), 0);
        write(Ink.bold(Ink.SILVER, line.toString()), width);
        System.out.flush();
    }

    @Override
    public void banner(Board board, String text) {
        String padded = "  " + text + "  ";
        int row = bannerRow(board);
        int col = Math.max(0, (board.cols() * CELL_WIDTH - padded.length()) / 2);
        moveTo(row, col);
        write(Ink.badge(Ink.COAL, Ink.GOLD, padded), padded.length());
        System.out.flush();
    }

    @Override
    public void clearBanner(Board board) {
        board.touchRow(bannerRow(board));
    }

    /** Kept clear of the middle, where the snake spawns and spends its time. */
    private static int bannerRow(Board board) {
        return Math.max(1, board.rows() / 3);
    }

    @Override
    public void finish(Board board) {
        moveTo(board.rows(), 0);
        newLine();
        System.out.print(SHOW_CURSOR);
        System.out.flush();
    }

    /**
     * Blinking treats and twitching creatures change without the model changing, so the
     * cells holding them are repainted every few ticks regardless of the dirty list.
     */
    private void repaintAnimated(Board board, int tick) {
        if (Math.floorMod(tick, 2) != 0)
            return;
        for (Pos pos : board.animated()) {
            moveTo(pos.row, pos.col * CELL_WIDTH);
            write(paint(board, pos, tick), CELL_WIDTH);
        }
    }

    private String paint(Board board, Pos pos, int tick) {
        Obj top = board.cell(pos).top();
        return " " + (top == null ? EMPTY : top.glyph(tick)) + " ";
    }

    /** Cursor moves clamp at the edges, so the caret model must clamp identically. */
    private void moveTo(int row, int col) {
        if (row < caretRow)
            System.out.print(cursor(caretRow - row, "A"));
        else if (row > caretRow)
            System.out.print(cursor(row - caretRow, "B"));

        if (col < caretCol)
            System.out.print(cursor(caretCol - col, "D"));
        else if (col > caretCol)
            System.out.print(cursor(col - caretCol, "C"));

        caretRow = row;
        caretCol = col;
    }

    private static String cursor(int amount, String code) {
        return "\u001b[" + amount + code;
    }

    private void write(String text, int visibleWidth) {
        System.out.print(text);
        caretCol += visibleWidth;
    }

    /** Carriage return as well as line feed: the terminal may be in raw mode by now. */
    private void newLine() {
        System.out.print("\r\n");
        caretRow += 1;
        caretCol = 0;
    }
}
