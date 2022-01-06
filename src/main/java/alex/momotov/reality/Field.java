package alex.momotov.reality;

import alex.momotov.Utils;
import com.github.tomaslanger.chalk.Ansi;

import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Supplier;

public class Field<T> {

    private final AtomicReferenceArray<AtomicReferenceArray<T>> field;
    public final int rows;
    public final int cols;
    public final int REFRESH_RATE = 100;

    public Field(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.field = new AtomicReferenceArray<>(rows);
        for (int i = 0; i < rows; i++)
            field.set(i, new AtomicReferenceArray<>(cols));
    }

    public void fill(Supplier<T> objSupplier) {
        for (int r = 0; r < field.length(); r++) {
            for (int c = 0; c < field.get(0).length(); c++) {
                field.get(r).set(c, objSupplier.get());
            }
        }
    }

    public void set(int row, int col, T obj) {
        field.get(row).set(col, obj);
    }

    public T get(int row, int col) {
        return field.get(row).get(col);
    }

    public void print() {
        for (int r = 0; r < field.length(); r++) {
            for (int c = 0; c < field.get(0).length(); c++) {
                System.out.print(" " + field.get(r).get(c) + " ");
            }
            System.out.println();
        }
    }

    public void monitor() {
        Thread t = new Thread() {
            @Override
            public void run() {
                boolean first = true;
                while (true) {
                    if (!first)
                        System.out.print(Ansi.cursorUp(rows));
                    first = false;
                    print();
                    Utils.sleep(REFRESH_RATE);
                }
            }
        };
        t.start();
    }

}
