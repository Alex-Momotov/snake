package alex.momotov.reality;

import alex.momotov.reality.objects.Obj;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** One square of the board: a stack of objects, the last one being what you see. */
public class Cell {

    private final List<Obj> objects = new ArrayList<>(2);

    public void add(Obj obj) {
        objects.add(obj);
    }

    /** Removes the first object of this type, if any. */
    public void remove(Class<? extends Obj> type) {
        for (Iterator<Obj> it = objects.iterator(); it.hasNext(); ) {
            if (type.isInstance(it.next())) {
                it.remove();
                return;
            }
        }
    }

    /** Removes one specific object, used where several of a kind share the board. */
    public void remove(Obj obj) {
        objects.remove(obj);
    }

    public boolean contains(Class<? extends Obj> type) {
        return find(type) != null;
    }

    public <T> T find(Class<T> type) {
        for (Obj obj : objects) {
            if (type.isInstance(obj))
                return type.cast(obj);
        }
        return null;
    }

    /** The object to render, or null when the cell is empty. */
    public Obj top() {
        return objects.isEmpty() ? null : objects.get(objects.size() - 1);
    }

    public boolean empty() {
        return objects.isEmpty();
    }

    public void clear() {
        objects.clear();
    }
}
