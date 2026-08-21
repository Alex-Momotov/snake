package alex.momotov.reality;

import org.junit.Test;

import static alex.momotov.reality.Direction.*;
import static org.junit.Assert.*;

public class DirectionTest {

    @Test
    public void oppositesPairUp() {
        assertEquals(DOWN, UP.opposite());
        assertEquals(UP, DOWN.opposite());
        assertEquals(RIGHT, LEFT.opposite());
        assertEquals(LEFT, RIGHT.opposite());
    }

    @Test
    public void isOppositeIsTrueOnlyForTheOpposite() {
        for (Direction d : values()) {
            for (Direction other : values())
                assertEquals(d.opposite() == other, d.isOpposite(other));
        }
    }

    @Test
    public void deltasMatchScreenCoordinates() {
        assertEquals(-1, UP.rowDelta);
        assertEquals(1, DOWN.rowDelta);
        assertEquals(-1, LEFT.colDelta);
        assertEquals(1, RIGHT.colDelta);
        for (Direction d : values())
            assertEquals(0, d.rowDelta + d.opposite().rowDelta + d.colDelta + d.opposite().colDelta);
    }
}
