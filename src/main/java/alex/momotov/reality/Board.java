package alex.momotov.reality;

import alex.momotov.reality.objects.Animated;
import alex.momotov.reality.objects.Obj;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The grid of cells. Knows nothing about terminals: it records which cells changed
 * and a view drains that list to repaint only what moved.
 */
public class Board {

    private final Cell[][] cells;
    private final Set<Pos> dirty = new LinkedHashSet<>();
    private final Set<Pos> animated = new LinkedHashSet<>();

    public Board(int rows, int cols) {
        cells = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++)
                cells[r][c] = new Cell();
        }
    }

    public int rows() {
        return cells.length;
    }

    public int cols() {
        return cells[0].length;
    }

    public Cell cell(Pos pos) {
        return cells[pos.row][pos.col];
    }

    public void add(Pos pos, Obj obj) {
        cell(pos).add(obj);
        dirty.add(pos);
        if (obj instanceof Animated)
            animated.add(pos);
    }

    public void remove(Pos pos, Class<? extends Obj> type) {
        cell(pos).remove(type);
        dirty.add(pos);
        forgetIfStill(pos);
    }

    /** Removes one specific object rather than the first of its kind. */
    public void remove(Pos pos, Obj obj) {
        cell(pos).remove(obj);
        dirty.add(pos);
        forgetIfStill(pos);
    }

    public boolean contains(Pos pos, Class<? extends Obj> type) {
        return cell(pos).contains(type);
    }

    public <T> T find(Pos pos, Class<T> type) {
        return cell(pos).find(type);
    }

    /** Marks every cell for repainting, for when the whole world is replaced. */
    public void touchAll() {
        dirty.addAll(positions());
    }

    /** Marks a whole row for repainting, to wipe a banner drawn across it. */
    public void touchRow(int row) {
        for (int c = 0; c < cols(); c++)
            dirty.add(new Pos(row, c));
    }

    public boolean inBounds(Pos pos) {
        return pos.row >= 0 && pos.row < rows() && pos.col >= 0 && pos.col < cols();
    }

    /** Teleports a position that ran off an edge round to the opposite one. */
    public Pos wrap(Pos pos) {
        return new Pos(Math.floorMod(pos.row, rows()), Math.floorMod(pos.col, cols()));
    }

    public List<Pos> positions() {
        List<Pos> all = new ArrayList<>(rows() * cols());
        for (int r = 0; r < rows(); r++) {
            for (int c = 0; c < cols(); c++)
                all.add(new Pos(r, c));
        }
        return all;
    }

    /** Cells holding something that animates on its own. */
    public List<Pos> animated() {
        return new ArrayList<>(animated);
    }

    private void forgetIfStill(Pos pos) {
        if (cell(pos).find(Animated.class) == null)
            animated.remove(pos);
    }

    /** Cells changed since the last call. Draining keeps the view and the model in step. */
    public List<Pos> drainDirty() {
        List<Pos> changed = new ArrayList<>(dirty);
        dirty.clear();
        return changed;
    }
}
