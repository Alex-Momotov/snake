package alex.momotov.reality;

import alex.momotov.reality.objects.Edible;
import alex.momotov.reality.objects.Treat;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class BoardTest {

    @Test
    public void wrapsRoundEveryEdge() {
        Board board = new Board(10, 8);
        assertEquals(new Pos(9, 3), board.wrap(new Pos(-1, 3)));
        assertEquals(new Pos(0, 3), board.wrap(new Pos(10, 3)));
        assertEquals(new Pos(4, 7), board.wrap(new Pos(4, -1)));
        assertEquals(new Pos(4, 0), board.wrap(new Pos(4, 8)));
    }

    @Test
    public void tracksOnlyTheCellsThatChanged() {
        Board board = new Board(5, 5);
        board.drainDirty();
        board.add(new Pos(2, 2), new Edible(Treat.APPLE));
        board.remove(new Pos(3, 3), Edible.class);

        List<Pos> dirty = board.drainDirty();
        assertEquals(2, dirty.size());
        assertTrue(dirty.contains(new Pos(2, 2)));
        assertTrue(dirty.contains(new Pos(3, 3)));
        assertTrue("draining clears the list", board.drainDirty().isEmpty());
    }

    @Test
    public void positionsCoverTheWholeGrid() {
        Board board = new Board(6, 4);
        assertEquals(24, board.positions().size());
        for (Pos pos : board.positions())
            assertTrue(board.inBounds(pos));
    }
}
