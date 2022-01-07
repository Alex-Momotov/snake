package alex.momotov.reality.field;

import alex.momotov.reality.objects.Snake;
import junit.framework.TestCase;

public class FieldTest extends TestCase {


    public void testRemove() {
        Field f = new Field(26, 20);
        f.print();
        f.add(5, 5, new Snake());
        f.clear(5, 5);
    }

}