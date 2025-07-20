package alex.momotov.reality;

import jline.console.ConsoleReader;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class DirectionTest {


    @Test
    public void testIsOpositeTrue() {
        assertTrue(Direction.UP.isOposite(Direction.DOWN));
        assertTrue(Direction.DOWN.isOposite(Direction.UP));
        assertTrue(Direction.LEFT.isOposite(Direction.RIGHT));
        assertTrue(Direction.RIGHT.isOposite(Direction.LEFT));
    }

    @Test
    public void testIsOpositeFalse() {
        assertFalse(Direction.UP.isOposite(Direction.LEFT));
        assertFalse(Direction.UP.isOposite(Direction.RIGHT));
        assertFalse(Direction.DOWN.isOposite(Direction.LEFT));
        assertFalse(Direction.DOWN.isOposite(Direction.RIGHT));
        assertFalse(Direction.LEFT.isOposite(Direction.UP));
        assertFalse(Direction.LEFT.isOposite(Direction.DOWN));
        assertFalse(Direction.RIGHT.isOposite(Direction.UP));
        assertFalse(Direction.RIGHT.isOposite(Direction.DOWN));
    }

}