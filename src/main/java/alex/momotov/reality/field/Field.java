package alex.momotov.reality.field;

import alex.momotov.reality.XY;
import alex.momotov.reality.objects.Obj;
import com.github.tomaslanger.chalk.Ansi;
import com.github.tomaslanger.chalk.Chalk;

import java.util.concurrent.atomic.AtomicReferenceArray;

public class Field {

    private final AtomicReferenceArray<AtomicReferenceArray<Cell>> field;
    private final XY carret;

    public Field(int rows, int cols) {
        this.field = new AtomicReferenceArray<>(rows);
        for (int i = 0; i < rows; i++) {
            AtomicReferenceArray<Cell> row = new AtomicReferenceArray<>(cols);
            for (int j = 0; j < row.length(); j++)
                row.set(j, new Cell());
            field.set(i, row);
        }
        this.carret = new XY(0, 0);
    }

    public synchronized void add(int row, int col, Obj obj) {
        field.get(row).get(col).add(obj);
        seek(row, col * 3);
        sysPrint(" " + field.get(row).get(col) + " ");
    }

    public synchronized Cell get(int row, int col) {
        return field.get(row).get(col);
    }

    public synchronized void remove(int row, int col, Class cls) {
        get(row, col).remove(cls);
        seek(row, col * 3);
        sysPrint(" " + field.get(row).get(col) + " ");
    }

    public synchronized boolean contains(int row, int col, Class cls) {
        return get(row, col).contains(cls);
    }

    public synchronized void clear(int row, int col) {
        get(row, col).clear();
        seek(row, col * 3);
        sysPrint(" " + field.get(row).get(col) + " ");
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
        if (row < carret.x)
            System.out.print(Ansi.cursorUp(carret.x - row));
        else if (row > carret.x)
            System.out.print(Ansi.cursorDown(row - carret.x));

        if (col < carret.y)
            System.out.print(Ansi.cursorLeft(carret.y - col));
        else if (col > carret.y)
            System.out.print(Ansi.cursorRight(col - carret.y));

        carret.x = row;
        carret.y = col;
    }

    public synchronized void sysPrint(String str) {
        System.out.print(str);
        carret.y += 3;
    }

    public synchronized void sysPrintTest(String str, int length) {
        System.out.print(str);
        carret.y += length;
    }

    public synchronized void sysPrintLn() {
        System.out.println();
        carret.x += 1;
    }

    public synchronized int rows() {
        return field.length();
    }

    public synchronized int cols() {
        return field.get(0).length();
    }

    public synchronized void message(String message, int msgLength) {
        int midRow = this.rows() / 2;
        int midCol = this.cols() * 3 / 2;
        int halfMessage = msgLength / 2;
        seek(midRow, midCol - halfMessage);
        sysPrintTest(message, msgLength);
    }

}
