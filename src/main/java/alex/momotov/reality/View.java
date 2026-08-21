package alex.momotov.reality;

/** What the game needs drawn. Implemented by ui/, so the game never sees a terminal. */
public interface View {

    void begin(Board board, int tick);

    /** Repaints whatever changed since the last call. */
    void draw(Board board, int tick);

    /** The status line under the board. */
    void hud(Board board, String text);

    /** A short message across the board, clear of where the snake starts. */
    void banner(Board board, String text);

    /** Wipes the last banner away again. */
    void clearBanner(Board board);

    void finish(Board board);
}
