package alex.momotov.reality.field;

import alex.momotov.reality.objects.Obj;

import java.util.ArrayList;
import java.util.List;

public class Cell {

    private final List<Obj> objects;

    public Cell() {
        objects = new ArrayList<>();
    }

    public synchronized void add(Obj obj) {
        objects.add(obj);
    }

    public synchronized Obj remove(Obj obj) {
        objects.remove(obj);
        return obj;
    }

    public synchronized Obj remove(Class cls) {
        for (Obj o: objects) {
            if (o.getClass().equals(cls))
                objects.remove(o);
                return o;
        }
        return null;
    }

    public synchronized boolean contains(Obj obj) {
        return objects.contains(obj);
    }

    public synchronized boolean contains(Class cls) {
        for (Obj o: objects) {
            if (o.getClass().equals(cls))
                return true;
        }
        return false;
    }

    public synchronized int size() {
        return objects.size();
    }

    public synchronized boolean empty() {
        return objects.isEmpty();
    }

    public synchronized void clear() {
        objects.clear();
    }

    @Override
    public synchronized String toString() {
        if (objects.isEmpty())
            return ".";
        else
            return objects.get(objects.size() - 1).toString();
    }
}
