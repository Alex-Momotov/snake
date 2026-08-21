package alex.momotov.reality;

/** Where the snake is heading. Terminal key codes are a UI concern and live in ui/. */
public enum Direction {

    UP(-1, 0),
    DOWN(1, 0),
    LEFT(0, -1),
    RIGHT(0, 1);

    public final int rowDelta;
    public final int colDelta;

    Direction(int rowDelta, int colDelta) {
        this.rowDelta = rowDelta;
        this.colDelta = colDelta;
    }

    public Direction opposite() {
        switch (this) {
            case UP:    return DOWN;
            case DOWN:  return UP;
            case LEFT:  return RIGHT;
            default:    return LEFT;
        }
    }

    public boolean isOpposite(Direction other) {
        return opposite() == other;
    }
}
