package alex.momotov.reality;

import alex.momotov.reality.objects.Edible;
import alex.momotov.reality.objects.SnakeBody;
import alex.momotov.reality.objects.Treat;
import org.junit.Test;

import static org.junit.Assert.*;

public class CellTest {

    @Test
    public void startsEmpty() {
        Cell cell = new Cell();
        assertTrue(cell.empty());
        assertNull(cell.top());
        assertFalse(cell.contains(Edible.class));
    }

    @Test
    public void topIsTheMostRecentlyAdded() {
        Cell cell = new Cell();
        cell.add(new Edible(Treat.APPLE));
        cell.add(new SnakeBody(0));
        assertTrue(cell.top() instanceof SnakeBody);
    }

    @Test
    public void removeTakesOnlyTheMatchingType() {
        Cell cell = new Cell();
        cell.add(new Edible(Treat.APPLE));
        cell.add(new SnakeBody(0));
        cell.remove(Edible.class);
        assertFalse(cell.contains(Edible.class));
        assertTrue(cell.contains(SnakeBody.class));
    }

    @Test
    public void removeByIdentityTakesOnlyThatOneObject() {
        Cell cell = new Cell();
        Edible first = new Edible(Treat.APPLE);
        Edible second = new Edible(Treat.MELON);
        cell.add(first);
        cell.add(second);
        cell.remove(first);
        assertSame(second, cell.find(Edible.class));
    }

    @Test
    public void removingAnAbsentTypeIsHarmless() {
        Cell cell = new Cell();
        cell.add(new SnakeBody(0));
        cell.remove(Edible.class);
        assertTrue(cell.contains(SnakeBody.class));
    }
}
