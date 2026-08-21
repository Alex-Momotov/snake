package alex.momotov.reality;

import org.junit.Test;

import static org.junit.Assert.*;

public class PosTest {

    @Test
    public void stepMovesOneSquare() {
        Pos start = new Pos(5, 5);
        assertEquals(new Pos(4, 5), start.step(Direction.UP));
        assertEquals(new Pos(6, 5), start.step(Direction.DOWN));
        assertEquals(new Pos(5, 4), start.step(Direction.LEFT));
        assertEquals(new Pos(5, 6), start.step(Direction.RIGHT));
    }

    @Test
    public void equalityIsByValue() {
        assertEquals(new Pos(2, 3), new Pos(2, 3));
        assertEquals(new Pos(2, 3).hashCode(), new Pos(2, 3).hashCode());
        assertNotEquals(new Pos(2, 3), new Pos(3, 2));
    }
}
