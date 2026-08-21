package alex.momotov.reality;

/** Everything the player can press. Terminal byte sequences are decoded in ui/. */
public enum Key {

    UP(Direction.UP),
    DOWN(Direction.DOWN),
    LEFT(Direction.LEFT),
    RIGHT(Direction.RIGHT),
    PAUSE(null),
    QUIT(null);

    public final Direction direction;

    Key(Direction direction) {
        this.direction = direction;
    }
}
