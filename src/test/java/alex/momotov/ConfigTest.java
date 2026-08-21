package alex.momotov;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConfigTest {

    @Test
    public void parsesEveryOption() {
        Config c = Config.parse(new String[]{
                "--rows=12", "--cols=14", "--step_ms=50", "--food_interval_ms=900",
                "--walls=false", "--collision=false", "--seed=7", "--max_ticks=20", "--start_level=3"});
        assertEquals(12, c.rows);
        assertEquals(14, c.cols);
        assertEquals(50, c.stepMs);
        assertEquals(900, c.foodIntervalMs);
        assertFalse(c.walls);
        assertFalse(c.collision);
        assertEquals(7L, c.seed);
        assertEquals(20, c.maxTicks);
        assertEquals(3, c.startLevel);
    }

    @Test
    public void missingOptionsKeepTheirDefaults() {
        Config c = Config.parse(new String[]{"--rows=12"});
        Config d = Config.defaults();
        assertEquals(12, c.rows);
        assertEquals(d.cols, c.cols);
        assertEquals(d.stepMs, c.stepMs);
        assertEquals(d.walls, c.walls);
    }

    @Test
    public void rejectsUnknownOptions() {
        assertRejected(new String[]{"--rowz=12"}, "Unknown option");
    }

    @Test
    public void rejectsMalformedArguments() {
        assertRejected(new String[]{"rows=12"}, "Unrecognised argument");
        assertRejected(new String[]{"--rows"}, "Expected --name=value");
        assertRejected(new String[]{"--rows=abc"}, "whole number");
        assertRejected(new String[]{"--walls=maybe"}, "true or false");
    }

    @Test
    public void rejectsBoardsTooSmallToPlayOn() {
        assertRejected(new String[]{"--rows=2"}, "rows must be at least 8");
        assertRejected(new String[]{"--cols=1"}, "cols must be at least 8");
        assertRejected(new String[]{"--step_ms=0"}, "step_ms must be at least 1");
    }

    private static void assertRejected(String[] args, String expectedFragment) {
        try {
            Config.parse(args);
            fail("expected " + String.join(" ", args) + " to be rejected");
        } catch (IllegalArgumentException e) {
            assertTrue("message was: " + e.getMessage(), e.getMessage().contains(expectedFragment));
        }
    }
}
