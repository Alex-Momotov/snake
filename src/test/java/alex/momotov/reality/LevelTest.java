package alex.momotov.reality;

import alex.momotov.reality.objects.Treat;
import alex.momotov.reality.objects.Wall;
import org.junit.Test;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.*;

public class LevelTest {

    @Test
    public void everyLayoutIsWalledInAndStillLeavesRoomToPlay() {
        for (int level = 1; level <= 12; level++) {
            Board board = new Board(24, 20);
            Level.build(board, level, new Random(level));

            assertTrue("level " + level + " must be fenced in",
                    board.contains(new Pos(0, 5), Wall.class));
            assertTrue(board.contains(new Pos(23, 5), Wall.class));
            assertTrue(board.contains(new Pos(5, 0), Wall.class));
            assertTrue(board.contains(new Pos(5, 19), Wall.class));

            int open = 0;
            for (Pos pos : board.positions()) {
                if (board.cell(pos).empty())
                    open++;
            }
            assertTrue("level " + level + " left only " + open + " free squares",
                    open > board.rows() * board.cols() / 3);
        }
    }

    @Test
    public void everyLevelHasANameAndGrowsHarder() {
        assertNotNull(Level.name(1));
        assertNotEquals(Level.name(1), Level.name(2));
        assertTrue(Level.quota(3) > Level.quota(1));
        assertTrue(Level.mobCount(4) > Level.mobCount(1));
        assertEquals("the crowd is capped", Level.mobCount(50), Level.mobCount(60));
    }

    @Test
    public void treatWeightsCoverTheWholeRange() {
        Set<Treat> seen = EnumSet.noneOf(Treat.class);
        for (int i = 0; i < Treat.totalWeight(); i++)
            seen.add(Treat.roll(i));
        assertEquals("every treat must be reachable", Treat.values().length, seen.size());
        assertTrue("apples should be the common case", Treat.APPLE.weight > Treat.GOLDEN.weight);
    }
}
