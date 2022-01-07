package alex.momotov.reality.field;

import alex.momotov.reality.objects.Snake;
import junit.framework.TestCase;
import org.junit.Test;

public class CellTest {

    @Test
    public void testRemove() {
        Cell c = new Cell();
        System.out.println(c);
        c.add(new Snake());
        System.out.println(c);
        c.remove(new Snake());
        System.out.println(c);
    }

}