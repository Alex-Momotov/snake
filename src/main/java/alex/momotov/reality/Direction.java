package alex.momotov.reality;

public enum Direction {

    UP(65),
    DOWN(66),
    LEFT(68),
    RIGHT(67);

    public final int keyCode;

    Direction(int keyCode) {
        this.keyCode = keyCode;
    }

    public static Direction fromCode(int keyCode) {
        for (Direction d : values()) {
            if (d.keyCode == keyCode)
                return d;
        }
        return null;
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
