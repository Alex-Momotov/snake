package alex.momotov.reality;

import alex.momotov.Utils;
import com.github.tomaslanger.chalk.Ansi;

import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Supplier;

public class Field<T> {

    private final AtomicReferenceArray<AtomicReferenceArray<T>> field;
    private final XY cursor;

    public Field(int rows, int cols) {
        this.field = new AtomicReferenceArray<>(rows);
        for (int i = 0; i < rows; i++)
            field.set(i, new AtomicReferenceArray<>(cols));
        this.cursor = new XY(0, 0);
    }

    public synchronized void fill(Supplier<T> objSupplier) {
        for (int r = 0; r < field.length(); r++) {
            for (int c = 0; c < field.get(0).length(); c++) {
                field.get(r).set(c, objSupplier.get());
            }
        }
    }

    public synchronized void set(int row, int col, T obj) {
        field.get(row).set(col, obj);
        seek(row, col * 3);
        sysPrint(" " + field.get(row).get(col) + " ");
    }

    public synchronized T get(int row, int col) {
        return field.get(row).get(col);
    }

    public synchronized void print() {
        for (int r = 0; r < field.length(); r++) {
            for (int c = 0; c < field.get(0).length(); c++) {
                sysPrint(" " + field.get(r).get(c) + " ");
            }
            sysPrintLn();
        }
        seek(0, 0);
    }

    public synchronized void seek(int row, int col) {
        if (row < cursor.x)
            System.out.print(Ansi.cursorUp(cursor.x - row));
        else if (row > cursor.x)
            System.out.print(Ansi.cursorDown(row - cursor.x));

        if (col < cursor.y)
            System.out.print(Ansi.cursorLeft(cursor.y - col));
        else if (col > cursor.y)
            System.out.print(Ansi.cursorRight(col - cursor.y));

        cursor.x = row;
        cursor.y = col;
    }

    private synchronized void sysPrint(String str) {
        System.out.print(str);
        cursor.y += 3;
    }

    private synchronized void sysPrintLn() {
        System.out.println();
        cursor.x += 1;
    }

    public synchronized int rows() {
        return field.length();
    }

    public synchronized int cols() {
        return field.get(0).length();
    }

}
