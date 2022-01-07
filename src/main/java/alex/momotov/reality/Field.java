package alex.momotov.reality;

import alex.momotov.Utils;
import com.github.tomaslanger.chalk.Ansi;

import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Supplier;

public class Field<T> {

    private final AtomicReferenceArray<AtomicReferenceArray<T>> field;
    public final int rows;
    public final int cols;
    private final XY cursor;

    public Field(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
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
        System.out.print(" " + field.get(row).get(col) + " ");
        cursor.y += 3;
    }

    public synchronized T get(int row, int col) {
        return field.get(row).get(col);
    }

    public synchronized void print() {
        for (int r = 0; r < field.length(); r++) {
            for (int c = 0; c < field.get(0).length(); c++) {
                System.out.print(" " + field.get(r).get(c) + " ");
                cursor.y += 3;
            }
            System.out.println();
            cursor.x += 1;
        }
        seek(0, 0);
    }
    public synchronized void seek(int row, int col) {
        if (row < cursor.x) {
            System.out.print(Ansi.cursorUp(cursor.x - row));
            cursor.x = row;
        } else if (row > cursor.x) {
            System.out.print(Ansi.cursorDown(row - cursor.x));
            cursor.x = row;
        }

        if (col < cursor.y) {
            System.out.print(Ansi.cursorLeft(cursor.y - col));
            cursor.y = col;
        } else if (col > cursor.y) {
            System.out.print(Ansi.cursorRight(col - cursor.y));
            cursor.y = col;
        }
    }

}
