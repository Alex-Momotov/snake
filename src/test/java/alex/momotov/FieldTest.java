package alex.momotov;

import alex.momotov.reality.objects.Space;
import alex.momotov.reality.objects.Snake;
import alex.momotov.reality.objects.Obj;
import alex.momotov.reality.Field;
import org.junit.Test;

public class FieldTest {

    @Test
    public void testFieldCreation() {
        Field<Obj> f = new Field<>(13, 10);

        f.fill(() -> new Space());
        f.set(4, 7, new Snake());
        f.print();
        System.out.println(f.get(4,8));
    }

}