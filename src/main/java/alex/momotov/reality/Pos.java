package alex.momotov.reality;

import java.util.Objects;

/** An immutable board coordinate. Named row/col because that is what they are. */
public final class Pos {

    public final int row;
    public final int col;

    public Pos(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public Pos step(Direction direction) {
        return new Pos(row + direction.rowDelta, col + direction.colDelta);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pos other = (Pos) o;
        return row == other.row && col == other.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }
}
