package alex.momotov.reality.field;

import junit.framework.TestCase;

public class FieldTest extends TestCase {


    public void testRemove() {
        Field f = new Field(26, 20);
        f.print();
        f.clear(5, 5);
        System.out.println();
        f.print();
    }

}