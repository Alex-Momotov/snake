package alex.momotov.reality;

public enum Direction {

    UP(16),
    DOWN(14),
    LEFT(2),
    RIGHT(6);

    public final int keyCode;

    Direction(int keyCode) {
        this.keyCode = keyCode;
    }

    public static Direction fromCode(int keyCode) {
        for (Direction d : values()) {
            if (d.keyCode == keyCode)
                return d;
        }
        throw new RuntimeException("Invalid key code - not a direction");
    }

    public boolean isOposite(Direction direction) {
        if ((this == UP && direction == DOWN) ||
            (this == DOWN && direction == UP) ||
            (this == LEFT && direction == RIGHT) ||
            (this == RIGHT && direction == LEFT)) {
            return true;
        }
        return false;
    }

}
