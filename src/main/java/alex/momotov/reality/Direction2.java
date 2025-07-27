package alex.momotov.reality;

public enum Direction2 {

    UP(119),
    DOWN(115),
    LEFT(97),
    RIGHT(100);

    public final int keyCode;

    Direction2(int keyCode) {
        this.keyCode = keyCode;
    }

    public static Direction2 fromCode(int keyCode) {
        for (Direction2 d : values()) {
            if (d.keyCode == keyCode)
                return d;
        }
        return null;
    }

    public boolean isOposite(Direction2 direction) {
        if ((this == UP && direction == DOWN) ||
            (this == DOWN && direction == UP) ||
            (this == LEFT && direction == RIGHT) ||
            (this == RIGHT && direction == LEFT)) {
            return true;
        }
        return false;
    }

}
