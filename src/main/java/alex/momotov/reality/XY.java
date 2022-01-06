package alex.momotov.reality;

import java.util.Objects;

public class XY {

    public int x;
    public int y;

    public XY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        XY xy = (XY) o;
        return x == xy.x && y == xy.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
