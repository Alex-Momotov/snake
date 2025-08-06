package alex.momotov.reality.field;

import alex.momotov.reality.objects.Obj;
import com.github.tomaslanger.chalk.Chalk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Cell {

    private final List<Obj> objects;

    public Cell() {
        objects = new ArrayList<>();
    }

    public synchronized void add(Obj obj) {
        objects.add(obj);
    }

    public synchronized void remove(Class cls) {
        for (Iterator<Obj> iterator = objects.iterator(); iterator.hasNext();) {
            if (iterator.next().getClass().equals(cls)) {
                iterator.remove();
                return;
            }
        }
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
            return Chalk.on(".").gray().bold().toString();
        else
            return objects.get(objects.size() - 1).toString();
    }
}
